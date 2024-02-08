package com.team05.assetsrepo;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
  public String extractForm(@RequestParam String title, @RequestParam int lines,
      @RequestParam String link, @RequestParam String lang, @RequestParam String assoc,
      @RequestParam String date, Model model) throws InvalidSelection {

    String message = validateTitleLink(title, link, lang, assoc);
    model.addAttribute("error", message);
    validateSelection(lang);

    insertAssetData(title, lines, link, lang, assoc, date);

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

    String statement = "INSERT INTO std_assets (title, lines, link, lang, assoc, date) "
        + "VALUES (:title, :lines, :link, :lang, :assoc, :date)";

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
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("title", title)
          .addValue("lines", lines).addValue("link", link).addValue("lang", lang)
          .addValue("assoc", assoc).addValue("date", sqlDate);

      jdbcTemplate.update(statement, params);

    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

  /* Validates the title and link attributes of the form, preventing duplicates */
  public String validateTitleLink(String title, String link, String lang, String assoc) {

    String UNIQUE_TITLE = "SELECT DISTINCT COUNT(title) FROM std_assets WHERE title = :title";
    String UNIQUE_LINK = "SELECT DISTINCT COUNT(link) FROM std_assets WHERE link = :link";

    try {
      Map<String, String> parameters = new HashMap();
      parameters.put("title", title);

      int titleResult = (int) jdbcTemplate.queryForObject(UNIQUE_TITLE, parameters, Integer.class);

      parameters.clear();
      parameters.put("link", link);

      int linkResult = (int) jdbcTemplate.queryForObject(UNIQUE_LINK, parameters, Integer.class);

      if (titleResult != 0) {
        throw new NotUnique("This title is not unique");
      } else if (linkResult != 0) {
        throw new NotUnique("This link is not unique");
      }

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



}
