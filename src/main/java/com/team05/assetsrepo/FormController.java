package com.team05.assetsrepo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller responsible for creating assets, creating / updating asset types, and fetching types /
 * attributes.
 */
@Controller
public class FormController {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * Injecting the JdbcTemplate registered in the Spring application context by the DBConfig class.
   */
  public FormController(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Creates a new row in the type table representing the new asset type, given a JSON string
   * representing its attributes.
   *
   * @param pairs the JSON string from the POST request body.
   * @return String a message indicating whether the creation of a new asset type was successful.
   */
  @PostMapping("/createType")
  public ResponseEntity<?> createType(@RequestBody String pairs) {
    try {

      /*
       * Uses the Jackson library to convert the JSON string from the POST request body into a JSON
       * object.
       */
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(pairs);

      // Extracts the name of the new asset type from the first pairing, then removes the pairing.
      String typeName = (jsonNode.get("type")).asText();
      ObjectNode object = (ObjectNode) jsonNode;
      object.remove("type");
      pairs = objectMapper.writeValueAsString(object);

      /*
       * Iterates over the JSON object. Sets up two lists: one for the attribute names, one for the
       * attribute values. These serve the purpose of storing the order of attributes. These can
       * then be displayed as required.
       */
      List<String> attrKeysList = new ArrayList<>();
      List<String> attrValuesList = new ArrayList<>();
      jsonNode.fields().forEachRemaining(entry -> {
        attrKeysList.add(entry.getKey());
        attrValuesList.add(entry.getValue().asText());
      });

      /*
       * Converts both lists from ArrayList<String> to String[] to ensure they can be stored in the
       * database.
       */
      String[] attrKeys = attrKeysList.toArray(new String[0]);
      String[] attrValues = attrValuesList.toArray(new String[0]);

      // Creates a new row in the type table
      String sql = "INSERT INTO type (type_name, attributes, attr_keys, attr_backend_types) "
          + "VALUES (:typeName, CAST(:pairs AS JSON), :attrKeys, :attrValues)";

      // Defines what Java variable / object each parameter in the SQL statement corresponds to.
      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("typeName", typeName).addValue("pairs", pairs)
              .addValue("attrKeys", attrKeys).addValue("attrValues", attrValues);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"New asset type created successfully!\"}");

    } catch (JsonProcessingException e) {

      e.printStackTrace();
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      e.printStackTrace();
      return ResponseEntity.badRequest().body(
          "{\"error\": \"" + "Please check that the name of your asset type is unique!" + "\"}");

    }
  }

  /**
   * Updates an existing row in the type table, given a JSON string representing an asset type's new
   * attributes.
   *
   * @param pairs the JSON string from the POST request body.
   * @return String a message indicating whether updating the asset type was successful.
   */
  @PostMapping("/updateType")
  public ResponseEntity<?> updateType(@RequestBody String pairs) {
    try {

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(pairs);

      String typeName = (jsonNode.get("type")).asText();
      ObjectNode object = (ObjectNode) jsonNode;
      object.remove("type");
      pairs = objectMapper.writeValueAsString(object);

      List<String> attrKeysList = new ArrayList<>();
      List<String> attrValuesList = new ArrayList<>();
      jsonNode.fields().forEachRemaining(entry -> {
        attrKeysList.add(entry.getKey());
        attrValuesList.add(entry.getValue().asText());
      });

      String[] attrKeys = attrKeysList.toArray(new String[0]);
      String[] attrValues = attrValuesList.toArray(new String[0]);

      String sql = "UPDATE type SET type_name = :typeName, attributes = CAST(:pairs AS JSON), "
          + "attr_keys = :attrKeys, attr_backend_types = :attrValues WHERE type_name = :typeName";

      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("typeName", typeName).addValue("pairs", pairs)
              .addValue("attrKeys", attrKeys).addValue("attrValues", attrValues);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"Asset type updated successfully!\"}");

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }

  /**
   * Handles the HTTP POST request to delete an asset type.
   *
   * @param selectedType The name of the asset type to be deleted.
   * @return ResponseEntity containing a JSON response indicating success or failure of the
   *         operation.
   */
  @PostMapping("/deleteType")
  public ResponseEntity<?> deleteType(@RequestBody String selectedType) {
    try {
      String statement = "DELETE FROM type WHERE type_name = :typeParam";
      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("typeParam", selectedType);
      jdbcTemplate.update(statement, params);

      // Return a JSON response with the success message
      return ResponseEntity.ok().body("{\"message\": \"Asset type deleted successfully!\"}");
    } catch (DataIntegrityViolationException e) {
      // Catch the specific exception for foreign key violation
      // Return a custom error message indicating the foreign key constraint violation
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
          "{\"error\": \"Unable to delete type: This type is still referenced by other records.\"}");
    } catch (Exception e) {
      // Catch other exceptions and return a JSON response with a generic error message
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("{\"error\": \"An error occurred while deleting type\"}");
    }
  }

  /**
   * Handles the HTTP POST request to rename an asset type.
   *
   * @param pairs A JSON string containing the old and new type names, along with attribute details.
   * @return ResponseEntity containing a JSON response indicating success or failure of the
   *         operation.
   */
  @PostMapping("/renameType")
  public ResponseEntity<?> renameType(@RequestBody String pairs) {
    try {
      // Use Jackson library to parse the JSON string from the request body
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(pairs);

      // Extract the new type name and remove it from the JSON object
      String newTypeName = (jsonNode.get("customType")).asText();
      ObjectNode object = (ObjectNode) jsonNode;
      // Extract the old type name from the JSON object
      String oldTypeName = (jsonNode.get("overarchingType")).asText();
      object.remove("customType");
      object.remove("overarchingType");
      pairs = objectMapper.writeValueAsString(object);


      // Convert the JSON object into lists of attribute names and values
      List<String> attrKeysList = new ArrayList<>();
      List<String> attrValuesList = new ArrayList<>();
      jsonNode.fields().forEachRemaining(entry -> {
        attrKeysList.add(entry.getKey());
        attrValuesList.add(entry.getValue().asText());
      });

      // Convert the lists into arrays to store in the database
      String[] attrKeys = attrKeysList.toArray(new String[0]);
      String[] attrValues = attrValuesList.toArray(new String[0]);

      // Update the row in the type table with the new type name and attributes
      String sql = "UPDATE type SET type_name = :newTypeName, attributes = CAST(:pairs AS JSON), "
          + "attr_keys = :attrKeys, attr_backend_types = :attrValues WHERE type_name = :oldTypeName";

      // Define the parameters for the SQL statement
      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("newTypeName", newTypeName).addValue("pairs", pairs)
              .addValue("attrKeys", attrKeys).addValue("attrValues", attrValues)
              .addValue("oldTypeName", oldTypeName);

      // Execute the update query
      jdbcTemplate.update(sql, params);

      // Return success message
      return ResponseEntity.ok().body("{\"message\": \"Asset type renamed successfully!\"}");

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "An error occurred while renaming asset type!" + "\"}");
    }
  }


  /**
   * Creates a new row in the asset table representing a new asset, given a JSON string representing
   * its attributes.
   *
   * @param obj the JSON string from the POST request body.
   * @return String a message indicating whether the creation of a new asset was successful.
   */
  @PostMapping("/submitAsset")
  public ResponseEntity<?> submitAsset(@RequestBody String obj) {
    try {

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(obj);

      String title = (jsonNode.get("Title").asText());
      String typeAsStr = (jsonNode.get("Type").asText());

      String sqlGetTypeId = "SELECT id FROM type WHERE type_name = :typeAsStr";

      MapSqlParameterSource params = new MapSqlParameterSource().addValue("typeAsStr", typeAsStr);

      /*
       * Since the type of an asset is stored as integer in the database, which also acts as a
       * foreign key, we execute the following operation to get the id from the type's name.
       */
      int type = jdbcTemplate.queryForObject(sqlGetTypeId, params, Integer.class);

      /*
       * Extracts the array representing asset associations from the JSON object, converts to Java
       * List.
       */
      JsonNode associationsNode = jsonNode.get("Association(s)");
      List<String> associationsList = new ArrayList<>();
      for (JsonNode node : associationsNode) {
        associationsList.add(node.asText());
      }

      /*
       * Iterates over the list of associations, executing the below SQL statement for each
       * associated asset to obtain its id. These are stored in the list associationIds.
       */
      List<Integer> associationIds = new ArrayList<>();
      for (String assetName : associationsList) {
        String sqlGetAssociationId = "SELECT id FROM assets WHERE title = :assetName";
        params = new MapSqlParameterSource().addValue("assetName", assetName);
        Integer assetId = jdbcTemplate.queryForObject(sqlGetAssociationId, params, Integer.class);
        associationIds.add(assetId);
      }

      /*
       * Converts from ArrayList<Integer> to Integer[] to ensure associations can be stored in the
       * database.
       */
      Integer[] associations = associationIds.toArray(new Integer[0]);

      /*
       * Ensures we pass a JSON string which only contains the type-specific attributes - this is
       * for the additional_attrs column.
       */
      ObjectNode object = (ObjectNode) jsonNode;
      object.remove(jsonNode.fieldNames().next());
      object.remove(jsonNode.fieldNames().next());
      object.remove(jsonNode.fieldNames().next());
      /*
       * Converts from JSON object --> JSON string to ensure it can be successfully stored in the
       * database.
       */
      obj = objectMapper.writeValueAsString(object);

      String sql = "INSERT INTO assets (title, type, associations, additional_attrs) "
          + "VALUES (:title, :type, :associations, CAST(:obj AS JSONB))";

      params = new MapSqlParameterSource().addValue("title", title).addValue("type", type)
          .addValue("associations", associations).addValue("obj", obj);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}");

    } catch (JsonProcessingException e) {

      e.printStackTrace();
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      e.printStackTrace();
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "Please check that the name of your asset is unique!" + "\"}");

    }
  }

  /**
   * Retrieves the HTML page for creating an asset and fetches types from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return The name of the HTML page for creating an asset ("create-asset").
   */
  @GetMapping("/create-asset.html")
  public String populateTypesCreateAsset(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type_name FROM type",
        Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    List<String> assets = jdbcTemplate.queryForList("SELECT DISTINCT title FROM assets",
        Collections.emptyMap(), String.class);
    model.addAttribute("assets", assets);
    return "create-asset";
  }

  /**
   * Retrieves the HTML page for creating a type and fetches types from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return The name of the HTML page for creating a type ("create-type").
   */
  @GetMapping("/create-type.html")
  public String populateTypesCreateType(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type_name FROM type",
        Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    return "create-type";
  }

  /**
   * Intermediary method for getting attributes associated with a given type from the database.
   *
   * @param type The given asset type.
   * @return A ResponseEntity containing a list of attributes associated with the provided type.
   */
  @GetMapping("/attributes/{type}")
  public ResponseEntity<List<String[]>> getAttributesForType(@PathVariable String type) {
    if (type.equals("newType")) {
      return ResponseEntity.ok(Collections.emptyList());
    }
    List<String[]> attributes = fetchAttributesForTypeFromDatabase(type);
    return ResponseEntity.ok(attributes);
  }

  /**
   * Intermediary method for getting attributes associated with a given type from the database.
   *
   * @param type The given asset type.
   * @return A ResponseEntity containing a list of attributes associated with the provided type.
   * @throws JsonProcessingException 
   * @throws JsonMappingException 
   */
  @GetMapping("/attributesWassetData/{id}")
  public ResponseEntity<List<String[]>> getAttributesForAsset(@PathVariable int id) throws JsonMappingException, JsonProcessingException {
    List<String[]> attributeData = fetchAttributesForAssetFromDatabase(id);
    return ResponseEntity.ok(attributeData);
  }

  /**
   * Retrieves the HTML page for creating an asset and fetches types from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return The name of the HTML page for creating an asset ("create-asset").
   */
  @GetMapping("/manage-asset.html")
  public String populateTypesManageAsset(Model model) {
    List<Map<String, Object>> types =
        jdbcTemplate.queryForList("SELECT DISTINCT type_name FROM type", Collections.emptyMap());
    model.addAttribute("types", types);

    List<Map<String, Object>> assets =
        jdbcTemplate.queryForList("SELECT assets.title, assets.id, type.type_name\r\n"
            + "FROM assets\r\n" + "JOIN type ON assets.type = type.id", Collections.emptyMap());
    model.addAttribute("assets", assets);

    return "manage-asset";
  }


  /**
   * Fetches attributes for the selected type from the database.
   *
   * @param type The selected asset type for which attributes are to be fetched.
   * @return A list of attributes associated with the provided type.
   */
  private List<String[]> fetchAttributesForTypeFromDatabase(String type) {
    String sql = "SELECT attr_keys, attr_backend_types FROM type WHERE type_name = :type";

    MapSqlParameterSource params = new MapSqlParameterSource().addValue("type", type);

    String[] keysAndTypes = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
      String keys = rs.getString("attr_keys");
      String types = rs.getString("attr_backend_types");
      return new String[] {keys, types};
    });

    String keys = keysAndTypes[0].replaceAll("[{}\"]", "");
    String types = keysAndTypes[1].replaceAll("[{}\"]", "");
    String[] parsedKeys = keys.split(",");
    String[] parsedTypes = types.split(",");

    List<String[]> attributesWithTypes = new ArrayList<>();
    for (int i = 0; i < parsedKeys.length; i++) {
      String key = parsedKeys[i].trim();
      String backendType = parsedTypes[i].trim();
      attributesWithTypes.add(new String[] {key, backendType});
    }
    System.out.println(Arrays.deepToString(attributesWithTypes.toArray()));
    return attributesWithTypes;
  }

  /**
   * Fetches attributes for the selected type from the database.
   *
   * @param type The selected asset type for which attributes are to be fetched.
   * @return A list of attributes associated with the provided type.
   * @throws JsonProcessingException
   * @throws JsonMappingException
   */
  public List<String[]> fetchAttributesForAssetFromDatabase(int id)
      throws JsonMappingException, JsonProcessingException {
    String sql = "SELECT additional_attrs FROM assets WHERE id = :id";

    MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

    String jsonString = jdbcTemplate.queryForObject(sql, params, String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> attributeMap =
        objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});

    List<String[]> descriptionValue = new ArrayList<>();
    for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      descriptionValue.add(new String[] {key, value.toString()});
    }
    System.out.println(Arrays.deepToString(descriptionValue.toArray()));
    return descriptionValue;
  }


}
