package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountControllerTests {

  
  @Autowired
  private AccountController mockMVC;
  
  @MockBean
  private final NamedParameterJdbcTemplate jdbcTemplate = null;

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotEquals(mockMVC, null);
  }
  

}
