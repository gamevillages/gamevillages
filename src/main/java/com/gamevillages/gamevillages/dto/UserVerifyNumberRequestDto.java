package com.gamevillages.gamevillages.dto;

import lombok.Getter;

@Getter
public class UserVerifyNumberRequestDto {
    private String verifyNumber;
    private String email;
}
