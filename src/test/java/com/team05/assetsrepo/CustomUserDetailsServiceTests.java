package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class CustomUserDetailsServiceTests {

	  
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
	
	@Test
	void testLoadUserByUsernameNotNull() {
		assertNotNull(customUserDetailsService.loadUserByUsername("Test@gmail.com"));
	}
	
	@Test
	void testLoadUserByUsernameCatch() {
		assertThrows(BadCredentialsException.class, () -> customUserDetailsService.loadUserByUsername("TestData@gmail.com"));
	}

}
