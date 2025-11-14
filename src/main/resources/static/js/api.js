class ApiService {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api';
    }

    // Helper: encabezados con token si existe
    _headers(withJson = true) {
        const headers = {};
        if (withJson) headers['Content-Type'] = 'application/json';
        const token = localStorage.getItem('token');
        if (token) headers['Authorization'] = `Bearer ${token}`;
        return headers;
    }

    async login(usernameOrEmail, password) {
        const body = { username: usernameOrEmail, password }; // ajusta si tu backend espera email/username
        const res = await fetch(`${this.baseUrl}/auth/login`, {
            method: 'POST',
            headers: this._headers(true),
            body: JSON.stringify(body)
        });
        return this._handleJson(res);
    }

    async register(userData) {
        const res = await fetch(`${this.baseUrl}/auth/register`, {
            method: 'POST',
            headers: this._headers(true),
            body: JSON.stringify(userData)
        });
        return this._handleJson(res);
    }

    async getHabits() {
        const res = await fetch(`${this.baseUrl}/habits`, {
            method: 'GET',
            headers: this._headers(false)
        });
        return this._handleJson(res);
    }

    async createHabit(habitData) {
        const res = await fetch(`${this.baseUrl}/habits`, {
            method: 'POST',
            headers: this._headers(true),
            body: JSON.stringify(habitData)
        });
        return this._handleJson(res);
    }

    async updateHabit(habitId, habitData) {
        const res = await fetch(`${this.baseUrl}/habits/${habitId}`, {
            method: 'PUT',
            headers: this._headers(true),
            body: JSON.stringify(habitData)
        });
        return this._handleJson(res);
    }

    async deleteHabit(habitId) {
        const res = await fetch(`${this.baseUrl}/habits/${habitId}`, {
            method: 'DELETE',
            headers: this._headers(false)
        });
        // some backends return no content -> return status
        if (res.status === 204) return { success: true };
        return this._handleJson(res);
    }

    // Marcar progreso: usa ProgressController POST /api/progress/{habitId}
    async markCompletion(habitId, fecha, cumplido = true) {
        const body = { fecha, cumplido };
        const res = await fetch(`${this.baseUrl}/progress/${habitId}`, {
            method: 'POST',
            headers: this._headers(true),
            body: JSON.stringify(body)
        });
        return this._handleJson(res);
    }

    // Stats endpoints (según tu ProgressController)
    async getDailyProgress() {
        const res = await fetch(`${this.baseUrl}/progress/diario`, {
            method: 'GET',
            headers: this._headers(false)
        });
        return this._handleJson(res);
    }

    async getWeeklyStats() {
        const res = await fetch(`${this.baseUrl}/progress/stats/weekly`, {
            method: 'GET',
            headers: this._headers(false)
        });
        return this._handleJson(res);
    }

    async getMonthlyStats() {
        const res = await fetch(`${this.baseUrl}/progress/stats/monthly`, {
            method: 'GET',
            headers: this._headers(false)
        });
        return this._handleJson(res);
    }

    // Helper: parse JSON and handle errors
    async _handleJson(res) {
        const txt = await res.text();
        let data;
        try { data = txt ? JSON.parse(txt) : null; } catch (e) { data = txt; }

        if (!res.ok) {
            // intenta extraer mensaje, sino status text
            const msg = (data && data.message) ? data.message : (data && data.error) ? data.error : res.statusText;
            const error = new Error(msg || 'Request failed');
            error.status = res.status;
            error.body = data;
            throw error;
        }
        return data;
    }
}

// exporta una instancia lista para usar
const apiService = new ApiService();
