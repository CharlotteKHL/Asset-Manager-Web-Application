package com.team05.assetsrepo;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FormController {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public FormController(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> extractForm(@RequestParam String title, @RequestParam int lines, @RequestParam String link, @RequestParam String lang, 
    @RequestParam String assoc, @RequestParam String date) {

        insertAssetData(title, lines, link, lang, assoc, date);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    public void insertAssetData(String title, int lines, String link, String lang, String assoc, String date) {

        String statement = "INSERT INTO std_assets (title, lines, link, lang, assoc, date) VALUES (:title, :lines, :link, :lang, :assoc, :date)";

        try {

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
            java.util.Date dateObj = format.parse(date);
            java.sql.Date sqlDate = new java.sql.Date(dateObj.getTime());

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("title", title)
                    .addValue("lines", lines)
                    .addValue("link", link)
                    .addValue("lang", lang)
                    .addValue("assoc", assoc)
                    .addValue("date", sqlDate);

            jdbcTemplate.update(statement, params);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    
}