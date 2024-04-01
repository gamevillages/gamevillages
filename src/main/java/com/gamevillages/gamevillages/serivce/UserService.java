package com.gamevillages.gamevillages.serivce;

import com.gamevillages.gamevillages.dto.UserRequestDto;
import com.gamevillages.gamevillages.dto.UserResponseDto;
import com.gamevillages.gamevillages.entity.User;
import com.gamevillages.gamevillages.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private  final UserRepository userRepository;

    public UserService(UserRepository userRepository){this.userRepository = userRepository;}

    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = new User(userRequestDto);
        Optional<User> findUser = userRepository.findUserByEmail(userRequestDto.getEmail());
        if(findUser.isPresent()){
            throw new RuntimeException("이미 해당 이메일을 사용하는 사용자가 존재합니다.");
        }
        User saveUser = userRepository.save(user);

        UserResponseDto userResponseDto = new UserResponseDto(saveUser);
        return userResponseDto;
    }
}
