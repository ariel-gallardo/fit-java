package gm.zona_fit.domain.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public record Cliente(
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Id Integer id,
    String nombre,
    String apellido,
    Integer membresia
) {
    
}
