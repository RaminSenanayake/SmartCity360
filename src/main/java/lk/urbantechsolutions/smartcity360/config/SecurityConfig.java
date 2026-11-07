package lk.urbantechsolutions.smartcity360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.context.annotation.Configuration;
import lk.urbantechsolutions.smartcity360.security.PortalAuthenticationProvider;
import lk.urbantechsolutions.smartcity360.security.PortalAuthenticationDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain portalSecurity(HttpSecurity http, UserDetailsService users, PasswordEncoder encoder) throws Exception {
        PortalAuthenticationProvider provider = new PortalAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        var residentFailure = new SimpleUrlAuthenticationFailureHandler("/sign-in?error");
        var staffFailure = new SimpleUrlAuthenticationFailureHandler("/sign-in?error&portal=staff");
        // No parent provider: portal failures must not fall back to password-only authentication.
        return http.authenticationManager(new ProviderManager(provider))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/sign-in", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/staff/**").hasRole("STAFF")
                        .requestMatchers("/resident/**").hasRole("RESIDENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/sign-in").loginProcessingUrl("/sign-in")
                        .authenticationDetailsSource(PortalAuthenticationDetails::new)
                        .defaultSuccessUrl("/dashboard", true)
                        .failureHandler((request, response, exception) -> {
                            var failure = "staff".equals(request.getParameter("portal")) ? staffFailure : residentFailure;
                            failure.onAuthenticationFailure(request, response, exception);
                        }).permitAll())
                .logout(logout -> logout.logoutUrl("/sign-out").logoutSuccessUrl("/sign-in?logout"))
                .exceptionHandling(errors -> errors.accessDeniedPage("/access-denied"))
                .build();
    }
}
