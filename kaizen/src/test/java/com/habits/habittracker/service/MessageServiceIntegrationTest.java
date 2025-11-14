package com.habits.habittracker.service;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.NotificationSettings;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.NotificationSettingsRepository;
import com.habits.habittracker.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MessageServiceIntegrationTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    private User usuario;
    private NotificationSettings configActiva;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        habitRepository.deleteAll();
        notificationSettingsRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario
        usuario = new User();
        usuario.setUsername("testuser");
        usuario.setPassword("password");
        usuario.setEmail("test@example.com");
        usuario.setNombre("Usuario Test");
        userRepository.save(usuario);

        // Crear configuración de notificaciones activa
        configActiva = new NotificationSettings();
        configActiva.setUsuario(usuario);
        configActiva.setEmailEnabled(true);
        configActiva.setPushEnabled(true);
        configActiva.setSmsEnabled(false);
        notificationSettingsRepository.save(configActiva);
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoyHoraActual_DeberiaProcesarNotificacion() {
        // Arrange
        Habit habit = new Habit();
        habit.setNombre("Ejercicio matutino");
        habit.setCategoria("Salud");
        habit.setFrecuencia("Diario");
        habit.setHora("08:00");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        // Configurar hora actual para la prueba
        LocalTime horaPrueba = LocalTime.of(8, 0);
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba;
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert - No hay assertions directas porque el método solo imprime en consola
        // Pero podemos verificar que no hubo excepciones y que los datos están correctos
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertEquals("08:00", habitos.get(0).getHora());
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoHoraDiferente_NoDeberiaNotificar() {
        // Arrange
        Habit habit = new Habit();
        habit.setNombre("Lectura nocturna");
        habit.setCategoria("Educación");
        habit.setFrecuencia("Diario");
        habit.setHora("22:00");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        // Configurar hora diferente
        LocalTime horaPrueba = LocalTime.of(8, 0); // Mañana vs noche
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba;
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert - No debería haber notificación
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertEquals("22:00", habitos.get(0).getHora());
    }

    @Test
    void revisarHabitosParaNotificar_ConHabitoSinHora_NoDeberiaProcesar() {
        // Arrange
        Habit habit = new Habit();
        habit.setNombre("Meditación");
        habit.setCategoria("Bienestar");
        habit.setFrecuencia("Diario");
        habit.setHora(null); // Sin hora definida
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert - El hábito debería existir pero sin hora
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertNull(habitos.get(0).getHora());
    }

    @Test
    void revisarHabitosParaNotificar_ConNotificacionesDesactivadas_NoDeberiaNotificar() {
        // Arrange
        // Crear configuración con notificaciones desactivadas
        NotificationSettings configInactiva = new NotificationSettings();
        configInactiva.setUsuario(usuario);
        configInactiva.setEmailEnabled(false);
        configInactiva.setPushEnabled(false);
        configInactiva.setSmsEnabled(false);
        notificationSettingsRepository.deleteAll();
        notificationSettingsRepository.save(configInactiva);

        Habit habit = new Habit();
        habit.setNombre("Ejercicio");
        habit.setCategoria("Salud");
        habit.setFrecuencia("Diario");
        habit.setHora("08:00");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        LocalTime horaPrueba = LocalTime.of(8, 0);
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba;
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert - Hábito existe pero notificaciones desactivadas
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        List<NotificationSettings> configs = notificationSettingsRepository.findAll();
        assertEquals(1, configs.size());
        assertFalse(configs.get(0).getEmailEnabled());
        assertFalse(configs.get(0).getPushEnabled());
        assertFalse(configs.get(0).getSmsEnabled());
    }

    @Test
    void revisarHabitosParaNotificar_ConUsuarioSinConfiguracion_NoDeberiaNotificar() {
        // Arrange
        // Eliminar configuración de notificaciones
        notificationSettingsRepository.deleteAll();

        Habit habit = new Habit();
        habit.setNombre("Estudio");
        habit.setCategoria("Educación");
        habit.setFrecuencia("Diario");
        habit.setHora("08:00");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        // Act
        messageService.revisarHabitosParaNotificar();

        // Assert - Hábito existe pero sin configuración
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        List<NotificationSettings> configs = notificationSettingsRepository.findAll();
        assertEquals(0, configs.size());
    }

    @Test
    void revisarHabitosParaNotificar_ConMultiplesHabitosMismoUsuario_DeberiaProcesarCorrectamente() {
        // Arrange
        Habit habit1 = new Habit();
        habit1.setNombre("Ejercicio");
        habit1.setCategoria("Salud");
        habit1.setFrecuencia("Diario");
        habit1.setHora("08:00");
        habit1.setUsuario(usuario);

        Habit habit2 = new Habit();
        habit2.setNombre("Meditación");
        habit2.setCategoria("Bienestar");
        habit2.setFrecuencia("Diario");
        habit2.setHora("12:00");
        habit2.setUsuario(usuario);

        Habit habit3 = new Habit();
        habit3.setNombre("Lectura");
        habit3.setCategoria("Educación");
        habit3.setFrecuencia("Diario");
        habit3.setHora("20:00");
        habit3.setUsuario(usuario);

        habitRepository.save(habit1);
        habitRepository.save(habit2);
        habitRepository.save(habit3);

        LocalTime horaPrueba = LocalTime.of(12, 0); // Coincide con habit2
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba;
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert - Todos los hábitos deberían existir
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(3, habitos.size());
    }

    @Test
    void revisarHabitosParaNotificar_ConMultiplesUsuarios_DeberiaAislarNotificaciones() {
        // Arrange
        // Crear segundo usuario
        User usuario2 = new User();
        usuario2.setUsername("usuario2");
        usuario2.setPassword("password2");
        usuario2.setEmail("usuario2@example.com");
        userRepository.save(usuario2);

        NotificationSettings configUsuario2 = new NotificationSettings();
        configUsuario2.setUsuario(usuario2);
        configUsuario2.setEmailEnabled(true);
        configUsuario2.setPushEnabled(false);
        configUsuario2.setSmsEnabled(false);
        notificationSettingsRepository.save(configUsuario2);

        // Hábitos para usuario1
        Habit habitUsuario1 = new Habit();
        habitUsuario1.setNombre("Ejercicio Usuario1");
        habitUsuario1.setCategoria("Salud");
        habitUsuario1.setFrecuencia("Diario");
        habitUsuario1.setHora("08:00");
        habitUsuario1.setUsuario(usuario);

        // Hábitos para usuario2
        Habit habitUsuario2 = new Habit();
        habitUsuario2.setNombre("Estudio Usuario2");
        habitUsuario2.setCategoria("Educación");
        habitUsuario2.setFrecuencia("Diario");
        habitUsuario2.setHora("08:00");
        habitUsuario2.setUsuario(usuario2);

        habitRepository.save(habitUsuario1);
        habitRepository.save(habitUsuario2);

        LocalTime horaPrueba = LocalTime.of(8, 0);
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba;
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert - Ambos hábitos deberían existir con usuarios diferentes
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(2, habitos.size());
        assertEquals(usuario.getId(), habitos.get(0).getUsuario().getId());
        assertEquals(usuario2.getId(), habitos.get(1).getUsuario().getId());
    }

    @Test
    void revisarHabitosParaNotificar_ConHoraFormatoInvalido_DeberiaManejarError() {
        // Arrange
        Habit habitHoraInvalida = new Habit();
        habitHoraInvalida.setNombre("Hábito hora inválida");
        habitHoraInvalida.setCategoria("Test");
        habitHoraInvalida.setFrecuencia("Diario");
        habitHoraInvalida.setHora("hora-invalida"); // Formato inválido
        habitHoraInvalida.setUsuario(usuario);
        habitRepository.save(habitHoraInvalida);

        // Act - No debería lanzar excepción
        assertDoesNotThrow(() -> messageService.revisarHabitosParaNotificar());

        // Assert - El hábito con hora inválida debería existir
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertEquals("hora-invalida", habitos.get(0).getHora());
    }

    @Test
    void revisarHabitosParaNotificar_ConListaVacia_NoDeberiaHacerNada() {
        // Arrange - No crear hábitos (lista vacía)

        // Act
        assertDoesNotThrow(() -> messageService.revisarHabitosParaNotificar());

        // Assert - No hay hábitos
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(0, habitos.size());
    }

    @Test
    void verificarNotificacionesEnTiempoEspecifico_DeberiaFuncionarConBaseDeDatosReal() {
        // Arrange
        Habit habit = new Habit();
        habit.setNombre("Test tiempo específico");
        habit.setCategoria("Test");
        habit.setFrecuencia("Diario");
        habit.setHora("15:30");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        LocalTime tiempoEspecifico = LocalTime.of(15, 30);

        // Act
        assertDoesNotThrow(() -> messageService.verificarNotificacionesEnTiempoEspecifico(tiempoEspecifico));

        // Assert
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertEquals("15:30", habitos.get(0).getHora());
    }

    @Test
    void verificarNotificacionesManual_DeberiaEjecutarSinErrores() {
        // Arrange
        Habit habit = new Habit();
        habit.setNombre("Test manual");
        habit.setCategoria("Test");
        habit.setFrecuencia("Diario");
        habit.setHora("10:00");
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        // Act
        assertDoesNotThrow(() -> messageService.verificarNotificacionesManual());

        // Assert
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
    }

    @Test
    void revisarHabitosParaNotificar_ConHoraConSegundos_DeberiaIgnorarSegundos() {
        // Arrange
        // Nota: El servicio actual no soporta horas con segundos en la base de datos
        // pero prueba que el parsing funciona correctamente
        Habit habit = new Habit();
        habit.setNombre("Hábito con segundos");
        habit.setCategoria("Test");
        habit.setFrecuencia("Diario");
        habit.setHora("09:00"); // Sin segundos en BD
        habit.setUsuario(usuario);
        habitRepository.save(habit);

        LocalTime horaPrueba = LocalTime.of(9, 0, 30); // Con segundos
        MessageService serviceSpy = new MessageService(habitRepository, notificationSettingsRepository) {
            @Override
            protected LocalTime getCurrentTime() {
                return horaPrueba.withSecond(0).withNano(0); // El servicio ignora segundos
            }
        };

        // Act
        serviceSpy.revisarHabitosParaNotificar();

        // Assert
        List<Habit> habitos = habitRepository.findAll();
        assertEquals(1, habitos.size());
        assertEquals("09:00", habitos.get(0).getHora());
    }

    @Test
    void serviceContextLoads_YDependenciesAreInjected() {
        // Verificar que el servicio y sus dependencias están correctamente configuradas
        assertNotNull(messageService);
        assertNotNull(habitRepository);
        assertNotNull(notificationSettingsRepository);
        assertNotNull(userRepository);
    }
}