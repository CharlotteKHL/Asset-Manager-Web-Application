package com.team05.assetsrepo;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Configuration class for the PostgreSQL (Supabase) database.
 *
 * <p>Uses the JdbcTemplate class which simplifies the use of JDBC - no need to create/close 
 * connections or create PreparedStatement objects.</p>
 */
@Configuration
public class DatabaseConfig {

  /**
   * A DataSource allows us to "ask" for connections to a SQL database. 
   * This is automatically set up using credentials taken from application.properties.
   */
  private final DataSource dataSource;

  /**
   * To use the DataSource class, we simply create an instance.
   *
   * @param dataSource the (final) DataSource instance.
   */
  public DatabaseConfig(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * This method is annotated with @Bean - indicating creation of a special object which is managed 
   * (creation, configuration, disposal) by Spring.
   *
   * <p>After starting the application, Spring identifies this class as a configuration class and 
   * automatically invokes this method.</p>
   *
   * <p>Creates a NamedParameterJdbcTemplate - this comes with a basic set of JDBC operations and 
   * allows the use of named parameters as opposed to typical "?" placeholders.</p>
   *
   * @return a NamedParameterJdbcTemplate instance configured to interact with the database via 
   *      the dataSource object.
   */
  @Bean
  public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(dataSource);
  }

}