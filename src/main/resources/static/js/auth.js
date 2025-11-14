// =============================
// Gestor de usuarios
// =============================
class AuthManager {
    constructor() {
        this.users = this.loadUsers();
    }

    loadUsers() {
        const users = localStorage.getItem('habitTrackerUsers');
        return users ? JSON.parse(users) : [];
    }

    saveUsers() {
        localStorage.setItem('habitTrackerUsers', JSON.stringify(this.users));
    }

    register(username, email, password, avatar = 'ardilla.png') {
        // Verificar si el usuario ya existe
        const existingUser = this.users.find(u => u.username === username || u.email === email);
        if (existingUser) {
            throw new Error('El usuario o email ya está registrado');
        }

        // Crear nuevo usuario
        const newUser = {
            id: 'user-' + Date.now(),
            username: username,
            email: email,
            password: password, // En una app real, esto debería estar encriptado
            avatar: avatar,
            fechaRegistro: new Date().toISOString()
        };

        this.users.push(newUser);
        this.saveUsers();
        
        return newUser;
    }

    login(username, password) {
        const user = this.users.find(u => u.username === username && u.password === password);
        if (!user) {
            throw new Error('Usuario o contraseña incorrectos');
        }
        return user;
    }

    setCurrentUser(user) {
        localStorage.setItem('usuarioActualId', user.id);
        localStorage.setItem(`usuario_${user.id}`, JSON.stringify(user));
    }
}

// =============================
// Inicialización
// =============================
const authManager = new AuthManager();

// =============================
// Selección de elementos
// =============================
const loginTab = document.getElementById('loginTab');
const registerTab = document.getElementById('registerTab');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const goToLogin = document.getElementById('goToLogin');
const avatarGrid = document.getElementById('avatarGrid');
const selectedAvatarInput = document.getElementById('selectedAvatar');

// =============================
// Tabs: alternar entre login y registro
// =============================
loginTab.addEventListener('click', () => {
    loginTab.classList.add('active');
    registerTab.classList.remove('active');
    loginForm.classList.add('active');
    registerForm.classList.remove('active');
    clearMessages();
});

registerTab.addEventListener('click', () => {
    registerTab.classList.add('active');
    loginTab.classList.remove('active');
    registerForm.classList.add('active');
    loginForm.classList.remove('active');
    clearMessages();
});

goToLogin.addEventListener('click', (e) => {
    e.preventDefault();
    loginTab.click();
});

// =============================
// Selección de avatar en el registro
// =============================
if (avatarGrid) {
    avatarGrid.querySelectorAll('.avatar-option').forEach(option => {
        option.addEventListener('click', () => {
            avatarGrid.querySelectorAll('.avatar-option').forEach(o => o.classList.remove('selected'));
            option.classList.add('selected');
            selectedAvatarInput.value = option.dataset.avatar;
        });
    });

    // Seleccionar el primer avatar por defecto
    const firstAvatar = avatarGrid.querySelector('.avatar-option');
    if (firstAvatar) {
        firstAvatar.click();
    }
}

// =============================
// Registro
// =============================
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitBtn = registerForm.querySelector('.auth-btn');
    const username = document.getElementById('registerUsername').value.trim();
    const email = document.getElementById('registerEmail').value.trim();
    const password = document.getElementById('registerPassword').value.trim();
    const avatar = selectedAvatarInput.value || 'ardilla.png';

    // Validaciones básicas
    if (!username || !email || !password) {
        showError('Por favor completa todos los campos');
        return;
    }

    if (username.length < 3) {
        showError('El usuario debe tener al menos 3 caracteres');
        return;
    }

    if (password.length < 6) {
        showError('La contraseña debe tener al menos 6 caracteres');
        return;
    }

    try {
        // Mostrar loading
        setLoading(submitBtn, true);

        // Registrar usuario
        const newUser = authManager.register(username, email, password, avatar);
        
        // Iniciar sesión automáticamente
        authManager.setCurrentUser(newUser);
        
        // Redirigir al dashboard
        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1000);

    } catch (error) {
        showError(error.message);
    } finally {
        setLoading(submitBtn, false);
    }
});

// =============================
// Login
// =============================
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitBtn = loginForm.querySelector('.auth-btn');
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    // Validaciones básicas
    if (!username || !password) {
        showError('Por favor completa todos los campos');
        return;
    }

    try {
        // Mostrar loading
        setLoading(submitBtn, true);

        // Intentar login
        const user = authManager.login(username, password);
        
        // Establecer usuario actual
        authManager.setCurrentUser(user);
        
        // Redirigir al dashboard
        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1000);

    } catch (error) {
        showError(error.message);
    } finally {
        setLoading(submitBtn, false);
    }
});

// =============================
// Funciones utilitarias
// =============================
function setLoading(button, isLoading) {
    if (isLoading) {
        button.classList.add('loading');
        button.disabled = true;
    } else {
        button.classList.remove('loading');
        button.disabled = false;
    }
}

function showError(message) {
    // Eliminar mensajes anteriores
    clearMessages();
    
    // Crear mensaje de error
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    
    // Insertar después del primer formulario activo
    const activeForm = document.querySelector('.auth-form.active');
    activeForm.insertBefore(errorDiv, activeForm.firstChild);
    
    // Auto-eliminar después de 5 segundos
    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}

function showSuccess(message) {
    // Eliminar mensajes anteriores
    clearMessages();
    
    // Crear mensaje de éxito
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    successDiv.style.display = 'block';
    
    // Insertar después del primer formulario activo
    const activeForm = document.querySelector('.auth-form.active');
    activeForm.insertBefore(successDiv, activeForm.firstChild);
    
    // Auto-eliminar después de 3 segundos
    setTimeout(() => {
        successDiv.remove();
    }, 3000);
}

function clearMessages() {
    const messages = document.querySelectorAll('.error-message, .success-message');
    messages.forEach(msg => msg.remove());
}

// =============================
// Verificar si ya está autenticado
// =============================
document.addEventListener('DOMContentLoaded', function() {
    const usuarioActualId = localStorage.getItem('usuarioActualId');
    if (usuarioActualId) {
        // Si ya está autenticado, redirigir al dashboard
        window.location.href = 'dashboard.html';
    }
});