package com.team05.assetsrepo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public AccountController(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * The extractLogin method hands POST requests to the "/successful-login" URL.
	 * RequestParam annotations are used to extract necessary login
	 * parameters/attributes.
	 * 
	 * @return returns a HTTP "Logged in" response, indicating the user has logged
	 *         in successfully.
	 * @throws InvalidLogin
	 */
	@PostMapping("/login")
	public String extractLogin(@RequestParam(required = false, value="username") String username, @RequestParam(required = false, value="password") String password, Model model)
			throws InvalidLogin {
	    System.out.println("HI");
		String message = validateLoginDetails(username, password);
		model.addAttribute("error", message);

		return "successful-login";
	}

	/*
	 * Validates the username & password of the login, preventing duplicate
	 * usernames & incorrect passwords
	 */
	public String validateLoginDetails(String username, String password) {

		String UNIQUE_USERNAME = "SELECT DISTINCT COUNT(username) FROM user2 WHERE username = :username";
		String CORRECT_PASSWORD = "SELECT DISTINCT COUNT(password) FROM user2 WHERE username = :username AND password = :password";

		try {
			Map<String, String> parameters = new HashMap();
			parameters.put("username", username);

			int usernameResult = (int) jdbcTemplate.queryForObject(UNIQUE_USERNAME, parameters, Integer.class);

			System.out.println(usernameResult);

			parameters.put("password", password);

			int passwordResult = (int) jdbcTemplate.queryForObject(CORRECT_PASSWORD, parameters, Integer.class);

			System.out.println(passwordResult);

			if (passwordResult != 0) {
				throw new InvalidLogin("This password is not correct");
			}
		} catch (InvalidLogin e) {
			e.printStackTrace();
			System.out.println("Error!");
			return e.getMessage();
		}
		return "Logged in successfully!";
	}

	/**
	 * Assigns a new user id to the new user and inserts the new information into
	 * the database.
	 *
	 * @param username the user name the user inputed
	 * @param password the password the user inputed
	 * @return html page to tell user of successful registration and link to login
	 *         page
	 */
	@PostMapping("/register")
	public String register(@RequestParam String username, @RequestParam String password, Model model) {

		// Sets the user's password to the hashed version

	    System.out.println("hello");
		String count = "SELECT COUNT(user_id) FROM user2";
		String insert = "INSERT INTO user (user_id, username, password, role) "
				+ "VALUES (:user_id, :username, :password, :role)";
		String uniqueName = "SELECT COUNT(user_id) FROM user2 WHERE username = :username";
		String message = "Registration successul";

		Map<String, String> parameters = new HashMap();

		int idCount = (int) jdbcTemplate.queryForObject(count, parameters, Integer.class);

		parameters.put("username", username);

		int nameCount = (int) jdbcTemplate.queryForObject(uniqueName, parameters, Integer.class);

		if (nameCount != 0) {

			MapSqlParameterSource params = new MapSqlParameterSource().addValue("user_id", count + 1)
					.addValue("username", username).addValue("password", password).addValue("role", "admin");

			jdbcTemplate.update(insert, params);
			model.addAttribute("message", message);

		} else {
			message = "Registration unsuccessful, username must be unique";
			model.addAttribute("message", message);
		}

		return "successful-register";
	}

	/* A method to allow you to get a user's password */
	public String getPassword(@RequestParam String username) {
		String password = "SELECT DISTINCT password FROM user2 WHERE username = :username";

		Map<String, String> parameters = new HashMap();

		parameters.put("username", username);

		String passwordValue = jdbcTemplate.queryForObject(password, parameters, String.class);

		return passwordValue;
	}

	public void setPassword(@RequestParam String username, @RequestParam String password) {
		String setPassword = "UPDATE user2 SET password = :password WHERE username = :username";

		Map<String, String> parameters = new HashMap();

		parameters.put("username", username);
		parameters.put("password", password);

		jdbcTemplate.execute(setPassword, parameters, null);
	}
}
