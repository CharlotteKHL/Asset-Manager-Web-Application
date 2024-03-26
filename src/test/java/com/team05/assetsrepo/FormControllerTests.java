package com.team05.assetsrepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
class FormControllerTests {

  
  @Mock
  private Model model;
	
	@Autowired
  private FormController formController;
  
  private static JSONObject typeObject;
  private static JSONObject assetObject;
  
  @InjectMocks
static
  MockHttpSession mockHttpSession;
  
  @BeforeAll
  static void setUp() throws JSONException {
	  typeObject = new JSONObject();
	  typeObject.put("type", "TestingData");
	  typeObject.put("test", "Text");
	  mockHttpSession = new MockHttpSession();
  }

  //checks to see if the FormController Exists
  @Test
  void contextLoads() {
      assertNotNull(formController);
  }
  
  //Testing the createType method
  @Test
  void testCreateTypeTry() throws JSONException {
	  typeObject = new JSONObject();
	  typeObject.put("type", "TestingDataV2");
	  typeObject.put("test", "Text");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"New asset type created successfully!\"}"), formController.createType(typeObject.toString(), mockHttpSession));
  }
  
  @Test 
  void testCreateTypeCatchJSONProcessing() throws JSONException {
	  String jsonErrorString = "{\"type\": TestingDataV3, \"test\": Text}";
	  String e = "Unrecognized token 'TestingDataV3': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (String)\"{\"type\": TestingDataV3, \"test\": Text}\"; line: 1, column: 23]";
  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" + e + "\"}"), formController.createType(jsonErrorString, mockHttpSession));
  }
  
  @Test
  void testCreateTypeCatchDuplicateKey() {
	  formController.createType(typeObject.toString(), mockHttpSession);
	  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" 
	      + "Please check that the name of your asset type is unique!" + "\"}"), formController.createType(typeObject.toString(), mockHttpSession));
  }
  
  //Testing the updateType method
  @Test
  void testUpdateTypeTry() throws JSONException {
	  typeObject = new JSONObject();
	  typeObject.put("type", "TestData");
	  typeObject.put("test", "Text");
	  typeObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset type updated successfully!\"}"), formController.updateType(typeObject.toString(), mockHttpSession));
  }
  
  @Test
  void testUpdateTypeCatchJSONProcessing() throws JSONException {
	  String jsonErrorString = "{\"type\": TestingData, \"test\": Text}";
	  String e = "Unrecognized token 'TestingData': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (String)\"{\"type\": TestingData, \"test\": Text}\"; line: 1, column: 21]";
  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" + e + "\"}"), formController.updateType(jsonErrorString, mockHttpSession));
  }
  
  //Testing the deleteType method
  @Test
  void testDeleteTypeTryString() {
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset type deleted successfully!\"}"), formController.deleteType("Testing", mockHttpSession));
  }
  
  //Testing the renameType method
  @Test
  void testRenameTypeTry() throws JSONException {
	  typeObject = new JSONObject();
	  typeObject.put("customType", "TestDataUpdated");
	  typeObject.put("overarchingType", "TestData");
	  typeObject.put("type", "TestData");
	  typeObject.put("test", "Text");
	  typeObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset type renamed successfully!\"}"), formController.renameType(typeObject.toString(), mockHttpSession));
  }
  
  @Test
  void testRenameTypeCatchJSONProcessing() throws JSONException {
	  String jsonErrorString = "{\"customType\": TestingDataRenamed, \"overarchingType\": TestData , \"test\": Text}";
	  String e = "Unrecognized token 'TestingDataRenamed': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (String)\"{\"customType\": TestingDataRenamed, \"overarchingType\": TestData , \"test\": Text}\"; line: 1, column: 34]";
  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" + e + "\"}"), formController.renameType(jsonErrorString, mockHttpSession));
  }
  
  @Test
  void testRenameTypeCatchExpection() throws JSONException {
	  typeObject = new JSONObject();
	  typeObject.put("cusType", "TestDataUpdated");
	  typeObject.put("overarchingType", "TestData");
	  typeObject.put("type", "TestData");
	  typeObject.put("test", "Text");
	  typeObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.badRequest()
	          .body("{\"error\": \"" + "An error occurred while renaming asset type!" + "\"}"), formController.renameType(typeObject.toString(), mockHttpSession));
  }
  
  @Test
  void testDeleteTypeCatchDataIntegrity() {
	  assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
	          "{\"error\": \"Unable to delete asset type - please remove any assets currently " 
	                  + "using this type.\"}"), formController.deleteType("Library", mockHttpSession));
  }
  
  //Testing the submitAsset method
  @Test
  void testSubmitAssetTry() throws JSONException {
	  assetObject = new JSONObject();
	  assetObject.put("Title", "TestingAsset");
	  assetObject.put("Association(s)", "AwesomeML");
	  assetObject.put("Type", "TestData");
	  assetObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}"), formController.submitAsset(assetObject.toString(), mockHttpSession));
  }
  
  @Test
  void testSubmitAssetTryMultipleAt() throws JSONException {
	  String jsonObject = "{" +
			    "\"Title\": \"TestingAssetAttributes\"," +
			    "\"Association(s)\": [\"RESTful API\"]," +
			    "\"Type\": \"API\"," +
			    "\"Description\": \"Test\"," +
			    "\"Endpoint\": \"Test\"," +
			    "\"Methods\": \"Test\"," +
			    "\"Response\": \"Test\"" +
			"}";
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}"), formController.submitAsset(jsonObject, mockHttpSession));
  }
  
  @Test
  void testSubmitAssetCatchDuplicateKey() throws JSONException {
	  assetObject = new JSONObject();
	  assetObject.put("Title", "Testing");
	  assetObject.put("Association(s)", "AwesomeML");
	  assetObject.put("Type", "TestData");
	  assetObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" 
		      + "Please check that the name of your asset is unique!" + "\"}"), formController.submitAsset(assetObject.toString(), mockHttpSession));
  }
  
  @Test
  void testSubmitAssetCatchJSONProcessing() throws JSONException {
	  String jsonErrorString = "{\"type\": TestingData, \"test\": Text}";
	  String e = "Unrecognized token 'TestingData': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (String)\"{\"type\": TestingData, \"test\": Text}\"; line: 1, column: 21]";
	  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" + e + "\"}"), formController.submitAsset(jsonErrorString, mockHttpSession));
  }
  
  //Testing the updateAsset method
  @Test
  void testUpdateAssetTry() throws JSONException {
	  assetObject = new JSONObject();
	  assetObject.put("Rename Asset", "UpdatedTesting");
	  assetObject.put("Association(s)", "AwesomeML");
	  assetObject.put("Type", "TestData");
	  assetObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset updated successfully!\"}"), formController.updateAsset(8, assetObject.toString(), mockHttpSession));
  }
  
  @Test
  void testUpdateAssetTryEmptyName() throws JSONException {
	  assetObject = new JSONObject();
	  assetObject.put("Rename Asset", "");
	  assetObject.put("Association(s)", "AwesomeML");
	  assetObject.put("Type", "TestData");
	  assetObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset updated successfully!\"}"), formController.updateAsset(8, assetObject.toString(), mockHttpSession));
  }
  
  @Test
  void testUpdateAssetTryNoRenameAtrribute() throws JSONException {
	  assetObject = new JSONObject();
	  assetObject.put("Association(s)", "AwesomeML");
	  assetObject.put("Type", "TestData");
	  assetObject.put("Description", "Testing the data.");
	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset updated successfully!\"}"), formController.updateAsset(8, assetObject.toString(), mockHttpSession));
  }
  
  @Test
  void testUpdateAssetCatchJSONProcessing() {
	  String jsonErrorString = "{\"type\": TestingData, \"test\": Text}";
	  String e = "Unrecognized token 'TestingData': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n at [Source: (String)\"{\"type\": TestingData, \"test\": Text}\"; line: 1, column: 21]";
	  assertEquals(ResponseEntity.badRequest().body("{\"error\": \"" + e + "\"}"), formController.updateAsset(8, jsonErrorString, mockHttpSession));
  }
  
  //Testing the populateTypesCreateAsset
  @Test
  void testPopulateTypesCreateAsset() {
	  assertEquals("create-asset", formController.populateTypesCreateAsset(model));
  }
  
  @Test
  void testPopulateTypesCreateType() {
	  assertEquals("create-type", formController.populateTypesCreateType(model));
  }
  
  @Test
  void testPopulateTypesManageAsset() {
	  assertEquals("manage-asset", formController.populateTypesManageAsset(model));
  }
  
  //Testing the auditTrail get request
  @Test
  void testGetAuditTrailPage() {
	  assertEquals("audit-trail", formController.populateAuditTrailPage(model));
  }
  
  //Testing the searchAssetPage get request
  @Test
  void testGetSearchAssetPage() {
	  assertEquals("search-asset", formController.getSearchAssetPage(model));
  }
  
  @Test
  void testSearchAssetQueryTryGeneral() throws JSONException {
	  assertEquals("[TestAsset, Testing]", formController.searchAssetQuery("[\"test\", \"TestData\", \"\", \"\", \"Alphabetical: A-Z\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryTryZA() throws JSONException {
	  assertEquals("[Testing branch, Testing]", formController.searchAssetQuery("[\"testing\", \"\", \"\", \"\", \"Alphabetical: Z-A\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryTryMostRecentOldest() throws JSONException {
	  assertEquals("[Testing, Testing branch]", formController.searchAssetQuery("[\"testing\", \"\", \"\", \"\", \"Date: Most Recent Update - Oldest Update\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryTryOldestMostRecent() throws JSONException {
	  assertEquals("[Testing branch, Testing]", formController.searchAssetQuery("[\"testing\", \"\", \"\", \"\", \"Date: Oldest Update - Most Recent Update\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryTryDates() throws JSONException {
	  assertEquals("[Testing]", formController.searchAssetQuery("[\"t\", \"\", \"2024-03-12\", \"2024-03-14\", \"Alphabetical: A-Z\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryTryDatesAndType() throws JSONException {
	  assertEquals("[Testing]", formController.searchAssetQuery("[\"t\", \"TestData\", \"2024-03-12\", \"2024-03-14\", \"Alphabetical: A-Z\"]").toString());
  }
  
  @Test
  void testSearchAssetQueryCatchJSONProcessing() {
	  assertEquals(Collections.emptyList(), formController.searchAssetQuery("DoesNotExsitTest"));
  }
  
  @Test
  void testGetAssetDataTry() {
	  String jsonString = "{\n" +
		        "  \"title\" : \"Testing\",\n" +
		        "  \"type\" : \"TestData\",\n" +
		        "  \"associations\" : \"{5}\",\n" +
		        "  \"last_updated\" : \"2024-03-13 16:23:01.072024\",\n" +
		        "  \"additional_attrs\" : \"{\\\"Description\\\":\\\"Testing the assets creation page.\\\"}\"\n" +
		        "}";
	  assertEquals(jsonString, formController.getAssetData("Testing"));
  }
  
  //Testing the deleteType method
  @Test
  void testDeleteTypeTryID() {
//	  assertEquals(ResponseEntity.ok().body("{\"message\": \"Asset deleted successfully!\"}"), formController.deleteType("TestData"));
  }
  
  
  //Testing the get and fetch attributes methods
  @Test
  void testGetAttributesForTypeNewType() {
	  assertEquals(ResponseEntity.ok(Collections.emptyList()), formController.getAttributesForType("newType"));
  }
  
  @Test
  void testGetAttributesForTypeExistingType() {
	  ResponseEntity<List<String[]>> response = formController.getAttributesForType("TestData");
	  List<String[]> info = response.getBody();
	  assertEquals(HttpStatus.OK,response.getStatusCode());
	  assertEquals("Description", info.get(0)[0]);
	  assertEquals("Text", info.get(0)[1]);
  }
  
  @Test
  void testGetAttributesForAsset() throws JsonMappingException, JsonProcessingException {
	  ResponseEntity<List<String[]>> response = formController.getAttributesForAsset(8);
	  List<String[]> info = response.getBody();
	  assertEquals(HttpStatus.OK,response.getStatusCode());
	  assertEquals("Description", info.get(0)[0]);
	  assertEquals("Testing the assets creation page.", info.get(0)[1]);
  }
  
  @Test
  void testFetchAttributesForTypeFromDatabaseType() {
	  List<String[]> attributesWithTypes = formController.fetchAttributesForTypeFromDatabase("TestData");
	  String formatedAttributes = Arrays.deepToString(attributesWithTypes.toArray());
	  assertEquals("[[Description, Text]]", formatedAttributes);
  }
  
  @Test
  void testFetchAttributesForAssetFromDatabaseId() throws JsonMappingException, JsonProcessingException {
	  List<String[]> attributesWithTypes = formController.fetchAttributesForAssetFromDatabase(4); 
	  String formatedAttributes = Arrays.deepToString(attributesWithTypes.toArray());
	  assertEquals("[[Link, https://examplehere.com/files/meme.md], "
	  		+ "[Format, Markdown], "
	  		+ "[Authors, John, Giorgios], "
	  		+ "[Description, Documentation for the Utility Library.]]", formatedAttributes);
  }
  
  
  

}
