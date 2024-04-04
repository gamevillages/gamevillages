package com.minevillages.minevillages.repository;

import com.minevillages.minevillages.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, String> {
    User findUserByEmailAndDeletedAtIsNull(String email);
    User findUserByIdAndDeletedAtIsNull(String id);

    default void deleteUserById(String id){
        findById(id).ifPresent(user ->{
            user.setDeletedAt(LocalDateTime.now());
            save(user);
        });
    }
    default User updateUserNameById(String id, String name){
        findById(id).ifPresent(user ->{
            if(name != null){
                user.setName(name);
            }
            user.setUpdatedAt(LocalDateTime.now());
            save(user);
        });

        return findUserByIdAndDeletedAtIsNull(id);
    };
}
