package gm.zona_fit.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gm.zona_fit.domain.Entities.Membresia;

@Repository
public interface IMembresiaRepository extends JpaRepository<Membresia, Integer> {
}
