package com.habits.habittracker.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.habits.habittracker.dto.request.ProgressRequest;
import com.habits.habittracker.dto.response.DailyProgressResponse;
import com.habits.habittracker.exception.ResourceNotFoundException;
import com.habits.habittracker.model.Habit;
import com.habits.habittracker.model.Progress;
import com.habits.habittracker.model.User;
import com.habits.habittracker.repository.HabitRepository;
import com.habits.habittracker.repository.ProgressRepository;
import com.habits.habittracker.repository.UserRepository;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    // Crear o actualizar progreso de un hábito
    public Progress marcarProgreso(Long habitId, ProgressRequest request) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + habitId));

        Progress progress = progressRepository.findByHabitoAndFecha(habit, request.getFecha())
                .orElse(new Progress());

        progress.setHabito(habit);
        progress.setFecha(request.getFecha());
        progress.setCumplido(request.isCumplido());

        return progressRepository.save(progress);
    }

    // Listar progreso por hábito
    public List<Progress> obtenerProgresoPorHabito(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + habitId));
        return progressRepository.findByHabito(habit);
    }

    // Eliminar progreso
    public void eliminarProgreso(Long id) {
        if (!progressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Progreso no encontrado con id: " + id);
        }
        progressRepository.deleteById(id);
    }

    // Obtener progreso diario resumido
    public Map<String, Object> obtenerProgresoDiario(String username) {
        User usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Habit> habitos = habitRepository.findByUsuario(usuario);
        LocalDate hoy = LocalDate.now();

        long totalHabitos = habitos.size();
        long cumplidosHoy = habitos.stream()
                .flatMap(h -> progressRepository.findByHabito(h).stream())
                .filter(p -> p.getFecha().equals(hoy) && p.isCumplido())
                .count();

        double porcentaje = totalHabitos == 0 ? 0 : (cumplidosHoy * 100.0 / totalHabitos);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("fecha", hoy);
        resultado.put("totalHabitos", totalHabitos);
        resultado.put("habitosCumplidos", cumplidosHoy);
        resultado.put("porcentaje", porcentaje);
        return resultado;
    }

    // Progreso diario detallado (por día)
    public DailyProgressResponse obtenerProgresoDiarioDetallado(String username, LocalDate fecha) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Habit> habitos = habitRepository.findByUsuario(user);
        List<Progress> progresosHoy = progressRepository.findByHabitoInAndFecha(habitos, fecha);

        List<String> cumplidos = progresosHoy.stream()
                .filter(Progress::isCumplido)
                .map(p -> p.getHabito().getNombre())
                .toList();

        List<String> pendientes = habitos.stream()
                .filter(h -> progresosHoy.stream().noneMatch(p -> p.getHabito().equals(h) && p.isCumplido()))
                .map(Habit::getNombre)
                .toList();

        double porcentaje = habitos.isEmpty() ? 0 : ((double) cumplidos.size() / habitos.size()) * 100;

        return new DailyProgressResponse(fecha, cumplidos, pendientes, porcentaje);
    }

    // Estadísticas semanales
    public Map<String, Object> obtenerEstadisticasSemanales(User usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate startOfWeek = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Habit> habitos = habitRepository.findByUsuario(usuario);
        List<Progress> progresos = progressRepository.findByHabitoInAndFechaBetween(habitos, startOfWeek, endOfWeek);

        long totalDias = habitos.size() * 7;
        long cumplidos = progresos.stream().filter(Progress::isCumplido).count();
        double porcentaje = totalDias > 0 ? ((double) cumplidos / totalDias) * 100 : 0;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("semanaInicio", startOfWeek);
        estadisticas.put("semanaFin", endOfWeek);
        estadisticas.put("totalHabitos", habitos.size());
        estadisticas.put("habitosCumplidos", cumplidos);
        estadisticas.put("porcentajeCumplimiento", porcentaje);
        return estadisticas;
    }

    // Estadísticas mensuales
    public Map<String, Object> obtenerEstadisticasMensuales(User usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<Habit> habitos = habitRepository.findByUsuario(usuario);
        List<Progress> progresos = progressRepository.findByHabitoInAndFechaBetween(habitos, inicioMes, finMes);

        long habitosCumplidos = progresos.stream().filter(Progress::isCumplido).count();
        int totalHabitos = habitos.size() * hoy.lengthOfMonth();

        double porcentaje = totalHabitos == 0 ? 0.0 : (habitosCumplidos * 100.0 / totalHabitos);

        return Map.of(
                "mes", hoy.getMonth().toString(),
                "habitosCumplidos", habitosCumplidos,
                "totalHabitos", totalHabitos,
                "porcentajeCumplimiento", porcentaje
        );
    }
}
