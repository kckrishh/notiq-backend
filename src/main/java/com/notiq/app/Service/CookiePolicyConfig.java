package com.notiq.app.Config.Security; // or wherever your config classes are

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class CookiePolicyConfig {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> sameSiteCookieFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {
                filterChain.doFilter(request, response);

                String header = response.getHeader("Set-Cookie");
                if (header != null && header.contains("JSESSIONID")) {
                    // ⚠️ Fix: Prevent duplicate SameSite
                    if (!header.contains("SameSite")) {
                        response.setHeader("Set-Cookie", header + "; SameSite=None; Secure");
                    }
                }
            }
        });

        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
