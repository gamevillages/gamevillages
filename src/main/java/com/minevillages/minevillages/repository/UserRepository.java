package com.minevillages.minevillages.repository;

import com.minevillages.minevillages.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findUserByEmail(String email);
}
