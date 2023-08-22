package org.cbioportal.security.spring;

import org.cbioportal.security.spring.authentication.openID.PortalUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// TODO decide on removal of this security option
@Configuration
// add new chain after api-filter chain (at position -2), but before the default fallback chain 
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
@ConditionalOnProperty(value = "authenticate", havingValue = "openid")
public class OpenIDSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PortalUserDetailsService portalUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .antMatcher("/**")
            .csrf().disable()
            .sessionManagement().sessionFixation().none()
            .and()
            .openidLogin()
                .loginPage("/login.html")
                .failureUrl("/login.html?login_error=true")
                .authenticationUserDetailsService(portalUserDetailsService)
                .attributeExchange("https://www.google.com/.*")
                    .attribute("email").type("http://axschema.org/contact/email").required(true).and()
                    .attribute("firstname").type("http://axschema.org/namePerson/first").required(true).and()
                    .attribute("lastname").type("http://axschema.org/namePerson/last").required(true).and()
                .and()
                .attributeExchange(".*yahoo.com.*")
                    .attribute("email").type("http://axschema.org/contact/email").required(true).and()
                    .attribute("fullname").type("http://axschema.org/namePerson").required(true).and()
                .and()
                .attributeExchange(".*myopenid.com.*")
                    .attribute("email").type("http://schema.openid.net/contact/email").required(true).and()
                    .attribute("fullname").type("http://schema.openid.net/namePerson").required(true).and()
                .and()
                .permitAll();

    }

}
