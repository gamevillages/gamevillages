package com.gamevillages.gamevillages.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IllegalAtDto {
    private LocalDateTime datetime;
    private String ip;
}
