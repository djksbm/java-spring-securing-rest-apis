package io.jzheaux.springsecurity.resolutions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpMethod.GET;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class ResolutionsApplication extends WebSecurityConfigurerAdapter {

	@Autowired UserRepositoryJwtAuthenticationConverter jwtAuthenticationConverter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authz -> authz
						.mvcMatchers(GET, "/resolutions", "/resolution/**").hasAuthority("resolution:read")
						.anyRequest().hasAuthority("resolution:write"))
				.httpBasic(basic -> {})
				.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken())
				.cors(cors -> {});
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository users) {
		return new UserRepositoryUserDetailsService(users);
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:4000")
						.allowedMethods("HEAD")
						.allowedHeaders("Authorization");
			}
		};
	}

	@Bean
	public OpaqueTokenIntrospector introspector(
			UserRepository users, OAuth2ResourceServerProperties properties) {
		OpaqueTokenIntrospector introspector = new NimbusOpaqueTokenIntrospector(
				properties.getOpaquetoken().getIntrospectionUri(),
				properties.getOpaquetoken().getClientId(),
				properties.getOpaquetoken().getClientSecret());
		return new UserRepositoryOpaqueTokenIntrospector(introspector, users);
	}
	@Bean
	public WebClient.Builder web() {
		return WebClient.builder()
				.baseUrl("http://localhost:8081")
				.filter(new ServletBearerExchangeFilterFunction());
	}
	public static void main(String[] args) {
		SpringApplication.run(ResolutionsApplication.class, args);
	}

}