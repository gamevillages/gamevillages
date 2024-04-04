package com.minevillages.minevillages.dto;

import com.minevillages.minevillages.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class UserUpdateResponseDto {
    private String id;
    private String email;
    private String type ;
    private Optional<String> name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public UserUpdateResponseDto(User user){
        this.id = user.getId();
        this.name = user.getName().describeConstable();
        this.email = user.getEmail();
        this.type = user.getType();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.deletedAt = user.getDeletedAt();
    }
}
