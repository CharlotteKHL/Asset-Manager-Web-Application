package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountControllerTests {

  
  @Autowired
  private AccountController mockMVC;

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotEquals(mockMVC, null);
      System.out.println(mockMVC);
  }
  
  //Testing the extractLogin method (with static variables)
  @Test
  void testExtractLoginTry() throws InvalidLogin {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Login successful\"}")), mockMVC.extractLogin("hi@gmail.com", "hi"));
  }
  
  //Testing the try block of the validateLoginDetails method.
  @Test
  void validateLoginDetailsTry() {
	  assertEquals(mockMVC.validateLoginDetails("hi@gmail.com", "hi"), "Login successful");
	  
  }
  
////Testing for the invalid Login catch block from the validateLoginDetails method.
//@Test
//void testValidateLoginDetailsCatch() throws InvalidLogin {
//	  InvalidLogin errorMessage = assertThrows(InvalidLogin.class, () -> mockMVC.extractLogin("wrongUsername@Gmail.com", "wrongPassword"));
//	  assertEquals("This password is not correct", errorMessage);
//}
  
  //Testing the getPassword method.
  @Test
  void testGetPassword() {
	  assertEquals("hi", mockMVC.getPassword("hi@gmail.com"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration successful\"}")), mockMVC.register("tested@Gmail.com", "testedData"));
  }
  
  //Testing the getPassword method.
  @Test
  void testRegisterNotUniqueUsername() {
	  assertEquals((ResponseEntity.ok().body("{\"message\": \"Registration unsuccessful, this email may already have an account\"}")), mockMVC.register("hi@gmail.com", "testedData"));
  }

}
