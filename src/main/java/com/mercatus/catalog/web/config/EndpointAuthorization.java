package com.mercatus.catalog.web.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class EndpointAuthorization {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String OPERATOR = "OPERATOR";
    private static final String ADMIN = "ADMIN";
    private static final String SERVICE = "SERVICE";

    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(GET, "/products/*/skus/*/price-history").hasRole(OPERATOR)
                .requestMatchers(GET, "/skus").hasAnyRole(CUSTOMER, OPERATOR, SERVICE)
                .requestMatchers(GET, "/categories", "/categories/*", "/products", "/products/*", "/skus/*")
                .hasAnyRole(CUSTOMER, OPERATOR)
                .requestMatchers(POST, "/categories", "/products", "/products/**").hasRole(ADMIN)
                .requestMatchers(PUT, "/categories/*", "/products/**").hasRole(ADMIN);
    }
}
