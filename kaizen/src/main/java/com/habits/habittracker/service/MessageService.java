package com.habits.habittracker.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.NotificationSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para manejar notificaciones y recordatorios de hábitos.
 * Se ejecuta automáticamente cada minuto para verificar hábitos pendientes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final HabitRepository habitRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;

    /**
     * Se ejecuta cada minuto para revisar si hay hábitos que deben notificar al usuario.
     * Este método es automáticamente invocado por Spring Scheduling.
     */
    @Scheduled(fixedRate = 60000) // 60,000 ms = 1 minuto
    public void revisarHabitosParaNotificar() {
        log.debug("Iniciando verificación de hábitos para notificación...");
        
        List<Habit> habitos = habitRepository.findAll();
        LocalTime ahora = getCurrentTime();
        
        log.debug("Hora actual: {}, Total hábitos a verificar: {}", ahora, habitos.size());

        int notificacionesEnviadas = 0;
        int errores = 0;

        for (Habit habit : habitos) {
            try {
                if (!esHabitValidoParaNotificacion(habit)) {
                    continue;
                }

                Optional<NotificationSettings> configOpt = 
                        notificationSettingsRepository.findByUsuario_Id(habit.getUsuario().getId());

                if (configOpt.isEmpty()) {
                    log.debug("Usuario {} no tiene configuración de notificaciones", habit.getUsuario().getId());
                    continue;
                }

                NotificationSettings config = configOpt.get();

                if (debeNotificar(habit, config, ahora)) {
                    enviarNotificacion(habit);
                    notificacionesEnviadas++;
                }

            } catch (Exception e) {
                errores++;
                log.error("Error al procesar hábito '{}': {}", 
                         habit != null ? habit.getNombre() : "null", e.getMessage());
            }
        }

        log.info("Verificación completada. Notificaciones enviadas: {}, Errores: {}, Total hábitos: {}", 
                notificacionesEnviadas, errores, habitos.size());
    }

    /**
     * Verifica si un hábito es válido para procesar notificaciones.
     */
    private boolean esHabitValidoParaNotificacion(Habit habit) {
        if (habit == null) {
            log.debug("Hábito nulo, omitiendo...");
            return false;
        }

        if (habit.getUsuario() == null) {
            log.debug("Hábito '{}' sin usuario asignado, omitiendo...", habit.getNombre());
            return false;
        }

        if (habit.getHora() == null || habit.getHora().trim().isEmpty()) {
            log.debug("Hábito '{}' sin hora definida, omitiendo...", habit.getNombre());
            return false;
        }

        return true;
    }

    /**
     * Determina si se debe enviar notificación para un hábito específico.
     */
    private boolean debeNotificar(Habit habit, NotificationSettings config, LocalTime ahora) {
        try {
            // Verificar si las notificaciones están activas
            boolean notificacionesActivas = config.getEmailEnabled() || 
                                          config.getPushEnabled() || 
                                          config.getSmsEnabled();

            if (!notificacionesActivas) {
                log.debug("Notificaciones desactivadas para usuario {}", habit.getUsuario().getId());
                return false;
            }

            // Parsear y comparar la hora del hábito
            LocalTime horaHabit = parseHora(habit.getHora());
            if (horaHabit == null) {
                return false;
            }

            boolean debeNotificar = horaHabit.equals(ahora);
            
            if (debeNotificar) {
                log.debug("Hábito '{}' coincide con hora actual: {}", habit.getNombre(), ahora);
            }

            return debeNotificar;

        } catch (Exception e) {
            log.error("Error al verificar notificación para hábito '{}': {}", habit.getNombre(), e.getMessage());
            return false;
        }
    }

    /**
     * Parsea la hora del hábito, manejando posibles formatos.
     */
    private LocalTime parseHora(String hora) {
        try {
            // Intentar parsear como HH:mm
            return LocalTime.parse(hora);
        } catch (Exception e1) {
            try {
                // Intentar parsear como HH:mm:ss
                return LocalTime.parse(hora).withSecond(0).withNano(0);
            } catch (Exception e2) {
                log.error("Formato de hora inválido: '{}'", hora);
                return null;
            }
        }
    }

    /**
     * Envía la notificación para un hábito.
     * Actualmente imprime en consola, pero puede extenderse para enviar emails, push, SMS, etc.
     */
    private void enviarNotificacion(Habit habit) {
        String mensaje = String.format(
            "🔔 Recordatorio: ¡Hora de cumplir tu hábito '%s'! ⏰ (%s) - Categoría: %s",
            habit.getNombre(),
            habit.getHora(),
            habit.getCategoria() != null ? habit.getCategoria() : "Sin categoría"
        );

        // Log para seguimiento
        log.info("ENVIANDO NOTIFICACIÓN: {}", mensaje);
        
        // Simular envío de notificación (en producción aquí irían los servicios reales)
        if (true) { // Siempre true por ahora, pero se puede condicionar por tipo de notificación
            System.out.println(mensaje);
        }

        // Aquí se podrían agregar:
        // - Envío de email
        // - Notificación push
        // - Mensaje SMS
        // - Integración con servicios externos
    }

    /**
     * Método protegido para facilitar testing, puede ser sobreescrito en tests.
     * Retorna la hora actual sin segundos ni nanosegundos.
     */
    protected LocalTime getCurrentTime() {
        return LocalTime.now().withSecond(0).withNano(0);
    }

    /**
     * Método para forzar una verificación manual (útil para testing o endpoints administrativos).
     */
    public void verificarNotificacionesManual() {
        log.info("Ejecutando verificación manual de notificaciones...");
        revisarHabitosParaNotificar();
    }

    /**
     * Método para verificar notificaciones en un tiempo específico (útil para testing).
     */
    public void verificarNotificacionesEnTiempoEspecifico(LocalTime tiempoEspecifico) {
        log.info("Ejecutando verificación de notificaciones en tiempo específico: {}", tiempoEspecifico);
        
        List<Habit> habitos = habitRepository.findAll();
        int notificacionesEnviadas = 0;

        for (Habit habit : habitos) {
            try {
                if (!esHabitValidoParaNotificacion(habit)) {
                    continue;
                }

                Optional<NotificationSettings> configOpt = 
                        notificationSettingsRepository.findByUsuario_Id(habit.getUsuario().getId());

                if (configOpt.isEmpty()) {
                    continue;
                }

                NotificationSettings config = configOpt.get();

                if (debeNotificar(habit, config, tiempoEspecifico)) {
                    enviarNotificacion(habit);
                    notificacionesEnviadas++;
                }

            } catch (Exception e) {
                log.error("Error en verificación manual para hábito '{}': {}", 
                         habit != null ? habit.getNombre() : "null", e.getMessage());
            }
        }

        log.info("Verificación manual completada. Notificaciones enviadas: {}", notificacionesEnviadas);
    }
}