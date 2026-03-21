package com.mednex.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.mednex.tenant.TenantFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, TenantFilter tenantFilter) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health/**").permitAll()
				.requestMatchers("/actuator/info").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		http.addFilterAfter(tenantFilter, BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(JwtDecoder.class)
	public JwtDecoder jwtDecoder(SecurityProperties props) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(props.jwkSetUri()).build();
		var issuerValidator = JwtValidators.createDefaultWithIssuer(props.issuer());
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator));
		return decoder;
	}

	private static JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new RealmRolesConverter());
		return converter;
	}

	static class RealmRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
		@Override
		public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
			Object realmAccess = jwt.getClaims().get("realm_access");
			if (!(realmAccess instanceof Map<?, ?> map)) {
				return Collections.emptyList();
			}
			Object roles = map.get("roles");
			if (!(roles instanceof Collection<?> roleList)) {
				return Collections.emptyList();
			}
			return roleList.stream()
				.filter(r -> r != null)
				.map(String::valueOf)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
		}
	}
}
