package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountControllerTests {
  
  @Autowired
  private AccountController accountController;

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotEquals(accountController, null);
      System.out.println(accountController);
  }
  
  //Testing the extractLogin method (with static variables)
  @Test
  void testExtractLoginTry() throws InvalidLogin {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Login successful\"}")), accountController.extractLogin("hi@gmail.com", "hi"));
  }
  
  //Testing the try block of the validateLoginDetails method.
  @Test
  void validateLoginDetailsTry() {
	  assertEquals(accountController.validateLoginDetails("hi@gmail.com", "hi"), "Login successful");
	  
  }
  
////Testing for the invalid Login catch block from the validateLoginDetails method.
//@Test
//void testValidateLoginDetailsCatch(){
//	  assertThrows(InvalidLogin.class, () -> accountController.extractLogin("wrongUsername@Gmail.com", "wrongPassword"));
//}
  
  //Testing the getPassword method.
  @Test
  void testGetPassword() {
	  assertEquals("hi", accountController.getPassword("hi@gmail.com"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration successful\"}")), accountController.register("testedData@gmail.com", "testedData"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterNotUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration unsuccessful, this email may already have an account\"}")), accountController.register("hi@gmail.com", "testedData"));
  }

}
