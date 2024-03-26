package com.team05.assetsrepo;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserEntityAndUserRepositoryTests {
	
	@Autowired
	private UserRepository userRepository;
	
	private UserEntity user;

	@BeforeEach
	void SetUp() {
		user = userRepository.findByUsername("test@gmail.com");
	}
	
	@Test
	void testGetID() {
		assertEquals(25, user.getID());
	}
	
	@Test
	void testSetID() {
		user.setID(110);
		assertEquals(110, user.getID());
	}
	
	@Test
	void testGetAuthorities() {
		assertEquals(user.getAuthorities().toString(), "[admin]");
	}
	
	@Test
	void testGetPassword() {
		assertEquals("$2a$10$G1qPCx8lNfo3VE.5de6/puaUjNQERycd.W.3f95MVxaDTGf5YLW12", user.getPassword());
	}
	
	@Test
	void testGetUsername() {
		assertEquals("test@gmail.com", user.getUsername());
	}
	
	@Test
	void testGetRole() {
		assertEquals("admin", user.getRole());
	}
	
	@Test
	void testSetUsername() {
		user.setUsername("TestedData@gmail.com");
		assertEquals("TestedData@gmail.com", user.getUsername());
	}
	
	@Test
	void testIsAccountNonExpired() {
		assertTrue(user.isAccountNonExpired());
	}
	
	@Test
	void testIsAccountNonLocked() {
		assertTrue(user.isAccountNonLocked());
	}
	
	@Test
	void testIsCredentialsNonExpired() {
		assertTrue(user.isCredentialsNonExpired());
	}
	
	@Test
	void testIsEnabled() {
		assertTrue(user.isEnabled());
	}

}
