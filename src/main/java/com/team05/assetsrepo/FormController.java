package com.team05.assetsrepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller responsible for extracting and processing data entered into the asset creation form.
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
   * The extractForm method handles POST requests to the "/submit" URL. RequestParam annotations are
   * used to extract necessary form parameters / attributes.
   *
   * @return returns a HTTP "Created" response, indicating that a resource has been successfully
   *         created.
   * @throws InvalidSelection
   * @throws ParseException
   */

  @PostMapping("/submit")
  public ResponseEntity<String> submit(HttpServletRequest request,
      @RequestParam(required = false) String title, @RequestParam(required = false) String type,
      @RequestParam(required = false) String link, @RequestParam(required = false) String lang,
      @RequestParam(required = false) String assoc, @RequestParam(required = false) String date,
      @RequestParam(required = false, name = "attributesContainer") String[] attributes,
      @RequestParam(required = false,
          name = "changeTypeNameCheckbox") String changeTypeNameCheckbox,
      Model model) throws InvalidSelection, ParseException {

    // Check if the request is coming from create-type.html
    String referer = request.getHeader("referer");
    String updatedTypeName = "";
    if (referer != null && referer.endsWith("/create-type.html")) {

      if ("Other".equals(type)) {
        // Retrieve the custom type entered by the user
        type = request.getParameter("customType");
      } else {
        if (request.getParameter("changeTypeNameCheckbox") != null) {
          updatedTypeName = request.getParameter("editType");
        }
      }
      System.out.println(request.getParameter("editType"));
      // Process the form submission
      extractFormType(type, updatedTypeName, attributes, model);
      return ResponseEntity.ok("Type form submitted successfully!");
    } else {
      // If the request is not from create-type.html, call the extractForm method
      extractForm(title, type, link, lang, assoc, date, model);
      return ResponseEntity.ok().body("Asset form submitted successfully!");
    }
  }

  /**
   * Extracts form data for attribute types and inserts them into the database.
   *
   * @param type The type of the attribute.
   * @param attributes An array of attribute values associated with the type.
   * @param model The Spring MVC model for adding attributes.
   * @return The name of the view to render after processing the form data.
   * @throws InvalidSelection If an error occurs due to invalid selection or input.
   * @throws ParseException If an error occurs while parsing the SQL query.
   */
  public String extractFormType(@RequestParam String type, String updatedTypeName,
      @RequestParam String[] attributes, Model model) throws InvalidSelection, ParseException {

    insertAttributeType(type, updatedTypeName, attributes);

    return "submit";
  }

  /**
   * Extracts form data from request parameters and inserts asset data and asset type into the
   * database.
   *
   * @param title The title of the asset.
   * @param type The type of the asset.
   * @param link The link of the asset.
   * @param lang The language of the asset.
   * @param assoc The association of the asset.
   * @param date The date of the asset.
   * @param model The Spring MVC model for adding attributes.
   * @return The name of the view to render after processing the form data.
   * @throws InvalidSelection If an error occurs due to invalid selection or input.
   */
  public String extractForm(@RequestParam String title, @RequestParam String type,
      @RequestParam String link, @RequestParam String lang, @RequestParam String assoc,
      @RequestParam String date, Model model) throws InvalidSelection {

    String message = "temp";
    model.addAttribute("error", message);

    insertAssetData(title, link, lang, assoc, date);
    insertAssetType(title, type, date);

    return "submit";
  }

  /**
   * Inserts a new attribute type into the database or updates existing attributes for the given
   * type.
   *
   * @param type The type of the attribute.
   * @param attributes An array of attribute values associated with the type.
   * @throws ParseException If an error occurs while parsing the SQL query.
   */
  public void insertAttributeType(String type, String updatedTypeName, String[] attributes)
      throws ParseException {
    // Check if any records exist for the given type in the type_updated table
    String existingAttributesQuery = "SELECT attributes FROM type WHERE type = :type";
    List<String> existingAttributes = jdbcTemplate.queryForList(existingAttributesQuery,
        Collections.singletonMap("type", type), String.class);

    String statement;
    boolean updateAttribute = false;
    boolean updateTypeAttribute = false;
    MapSqlParameterSource params = null;

    // If existing attributes are found, append the new attributes to them
    if (existingAttributes != null && !existingAttributes.isEmpty()) {
      updateAttribute = true;
      if (!(updatedTypeName).equals("")) {
        updateAttribute = false;
        updateTypeAttribute = true;
      }
    }

    // Ensures that the primary key id is correctly numbered to be 1 more than
    // the previous highest.
    String maxIDQuery = "SELECT MAX(type_id) FROM type";
    Integer maxID = jdbcTemplate.queryForObject(maxIDQuery, Collections.emptyMap(), Integer.class);

    if (maxID == null) {
      maxID = 0; // if no records found, initialise maxID to 0
    }

    int insertID = maxID + 1;

    if (updateAttribute) {
      statement = "UPDATE type SET attributes = :attributes WHERE type = :type";
      System.out.println("a");
    } else if (updateTypeAttribute) {
      statement =
          "UPDATE type SET type = :updatedTypeName, attributes = :attributes WHERE type = :type";
      params = new MapSqlParameterSource().addValue("type_id", insertID)
          .addValue("updatedTypeName", updatedTypeName).addValue("attributes", attributes);
      System.out.println("b");
    } else {
      statement =
          "INSERT INTO type (type_id, type, attributes) " + "VALUES (:type_id, :type, :attributes)";
    }
    // Convert the attributes array to a comma-separated string representation

    if (!(updateTypeAttribute)) {
      params = new MapSqlParameterSource().addValue("type_id", insertID).addValue("type", type)
          .addValue("attributes", attributes);
    }

    jdbcTemplate.update(statement, params);
  }

  /**
   * Inserts a row into the "std_assets" table in the database representing the asset.
   *
   * @param title the asset's title.
   * @param link the link to the asset.
   * @param lang the programming language the asset is written in.
   * @param assoc the asset's association(s).
   * @param date the asset's creation date.
   */
  public void insertAssetData(String title, String link, String lang, String assoc, String date) {

    // Ensures that the primary key id is correctly numbered to be 1 more than
    // the previous highest.
    String maxIDQuery = "SELECT MAX(id) FROM std_assets";
    Integer maxID = jdbcTemplate.queryForObject(maxIDQuery, Collections.emptyMap(), Integer.class);

    if (maxID == null) {
      maxID = 0; // if no records found, initialise maxID to 0
    }

    int insertID = maxID + 1;

    String statement = "INSERT INTO std_assets (id, title, lines, link, lang, assoc, date) "
        + "VALUES (:id, :title, :lines, :link, :lang, :assoc, :date)";

    try {

      // Date entered by the user is parsed so that it conforms to the dd/MM/yy
      // format.
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
      java.util.Date dateObj = format.parse(date);
      // Convert date from (Java) Util to (Java) SQL object.
      java.sql.Date sqlDate = new java.sql.Date(dateObj.getTime());

      /*
       * Here, the database is updated using the jdbcTemplate object. This is done by passing the
       * previously written SQL statement along with the map of parameters to the update method.
       *
       * The key corresponds to the named parameters written after "VALUES" in the SQL statement.
       * The value corresponds to the actual values that will replace those named parameters in the
       * SQL query.
       */
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", insertID)
          .addValue("title", title).addValue("link", link).addValue("lang", lang)
          .addValue("assoc", assoc).addValue("date", sqlDate);

      jdbcTemplate.update(statement, params);

    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

  /**
   * 
   * @param title the associated asset's title.
   * @param type the asset's type.
   * @param date the asset's type inclusion/creation date.
   */
  public void insertAssetType(String title, String type, String date) {
    // Ensures that the primary key type_id is correctly numbered to be 1 more than
    // the previous highest.
    String maxIDQuery = "SELECT MAX(type_id) FROM type";
    Integer maxID = jdbcTemplate.queryForObject(maxIDQuery, Collections.emptyMap(), Integer.class);

    if (maxID == null) {
      maxID = 0; // if no records found, initialise maxID to 0
    }

    int insertID = maxID + 1;
    String statement =
        "INSERT INTO type (type_id, name, attributes, date) VALUES (:type_id, :name, :attributes, :date)";

    try {
      // Date entered by the user is parsed so that it conforms to the dd/MM/yy
      // format.
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
      java.util.Date dateObj = format.parse(date);
      // Convert date from (Java) Util to (Java) SQL object.
      java.sql.Date sqlDate = new java.sql.Date(dateObj.getTime());

      /*
       * Here, the database is updated using the jdbcTemplate object. This is done by passing the
       * previously written SQL statement along with the map of parameters to the update method.
       *
       * The key corresponds to the named parameters written after "VALUES" in the SQL statement.
       * The value corresponds to the actual values that will replace those named parameters in the
       * SQL query.
       */

      MapSqlParameterSource params = new MapSqlParameterSource().addValue("type_id", insertID)
          .addValue("name", type).addValue("attributes", title).addValue("date", sqlDate);

      jdbcTemplate.update(statement, params);

    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * Validates the title and link attributes of the form via SQL queries, thus preventing the
   * creation of assets with a pre-existing title and/or link.
   *
   * @param title the title of the asset.
   * @param link the link of the asset.
   */
  public void validateTitleAndLink(String title, String link) throws NotUniqueException {

    String uniqueTitleQuery = "SELECT DISTINCT COUNT(title) FROM std_assets WHERE title = :title";
    String uniqueLinkQuery = "SELECT DISTINCT COUNT(link) FROM std_assets WHERE link = :link";

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("title", title);

    int titleResult =
        (int) jdbcTemplate.queryForObject(uniqueTitleQuery, parameters, Integer.class);

    parameters.clear();
    parameters.put("link", link);

    int linkResult = (int) jdbcTemplate.queryForObject(uniqueLinkQuery, parameters, Integer.class);

    if (titleResult != 0) {
      throw new NotUniqueException("This title is not unique.");
    } else if (linkResult != 0) {
      throw new NotUniqueException("This link is not unique.");
    }
  }

  /**
   * Retrieves the HTML page for creating an asset.
   *
   * @return The name of the HTML page for creating an asset ("create-asset").
   */
  @GetMapping("/create-asset.html")
  public String showCreateAssetPage() {
    return "create-asset";
  }

  /**
   * Retrieves the HTML page for creating a type and fetches types from the database to populate a
   * dropdown menu.
   *
   * @param model The model to which types retrieved from the database will be added.
   * @return The name of the HTML page for creating a type ("create-type").
   */
  @GetMapping("/create-type.html")
  public String showTypeFromDB(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type FROM type",
        Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    return "create-type";
  }

  /**
   * Retrieves attributes associated with a given type from the database.
   *
   * @param type The type for which attributes are to be retrieved.
   * @return A ResponseEntity containing a list of attributes associated with the provided type.
   */
  @GetMapping("/attributes/{type}")
  public ResponseEntity<List<String>> getAttributesForType(@PathVariable String type) {
    // Fetch attributes dynamically for the selected type
    List<String> attributes = fetchAttributesForTypeFromDatabase(type);
    return ResponseEntity.ok(attributes);
  }

  /**
   * Fetches attributes for the selected type from the database.
   *
   * @param type The type for which attributes are to be fetched.
   * @return A list of attributes associated with the provided type.
   */
  private List<String> fetchAttributesForTypeFromDatabase(String type) {
    // Fetch attributes for the selected type from the database
    // Use appropriate SQL query to retrieve attributes based on the selected type
    String sql = "SELECT attributes FROM type WHERE type = :type";
    Map<String, Object> paramMap = Collections.singletonMap("type", type);
    List<String> attributes = jdbcTemplate.queryForList(sql, paramMap, String.class);
    // Assuming attributes are stored as a comma-separated string in the database
    // Split the string to get individual attributes
    if (!attributes.isEmpty()) {
      String[] attributeArray = attributes.get(0).split(","); // Assuming attributes are stored as
                                                              // comma-separated values
      return Arrays.asList(attributeArray);
    } else {
      return Collections.emptyList();
    }
  }
}
