package gm.zona_fit.application.dto;

import java.time.LocalDateTime;

public record ClientDTO(String nombre, String apellido, Integer membresia, LocalDateTime membresiaExpiraEn) {
    
}
