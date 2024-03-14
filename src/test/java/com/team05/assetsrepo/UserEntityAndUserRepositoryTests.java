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
		user = userRepository.findByUsername("Test@gmail.com");
	}
	
	@Test
	void testGetID() {
		assertEquals(2, user.getID());
	}
	
	@Test
	void testSetID() {
		user.setID(110);
		assertEquals(110, user.getID());
	}
	
	@Test
	void testGetAuthorities() {
		assertNull(user.getAuthorities());
	}
	
	@Test
	void testGetPassword() {
		assertEquals("test", user.getPassword());
	}
	
	@Test
	void testGetUsername() {
		assertEquals("Test@gmail.com", user.getUsername());
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
