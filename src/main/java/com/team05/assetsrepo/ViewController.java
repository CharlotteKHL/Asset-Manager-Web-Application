package com.team05.assetsrepo;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsible for representing the off canvas window for the view page.
 */
@Controller
public class ViewController {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
    * Injecting the JdbcTemplate registered in the Spring application context by the DBConfig class.
    */
    public ViewController(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
    * Navigates to the view page when using the navigation bar.
    */
    @GetMapping("/view.html")
    public String openViewPage() {
        return "view";
    }

    /**
     * Finds the particular asset within the table and sends the correct information to the front-end via a JSON object.
     *
     * @param asset the asset id of asset to be found within the asset database.
     * @return A JSON response containing the asset information from the table.
     */
    @PostMapping("/view")
    public ResponseEntity<Map<String,String>> getAssetFromDatabase(@RequestParam String asset) {
        Map<String,String> response = new HashMap<>();
        response.put("title", getAsset(asset));
        return ResponseEntity.ok().body(response);
    }

    /**
     * Selects the asset from the database.
     *
     * @param asset the asset id of asset to be found within the asset database.
     * @return String a containing the asset information directly from the table.
     */
    public String getAsset(String asset) {
        String assetValues = null;

        try {
            String sqlQuery = "SELECT title FROM assets WHERE id = " + asset;

            Map<String, String> assetList = new HashMap<>();

            assetValues = jdbcTemplate.queryForObject(sqlQuery, assetList, String.class).toString();

        } catch (Exception e) {
            System.out.println("Unable to retrieve from the database.");
            System.out.println(e);
        }

        return assetValues;
    }
}
