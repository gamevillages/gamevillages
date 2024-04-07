package com.minevillages.minevillages.dto;

import com.minevillages.minevillages.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNameResponseDto {
    private String id;
    private String name;

    public UserNameResponseDto(User user){
        this.id = user.getId();
        this.name = user.getName();
    }
}
