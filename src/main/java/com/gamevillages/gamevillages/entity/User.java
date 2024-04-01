package com.gamevillages.gamevillages.entity;

import com.gamevillages.gamevillages.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Setter
@Table(name = "user")
@NoArgsConstructor
public class User extends  Timestamped {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name")
    private String name;

    public User(UserRequestDto userRequestDto){
        this.email = userRequestDto.getEmail();
        this.password = userRequestDto.getPassword();
        this.type = userRequestDto.getType();
        this.name = String.valueOf(userRequestDto.getName());
    }
}
