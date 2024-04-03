package com.gamevillages.gamevillages.controller;

import com.gamevillages.gamevillages.dto.*;
import com.gamevillages.gamevillages.serivce.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody UserEmailRequestDto userEmailRequestDto){
        try{
            UserEmailResponseDto userEmailResponseDto = userService.sendEmail(userEmailRequestDto);
            return ResponseEntity.ok(userEmailResponseDto);
        } catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/verify")
    public  ResponseEntity<?> verifyNumber(@RequestBody UserVerifyNumberRequestDto userVerifyNumberRequestDto){
        try{
            UserVerifyNumberResponseDto userVerifyNumberResponseDto = userService.verifyNumber(userVerifyNumberRequestDto);
            return ResponseEntity.ok(userVerifyNumberResponseDto);

        } catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequestDto userCreateRequestDto){
        try {
            UserCreateResponseDto userCreateResponseDto = userService.createUser(userCreateRequestDto);
            return ResponseEntity.ok(userCreateResponseDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto){
        try{
            UserLoginResponseDto userLoginResponseDto = userService.loginUser(userLoginRequestDto);
            return ResponseEntity.ok(userLoginResponseDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
