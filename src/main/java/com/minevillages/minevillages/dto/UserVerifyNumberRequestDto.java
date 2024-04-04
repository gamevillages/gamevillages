package com.minevillages.minevillages.dto;

import lombok.Getter;

@Getter
public class UserVerifyNumberRequestDto {
    private String verifyNumber;
    private String email;
}
