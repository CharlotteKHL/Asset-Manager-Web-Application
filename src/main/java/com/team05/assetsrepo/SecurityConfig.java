package com.team05.assetsrepo;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@ComponentScan(basePackages = {"com.team05.assetsrepo"})
@EnableWebSecurity
public class SecurityConfig {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  @Bean
  public CustomUserDetailsService customUserDetailsService() {
    return new CustomUserDetailsService(userRepository);
  }

  @Autowired
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Autowired
  @Bean
  public DaoAuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(customUserDetailsService());
    authProvider.setPasswordEncoder(encoder());
    return authProvider;
  }

  @Autowired
  @Bean
  public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .authenticationProvider(authProvider()).build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.formLogin(formLogin -> formLogin.loginPage("/login.html").defaultSuccessUrl("/", true)
        .usernameParameter("exampleInputEmail").passwordParameter("exampleInputPassword"));
    http.authorizeHttpRequests(
        request -> request.requestMatchers(new AntPathRequestMatcher("/")).hasRole("ADMIN"));
    http.authorizeHttpRequests(request -> request.requestMatchers("/*").permitAll())
        .csrf(AbstractHttpConfigurer::disable);
    http.authorizeHttpRequests(request -> request
        .requestMatchers("/checkSession/**", "/loginScript/**", "/registerScript/**", "/script/**")
        .permitAll());
    http.logout(logout -> logout.logoutSuccessUrl("/logout").deleteCookies("SESSION").permitAll());
    http.headers(header -> header.frameOptions(
        frameOptions -> frameOptions.disable().contentTypeOptions(cto -> cto.disable())));
    return http.build();
  }

  @Autowired
  @Bean
  public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername("hi2@gmail.com").password(passwordEncoder.encode("hi"))
        // .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user);
  }

}
