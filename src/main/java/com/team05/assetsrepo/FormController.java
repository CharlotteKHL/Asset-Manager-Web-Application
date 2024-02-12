package com.team05.assetsrepo;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsible for extracting and processing data entered into the asset creation form.
 */
@Controller
public class FormController {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private HashSet<String> programmingLanguagesSet;

  /**
   * Injecting the JdbcTemplate registered in the Spring appliction context by the DBConfig class.
   */
  public FormController(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    setProgLangSet();
  }

  /**
   * Setting up the hashset for the different possible programming languages.
   */
  public void setProgLangSet() {
    programmingLanguagesSet = new HashSet<String>();
    programmingLanguagesSet.add("Java");
    programmingLanguagesSet.add("JavaScript");
    programmingLanguagesSet.add("C++");
    programmingLanguagesSet.add("Other");
  }

  /**
   * Adds new programming languages as possible valid selections.
   * 
   * @param value - The programming language to be added to the set.
   */
  public void addProgLangSet(String value) {
    programmingLanguagesSet.add(value);
  }

  /**
   * The extractForm method handles POST requests to the "/submit" URL. RequestParam annotations are
   * used to extract necessary form parameters / attributes.
   *
   * @return returns a HTTP "Created" response, indicating that a resource has been successfully
   *         created.
   * @throws InvalidSelection
   */
  @PostMapping("/submit")
  public String extractForm(@RequestParam String title, @RequestParam String type,
      @RequestParam int lines, @RequestParam String link, @RequestParam String lang,
      @RequestParam String assoc, @RequestParam String date, Model model) throws InvalidSelection {

    String message = validateTitleTypeLink(title, type, link, lang, assoc);
    model.addAttribute("error", message);
    validateSelection(lang);

    insertAssetData(title, lines, link, lang, assoc, date);
    insertAssetType(title, type, date);

    return "submit";

  }

  /**
   * Inserts a row into the "std_assets" table in the database representing the asset.
   *
   * @param title the asset's title.
   * @param lines the number of lines of the asset.
   * @param link the link to the asset.
   * @param lang the programming language the asset is written in.
   * @param assoc the asset's association(s).
   * @param date the asset's creation date.
   */
  public void insertAssetData(String title, int lines, String link, String lang, String assoc,
      String date) {

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
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", insertID).addValue("title", title)
          .addValue("lines", lines).addValue("link", link).addValue("lang", lang)
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

  /* Validates the title, type, and link attributes of the form, preventing duplicates */
  public String validateTitleTypeLink(String title, String type, String link, String lang,
      String assoc) {

    String UNIQUE_TITLE = "SELECT DISTINCT COUNT(title) FROM std_assets WHERE title = :title";
    String UNIQUE_LINK = "SELECT DISTINCT COUNT(link) FROM std_assets WHERE link = :link";
    /*
     * String UNIQUE_TITLE_TYPE =
     * "SELECT DISTINCT COUNT(*) FROM type WHERE name = :type AND attributes = :title";
     */

    try {
      Map<String, String> parameters = new HashMap();
      parameters.put("title", title);

      int titleResult = (int) jdbcTemplate.queryForObject(UNIQUE_TITLE, parameters, Integer.class);

      parameters.clear();
      parameters.put("link", link);

      int linkResult = (int) jdbcTemplate.queryForObject(UNIQUE_LINK, parameters, Integer.class);

      /*
       * parameters.clear(); parameters.put("type", type); parameters.put("title", title);
       * 
       * int typeResult = (int) jdbcTemplate.queryForObject(UNIQUE_TITLE_TYPE, parameters,
       * Integer.class);
       * 
       */
      if (titleResult != 0) {
        throw new NotUnique("This title is not unique");
      } else if (linkResult != 0) {
        throw new NotUnique("This link is not unique");
      } /*
         * else if (typeResult != 0) { throw new
         * NotUnique("This combination of title and type is not unique"); }
         */

    } catch (NotUnique e) {
      e.printStackTrace();
      System.out.println("Error!");
      return e.getMessage();
    }
    return "Form submitted successfully";
  }

  /*
   * Validates the programming language selection attributes of the form, preventing unwanted
   * results
   */
  public void validateSelection(String lang) throws InvalidSelection {
    if (!programmingLanguagesSet.contains(lang)) {
      throw new InvalidSelection("This programming language is not valid.");
    }
  }
  
  @GetMapping("/create-asset.html")
  public String showCreateAssetPage() {
      return "create-asset";
  }
  
  @GetMapping("/create-type.html")
  public String showTypeFromDB(Model model) {
    List<String> types = jdbcTemplate.queryForList("SELECT DISTINCT type FROM type_updated", Collections.emptyMap(), String.class);
    model.addAttribute("types", types);
    System.out.println("Types from database: " + types);
    return "create-type";
  }

}
