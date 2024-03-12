package com.team05.assetsrepo;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller responsible for creating assets, creating / updating asset types, and fetching 
 * types / attributes.
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
   * @return ResponseEntity containing a message indicating whether the creation of a new asset 
   *      type was successful.
   */
  @PostMapping("/createType")
  public ResponseEntity<?> createType(@RequestBody String pairs) {
    try {

      /* Uses the Jackson library to convert the JSON string from the POST request body into a 
      JSON object. */
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(pairs);

      // Extracts the name of the new asset type from the first pairing, then removes the pairing.
      String typeName = (jsonNode.get("type")).asText();
      ObjectNode object = (ObjectNode) jsonNode;
      object.remove("type");
      pairs = objectMapper.writeValueAsString(object);

      /* Iterates over the JSON object.
      Sets up two lists: one for the attribute names, one for the attribute values. 
      These serve the purpose of storing the order of attributes.
      These can then be displayed as required. */
      List<String> attrKeysList = new ArrayList<>();
      List<String> attrValuesList = new ArrayList<>();
      jsonNode.fields().forEachRemaining(entry -> {
        attrKeysList.add(entry.getKey());
        attrValuesList.add(entry.getValue().asText());
      });

      /* Converts both lists from ArrayList<String> to String[] to ensure they can be 
      stored in the database. */
      String[] attrKeys = attrKeysList.toArray(new String[0]);
      String[] attrValues = attrValuesList.toArray(new String[0]);

      // Creates a new row in the type table
      String sql = "INSERT INTO type (type_name, attributes, attr_keys, attr_backend_types) "
            + "VALUES (:typeName, CAST(:pairs AS JSON), :attrKeys, :attrValues)";

      // Defines what Java variable / object each parameter in the SQL statement corresponds to.
      MapSqlParameterSource params = new MapSqlParameterSource()
          .addValue("typeName", typeName)
          .addValue("pairs", pairs)
          .addValue("attrKeys", attrKeys)
          .addValue("attrValues", attrValues);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"New asset type created successfully!\"}");

    } catch (JsonProcessingException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" 
      + "Please check that the name of your asset type is unique!" + "\"}");
      
    }
  }
  
  /**
   * Updates an existing row in the type table, given a JSON string representing an asset type's 
   * new attributes.
   *
   * @param pairs the JSON string from the POST request body.
   * @return ResponseEntity containing a message indicating whether updating the asset type was 
   *      successful.
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

      MapSqlParameterSource params = new MapSqlParameterSource()
          .addValue("typeName", typeName)
          .addValue("pairs", pairs)
          .addValue("attrKeys", attrKeys)
          .addValue("attrValues", attrValues);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"Asset type updated successfully!\"}");

    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }
  
  /** 
   * Creates a new row in the asset table representing a new asset, given a JSON string representing
   * its attributes.
   *
   * @param obj the JSON string from the POST request body.
   * @return ResponseEntity containing a message indicating whether the creation of a new asset was 
   *      successful.
   */
  @PostMapping("/submitAsset")
  public ResponseEntity<?> submitAsset(@RequestBody String obj) {
    try {

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(obj);

      String title = (jsonNode.get("Title").asText());
      String typeAsStr = (jsonNode.get("Type").asText());

      String sqlGetTypeId = "SELECT id FROM type WHERE type_name = :typeAsStr";
      
      MapSqlParameterSource params = new MapSqlParameterSource()
          .addValue("typeAsStr", typeAsStr);

      /* Since the type of an asset is stored as integer in the database, which also acts as
      a foreign key, we execute the following operation to get the id from the type's name. */
      int type = jdbcTemplate.queryForObject(sqlGetTypeId, params, Integer.class);

      /* Extracts the array representing asset associations from the JSON object, 
      converts to Java List. */
      JsonNode associationsNode = jsonNode.get("Association(s)");
      List<String> associationsList = new ArrayList<>();
      for (JsonNode node : associationsNode) {
        associationsList.add(node.asText());
      }
      
      /* Iterates over the list of associations, executing the below SQL statement for each 
      associated asset to obtain its id. These are stored in the list associationIds. */
      List<Integer> associationIds = new ArrayList<>();
      for (String assetName : associationsList) {
        String sqlGetAssociationId = "SELECT id FROM assets WHERE title = :assetName";
        params = new MapSqlParameterSource().addValue("assetName", assetName);
        Integer assetId = jdbcTemplate.queryForObject(sqlGetAssociationId, params, Integer.class);
        associationIds.add(assetId);
      }

      /* Converts from ArrayList<Integer> to Integer[] to ensure associations can be stored 
      in the database. */
      Integer[] associations = associationIds.toArray(new Integer[0]);

      /* Ensures we pass a JSON string which only contains the type-specific 
      attributes - this is for the additional_attrs column. */
      ObjectNode object = (ObjectNode) jsonNode;
      object.remove(jsonNode.fieldNames().next());
      object.remove(jsonNode.fieldNames().next());
      object.remove(jsonNode.fieldNames().next());
      /* Converts from JSON object --> JSON string to ensure it can be successfully stored in the 
      database. */
      obj = objectMapper.writeValueAsString(object);

      String sql = "INSERT INTO assets (title, type, associations, additional_attrs) "
          + "VALUES (:title, :type, :associations, CAST(:obj AS JSONB))";

      params = new MapSqlParameterSource()
          .addValue("title", title)
          .addValue("type", type)
          .addValue("associations", associations)
          .addValue("obj", obj);

      jdbcTemplate.update(sql, params);

      return ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}");

    } catch (JsonProcessingException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");

    } catch (DuplicateKeyException e) {

      System.err.println(e.getMessage());
      return ResponseEntity.badRequest().body("{\"error\": \"" 
      + "Please check that the name of your asset is unique!" + "\"}");

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
   * @return String the name of the HTML page for creating a type ("create-type").
   */
  @GetMapping("/create-type.html")
  public String populateTypesCreateType(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type_name FROM type",
        Collections.emptyMap(), String.class);
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
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type_name FROM type",
        Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    return "search-asset";
  }

  /**
   * Queries the database for matching assets based on the keyword entered / filters selected 
   * by the user.
   *
   * @param searchData a JSON string containing the keyword entered, followed by the values of 
   *      each selection from the filter.
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

      /* Default SQL statement which obtains the name of the asset which is similar to the keyword 
      entered. */
      String sql = "SELECT title FROM assets WHERE title ILIKE :assetToQuery";

      // The "assetToQuery" parameter stores the keyword.
      MapSqlParameterSource params = new MapSqlParameterSource()
          .addValue("assetToQuery", "%" + assetToQuery + "%");

      // Checks whether a type was selected via the "Type" filter.
      if (searchDataArr[1] != "") {
        /* Then, we check whether both a start date and an end date were specified in the 
        "Date Range" filter. */
        if (searchDataArr[2] != "" && searchDataArr[3] != "") {
          /* Builds upon the default SQL statement to also consider the chosen type, start date, 
          and end date. */
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
      /* If only the dates were specified, then only the dates are considered as an 
      additional filter. */
      } else {
        if (searchDataArr[2] != "" && searchDataArr[3] != "") {
          sql = "SELECT title FROM assets WHERE title ILIKE :assetToQuery AND assets.last_updated "
            + "BETWEEN CAST(:dateFrom AS TIMESTAMP) AND CAST(:dateTo AS TIMESTAMP)";
          params.addValue("dateFrom", searchDataArr[2]);
          params.addValue("dateTo", searchDataArr[3]);
        }
      }

      /* Checks whether the user selected a choice in the "Sort by" filter section, adding to the
      SQL statement accordingly. */
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

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("assetName", assetName);

    // Map used to store the result.
    Map<String, Object> assetData = jdbcTemplate.queryForMap(sql, params);

    // Removes the entry relating to the id, as this is not required.
    assetData.remove("id");
    // Removes the entry containing the additional attributes of the asset.
    // This entry is stored in additionalAttrs.
    Object additionalAttrs = assetData.remove("additional_attrs");

    /* Since the type of the asset is stored as the type's id as opposed 
    to the name, we retrieve the name from the id itself. */
    Object typeId = assetData.get("type");

    sql = "SELECT type_name FROM type WHERE id = :typeId";
      
    params = new MapSqlParameterSource()
        .addValue("typeId", typeId);

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
      /* Extracts only the additional attributes of the asset via the field name "value", 
      hence ignoring any other metadata. */
      String additionalAttrsStr = additionalAttrsNode.get("value").asText();
      // Converts the previous JSON string back into a JSON object.
      JsonNode additionalAttrsData = mapper.readTree(additionalAttrsStr);

      /* Sets up a LinkedHashMap so that the additional attributes of the asset appear 
      in the order defined within the asset's type. */
      Map<String, Object> additionalAttrsOrdered = new LinkedHashMap<>();
      
      /* Obtains 2D array in the form [["Attribute Name", "Custom Datatype"], [...]]
      representing each attribute name and its custom datatype in their originally 
      defined order via the fetchAttributesForTypeFromDatabase method. */
      List<String[]> attrKeysAndValues = fetchAttributesForTypeFromDatabase(type);
      /* Since the entries of attrKeysAndValues are in their originally defined order, 
      we can display the asset's attributes in the order they appear when viewing the 
      attributes of its type. */
      for (String[] pair : attrKeysAndValues) {
        /* Adds a new entry (key-value pair) to the LinkedHashMap in the form 
        "Attribute": Value, where the value is obtained from the 
        original (unordered) JSON containing the additional attributes. */
        additionalAttrsOrdered.put(pair[0], additionalAttrsData.get(pair[0]));
      }

      // Converts the LinkedHashMap to JSON.
      JsonNode additionalAttrsDataOrdered = mapper.convertValue(
          additionalAttrsOrdered, JsonNode.class);

      /* Then, convert to a TextNode to ensure we can accordingly place 
      the additional attributes JSON within our overall JSON. */
      TextNode additionalAttrsTextNode = new TextNode(
          mapper.writeValueAsString(additionalAttrsDataOrdered));

      assetDataJson.set("additional_attrs", additionalAttrsTextNode);

      // Convert to JSON string.
      String assetDataJsonStr = mapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(assetDataJson);

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
    System.out.println(attributes);
    return ResponseEntity.ok(attributes);
  }

  /**
   * Fetches attributes for the selected type, in their originally defined order, from the database.
   *
   * @param type The selected asset type for which attributes are to be fetched.
   * @return 2D array in the form [["Attribute Name", "Custom Datatype"], [...]] representing each 
   *      attribute name and its custom datatype.
   */
  private List<String[]> fetchAttributesForTypeFromDatabase(String type) {
    String sql = "SELECT attr_keys, attr_backend_types FROM type WHERE type_name = :type";

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("type", type);

    String[] keysAndTypes = jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
      String keys = rs.getString("attr_keys");
      String types = rs.getString("attr_backend_types");
      return new String[] { keys, types };
    });

    String keys = keysAndTypes[0].replaceAll("[{}\"]", "");
    String types = keysAndTypes[1].replaceAll("[{}\"]", "");
    String[] parsedKeys = keys.split(",");
    String[] parsedTypes = types.split(",");

    List<String[]> attributesWithTypes = new ArrayList<>();
    for (int i = 0; i < parsedKeys.length; i++) {
      String key = parsedKeys[i].trim();
      String backendType = parsedTypes[i].trim();
      attributesWithTypes.add(new String[] { key, backendType });
    }
    // System.out.println(Arrays.deepToString(attributesWithTypes.toArray()));
    return attributesWithTypes;
  }

}