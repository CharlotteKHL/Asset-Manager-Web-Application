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

import jakarta.servlet.http.HttpSession;

/**
 * Controller class for login and register functionality.
 */
@Controller
public class AccountController {
  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final String INVALID_LOGIN_MSG = "This username or password is not correct";
  private UserRepository userRepository;

  /**
   * Constructor for AccountController object.
   * 
   * @param jdbcTemplate is the template object that allows the use of named parameters in JDBC
   *        queries.
   */

  public AccountController(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Bean
  public CustomUserDetailsService service() {
    return new CustomUserDetailsService(userRepository);
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Logs out the user.
   *
   * @param session the user's session
   */
  @PostMapping("/logout")
  public String logout(HttpSession session) {
    String remove_session = "DELETE FROM sessions WHERE session_id = :id";
    String id = session.getId();
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("id", id);
    jdbcTemplate.update(remove_session, parameters);

    return "Logout success";
  }

  @PostMapping("/check")
  public ResponseEntity<String> checkSession(HttpSession session) {

    String find_username = "SELECT DISTINCT username FROM sessions WHERE session_id = :id";
    String delete_old = "DELETE FROM sessions WHERE username = :username AND NOT session_id = :id";
    String CHECK_CURR_LOGIN = "SELECT COUNT(session_id) FROM spring_session WHERE session_id = :id";
    String count_curr_login = "SELECT COUNT(username) FROM sessions WHERE session_id = :id";

    String id = session.getId();
    System.out.println("Webpage being accessed by session ID: " + id);
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("id", id);

    String username = "You are no longer logged in";

    int currLogin = jdbcTemplate.queryForObject(CHECK_CURR_LOGIN, parameters, Integer.class);
    int countCurrLogin = jdbcTemplate.queryForObject(count_curr_login, parameters, Integer.class);

    if (currLogin != 0 && countCurrLogin != 0) {
      username = jdbcTemplate.queryForObject(find_username, parameters, String.class);
    }

    if (username == null) {
      username = "You are no longer logged in";
    }
    parameters.put("username", username);
    jdbcTemplate.update(delete_old, parameters);

    if(username != "You are no longer logged in") {
      String[] usernameSplit = username.split("@");
      String finalUsername = usernameSplit[0];
      username = finalUsername;
    }
    return ResponseEntity.ok().body("{\"username\": \"" + username + "\"}");
  }

  /**
   * Allows you to get the role of a given user.
   *
   * @param username the username of the user you'd like to find the role for
   * @return the role of a given user.
   */
  public String getRole(String username) {
      Map<String, String> parameters = new HashMap<String, String>();
      String find_role = "SELECT DISTINCT role FROM user2 WHERE username = :username";
      parameters.put("username", username);
      String roleResult = jdbcTemplate.queryForObject(find_role, parameters, String.class);

      return roleResult;
  }

  @PostMapping("/adminCheck")
  public ResponseEntity<String> adminCheck(HttpSession session) {
      String count_role = "SELECT COUNT(role) FROM sessions WHERE session_id = :id";
      String id = session.getId();
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("id", id);
      int countRoleResult = jdbcTemplate.queryForObject(count_role, parameters, Integer.class);

      if(countRoleResult != 0) {
        String find_role = "SELECT DISTINCT role FROM sessions WHERE session_id = :id";
        String roleResult = jdbcTemplate.queryForObject(find_role, parameters, String.class);
        return ResponseEntity.ok().body("{\"adminCheckResult\": \"" + roleResult + "\"}");
      } else {
        return ResponseEntity.ok().body("{\"adminCheckResult\": \"" + "" + "\"}");
      }
  }

  /**
   * The extractLogin method handles POST requests to the "/login" URL. RequestParam annotations are
   * used to extract necessary login parameters/attributes from the html page.
   * 
   * @return returns a HTTP "Logged in" response, indicating the user has logged in successfully.
   * @throws InvalidLogin if the username and password pair do not match a pair in the database.
   */

  @PostMapping("/login")
  public ResponseEntity<String> extractLogin(@RequestParam String username,
      @RequestParam String password, HttpSession session) throws InvalidLogin {

    String role = getRole(username);
    String newSession =
        "INSERT INTO sessions (username, session_id, role) VALUES (:username, :session_id, :role)";
    String message = validateLoginDetails(username, password);
    if (message == "Login successful") {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("username", username);
      parameters.put("session_id", session.getId());
      parameters.put("role", role);
      jdbcTemplate.update(newSession, parameters);
    }

    return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
  }

  /**
   * Validates the username & password of the login, preventing duplicate usernames & incorrect
   * passwords.
   * 
   * @param username is the username the user has inputed, to be checked against the database.
   * @param password is the password the user has inputed, to be checked against the database.
   * 
   * @return a string indicating if the login was successful or not.
   * 
   */

  public String validateLoginDetails(String username, String password) {

    String UNIQUE_USERNAME =
        "SELECT DISTINCT COUNT(username) FROM user2 WHERE username = :username";

    try {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("username", username);

      int usernameResult =
          (int) jdbcTemplate.queryForObject(UNIQUE_USERNAME, parameters, Integer.class);

      // System.out.println(usernameResult);

      // checks username exists in database
      if (usernameResult == 1) {

        String passwordResult = getPassword(username);

        // checks if password in database for username matches input from user
        if (!encoder().matches(password, passwordResult)) {
          throw new InvalidLogin(INVALID_LOGIN_MSG);
        }

      } else {
        throw new InvalidLogin(INVALID_LOGIN_MSG);
      }

    } catch (InvalidLogin e) {
      System.err.println(e.getMessage());
      return e.getMessage();
    }
    return "Login successful";
  }

  /**
   * The register method handles POST requests to the "/register" URL. Assigns a new user id
   * (sequentially to current entries in the database) to the new user and inserts the new
   * information into the database.
   *
   * @param username the user name the user inputed, to be checked against the database.
   * @param password the password the user inputed, to be checked against the database.
   * 
   * @return a response entity including a string to indicate if registration was successful or not
   *         in the form of a JSON message.
   */

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestParam String username,
      @RequestParam String password) {
    // prepared sql statements
    String count = "SELECT MAX(user_id) FROM user2";
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

      // hash password
      password = encoder().encode(password);

      // if username unique add new user to the database
      MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", idCount + 1)
          .addValue("username", username).addValue("password", password).addValue("role", "USER");

      jdbcTemplate.update(insert, params);

    } else { // if username not unique notify user

      message = "Registration unsuccessful, this email may already have an account";
    }

    return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
  }


  /**
   * A method to allow you to get a user's password.
   * 
   * @param username is the username to search the database with.
   * 
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
