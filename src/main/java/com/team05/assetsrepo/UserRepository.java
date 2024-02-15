package com.team05.assetsrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	User findByUsername(String username);
}
