package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountControllerTests {
  
  @Autowired
  private AccountController accountController;
  
  @InjectMocks
  static MockHttpSession mockHttpSession;
  
  @BeforeAll
  static void setUp() {
	  mockHttpSession = new MockHttpSession();
	  mockHttpSession.setAttribute("id", 1);
  }

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotNull(accountController);
  }
  
//  @Test
//  void testCheckSessionValid() {
//	  assertEquals(ResponseEntity.ok().body("{\"username\": \"" + "testSessions" + "\"}"), accountController.checkSession(mockHttpSession));
//  }
  
  @Test
  void testCheckSessionInvalid() {
	  MockHttpSession mockHttpSessionInvalid = new MockHttpSession();
	  assertEquals(ResponseEntity.ok().body("{\"username\": \"" + "You are no longer logged in" + "\"}"), accountController.checkSession(mockHttpSessionInvalid));
	  
  }
  
  //Testing the extractLogin method (with static variables)
  @Test
  void testExtractLoginTry() throws InvalidLogin {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Login successful\"}")), accountController.extractLogin("test@gmail.com", "test", mockHttpSession));
  }
  
  //Testing the try block of the validateLoginDetails method.
  @Test
  void validateLoginDetailsTry() {
	  assertEquals(accountController.validateLoginDetails("test@gmail.com", "test"), "Login successful");
	  
  }
  
  //Testing the getPassword method.
  @Test
  void testGetPassword() {
	  assertEquals("$2a$10$G1qPCx8lNfo3VE.5de6/puaUjNQERycd.W.3f95MVxaDTGf5YLW12", accountController.getPassword("test@gmail.com"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration successful\"}")), accountController.register("testedData@gmail.com", "testedData"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterNotUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration unsuccessful, this email may already have an account\"}")), accountController.register("test@gmail.com", "testedData"));
  }
  
  @Test
  void testLogout() {
	  assertEquals("Logout success", accountController.logout(mockHttpSession));
  }

}
