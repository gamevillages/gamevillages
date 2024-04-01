package com.gamevillages.gamevillages.controller;

import com.gamevillages.gamevillages.dto.UserRequestDto;
import com.gamevillages.gamevillages.dto.UserResponseDto;
import com.gamevillages.gamevillages.serivce.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDto userRequestDto){
        try {
            UserResponseDto userResponseDto = userService.createUser(userRequestDto);
            return ResponseEntity.ok(userResponseDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
