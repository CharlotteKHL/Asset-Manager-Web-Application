package com.team05.assetsrepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ComponentScan(basePackages = { "com.team05.assetsrepo" })
@EnableWebSecurity
public class SecurityConfig {
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
		this.customUserDetailsService = customUserDetailsService;
	}

	@Autowired
	public PasswordEncoder encoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailsService).passwordEncoder(encoder());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.formLogin(form -> form.loginPage("/login").permitAll());
		return http.build();
	}

//	@Bean
//	public DaoAuthenticationProvider authProvider() {
//		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//		authProvider.setUserDetailsService(customUserDetailsService);
//		authProvider.setPasswordEncoder(encoder());
//		return authProvider;
//	}

//	@Bean
//	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
//		return http.getSharedObject(AuthenticationManagerBuilder.class).authenticationProvider(authProvider()).build();
//	}
}
