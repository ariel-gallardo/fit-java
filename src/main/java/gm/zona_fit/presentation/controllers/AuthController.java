package gm.zona_fit.presentation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gm.zona_fit.application.dto.AuthResponseDTO;
import gm.zona_fit.application.dto.UserLoginDTO;
import gm.zona_fit.application.dto.UserRegisterDTO;
import gm.zona_fit.application.services.IAuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody UserLoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/oauth/success")
    public ResponseEntity<AuthResponseDTO> oauthSuccess(OAuth2AuthenticationToken oauthToken) {
        var email = (String) oauthToken.getPrincipal().getAttributes().get("email");
        var oauthName = oauthToken.getPrincipal().getName();
        return ResponseEntity.ok(authService.oauthLogin(email, oauthName));
    }
}
