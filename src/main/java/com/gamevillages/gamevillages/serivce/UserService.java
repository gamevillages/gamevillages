package com.gamevillages.gamevillages.serivce;

import com.gamevillages.gamevillages.GamevillagesApplication;
import com.gamevillages.gamevillages.dto.*;
import com.gamevillages.gamevillages.entity.User;
import com.gamevillages.gamevillages.repository.UserRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    Argon2 argon2 = Argon2Factory.create();
    private final UserRepository userRepository;

    @Autowired
    JavaMailSender emailSender;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEmailResponseDto sendEmail(UserEmailRequestDto userEmailRequestDto) {

        // 이메일 중복 검사
        User findUser = userRepository.findUserByEmail(userEmailRequestDto.getEmail());
        if (findUser != null) {
            throw new RuntimeException("User already Exists.");
        }

        String randomNumber = generateRandomNumbers();

        // 인증 번호 메일 발송
        try {
            String ko = "[Game Villages] 인증번호를 확인해 주세요";
            String en = "[Game Villages] Please check your verify number";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmailRequestDto.getEmail());
            if (userEmailRequestDto.getClientLanguage().equals("ko")) {
                message.setSubject(ko);
            } else {
                message.setSubject(en);
            }
            message.setText(randomNumber);
            emailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        GamevillagesApplication.jedis.setex("randomNumber_" + userEmailRequestDto.getEmail(), 300, randomNumber);
        UserEmailResponseDto userEmailResponseDto = new UserEmailResponseDto();
        userEmailResponseDto.setEmail(userEmailRequestDto.getEmail());
        return userEmailResponseDto;
    }

    public UserVerifyNumberResponseDto verifyNumber(UserVerifyNumberRequestDto userVerifyNumberRequestDto) {
        String redisNumber = GamevillagesApplication.jedis.get("randomNumber_" + userVerifyNumberRequestDto.getEmail());
        if (redisNumber == null && !redisNumber.equals(userVerifyNumberRequestDto.getVerifyNumber())) {
            throw new RuntimeException("Verify Number is not vaild.");
        }
        GamevillagesApplication.jedis.setex("verified_" + userVerifyNumberRequestDto.getEmail(), 600, "true");
        UserVerifyNumberResponseDto userVerifyNumberResponseDto = new UserVerifyNumberResponseDto();
        userVerifyNumberResponseDto.setEmail(userVerifyNumberRequestDto.getEmail());
        return userVerifyNumberResponseDto;
    }

    public UserCreateResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        User user = new User(userCreateRequestDto);

        // 이메일 인증 확인
        if (!GamevillagesApplication.jedis.get("verified_" + userCreateRequestDto.getEmail()).equals("true")) {
            throw new RuntimeException("Verification is not valid.");
        }

        // 비밀번호 해싱 : Argon2

        String hashedPassword = argon2.hash(10, 65536, 1, user.getPassword());
        user.setPassword(hashedPassword);

        User saveUser = userRepository.save(user);

        UserCreateResponseDto userCreateResponseDto = new UserCreateResponseDto(saveUser);

        String sessionKey = generateSessionKey(saveUser.getId());

        userCreateResponseDto.setSessionKey(sessionKey);

        return userCreateResponseDto;
    }

    public UserLoginResponseDto loginUser(UserLoginRequestDto userLoginRequestDto, String ip) throws UnknownHostException {

        // 유저 찾기
        User findUser = userRepository.findUserByEmail(userLoginRequestDto.getEmail());
        if (findUser == null) {
            throw new RuntimeException("Login Error.");
        }

        // 비밀번호 검증
        Boolean verifiedPassword = argon2.verify(findUser.getPassword(), userLoginRequestDto.getPassword());
        if (!verifiedPassword) {
            String countString = GamevillagesApplication.jedis.get("illegalCount_" + userLoginRequestDto.getEmail());
            int count = 1;
            if (countString != null) {
                count += Integer.valueOf(countString);
            }

            if (count > 4) {
                IllegalAtDto illegalAtDto = new IllegalAtDto();
                illegalAtDto.setIp(ip);
                LocalDateTime now = LocalDateTime.now();
                illegalAtDto.setDatetime(now);
                GamevillagesApplication.jedis.set("illegalAt_" + userLoginRequestDto.getEmail(), String.valueOf(illegalAtDto));

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(userLoginRequestDto.getEmail());
                String ko = "[Game Villages] 계정에 비정상적인 접근이 있습니다";
                String en = "[Game Villages] Illegal Access To Account";

                if (userLoginRequestDto.getClientLanguage().equals("ko")) {
                    message.setSubject(ko);
                    message.setText("IP" + ip  + "주소에서 " + now + "에 비정상적인 접근이 확인되었습니다.");
                } else {
                    message.setSubject(en);
                    message.setText("Illegal Access from IP " +  ip + " at " + now);
                }
                emailSender.send(message);
            }
            GamevillagesApplication.jedis.set("illegalCount_" + userLoginRequestDto.getEmail(), String.valueOf(count));
            throw new RuntimeException("Login Error.");
        }
        GamevillagesApplication.jedis.del("illegalCount_" + userLoginRequestDto.getEmail());
        GamevillagesApplication.jedis.del("illegalAt_" + userLoginRequestDto.getEmail());
        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(findUser);
        String sessionKey = generateSessionKey(findUser.getId());
        userLoginResponseDto.setSessionKey(sessionKey);

        return userLoginResponseDto;

    }

    // 세션 키 생성 메서드
    public String generateSessionKey(String userId) {
        String sessionKey = UUID.randomUUID().toString();
        GamevillagesApplication.jedis.setex(sessionKey, 3600, userId);
        return sessionKey;
    }

    // 인증 번호 생성 메서드
    public String generateRandomNumbers() {
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int randomNumber = random.nextInt(9);
            result.append(randomNumber);
        }
        return result.toString();
    }



}
