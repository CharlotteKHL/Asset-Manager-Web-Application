package com.team05.assetsrepo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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
   * @return ResponseEntity containing a message indicating whether the creation of a new asset type
   *         was successful.
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

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:typeName, 'Created', '[...]', 'Type')";
      MapSqlParameterSource logParams = new MapSqlParameterSource().addValue("typeName", typeName);
      jdbcTemplate.update(logSql, logParams);

      return ResponseEntity.ok().body("{\"message\": \"New asset type created successfully!\"}");

    } catch (JsonProcessingException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body(
          "{\"error\": \"" + "Please check that the name of your asset type is unique!" + "\"}");

    }
  }

  /**
   * Updates an existing row in the type table, given a JSON string representing an asset type's new
   * attributes.
   *
   * @param pairs the JSON string from the POST request body.
   * @return ResponseEntity containing a message indicating whether updating the asset type was
   *         successful.
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

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:typeName, 'Updated', '[...]', 'Type')";
      MapSqlParameterSource logParams = new MapSqlParameterSource().addValue("typeName", typeName);
      jdbcTemplate.update(logSql, logParams);

      return ResponseEntity.ok().body("{\"message\": \"Asset type updated successfully!\"}");

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
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

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:typeName, 'Deleted', '[...]', 'Type')";
      MapSqlParameterSource logParams =
          new MapSqlParameterSource().addValue("typeName", selectedType);
      jdbcTemplate.update(logSql, logParams);

      // Return a JSON response with the success message
      return ResponseEntity.ok().body("{\"message\": \"Asset type deleted successfully!\"}");

    } catch (DataIntegrityViolationException e) {
      // Catch the specific exception for foreign key violation
      // Return a custom error message indicating the foreign key constraint violation
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("{\"error\": \"Unable to delete asset type - please remove any assets currently "
              + "using this type.\"}");
    } catch (Exception e) {
      // Catch other exceptions and return a JSON response with a generic error message
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("{\"error\": \"An error occurred whilst attempting to delete the type.\"}");
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
          + "attr_keys = :attrKeys, attr_backend_types = :attrValues "
          + "WHERE type_name = :oldTypeName";

      // Define the parameters for the SQL statement
      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("newTypeName", newTypeName).addValue("pairs", pairs)
              .addValue("attrKeys", attrKeys).addValue("attrValues", attrValues)
              .addValue("oldTypeName", oldTypeName);

      // Execute the update query
      jdbcTemplate.update(sql, params);

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:typeName, 'Updated', '[...]', 'Type')";
      MapSqlParameterSource logParams =
          new MapSqlParameterSource().addValue("typeName", oldTypeName);
      jdbcTemplate.update(logSql, logParams);

      // Return success message
      return ResponseEntity.ok().body("{\"message\": \"Asset type renamed successfully!\"}");

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (Exception e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "An error occurred while renaming asset type!" + "\"}");
    }
  }

  /**
   * Creates a new row in the asset table representing a new asset, given a JSON string representing
   * its attributes.
   *
   * @param obj the JSON string from the POST request body.
   * @return ResponseEntity containing a message indicating whether the creation of a new asset was
   *         successful.
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

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:assetName, 'Created', '[...]', 'Asset')";
      MapSqlParameterSource logParams = new MapSqlParameterSource().addValue("assetName", title);
      jdbcTemplate.update(logSql, logParams);

      return ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}");

    } catch (JsonProcessingException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "Please check that the name of your asset is unique!" + "\"}");

    }
  }

  /**
   * Updates an existing asset in the database with the specified ID using the provided JSON object.
   *
   * @param id The ID of the asset to be updated.
   * @param obj The JSON object containing the updated asset data.
   * @return ResponseEntity indicating the success or failure of the update operation.
   */
  @PostMapping("/updateAsset/{id}")
  public ResponseEntity<?> updateAsset(@PathVariable("id") int id, @RequestBody String obj) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(obj);
      ObjectNode object = (ObjectNode) jsonNode;

      String title = "";
      try {
        // Extract the title from the JSON object
        title = jsonNode.get("Rename Asset").asText();
        object.remove(jsonNode.fieldNames().next());
        object.remove(jsonNode.fieldNames().next());
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.out.println("Title not changed.");
      }

      // Remove the fields from the JSON object that you don't want to update
      object.remove(jsonNode.fieldNames().next());

      // Convert the modified JSON object back to a string
      obj = objectMapper.writeValueAsString(object);

      String sql = null;
      SqlParameterSource params = null;

      if (!(title.equals(""))) {
        // Update the title and additional_attrs in the assets table
        sql = "UPDATE assets SET title = :title, additional_attrs = CAST(:obj AS JSONB) "
            + "WHERE id = :id";
        params = new MapSqlParameterSource().addValue("title", title).addValue("obj", obj)
            .addValue("id", id);
      } else {
        sql = "UPDATE assets SET additional_attrs = CAST(:obj AS JSONB) WHERE id = :id";
        params = new MapSqlParameterSource().addValue("obj", obj).addValue("id", id);
      }

      jdbcTemplate.update(sql, params);

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:assetName, 'Updated', '[...]', 'Asset')";
      MapSqlParameterSource logParams = new MapSqlParameterSource().addValue("assetName", title);
      jdbcTemplate.update(logSql, logParams);

      return ResponseEntity.ok().body("{\"message\": \"Asset updated successfully!\"}");

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
    } catch (DuplicateKeyException e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "Please check that the name of your asset is unique!" + "\"}");
    } catch (DataAccessException e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest()
          .body("{\"error\": \"" + "Failed to update the asset in the database." + "\"}");
    }
  }

  /**
   * Retrieves the HTML page for creating an asset and fetches types from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return String the name of the HTML page for creating an asset ("create-asset").
   */
  @GetMapping("/create-asset.html")
  public String populateTypesCreateAsset(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT type_name FROM type ORDER BY id ASC",
        Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    List<String> assets = jdbcTemplate.queryForList("SELECT title FROM assets " + "ORDER BY id ASC",
        Collections.emptyMap(), String.class);
    model.addAttribute("assets", assets);
    return "create-asset";
  }

  /**
   * Retrieves the HTML page for creating a type and fetches types from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return String the name of the HTML page for creating a type ("create-type").
   */
  @GetMapping("/create-type.html")
  public String populateTypesCreateType(Model model) {
    List<String> types = jdbcTemplate.queryForList(
        "SELECT type_name FROM type " + "ORDER BY id ASC", Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    return "create-type";
  }

  /**
   * This method is required to load the search-asset.html page / template.
   *
   * @return String the "search-asset.html" HTML page.
   */
  @GetMapping("/search-asset.html")
  public String getSearchAssetPage(Model model) {
    List<String> types = jdbcTemplate.queryForList(
        "SELECT type_name FROM type " + "ORDER BY id ASC", Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    return "search-asset";
  }

  /**
   * This method is required to load the audit-trail.html page.
   *
   * @return String the "audit-trail.html" HTML page.
   */
  @GetMapping("/audit-trail.html")
  public String populateAuditTrailPage(Model model) {
    List<Map<String, Object>> auditTrailData = jdbcTemplate
        .queryForList("SELECT * FROM audit_log ORDER BY last_updated", Collections.emptyMap());
    model.addAttribute("auditTrailData", auditTrailData);
    return "audit-trail";
  }

  /**
   * Queries the database for matching assets based on the keyword entered / filters selected by the
   * user.
   *
   * @param searchData a JSON string containing the keyword entered, followed by the values of each
   *        selection from the filter.
   * @return List containing the names of the assets which match the criteria.
   */
  @PostMapping("/searchAssetQuery")
  @ResponseBody
  public List<String> searchAssetQuery(@RequestBody String searchData) {
    try {

      // Converts the JSON string to an array using Jackson.
      ObjectMapper mapper = new ObjectMapper();
      String[] searchDataArr = mapper.readValue(searchData, String[].class);

      // Obtain the keyword relating to the asset to query.
      String assetToQuery = searchDataArr[0];

      /*
       * Default SQL statement which obtains the name of the asset which is similar to the keyword
       * entered.
       */
      String sql = "SELECT title FROM assets WHERE title ILIKE :assetToQuery";

      // The "assetToQuery" parameter stores the keyword.
      MapSqlParameterSource params =
          new MapSqlParameterSource().addValue("assetToQuery", "%" + assetToQuery + "%");

      // Checks whether a type was selected via the "Type" filter.
      if (searchDataArr[1] != "") {
        /*
         * Then, we check whether both a start date and an end date were specified in the
         * "Date Range" filter.
         */
        if (searchDataArr[2] != "" && searchDataArr[3] != "") {
          /*
           * Builds upon the default SQL statement to also consider the chosen type, start date, and
           * end date.
           */
          sql = "SELECT title FROM assets JOIN type ON assets.type = type.id WHERE assets.title "
              + "ILIKE :assetToQuery AND type.type_name = :chosenType AND assets.last_updated "
              + "BETWEEN CAST(:dateFrom AS TIMESTAMP) AND CAST(:dateTo AS TIMESTAMP)";
          params.addValue("chosenType", searchDataArr[1]);
          params.addValue("dateFrom", searchDataArr[2]);
          params.addValue("dateTo", searchDataArr[3]);
        } else {
          // Otherwise, we only consider the type as an additional filter.
          sql = "SELECT title FROM assets JOIN type ON assets.type = type.id WHERE assets.title "
              + "ILIKE :assetToQuery AND type.type_name = :chosenType";
          params.addValue("chosenType", searchDataArr[1]);
        }
        /*
         * If only the dates were specified, then only the dates are considered as an additional
         * filter.
         */
      } else {
        if (searchDataArr[2] != "" && searchDataArr[3] != "") {
          sql = "SELECT title FROM assets WHERE title ILIKE :assetToQuery AND assets.last_updated "
              + "BETWEEN CAST(:dateFrom AS TIMESTAMP) AND CAST(:dateTo AS TIMESTAMP)";
          params.addValue("dateFrom", searchDataArr[2]);
          params.addValue("dateTo", searchDataArr[3]);
        }
      }

      /*
       * Checks whether the user selected a choice in the "Sort by" filter section, adding to the
       * SQL statement accordingly.
       */
      switch (searchDataArr[4]) {
        case "Alphabetical: A-Z":
          sql = sql + " ORDER BY title ASC";
          break;
        case "Alphabetical: Z-A":
          sql = sql + " ORDER BY title DESC";
          break;
        case "Date: Most Recent Update - Oldest Update":
          sql = sql + " ORDER BY assets.last_updated ASC";
          break;
        case "Date: Oldest Update - Most Recent Update":
          sql = sql + " ORDER BY assets.last_updated DESC";
          break;
        default:
          break;
      }

      // Carry out the query and store the names of the resulting assets in a list.
      List<String> matchingAssets = jdbcTemplate.queryForList(sql, params, String.class);

      return matchingAssets;

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Retrieves the rest of an asset's data given its name.
   *
   * @param assetName the name of the asset whose metadata is to be retrieved.
   * @return String a JSON string containing the given asset's metadata.
   */
  @PostMapping("/getAssetData")
  @ResponseBody
  public String getAssetData(@RequestBody String assetName) {
    // Obtains data from all the other columns of an asset given its name.
    String sql = "SELECT * FROM assets WHERE title = :assetName";

    MapSqlParameterSource params = new MapSqlParameterSource().addValue("assetName", assetName);

    // Map used to store the result.
    Map<String, Object> assetData = jdbcTemplate.queryForMap(sql, params);

    // Removes the entry relating to the id, as this is not required.
    assetData.remove("id");
    // Removes the entry containing the additional attributes of the asset.
    // This entry is stored in additionalAttrs.
    Object additionalAttrs = assetData.remove("additional_attrs");

    /*
     * Since the type of the asset is stored as the type's id as opposed to the name, we retrieve
     * the name from the id itself.
     */
    Object typeId = assetData.get("type");

    sql = "SELECT type_name FROM type WHERE id = :typeId";

    params = new MapSqlParameterSource().addValue("typeId", typeId);

    String type = jdbcTemplate.queryForObject(sql, params, String.class);

    // Change the entry relating to the asset's type to contain the actual name of the type.
    assetData.put("type", type);

    try {

      // Below, we create a JSON object from scratch via Jackson's ObjectNode.
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode assetDataJson = mapper.createObjectNode();

      // Adds each Map entry as a JSON entry to the JSON object.
      assetData.forEach((key, value) -> {
        assetDataJson.put(key, value.toString());
      });

      // Converts the Java Object containing the additional attributes of the asset to JSON.
      JsonNode additionalAttrsNode = mapper.valueToTree(additionalAttrs);
      /*
       * Extracts only the additional attributes of the asset via the field name "value", hence
       * ignoring any other metadata.
       */
      String additionalAttrsStr = additionalAttrsNode.get("value").asText();
      // Converts the previous JSON string back into a JSON object.
      JsonNode additionalAttrsData = mapper.readTree(additionalAttrsStr);

      /*
       * Sets up a LinkedHashMap so that the additional attributes of the asset appear in the order
       * defined within the asset's type.
       */
      Map<String, Object> additionalAttrsOrdered = new LinkedHashMap<>();

      /*
       * Obtains 2D array in the form [["Attribute Name", "Custom Datatype"], [...]] representing
       * each attribute name and its custom datatype in their originally defined order via the
       * fetchAttributesForTypeFromDatabase method.
       */
      List<String[]> attrKeysAndValues = fetchAttributesForTypeFromDatabase(type);
      /*
       * Since the entries of attrKeysAndValues are in their originally defined order, we can
       * display the asset's attributes in the order they appear when viewing the attributes of its
       * type.
       */
      for (String[] pair : attrKeysAndValues) {
        /*
         * Adds a new entry (key-value pair) to the LinkedHashMap in the form "Attribute": Value,
         * where the value is obtained from the original (unordered) JSON containing the additional
         * attributes.
         */
        additionalAttrsOrdered.put(pair[0], additionalAttrsData.get(pair[0]));
      }

      // Converts the LinkedHashMap to JSON.
      JsonNode additionalAttrsDataOrdered =
          mapper.convertValue(additionalAttrsOrdered, JsonNode.class);

      /*
       * Then, convert to a TextNode to ensure we can accordingly place the additional attributes
       * JSON within our overall JSON.
       */
      TextNode additionalAttrsTextNode =
          new TextNode(mapper.writeValueAsString(additionalAttrsDataOrdered));

      assetDataJson.set("additional_attrs", additionalAttrsTextNode);

      // Convert to JSON string.
      String assetDataJsonStr =
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(assetDataJson);

      // Return the asset data as a JSON string.
      return assetDataJsonStr;

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
      return e.getMessage();
    }
  }

  /**
   * Intermediary method for getting attributes associated with a given type, in their originally
   * defined order, from the database.
   *
   * @param type The given asset type.
   * @return ResponseEntity containing a list of attributes associated with the provided type.
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
   * Retrieves attribute data for a specific asset identified by the provided ID.
   *
   * @param id The ID of the asset for which attribute data is to be retrieved.
   * @return ResponseEntity containing the attribute data for the asset.
   * @throws JsonMappingException If there is an issue with mapping JSON data.
   * @throws JsonProcessingException If there is an issue with processing JSON data.
   */
  @GetMapping("/attributesWassetData/{id}")
  public ResponseEntity<List<String[]>> getAttributesForAsset(@PathVariable int id)
      throws JsonMappingException, JsonProcessingException {
    List<String[]> attributeData = fetchAttributesForAssetFromDatabase(id);
    return ResponseEntity.ok(attributeData);
  }

  /**
   * Populates the "Manage Assets" page with types and assets data.
   *
   * @param model The model to which the types and assets data will be added.
   * @return The name of the view to render, in this case, "manage-asset.html".
   */
  @GetMapping("/manage-asset.html")
  public String populateTypesManageAsset(Model model) {
    List<Map<String, Object>> types = jdbcTemplate
        .queryForList("SELECT type_name FROM type ORDER BY id ASC", Collections.emptyMap());
    model.addAttribute("types", types);

    List<Map<String, Object>> assets = jdbcTemplate
        .queryForList("SELECT assets.title, assets.id, type.type_name\r\n" + "FROM assets\r\n"
            + "JOIN type ON assets.type = type.id ORDER BY id ASC", Collections.emptyMap());
    model.addAttribute("assets", assets);

    return "manage-asset";
  }

  /**
   * Deletes the asset with the specified ID from the database.
   *
   * @param id The ID of the asset to be deleted.
   * @return A ResponseEntity containing a JSON response with a success message if the asset is
   *         deleted successfully, or an error message if an exception occurs.
   */
  @PostMapping("/deleteAsset/{id}")
  public ResponseEntity<?> deleteAsset(@PathVariable("id") int id) {
    try {
      String assetNameSql = "SELECT title FROM assets WHERE id = :assetid";
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("assetid", id);
      String logAssetName = jdbcTemplate.queryForObject(assetNameSql, params, String.class);

      String statement = "DELETE FROM assets WHERE id = :assetid";
      jdbcTemplate.update(statement, params);

      String logSql = "INSERT INTO audit_log (title, action, username, asset_or_type) "
          + "VALUES (:assetName, 'Deleted', '[...]', 'Asset')";
      MapSqlParameterSource logParams =
          new MapSqlParameterSource().addValue("assetName", logAssetName);
      jdbcTemplate.update(logSql, logParams);

      // Return a JSON response with the success message
      return ResponseEntity.ok().body("{\"message\": \"Asset deleted successfully!\"}");
    } catch (Exception e) {
      // Catch other exceptions and return a JSON response with a generic error message
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          "{\"error\": \"An error occurred whilst deleting the asset: " + e.getMessage() + "\"}");
    }
  }

  /**
   * Fetches attributes for the selected type, in their originally defined order, from the database.
   *
   * @param type The selected asset type for which attributes are to be fetched.
   * @return 2D array in the form [["Attribute Name", "Custom Datatype"], [...]] representing each
   *         attribute name and its custom datatype.
   */
  List<String[]> fetchAttributesForTypeFromDatabase(String type) {
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
    // System.out.println(Arrays.deepToString(attributesWithTypes.toArray()));
    return attributesWithTypes;
  }

  /**
   * Fetches the attributes for the asset with the specified ID from the database. Retrieves
   * additional attributes stored as JSON in the 'additional_attrs' column of the 'assets' table.
   *
   * @param id The ID of the asset for which attributes are to be fetched.
   * @return A list of string arrays containing attribute names and their corresponding values.
   * @throws JsonMappingException If there is an issue with mapping JSON data.
   * @throws JsonProcessingException If there is an issue with processing JSON data.
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
    // System.out.println(Arrays.deepToString(descriptionValue.toArray()));
    return descriptionValue;
  }

  /**
   * Retrieves the HTML page for managing users and fetches users from the database to populate a
   * drop-down menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return String the name of the HTML page for creating a type ("create-type").
   */
  @GetMapping("/manage-users.html")
  public String populateUsers(Model model) {
    List<Map<String, Object>> users =
        jdbcTemplate.queryForList("SELECT user_id, username, role FROM user2 " + "ORDER BY user_id ASC",
            Collections.emptyMap());
    model.addAttribute("users", users);
    return "manage-users";
  }

}
