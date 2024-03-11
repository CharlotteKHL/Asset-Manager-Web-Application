package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AssetsrepoApplicationTests {

  
  @Autowired
  private FormController controller;

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotEquals(controller, null);
  }

}
