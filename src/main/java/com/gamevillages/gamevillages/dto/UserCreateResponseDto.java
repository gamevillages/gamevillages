package com.gamevillages.gamevillages.dto;

import com.gamevillages.gamevillages.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class UserCreateResponseDto {
    private String id;
    private String email;
    private String type ;
    private Optional<String> name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String sessionKey;

    public UserCreateResponseDto(User user){
        this.id = user.getId();
        this.name = user.getName().describeConstable();
        this.email = user.getEmail();
        this.type = user.getType();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.deletedAt = user.getDeletedAt();
    }
}
