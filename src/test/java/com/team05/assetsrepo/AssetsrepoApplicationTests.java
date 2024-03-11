package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AssetsrepoApplicationTests {

  
  @Autowired
  private FormController mockMVC;
  
  @MockBean
  private final NamedParameterJdbcTemplate jdbcTemplate = null;
  
//  @BeforeAll
//  void setUp() {
//	  this.jdbcTemplate = 
//  }

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotEquals(mockMVC, null);
  }

}
