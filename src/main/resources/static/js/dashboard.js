// Gestor de datos por usuario
class GestorUsuario {
    constructor() {
        this.usuarioActual = this.obtenerUsuarioActual();
        this.habitos = [];
    }

    obtenerUsuarioActual() {
        // Intentar obtener el usuario actual de localStorage
        const usuarioId = localStorage.getItem('usuarioActualId');
        
        if (!usuarioId) {
            // Si no hay usuario, redirigir al login
            this.redirigirALogin();
            return null;
        }
        
        // Cargar usuario existente
        const usuarioGuardado = localStorage.getItem(`usuario_${usuarioId}`);
        if (usuarioGuardado) {
            return JSON.parse(usuarioGuardado);
        } else {
            // Si el usuario guardado no existe, redirigir al login
            this.redirigirALogin();
            return null;
        }
    }

    redirigirALogin() {
        window.location.href = 'auth.html';
    }

    guardarUsuarioActual() {
        localStorage.setItem(`usuario_${this.usuarioActual.id}`, JSON.stringify(this.usuarioActual));
    }

    obtenerClaveHabitos() {
        return `habitos_${this.usuarioActual.id}`;
    }

    async cargarHabitos() {
        try {
            const clave = this.obtenerClaveHabitos();
            const habitosGuardados = localStorage.getItem(clave);
            
            if (habitosGuardados) {
                this.habitos = JSON.parse(habitosGuardados);
            } else {
                // Usuario empieza con hábitos vacíos
                this.habitos = [];
                await this.guardarHabitos();
            }
            
            console.log(`Hábitos cargados para usuario ${this.usuarioActual.username}:`, this.habitos);
            return this.habitos;
            
        } catch (error) {
            console.error('Error al cargar hábitos:', error);
            throw error;
        }
    }

    async guardarHabitos() {
        const clave = this.obtenerClaveHabitos();
        localStorage.setItem(clave, JSON.stringify(this.habitos));
    }

    async agregarHabito(habito) {
        const nuevoHabito = {
            ...habito,
            id: Date.now(),
            usuarioId: this.usuarioActual.id,
            fechaCreacion: new Date().toISOString(),
            completado: false
        };
        
        this.habitos.push(nuevoHabito);
        await this.guardarHabitos();
        return nuevoHabito;
    }

    async actualizarHabito(id, cambios) {
        const indice = this.habitos.findIndex(h => h.id === id && h.usuarioId === this.usuarioActual.id);
        if (indice !== -1) {
            this.habitos[indice] = { ...this.habitos[indice], ...cambios };
            await this.guardarHabitos();
            return this.habitos[indice];
        }
        return null;
    }

    async eliminarHabito(id) {
        this.habitos = this.habitos.filter(h => !(h.id === id && h.usuarioId === this.usuarioActual.id));
        await this.guardarHabitos();
    }

    async actualizarUsuario(nuevosDatos) {
        this.usuarioActual = { ...this.usuarioActual, ...nuevosDatos };
        this.guardarUsuarioActual();
    }

    obtenerEstadisticas() {
        const totalHabitos = this.habitos.length;
        const completados = this.habitos.filter(h => h.completado).length;
        const pendientes = totalHabitos - completados;
        const porcentajeCompletado = totalHabitos > 0 ? Math.round((completados / totalHabitos) * 100) : 0;

        return {
            totalHabitos,
            completados,
            pendientes,
            porcentajeCompletado
        };
    }

    cerrarSesion() {
        localStorage.removeItem('usuarioActualId');
        this.redirigirALogin();
    }
}

// Instancia global del gestor de usuario
const gestorUsuario = new GestorUsuario();

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM cargado - Inicializando aplicación...');
    
    if (!gestorUsuario.usuarioActual) {
        return; // Ya se redirigió al login
    }
    
    console.log('Usuario actual:', gestorUsuario.usuarioActual.username);
    
    inicializarAplicacion();
});

async function inicializarAplicacion() {
    try {
        console.log('Iniciando aplicación para usuario:', gestorUsuario.usuarioActual.username);
        
        // Cargar datos del usuario
        await cargarDatosUsuario();
        
        // Inicializar event listeners
        inicializarEventListeners();
        
        console.log('Aplicación inicializada correctamente');
        
    } catch (error) {
        console.error('Error al inicializar la aplicación:', error);
        mostrarNotificacion('Error al cargar la aplicación', 'error');
    }
}

// Función para cargar datos del usuario
async function cargarDatosUsuario() {
    try {
        // Cargar hábitos del usuario actual
        await gestorUsuario.cargarHabitos();
        
        // Actualizar interfaz
        actualizarInterfazUsuario();
        renderizarListaHabitos();
        renderizarEstadisticas();
        
        // Inicializar planner simple
        renderizarPlanner();
        
    } catch (error) {
        console.error('Error al cargar datos del usuario:', error);
        throw error;
    }
}

// Función para renderizar el planner simple
function renderizarPlanner() {
    console.log('Renderizando planner simple...');
    
    const calendarEl = document.getElementById('calendarContainer');
    
    if (!calendarEl) {
        console.error('No se encontró el contenedor del calendario');
        return;
    }

    // Obtener hábitos con fechas
    const habitosConFecha = gestorUsuario.habitos.filter(h => h.fechaObjetivo);
    const habitosSinFecha = gestorUsuario.habitos.filter(h => !h.fechaObjetivo);
    
    // Agrupar hábitos por fecha
    const habitosPorFecha = {};
    habitosConFecha.forEach(habito => {
        if (!habitosPorFecha[habito.fechaObjetivo]) {
            habitosPorFecha[habito.fechaObjetivo] = [];
        }
        habitosPorFecha[habito.fechaObjetivo].push(habito);
    });

    // Ordenar fechas
    const fechasOrdenadas = Object.keys(habitosPorFecha).sort();

    let html = `
        <div class="planner-container">
            <div class="planner-header">
                <h3>📅 Mi Planner de Hábitos</h3>
                <p>Visualiza y planifica tus hábitos por fecha</p>
            </div>
            
            <div class="planner-content">
    `;

    if (fechasOrdenadas.length > 0) {
        html += `
            <div class="planner-fechas">
                <h4>🗓️ Hábitos Programados</h4>
                <div class="fechas-lista">
        `;

        fechasOrdenadas.forEach(fecha => {
            const fechaFormateada = formatearFecha(fecha);
            const habitos = habitosPorFecha[fecha];
            
            html += `
                <div class="fecha-item">
                    <div class="fecha-header">
                        <span class="fecha-title">${fechaFormateada}</span>
                        <span class="fecha-count">${habitos.length} hábito${habitos.length > 1 ? 's' : ''}</span>
                    </div>
                    <div class="habitos-fecha">
            `;

            habitos.forEach(habito => {
                const icono = habito.completado ? '✅' : '⏳';
                const estadoClase = habito.completado ? 'completado' : 'pendiente';
                
                html += `
                    <div class="habito-planner ${estadoClase}" data-habito-id="${habito.id}">
                        <span class="habito-icon">${icono}</span>
                        <span class="habito-nombre">${habito.nombre}</span>
                        ${habito.categoria ? `<span class="habito-categoria">${habito.categoria}</span>` : ''}
                        <div class="habito-acciones">
                            <button class="btn-planner-toggle" onclick="toggleCompletado(${habito.id})">
                                ${habito.completado ? '↶' : '✓'}
                            </button>
                        </div>
                    </div>
                `;
            });

            html += `
                    </div>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;
    }

    if (habitosSinFecha.length > 0) {
        html += `
            <div class="planner-sin-fecha">
                <h4>📝 Hábitos Sin Fecha Específica</h4>
                <div class="habitos-lista-sin-fecha">
        `;

        habitosSinFecha.forEach(habito => {
            const icono = habito.completado ? '✅' : '⏳';
            const estadoClase = habito.completado ? 'completado' : 'pendiente';
            
            html += `
                <div class="habito-planner ${estadoClase}" data-habito-id="${habito.id}">
                    <span class="habito-icon">${icono}</span>
                    <span class="habito-nombre">${habito.nombre}</span>
                    <span class="habito-frecuencia">${obtenerTextoFrecuencia(habito.frecuencia)}</span>
                    ${habito.categoria ? `<span class="habito-categoria">${habito.categoria}</span>` : ''}
                    <div class="habito-acciones">
                        <button class="btn-planner-toggle" onclick="toggleCompletado(${habito.id})">
                            ${habito.completado ? '↶' : '✓'}
                        </button>
                        <button class="btn-planner-editar" onclick="editarHabito(${habito.id})">✏️</button>
                    </div>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;
    }

    if (gestorUsuario.habitos.length === 0) {
        html += `
            <div class="planner-vacio">
                <div class="vacio-content">
                    <span class="vacio-icon">🎯</span>
                    <h4>No hay hábitos registrados</h4>
                    <p>Comienza agregando tu primer hábito usando el formulario de la izquierda.</p>
                </div>
            </div>
        `;
    }

    // Resumen rápido
    const stats = gestorUsuario.obtenerEstadisticas();
    html += `
            </div>
            
            <div class="planner-resumen">
                <div class="resumen-stats">
                    <div class="resumen-stat">
                        <span class="stat-numero">${stats.totalHabitos}</span>
                        <span class="stat-label">Total</span>
                    </div>
                    <div class="resumen-stat">
                        <span class="stat-numero" style="color: #10b981;">${stats.completados}</span>
                        <span class="stat-label">Completados</span>
                    </div>
                    <div class="resumen-stat">
                        <span class="stat-numero" style="color: #3b82f6;">${stats.pendientes}</span>
                        <span class="stat-label">Pendientes</span>
                    </div>
                    <div class="resumen-stat">
                        <span class="stat-numero">${stats.porcentajeCompletado}%</span>
                        <span class="stat-label">Progreso</span>
                    </div>
                </div>
                <div class="planner-leyenda">
                    <p><strong>Leyenda:</strong> ✅ Completado | ⏳ Pendiente</p>
                </div>
            </div>
        </div>
    `;

    calendarEl.innerHTML = html;
}

// Función para renderizar la lista de hábitos
function renderizarListaHabitos() {
    const habitList = document.getElementById('habitList');
    
    if (!habitList) return;

    if (gestorUsuario.habitos.length === 0) {
        habitList.innerHTML = `
            <li class="no-habits">
                <div class="empty-state">
                    <span class="empty-icon">🎯</span>
                    <p>No hay hábitos registrados</p>
                    <small>¡Comienza agregando tu primer hábito!</small>
                </div>
            </li>
        `;
        return;
    }

    habitList.innerHTML = gestorUsuario.habitos.map(habito => `
        <li class="habit-item ${habito.completado ? 'completed' : ''}" data-habito-id="${habito.id}">
            <div class="habit-info">
                <h4 class="habit-name">${habito.nombre}</h4>
                <div class="habit-meta">
                    ${habito.categoria ? `<span class="habit-category">${habito.categoria}</span>` : ''}
                    <span class="habit-frequency">${obtenerTextoFrecuencia(habito.frecuencia)}</span>
                    ${habito.fechaObjetivo ? `<span class="habit-date">${formatearFecha(habito.fechaObjetivo)}</span>` : ''}
                </div>
                ${habito.descripcion ? `<p class="habit-description">${habito.descripcion}</p>` : ''}
            </div>
            <div class="habit-actions">
                <button class="btn-toggle-complete ${habito.completado ? 'btn-undo' : 'btn-complete'}" 
                        onclick="toggleCompletado(${habito.id})">
                    ${habito.completado ? '↶' : '✓'}
                </button>
                <button class="btn-edit" onclick="editarHabito(${habito.id})">✏️</button>
                <button class="btn-delete" onclick="eliminarHabito(${habito.id})">🗑️</button>
            </div>
        </li>
    `).join('');
}

// Función para renderizar estadísticas
function renderizarEstadisticas() {
    const statsContainer = document.getElementById('statsContainer');
    
    if (!statsContainer) return;

    const stats = gestorUsuario.obtenerEstadisticas();

    statsContainer.innerHTML = `
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-value">${stats.totalHabitos}</div>
                <div class="stat-label">Total Hábitos</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${stats.completados}</div>
                <div class="stat-label">Completados</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${stats.pendientes}</div>
                <div class="stat-label">Pendientes</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${stats.porcentajeCompletado}%</div>
                <div class="stat-label">Progreso</div>
            </div>
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: ${stats.porcentajeCompletado}%"></div>
        </div>
    `;
}

// Función para actualizar la interfaz de usuario
function actualizarInterfazUsuario() {
    const userAvatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');
    
    if (userAvatar) {
        userAvatar.src = `images/avatars/${gestorUsuario.usuarioActual.avatar}`;
        userAvatar.alt = `Avatar de ${gestorUsuario.usuarioActual.username}`;
        userAvatar.onerror = function() {
            this.src = 'images/avatars/ardilla.png';
        };
    }
    
    if (userName) {
        userName.textContent = gestorUsuario.usuarioActual.username;
    }
}

// Función para inicializar event listeners
function inicializarEventListeners() {
    // Formulario de hábitos
    const habitForm = document.getElementById('habitForm');
    if (habitForm) {
        habitForm.addEventListener('submit', guardarHabito);
    }

    // Botón de configuración
    const settingsBtn = document.getElementById('settingsBtn');
    if (settingsBtn) {
        settingsBtn.addEventListener('click', mostrarConfiguracion);
    }

    // Botón de salir - CORREGIDO
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
                gestorUsuario.cerrarSesion();
            }
        });
    }

    // Formulario de configuración
    const configForm = document.getElementById('configForm');
    if (configForm) {
        configForm.addEventListener('submit', guardarConfiguracion);
    }
}

// Función para guardar nuevo hábito
async function guardarHabito(event) {
    event.preventDefault();
    
    try {
        const formData = new FormData(event.target);
        const nuevoHabito = {
            nombre: formData.get('nombre'),
            categoria: formData.get('categoria'),
            frecuencia: formData.get('frecuencia'),
            hora: formData.get('hora'),
            fechaObjetivo: formData.get('fechaObjetivo'),
            descripcion: formData.get('descripcion')
        };

        await gestorUsuario.agregarHabito(nuevoHabito);
        
        renderizarListaHabitos();
        renderizarEstadisticas();
        renderizarPlanner();
        
        event.target.reset();
        
        mostrarNotificacion('Hábito agregado correctamente', 'success');
        
    } catch (error) {
        console.error('Error al guardar hábito:', error);
        mostrarNotificacion('Error al guardar el hábito', 'error');
    }
}

// Función para alternar estado de completado
async function toggleCompletado(habitoId) {
    try {
        const habitoActual = gestorUsuario.habitos.find(h => h.id === habitoId);
        if (!habitoActual) return;
        
        const habito = await gestorUsuario.actualizarHabito(habitoId, { 
            completado: !habitoActual.completado
        });
        
        if (habito) {
            renderizarListaHabitos();
            renderizarEstadisticas();
            renderizarPlanner();
            
            const estado = habito.completado ? 'completado' : 'pendiente';
            mostrarNotificacion(`Hábito marcado como ${estado}`, 'success');
        }
    } catch (error) {
        console.error('Error al actualizar hábito:', error);
        mostrarNotificacion('Error al actualizar el hábito', 'error');
    }
}

// Función para eliminar hábito
async function eliminarHabito(habitoId) {
    if (confirm('¿Estás seguro de que quieres eliminar este hábito?')) {
        try {
            await gestorUsuario.eliminarHabito(habitoId);
            renderizarListaHabitos();
            renderizarEstadisticas();
            renderizarPlanner();
            mostrarNotificacion('Hábito eliminado correctamente', 'success');
        } catch (error) {
            console.error('Error al eliminar hábito:', error);
            mostrarNotificacion('Error al eliminar el hábito', 'error');
        }
    }
}

// Función para editar hábito
function editarHabito(habitoId) {
    const habito = gestorUsuario.habitos.find(h => h.id === habitoId);
    if (habito) {
        // Llenar el formulario con los datos del hábito
        document.getElementById('nombre').value = habito.nombre;
        document.getElementById('categoria').value = habito.categoria || '';
        document.getElementById('frecuencia').value = habito.frecuencia;
        document.getElementById('hora').value = habito.hora || '';
        document.getElementById('fechaObjetivo').value = habito.fechaObjetivo || '';
        document.getElementById('descripcion').value = habito.descripcion || '';
        
        // Eliminar el hábito antiguo
        eliminarHabito(habitoId);
        
        // Hacer scroll al formulario
        document.getElementById('habitFormSection').scrollIntoView({ behavior: 'smooth' });
    }
}

// Funciones de configuración
function mostrarConfiguracion() {
    const modal = document.getElementById('configModal');
    if (modal) {
        // Llenar formulario con datos actuales
        document.getElementById('configUsername').value = gestorUsuario.usuarioActual.username;
        
        // Seleccionar avatar actual - CORREGIDO
        const avatarRadio = document.querySelector(`input[name="avatar"][value="${gestorUsuario.usuarioActual.avatar}"]`);
        if (avatarRadio) {
            avatarRadio.checked = true;
        } else {
            // Si no encuentra el avatar, seleccionar el primero
            const primerAvatar = document.querySelector('input[name="avatar"]');
            if (primerAvatar) {
                primerAvatar.checked = true;
            }
        }
        
        // Configurar notificaciones
        if (gestorUsuario.usuarioActual.configuracion) {
            document.getElementById('emailNotifications').checked = gestorUsuario.usuarioActual.configuracion.notificacionesEmail || true;
            document.getElementById('pushNotifications').checked = gestorUsuario.usuarioActual.configuracion.notificacionesPush || true;
            document.getElementById('reminderNotifications').checked = gestorUsuario.usuarioActual.configuracion.recordatorios || true;
        }
        
        modal.style.display = 'flex';
    }
}

function cerrarConfiguracion() {
    const modal = document.getElementById('configModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

async function guardarConfiguracion(event) {
    event.preventDefault();
    
    try {
        const nuevoNombre = document.getElementById('configUsername').value;
        const avatarSeleccionado = document.querySelector('input[name="avatar"]:checked');
        const nuevaPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        // Validar contraseña si se proporciona
        if (nuevaPassword && nuevaPassword !== confirmPassword) {
            mostrarNotificacion('Las contraseñas no coinciden', 'error');
            return;
        }
        
        // Actualizar usuario - CORREGIDO
        const nuevosDatos = {
            username: nuevoNombre || gestorUsuario.usuarioActual.username,
            avatar: avatarSeleccionado ? avatarSeleccionado.value : gestorUsuario.usuarioActual.avatar,
            configuracion: {
                notificacionesEmail: document.getElementById('emailNotifications').checked,
                notificacionesPush: document.getElementById('pushNotifications').checked,
                recordatorios: document.getElementById('reminderNotifications').checked
            }
        };
        
        await gestorUsuario.actualizarUsuario(nuevosDatos);
        
        // Actualizar interfaz
        actualizarInterfazUsuario();
        
        cerrarConfiguracion();
        mostrarNotificacion('Configuración guardada correctamente', 'success');
        
    } catch (error) {
        console.error('Error al guardar configuración:', error);
        mostrarNotificacion('Error al guardar la configuración', 'error');
    }
}

// Funciones utilitarias
function obtenerTextoFrecuencia(frecuencia) {
    const frecuencias = {
        diaria: 'Diario',
        semanal: 'Semanal',
        mensual: 'Mensual'
    };
    return frecuencias[frecuencia] || frecuencia;
}

function formatearFecha(fechaString) {
    if (!fechaString) return 'Sin fecha';
    
    try {
        const fecha = new Date(fechaString);
        return fecha.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    } catch (error) {
        return 'Fecha inválida';
    }
}

function mostrarNotificacion(mensaje, tipo = 'info') {
    const notificacion = document.createElement('div');
    notificacion.className = `notificacion notificacion-${tipo}`;
    notificacion.textContent = mensaje;
    notificacion.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 4px;
        color: white;
        z-index: 1000;
        font-weight: 500;
        background-color: ${tipo === 'success' ? '#10b981' : tipo === 'error' ? '#ef4444' : '#3b82f6'};
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;
    
    document.body.appendChild(notificacion);
    
    setTimeout(() => {
        if (notificacion.parentNode) {
            notificacion.parentNode.removeChild(notificacion);
        }
    }, 3000);
}

// Manejar errores no capturados
window.addEventListener('error', function(event) {
    console.error('Error global:', event.error);
});

// Exportar funciones globales para uso en HTML
window.cerrarConfiguracion = cerrarConfiguracion;
window.toggleCompletado = toggleCompletado;
window.editarHabito = editarHabito;
window.eliminarHabito = eliminarHabito;