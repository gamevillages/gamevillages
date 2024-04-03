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

import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    Argon2 argon2 = Argon2Factory.create();
    private  final UserRepository userRepository;

    @Autowired
    JavaMailSender emailSender;

    public UserService(UserRepository userRepository){this.userRepository = userRepository;}

    public UserEmailDto sendEmail(UserEmailDto userEmailRequestDto) {

        // 이메일 중복 검사
        User findUser = userRepository.findUserByEmail(userEmailRequestDto.getEmail());
        if(findUser != null){
            throw new RuntimeException("이미 해당 이메일을 사용하는 사용자가 존재합니다.");
        }

        String randomNumber = generateRandomNumbers();

        // 인증 번호 메일 발송
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmailRequestDto.getEmail());
            message.setSubject("[Game Villages] 인증번호를 확인해 주세요");
            message.setText(randomNumber);
            emailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        GamevillagesApplication.jedis.setex("randomNumber_"+userEmailRequestDto.getEmail(), 300, randomNumber);
        return userEmailRequestDto;
    }

    public UserVerifyNumberResponseDto verifyNumber(UserVerifyNumberRequestDto userVerifyNumberRequestDto) {
        String redisNumber = GamevillagesApplication.jedis.get("randomNumber_"+userVerifyNumberRequestDto.getEmail());
        if (!redisNumber.equals(userVerifyNumberRequestDto.getVerifyNumber())){
            throw new RuntimeException("인증번호가 유효하지 않습니다.");
        }
        GamevillagesApplication.jedis.setex("verified_" + userVerifyNumberRequestDto.getEmail(), 600, "true");
        UserVerifyNumberResponseDto userVerifyNumberResponseDto = new UserVerifyNumberResponseDto();
        userVerifyNumberResponseDto.setEmail(userVerifyNumberRequestDto.getEmail());
        return userVerifyNumberResponseDto;
    }

    public UserCreateResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        User user = new User(userCreateRequestDto);

        // 이메일 인증 확인
        if(!GamevillagesApplication.jedis.get("verified_" + userCreateRequestDto.getEmail()).equals("true")){
            throw new RuntimeException("이메일 인증이 유효하지 않습니다.");
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

    public UserLoginResponseDto loginUser(UserLoginRequestDto userLoginRequestDto) {

        // 유저 찾기
        User findUser = userRepository.findUserByEmail(userLoginRequestDto.getEmail());
        if(findUser == null) {
            throw new RuntimeException("로그인 오류가 발생하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        }

        // 비밀번호 검증
        Boolean verifiedPassword = argon2.verify(findUser.getPassword(), userLoginRequestDto.getPassword());
        if (!verifiedPassword) {
            throw new RuntimeException("로그인 오류가 발생하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        }

        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(findUser);
        String sessionKey = generateSessionKey(findUser.getId());
        userLoginResponseDto.setSessionKey(sessionKey);

        return userLoginResponseDto;

    }

    // 세션 키 생성 메서드
    public String generateSessionKey(String userId){
        String sessionKey = UUID.randomUUID().toString();
        GamevillagesApplication.jedis.setex(sessionKey,3600,userId);
        return sessionKey;
    }

    public static String generateRandomNumbers() {
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int randomNumber = random.nextInt(9) ;
            result.append(randomNumber);
        }
        return result.toString();
    }



}
