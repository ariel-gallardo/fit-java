package gm.zona_fit.application.services;

import java.util.List;

import gm.zona_fit.application.dto.UserDTO;
import gm.zona_fit.application.dto.UserRegisterDTO;

public interface IUsuarioService {
    UserDTO register(UserRegisterDTO dto);
    List<UserDTO> getAll();
    UserDTO getById(Integer id);
}
