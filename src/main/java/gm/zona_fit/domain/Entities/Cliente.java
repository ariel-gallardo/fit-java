package gm.zona_fit.domain.Entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Cliente {
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Id
    private Integer id;
    private String nombre;
    private String apellido;

    @ManyToOne(optional = true)
    @JoinColumn(name = "membresia_id", nullable = true)
    private Membresia membresia;

    private LocalDateTime membresiaExpiraEn;

    public Cliente(Integer id, String nombre, String apellido, Membresia membresia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.membresia = membresia;
        this.membresiaExpiraEn = null;
    }

    public Integer id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String apellido() {
        return apellido;
    }

    public Integer membresia() {
        return membresia != null ? membresia.getId() : null;
    }
}
