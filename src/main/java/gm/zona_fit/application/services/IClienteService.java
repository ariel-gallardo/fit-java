package gm.zona_fit.application.services;

import java.util.List;

import gm.zona_fit.application.dto.ClientDTO;
import gm.zona_fit.domain.Entities.Cliente;

public interface IClienteService {
    List<Cliente> getAll();
    List<Cliente> getWithExpiredMembresia();
    Cliente getById(Integer id);
    Integer create(ClientDTO cliente);
    void update(Integer id, ClientDTO cliente);
    void patchMembresia(Integer id, Integer membresiaId, java.time.LocalDateTime membresiaExpiraEn);
    void delete(Integer id);
}
