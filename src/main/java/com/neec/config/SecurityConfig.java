package com.neec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	private static final String[] PUBLIC_ENPOINTS = {
		"/api/v1/auth/register",
		"/api/v1/auth/login",
		"/swagger-ui/**",	// http://localhost:9051/swagger-ui/index.html
		"/v3/api-docs/**"
			/*,
		"/swagger-ui.html",
		*/
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ENPOINTS).permitAll()
					.anyRequest().authenticated()).build();
	}
}
