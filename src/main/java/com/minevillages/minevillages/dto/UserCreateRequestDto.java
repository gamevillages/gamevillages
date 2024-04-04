package com.minevillages.minevillages.dto;

import lombok.Getter;

import java.util.Optional;

@Getter
public class UserCreateRequestDto {
    private String email;
    private String password;
    private String type;
    private Optional<String> name;
}
