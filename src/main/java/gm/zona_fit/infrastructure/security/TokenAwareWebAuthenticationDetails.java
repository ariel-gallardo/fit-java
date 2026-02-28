package gm.zona_fit.infrastructure.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public class TokenAwareWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String token;

    public TokenAwareWebAuthenticationDetails(HttpServletRequest request, String token) {
        super(request);
        this.token = token;
    }

    public String token() {
        return token;
    }
}