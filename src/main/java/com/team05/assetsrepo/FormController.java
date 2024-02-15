package com.team05.assetsrepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsible for extracting and processing data entered into the asset creation form.
 */
@Controller
public class FormController {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private Set<String> langSet;

  /**
   * Injecting the JdbcTemplate registered in the Spring appliction context by the DBConfig class.
   */
  public FormController(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    setProgLangSet();
  }

  /**
   * Sets up a HashSet comprised of the valid options for the programming language field.
   */
  public void setProgLangSet() {
    langSet = new HashSet<String>();
    langSet.add("Java");
    langSet.add("JavaScript");
    langSet.add("C++");
    langSet.add("Other");
  }

  /**
   * This method can be used to add a new programming language to the set of possible selections.
   *
   * @param value the programming language to be added to the set.
   */
  public void addProgLangSet(String value) {
    langSet.add(value);
  }

  /**
   * The extractForm method handles POST requests to the "/submit" URL.
   * RequestParam annotations are used to extract necessary form parameters.
   *
   * @return a HTTP response indicating whether the asset data was successfully processed.
   * @throws InvalidSelection if an invalid programming language was detected.
   * @throws DbUpdateException if an error was encountered whilst attempting to update the database.
   * @throws NotUniqueException if the title and/or link is determined to be non-unique.
   */
  @PostMapping("/submit")
  /* N.B. Spring binds the parameters from the FormData JSON body (see script.js) to the method 
  parameters via @RequestParam. */
  public ResponseEntity<?> extractForm(@RequestParam String title, @RequestParam int lines,
      @RequestParam String link, @RequestParam String lang, @RequestParam String assoc,
      @RequestParam String date) throws InvalidSelection, DbUpdateException, NotUniqueException {

    try {
      /* 
       * We call the methods for title and link validation, programming language validation, and 
       * insertion into the database.
       * 
       * If all run without throwing any exceptions, the asset form data was successfully processed.
       * 
       * Otherwise, the relevant exception is caught, and the user is notified (on the front-end) 
       * accordingly.
      */
      validateTitleAndLink(title, link);
      validateLang(lang);
      insertAssetData(title, lines, link, lang, assoc, date);
      return ResponseEntity.ok().body("{\"message\": \"Asset created successfully!\"}");
    } catch (InvalidSelection | DbUpdateException | NotUniqueException e) {
      return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
    }

  }

  /**
   * Inserts a row representing the asset into the "std_assets" table in the database.
   *
   * @param title the asset's title.
   * @param lines the number of lines of the asset.
   * @param link the link to the asset.
   * @param lang the programming language the asset is written in.
   * @param assoc the asset's association(s).
   * @param date the asset's creation date.
   */
  public void insertAssetData(String title, int lines, String link, String lang, String assoc, 
      String date) throws DbUpdateException {

    String statement = "INSERT INTO std_assets (title, lines, link, lang, assoc, date) "
        + "VALUES (:title, :lines, :link, :lang, :assoc, :date)";

    try {

      // Date entered by the user is parsed so that it conforms to the dd/MM/yyyy format.
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
      java.util.Date dateObj = format.parse(date);
      // Convert date from (Java) Util to (Java) SQL object.
      java.sql.Date sqlDate = new java.sql.Date(dateObj.getTime());

      /*
        * Here, the database is updated using the jdbcTemplate object. This is done by
        * passing the previously written SQL statement along with the map of parameters
        * to the update method.
        *
        * The key corresponds to the named parameters written after "VALUES" in the SQL
        * statement. The value corresponds to the actual values that will replace those
        * named parameters in the SQL query.
        */
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("title", title)
          .addValue("lines", lines).addValue("link", link).addValue("lang", lang)
          .addValue("assoc", assoc).addValue("date", sqlDate);

      jdbcTemplate.update(statement, params);

    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof ParseException) {
        throw new DbUpdateException("Please ensure your date is in dd/MM/yyyy format.");
      }
      throw new DbUpdateException(e.getClass().getSimpleName()
      + " encountered whilst updating the database.");
    }

  }

  /**
   * Validates the title and link attributes of the form via SQL queries, thus preventing the
   * creation of assets with a pre-existing title and/or link.
   *
   * @param title the title of the asset.
   * @param link the link of the asset.
   */
  public void validateTitleAndLink(String title, String link)
      throws NotUniqueException {

    String uniqueTitleQuery = "SELECT DISTINCT COUNT(title) FROM std_assets WHERE title = :title";
    String uniqueLinkQuery = "SELECT DISTINCT COUNT(link) FROM std_assets WHERE link = :link";

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("title", title);

    int titleResult = (int) jdbcTemplate
        .queryForObject(uniqueTitleQuery, parameters, Integer.class);

    parameters.clear();
    parameters.put("link", link);

    int linkResult = (int) jdbcTemplate
        .queryForObject(uniqueLinkQuery, parameters, Integer.class);

    if (titleResult != 0) {
      throw new NotUniqueException("This title is not unique.");
    } else if (linkResult != 0) {
      throw new NotUniqueException("This link is not unique.");
    }

  }

  /**
   * Validates the form's programming language field, preventing the creation of invalid database
   * entries due to intentional client-side modifications.
   *
   * @param lang the programming language extracted from the form.
   * @throws InvalidSelection if the programming language is found to be something other than one
   *     of the dropdown options.
   */
  public void validateLang(String lang) throws InvalidSelection {
    if (!langSet.contains(lang)) {
      throw new InvalidSelection("This programming language is not valid.");
    }
  }

}