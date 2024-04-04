package com.minevillages.minevillages.controller;

import com.minevillages.minevillages.dto.*;
import com.minevillages.minevillages.serivce.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @Autowired
    private HttpServletRequest request;

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

            UserLoginResponseDto userLoginResponseDto = userService.loginUser(userLoginRequestDto, request.getRemoteAddr());
            return ResponseEntity.ok(userLoginResponseDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (UnknownHostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
