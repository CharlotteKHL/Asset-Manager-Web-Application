package com.team05.assetsrepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@ComponentScan(basePackages = {"com.team05.assetsrepo"})
@EnableWebSecurity
public class SecurityConfig {
  private final UserRepository userRepository;
  private final AuthenticationConfiguration authenticationConfiguration;

  public SecurityConfig(UserRepository userRepository, AuthenticationConfiguration authenticationConfiguration) {
    this.userRepository = userRepository;
    this.authenticationConfiguration = authenticationConfiguration;
  }

  @Bean
  public CustomUserDetailsService customUserDetailsService() {
    return new CustomUserDetailsService(userRepository);
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService());
    authProvider.setPasswordEncoder(encoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Configure authorisation rules for specific request matchers
        .authorizeHttpRequests(authorize -> authorize.requestMatchers("/create-type/**")
            .hasAuthority("ROLE_ADMIN").requestMatchers("/create-asset/**", "/manage-asset/**")
            .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN").requestMatchers("/search-asset/**", "/script/**")
            .hasAnyAuthority("ROLE_VIEWER", "ROLE_USER", "ROLE_ADMIN")
            // Public access to the registration, login page, and related resources
            .requestMatchers("/register.html", "/custom.css", "/loginScript.js",
                "/registerScript.js", "/login", "/register", "/index.html")
            .permitAll().anyRequest().authenticated() // All other requests require authentication
        ).formLogin(formLogin -> formLogin.loginPage("/login.html") // Specify the custom login page
            .defaultSuccessUrl("/index.html", true) // Redirect to this URL on successful login
            .permitAll() // Allow all users to view the login page
        ).sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        )
        .csrf(csrf -> csrf.disable()); // Disable CSRF protection as per your setup

    return http.build();
  }
}
