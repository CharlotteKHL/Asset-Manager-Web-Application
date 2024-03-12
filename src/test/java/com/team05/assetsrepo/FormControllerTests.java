package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class FormControllerTests {

  
  @Autowired
  private FormController formController;

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotNull(formController);
  }
  
  @Test
  void testDeleteTypeCatch() {
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset type deleted successfully!\"}"), formController.deleteType("TestData"));
  }
  
  @Test
  void testDeleteTypeCatchDataIntegrity() {
	  assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
	          "{\"error\": \"Unable to delete type: This type is still referenced by other records.\"}"), formController.deleteType("Library"));
  }
  
  @Test
  void testGetAttributesForTypeNewType() {
	  assertEquals(ResponseEntity.ok(Collections.emptyList()), formController.getAttributesForType("newType"));
  }
  
  @Test
  void testFetchAttributesForTypeFromDatabase() {
	  List<String[]> attributesWithTypes = formController.fetchAttributesForTypeFromDatabase("TestData");
	  String formatedAttributes = Arrays.deepToString(attributesWithTypes.toArray());
	  assertEquals("[[Description, Text]]", formatedAttributes);
  }
  
  
  

}
