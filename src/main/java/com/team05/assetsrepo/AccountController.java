package com.team05.assetsrepo;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for login and register functionality.
 */
@Controller
public class AccountController {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  /** Constructor for AccountController object.
   *
   * @param jdbcTemplate is the template object that allows the use of named parameters in 
   *      JDBC queries.
   */
  
  public AccountController(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * The extractLogin method handles POST requests to the "/login" URL.
   * RequestParam annotations are used to extract necessary login
   * parameters/attributes from the html page.
   *
   * @return returns a HTTP "Logged in" response, indicating the user has logged in successfully.
   * @throws InvalidLogin if the username and password pair do not match a pair in the database.
   */
  
  @PostMapping("/login")
  public ResponseEntity<String> extractLogin(@RequestParam String username, 
      @RequestParam String password)
      throws InvalidLogin {
    
    String message = validateLoginDetails(username, password);
    return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
  }

  /**
   * Validates the username & password of the login, preventing duplicate 
   * usernames & incorrect passwords.
   *
   * @param username is the username the user has inputted, to be checked against the database.
   * @param password is the password the user has inputted, to be checked against the database.
   * 
   * @return a string indicating if the login was successful or not.
   * 
   */
  
  public String validateLoginDetails(String username, String password) {

    String UNIQUE_USERNAME = "SELECT DISTINCT COUNT(username) FROM user2 "
        + "WHERE username = :username";
    String CORRECT_PASSWORD = "SELECT DISTINCT COUNT(password) FROM user2 "
        + "WHERE username = :username AND password = :password";

    try {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("username", username);

      int usernameResult = (int) jdbcTemplate.queryForObject(UNIQUE_USERNAME, 
          parameters, Integer.class);

      System.out.println(usernameResult);

      parameters.put("password", password);

      int passwordResult = (int) jdbcTemplate.queryForObject(CORRECT_PASSWORD, 
          parameters, Integer.class);

      System.out.println(passwordResult);

      if (passwordResult == 0) {
        throw new InvalidLogin("This password is not correct");
      }
    } catch (InvalidLogin e) {
      e.printStackTrace();
      System.out.println("Error!");
      return e.getMessage();
    }
    return "Login successful";
  }

  /**
   * The register method handles POST requests to the "/register" URL.
   * Assigns a new user id (sequentially to current entries in the database)
   * to the new user and inserts the new information into the database.
   *
   * @param username the user name the user inputed, to be checked against the database.
   * @param password the password the user inputed, to be checked against the database.
   * 
   * @return a response entity including a string to indicate if 
   *         registration was successful or not in the form of a JSON message.
   */
  
  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestParam String username, 
      @RequestParam String password) {
    // prepared sql statements
    String count = "SELECT COUNT(user_id) FROM user2";
    String insert = "INSERT INTO user2 (user_id, username, password, role) "
        + "VALUES (:user_id, :username, :password, :role)";
    String uniqueName = "SELECT COUNT(user_id) FROM user2 WHERE username = :username";
    
    String message = "Registration successful";

    Map<String, String> parameters = new HashMap<String, String>();

    // query database for current number of users to calculate user_id for new user
    int idCount = (int) jdbcTemplate.queryForObject(count, parameters, Integer.class);

    parameters.put("username", username);

    // query database to check if username already exists in database
    int nameCount = (int) jdbcTemplate.queryForObject(uniqueName, parameters, Integer.class);
    
    if (nameCount == 0) { // if username is unique

      // if username unique add new user to the database
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", idCount + 1)
          .addValue("username", username).addValue("password", password).addValue("role", "admin");

      jdbcTemplate.update(insert, params);

    } else { // if username not unique notify user
        
      message = "Registration unsuccessful, this email may already have an account";
    }

    // sets the user's password to the hashed version
    setPassword(username, encoder().encode(getPassword(username)));

    return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
  }

  
  /**
   * A method to allow you to get a user's password.
   *
   * @param username is the username to search the database with.
   * @return a password, if found, from the database that corresponds to the username.
   */
  
  public String getPassword(@RequestParam String username) {
    String password = "SELECT DISTINCT password FROM user2 WHERE username = :username";

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("username", username);

    String passwordValue = jdbcTemplate.queryForObject(password, parameters, String.class);

    return passwordValue;
  }

  /**
   * Changes a users password.
   *
   * @param username is the username of the user that would like to change their password.
   * @param password is the new password the user would like to change to.
   */
  
  public void setPassword(@RequestParam String username, @RequestParam String password) {
    String setPassword = "UPDATE user2 SET password = :password WHERE username = :username";

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("username", username);
    parameters.put("password", password);

    jdbcTemplate.update(setPassword, parameters);
  }
}
