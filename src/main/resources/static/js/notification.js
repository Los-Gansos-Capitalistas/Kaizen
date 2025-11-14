(async function loadNotifications() {
  const token = localStorage.getItem('token');
  const res = await fetch('/api/notifications', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await res.json();

  const list = document.getElementById('notificationList');
  list.innerHTML = data.length
    ? data.map(n => `<li>${n.mensaje}</li>`).join('')
    : '<p>No hay notificaciones pendientes.</p>';
})();
