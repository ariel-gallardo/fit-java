package gm.zona_fit.presentation.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import gm.zona_fit.application.dto.ClientDTO;
import gm.zona_fit.application.dto.ClienteMembresiaPatchDTO;
import gm.zona_fit.application.services.IClienteService;
import gm.zona_fit.domain.Entities.Cliente;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Cliente>> get(@RequestParam(required = false) Integer id) {
        if (id != null) {
            var cliente = clienteService.getById(id);
            return ResponseEntity.ok(List.of(cliente));
        }
        return ResponseEntity.ok(clienteService.getAll());
    }

    @GetMapping("/membresias-vencidas")
    public ResponseEntity<List<Cliente>> getMembresiasVencidas() {
        return ResponseEntity.ok(clienteService.getWithExpiredMembresia());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody ClientDTO clienteDTO,
            UriComponentsBuilder ucb) {

        var clientId = clienteService.create(clienteDTO);

        var location = ucb.path("/clientes")
                .queryParam("id", clientId)
                .build()
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(
            @PathVariable Integer id,
            @RequestBody ClientDTO clienteDTO) {
        clienteService.update(id, clienteDTO);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/membresia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patchMembresia(
            @PathVariable Integer id,
            @RequestBody(required = false) ClienteMembresiaPatchDTO patchDTO) {
        Integer membresiaId = patchDTO != null ? patchDTO.membresiaId() : null;
        var membresiaExpiraEn = patchDTO != null ? patchDTO.membresiaExpiraEn() : null;
        clienteService.patchMembresia(id, membresiaId, membresiaExpiraEn);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
