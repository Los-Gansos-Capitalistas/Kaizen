# Kaizen
# Manual de Instalación - HabitTracker

##  Guía Completa de Instalación y Uso

### **Requisitos del Sistema**

- **Java 17** o superior
- **Maven 3.6** o superior
- **Docker desktop**
- **Navegador web moderno** (Chrome, Firefox, Safari, Edge)
- **Conexión a internet** (para dependencias)

---

##  Instalación Paso a Paso

### **1. Descargar el Proyecto**

```bash
# Clonar o descargar el proyecto
# Extraer el archivo ZIP en la carpeta deseada
cd habittracker
```

### **2. Verificar Instalaciones Previas**

```bash
# Verificar Java
java -version
# Debe mostrar: java version "17" o superior

# Verificar Maven
mvn -version
# Debe mostrar: Apache Maven 3.6+ 
```

### **3. Compilar y Ejecutar la Aplicación**

#### **Ejecutar Docker**

```bash
# Compilar el proyecto
 docker-compose up --build
```

### **4. Acceder a la Aplicación**

Una vez ejecutada, abre tu navegador y ve a:
```
http://localhost:8080/auth.html
```

---

##  Primeros Pasos

### **1. Registro de Usuario**
- Haz clic en "Registrarse" en la página de inicio
- Completa:
  - **Nombre de usuario**
  - **Correo electrónico**
  - **Contraseña**
- ¡Tu cuenta se creará automáticamente!

### **2. Configurar tu Perfil**
- Haz clic en el botón **⚙️** (configuración) en la esquina superior derecha
- **Selecciona tu avatar** entre las opciones disponibles:
  -  Perro
  -  Gato  
  -  Ardilla
  -  Erizo
  -  Pez
  -  Nutria
  -  Rata
  -  Oso
- **Personaliza tu nombre de usuario**
- **Guarda los cambios**

### **3. Crear tu Primer Hábito**

En la sección " Mis Hábitos", completa el formulario:

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| **Nombre*** | Nombre del hábito | "Hacer ejercicio" |
| **Categoría** | Tipo de hábito | "Salud" |
| **Frecuencia*** | Cada cuánto repetirlo | Diaria, Semanal, Mensual |
| **Fecha objetivo** | Fecha límite (opcional) | 2024-12-31 |
| **Hora** | Recordatorio (opcional) | 08:00 |
| **Descripción** | Detalles adicionales | "30 minutos de cardio" |

*\* Campos obligatorios*

### **4. Gestionar tus Hábitos**

- ** Marcar como completado**: Haz clic en el checkbox
- ** Eliminar hábito**: Botón de papelera
- ** Ver en calendario**: Los hábitos aparecen automáticamente
- ** Ver estadísticas**: Progreso en tiempo real

---

##  Funcionalidades del Calendario

### **Vistas Disponibles**
- **Vista mensual**: Overview completo
- **Vista semanal**: Detalle por semana
- **Vista diaria**: Actividades del día

### **Colores y Símbolos**
- ** Azul**: Hábitos pendientes
- ** Verde**: Hábitos completados
- ****: Hábito programado
- ****: Hábito completado

### **Interacción**
- **Hover**: Ver detalles del hábito
- **Clic en fecha**: Cambiar a vista diaria
- **Navegación**: Flechas para cambiar mes/semana

---

##  Sistema de Estadísticas

### **Métricas en Tiempo Real**
- **Total de hábitos**: Número total registrados
- **Completados hoy**: Progreso diario
- **Con recordatorio**: Hábitos programados
- **Fecha próxima**: Hábitos con fecha límite cercana

### **Progreso Visual**
- **Barra de progreso**: Porcentaje de completados vs total
- **Racha promedio**: Días consecutivos de actividad
- **Consejos personalizados**: Sugerencias para mejorar

---

##  Configuración y Personalización

### **Preferencias de Usuario**
- **Cambiar avatar** en cualquier momento
- **Actualizar nombre de usuario**
- **Modificar contraseña**
- **Configurar preferencias de notificaciones**

### **Gestión de Hábitos**
- **Editar frecuencia** según tus necesidades
- **Ajustar recordatorios** por hora específica
- **Establecer fechas objetivo** para metas a largo plazo

---

##  Sistema de Notificaciones

### **Recordatorios Automáticos**
- **Notificaciones push** del navegador
- **Recordatorios por hora** configurada
- **Alertas de hábitos pendientes**

### **Permisos Requeridos**
La primera vez que uses recordatorios, el navegador pedirá permiso para mostrar notificaciones.

---

##  Solución de Problemas Comunes

### **La aplicación no inicia**
```bash
# Verificar que el puerto 8080 esté libre
netstat -an | findstr 8080  # Windows
lsof -i :8080               # Linux/Mac

# Si está ocupado, cambiar puerto:
./mvnw spring-boot:run -Dserver.port=8081
```

### **Problemas de base de datos**
- La aplicación usa H2 (base de datos en memoria)
- Los datos se reinician al reiniciar la aplicación
- En producción, configurar base de datos persistente

### **El calendario no se muestra**
- Verificar conexión a internet (para cargar FullCalendar)
- Revisar la consola del navegador (F12) por errores
- Recargar la página (Ctrl+F5)

### **Problemas de estilos CSS**
- Limpiar cache del navegador
- Verificar que todos los archivos CSS se carguen
- Revisar la ruta de los avatares en `/images/avatar/`

---

##  Consejos de Uso

### **Para Mejor Experiencia**
1. **Comienza con pocos hábitos** (3-5 máximo)
2. **Establece recordatorios realistas**
3. **Revisa el calendario regularmente**
4. **Celebra tus progresos** en las estadísticas

### **Mejores Prácticas**
- **Hábitos específicos**: "Leer 20 minutos" vs "Leer más"
- **Frecuencia realista**: Comienza con diario/semanal
- **Recordatorios estratégicos**: Horas que realmente uses

---

##  Seguridad y Datos

### **Autenticación**
- **Tokens JWT** para sesiones seguras
- **Contraseñas encriptadas**
- **Logout automático** al cerrar navegador

### **Almacenamiento Local**
- **Datos de sesión** en localStorage del navegador
- **Configuración de usuario** persistente
- **Los hábitos** se guardan en base de datos


### **Logs y Diagnóstico**
```bash
# Ver logs de la aplicación
tail -f logs/application.log

# Modo debug
./mvnw spring-boot:run --debug
```

---

¡Listo para Usar!

¡Felicidades! Tu HabitTracker está listo para ayudarte a construir mejores hábitos. 
