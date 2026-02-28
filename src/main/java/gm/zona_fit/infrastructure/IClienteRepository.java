package gm.zona_fit.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gm.zona_fit.domain.Entities.Cliente;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
	List<Cliente> findByMembresiaIsNotNullAndMembresiaExpiraEnIsNotNullAndMembresiaExpiraEnBefore(LocalDateTime fecha);
}
