package com.minevillages.minevillages.serivce;

import com.minevillages.minevillages.MinevillagesApplication;
import com.minevillages.minevillages.dto.*;
import com.minevillages.minevillages.entity.User;
import com.minevillages.minevillages.repository.UserRepository;
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
        User findUser = userRepository.findUserByEmailAndDeletedAtIsNull(userEmailRequestDto.getEmail());
        if (findUser != null) {
            throw new RuntimeException("User already Exists.");
        }

        String randomNumber = generateRandomNumbers();

        // 인증 번호 메일 발송
        try {
            String ko = "[Mine Villages] 인증번호를 확인해 주세요";
            String en = "[Mine Villages] Please check your verify number";

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

        MinevillagesApplication.jedis.setex("randomNumber_" + userEmailRequestDto.getEmail(), 300, randomNumber);
        UserEmailResponseDto userEmailResponseDto = new UserEmailResponseDto();
        userEmailResponseDto.setEmail(userEmailRequestDto.getEmail());
        return userEmailResponseDto;
    }

    public UserVerifyNumberResponseDto verifyNumber(UserVerifyNumberRequestDto userVerifyNumberRequestDto) {
        String redisNumber = MinevillagesApplication.jedis.get("randomNumber_" + userVerifyNumberRequestDto.getEmail());
        if (redisNumber == null && !redisNumber.equals(userVerifyNumberRequestDto.getVerifyNumber())) {
            throw new RuntimeException("Verify Number is not vaild.");
        }
        MinevillagesApplication.jedis.setex("verified_" + userVerifyNumberRequestDto.getEmail(), 600, "true");
        UserVerifyNumberResponseDto userVerifyNumberResponseDto = new UserVerifyNumberResponseDto();
        userVerifyNumberResponseDto.setEmail(userVerifyNumberRequestDto.getEmail());
        return userVerifyNumberResponseDto;
    }

    public UserCreateResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        User user = new User(userCreateRequestDto);

        // 이메일 인증 확인
        if (!MinevillagesApplication.jedis.get("verified_" + userCreateRequestDto.getEmail()).equals("true")) {
            throw new RuntimeException("Verification is not valid.");
        }

        // 비밀번호 해싱 : Argon2
        String hashedPassword = argon2.hash(10, 65536, 1, user.getPassword());
        user.setPassword(hashedPassword);

        user.setCreatedAt(LocalDateTime.now());

        User saveUser = userRepository.save(user);

        UserCreateResponseDto userCreateResponseDto = new UserCreateResponseDto(saveUser);

        String sessionKey = generateSessionKey(saveUser.getId());

        userCreateResponseDto.setSessionKey(sessionKey);

        return userCreateResponseDto;
    }

    public UserLoginResponseDto loginUser(UserLoginRequestDto userLoginRequestDto, String ip) throws UnknownHostException {

        // 유저 찾기
        User findUser = userRepository.findUserByEmailAndDeletedAtIsNull(userLoginRequestDto.getEmail());
        if (findUser == null) {
            throw new RuntimeException("Login Error.");
        }

        // 비밀번호 검증
        Boolean verifiedPassword = argon2.verify(findUser.getPassword(), userLoginRequestDto.getPassword());
        if (!verifiedPassword) {
            String countString = MinevillagesApplication.jedis.get("illegalCount_" + userLoginRequestDto.getEmail());
            int count = 1;
            if (countString != null) {
                count += Integer.valueOf(countString);
            }

            if (count > 4) {
                IllegalAtDto illegalAtDto = new IllegalAtDto();
                illegalAtDto.setIp(ip);
                LocalDateTime now = LocalDateTime.now();
                illegalAtDto.setDatetime(now);
                MinevillagesApplication.jedis.set("illegalAt_" + userLoginRequestDto.getEmail(), String.valueOf(illegalAtDto));

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(userLoginRequestDto.getEmail());
                String ko = "[Mine Villages] 계정에 비정상적인 접근이 있습니다";
                String en = "[Mine Villages] Illegal Access To Account";

                if (userLoginRequestDto.getClientLanguage().equals("ko")) {
                    message.setSubject(ko);
                    message.setText("IP" + ip  + "주소에서 " + now + "에 비정상적인 접근이 확인되었습니다.");
                } else {
                    message.setSubject(en);
                    message.setText("Illegal Access from IP " +  ip + " at " + now);
                }
                emailSender.send(message);
            }
            MinevillagesApplication.jedis.set("illegalCount_" + userLoginRequestDto.getEmail(), String.valueOf(count));
            throw new RuntimeException("Login Error.");
        }
        MinevillagesApplication.jedis.del("illegalCount_" + userLoginRequestDto.getEmail());
        MinevillagesApplication.jedis.del("illegalAt_" + userLoginRequestDto.getEmail());
        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(findUser);
        String sessionKey = generateSessionKey(findUser.getId());
        userLoginResponseDto.setSessionKey(sessionKey);

        return userLoginResponseDto;

    }

    // 세션 키 생성 메서드
    public String generateSessionKey(String userId) {
        String sessionKey = UUID.randomUUID().toString();
        MinevillagesApplication.jedis.setex(sessionKey, 3600, userId);
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

    public UserUpdateResponseDto updateUserName(String authorization, UserUpdateRequestDto userUpdateRequestDto) {
        String userId = MinevillagesApplication.jedis.get(authorization);
        User resultUser = userRepository.updateUserNameById(userId, userUpdateRequestDto.getName());
        UserUpdateResponseDto userUpdateResponseDto = new UserUpdateResponseDto(resultUser);
        return userUpdateResponseDto;
    }


    public void deleteUser(String authorization) {
        String userId = MinevillagesApplication.jedis.get(authorization);
        userRepository.deleteUserById(userId);
    }
}
