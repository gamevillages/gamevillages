package com.minevillages.minevillages.dto;

import lombok.Getter;

@Getter
public class UserCreateRequestDto {
    private String email;
    private String password;
    private String type;
    private String name;
}
