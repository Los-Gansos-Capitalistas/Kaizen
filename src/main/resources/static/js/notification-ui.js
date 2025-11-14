class NotificationUI {
    constructor() {
        this.notificationService = null;
        this.init();
    }

    init() {
        // Esperar a que el servicio de notificaciones esté listo
        setTimeout(() => {
            this.notificationService = notificationService;
            this.setupUI();
        }, 1000);
    }

    setupUI() {
        this.setupNotificationBell();
        this.setupNotificationsPanel();
    }

    setupNotificationBell() {
        const bell = document.getElementById('notificationBell');
        const overlay = document.getElementById('notificationsOverlay');
        const panel = document.getElementById('notificationsPanel');

        if (bell && panel) {
            bell.addEventListener('click', () => {
                this.openNotificationsPanel();
            });

            overlay.addEventListener('click', () => {
                this.closeNotificationsPanel();
            });

            document.getElementById('closeNotifications')?.addEventListener('click', () => {
                this.closeNotificationsPanel();
            });
        }
    }

    setupNotificationsPanel() {
        document.getElementById('markAllRead')?.addEventListener('click', () => {
            this.notificationService.markAllAsRead();
            this.renderNotificationsList();
        });
    }

    openNotificationsPanel() {
        const overlay = document.getElementById('notificationsOverlay');
        const panel = document.getElementById('notificationsPanel');

        overlay.classList.add('show');
        panel.classList.add('open');
        
        this.renderNotificationsList();
    }

    closeNotificationsPanel() {
        const overlay = document.getElementById('notificationsOverlay');
        const panel = document.getElementById('notificationsPanel');

        overlay.classList.remove('show');
        panel.classList.remove('open');
    }

    renderNotificationsList() {
        const list = document.getElementById('notificationsList');
        if (!list || !this.notificationService) return;

        const notifications = this.notificationService.notifications;

        if (notifications.length === 0) {
            list.innerHTML = `
                <div class="notifications-empty">
                    <div style="font-size: 48px; margin-bottom: 15px;">🔔</div>
                    <div>No hay notificaciones</div>
                    <div style="font-size: 12px; margin-top: 8px;">Te avisaremos cuando tengas recordatorios</div>
                </div>
            `;
            return;
        }

        list.innerHTML = notifications.map(notification => `
            <div class="notification-item ${notification.read ? '' : 'unread'}" 
                 onclick="notificationUI.handleNotificationClick(${notification.id})">
                <div class="notification-item-header">
                    <div class="notification-item-title">${notification.title}</div>
                    <div class="notification-item-time">${this.formatTime(notification.timestamp)}</div>
                </div>
                <div class="notification-item-message">${notification.message}</div>
                <div class="notification-item-actions">
                    ${!notification.read ? `
                        <button class="notification-action-btn" onclick="event.stopPropagation(); notificationUI.markAsRead(${notification.id})">
                            Marcar como leído
                        </button>
                    ` : ''}
                    <button class="notification-action-btn" onclick="event.stopPropagation(); notificationUI.deleteNotification(${notification.id})">
                        Eliminar
                    </button>
                </div>
            </div>
        `).join('');
    }

    handleNotificationClick(notificationId) {
        const notification = this.notificationService.notifications.find(n => n.id === notificationId);
        
        if (notification && notification.habitId) {
            // Navegar al hábito relacionado
            this.closeNotificationsPanel();
            // Aquí puedes agregar lógica para mostrar el hábito específico
        }

        this.markAsRead(notificationId);
    }

    markAsRead(notificationId) {
        this.notificationService.markAsRead(notificationId);
        this.renderNotificationsList();
    }

    deleteNotification(notificationId) {
        this.notificationService.deleteNotification(notificationId);
        this.renderNotificationsList();
    }

    formatTime(timestamp) {
        return this.notificationService.formatTime(timestamp);
    }
}

// Inicializar la UI de notificaciones
let notificationUI;
document.addEventListener('DOMContentLoaded', () => {
    notificationUI = new NotificationUI();
});