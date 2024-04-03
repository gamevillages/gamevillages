package com.gamevillages.gamevillages.repository;

import com.gamevillages.gamevillages.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findUserByEmail(String email);
}
