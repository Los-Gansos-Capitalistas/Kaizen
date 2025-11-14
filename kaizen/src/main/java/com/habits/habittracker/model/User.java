package com.habits.habittracker.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nombre;

    private String email;

    // Relación con hábitos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habit> habitos = new ArrayList<>();

    // Relación con configuraciones de notificación
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationSettings> configuracionesNotificacion = new ArrayList<>();

    /**
     * Método auxiliar para mantener la consistencia bidireccional entre
     * User y NotificationSettings.
     */
    public void addNotificationSettings(NotificationSettings settings) {
        configuracionesNotificacion.add(settings);
        settings.setUsuario(this);
    }

    public void removeNotificationSettings(NotificationSettings settings) {
        configuracionesNotificacion.remove(settings);
        settings.setUsuario(null);
    }
}
