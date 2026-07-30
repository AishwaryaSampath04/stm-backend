package com.example.jwt_demo.security;

import com.example.jwt_demo.security.AuthTokenFilter;
import com.example.jwt_demo.security.AuthEntryPointJwt;
import com.example.jwt_demo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private AuthTokenFilter  authenticationJwtTokenFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    

     @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
             .cors() // ENABLE CORS!!
        .and()
        .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/api/test/all", "/api/todos/upload", "/api/todos/getLangType",
                "/api/todos/getStatuse",
                "/api/todos/languages",
                 "/api/stm/**",
                "/api/todos/writeJsonFile").permitAll()
                .anyRequest().authenticated();

        // Add JWT token filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}