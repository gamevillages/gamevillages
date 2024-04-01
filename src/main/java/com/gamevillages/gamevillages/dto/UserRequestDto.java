package com.gamevillages.gamevillages.dto;

import lombok.Getter;

import java.util.Optional;

@Getter
public class UserRequestDto {
    private String email;
    private String password;
    private String type;
    private Optional<String> name;
}
