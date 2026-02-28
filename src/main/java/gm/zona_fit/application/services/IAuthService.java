package gm.zona_fit.application.services;

import gm.zona_fit.application.dto.AuthResponseDTO;
import gm.zona_fit.application.dto.UserLoginDTO;
import gm.zona_fit.application.dto.UserRegisterDTO;

public interface IAuthService {
    AuthResponseDTO register(UserRegisterDTO dto);
    AuthResponseDTO login(UserLoginDTO dto);
    AuthResponseDTO oauthLogin(String email, String oauthName);
}
