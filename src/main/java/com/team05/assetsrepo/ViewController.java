package com.team05.assetsrepo;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
public class ViewController {
    private final NamedParameterJdbcTemplate jdbcTemplate;

	public ViewController(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

    @PostMapping
    public ResponseEntity<String> getAssetFromDatabase(@RequestParam String asset){
        String databaseAsset = getAsset(asset);
        return ResponseEntity.ok().body(databaseAsset);
    }
    
    public String getAsset(String asset){
        String assetValues = null;

        try{
            String sqlQuery = "SELECT * FROM asset";

            Map<String, String> assetList = new HashMap<>();

            String assetInfo = jdbcTemplate.queryForObject(sqlQuery, assetList, String.class);

            System.out.println(assetInfo.toString());


        }catch(Exception e){
            System.out.println("Unable to retrieve from the database.");
        }

        return assetValues;
    }
}
