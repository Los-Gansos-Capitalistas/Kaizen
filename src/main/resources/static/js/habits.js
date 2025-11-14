const token = localStorage.getItem('token');
const API_URL = '/api/habits';

const habitList = document.getElementById('habitList');
const addHabitBtn = document.getElementById('addHabitBtn');
const modal = document.getElementById('habitModal');
const form = document.getElementById('habitForm');
const cancelBtn = document.getElementById('cancelHabitBtn');

loadHabits();

addHabitBtn.addEventListener('click', () => {
  form.reset();
  document.getElementById('habitId').value = '';
  document.getElementById('habitModalTitle').textContent = 'Nuevo hábito';
  modal.classList.remove('hidden');
});

cancelBtn.addEventListener('click', () => modal.classList.add('hidden'));

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const habit = {
    nombre: document.getElementById('habitNombre').value,
    categoria: document.getElementById('habitCategoria').value,
    frecuencia: document.getElementById('habitFrecuencia').value,
    hora: document.getElementById('habitHora').value,
    descripcion: document.getElementById('habitDescripcion').value
  };
  const id = document.getElementById('habitId').value;

  const method = id ? 'PUT' : 'POST';
  const url = id ? `${API_URL}/${id}` : API_URL;

  await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(habit)
  });
  modal.classList.add('hidden');
  loadHabits();
});

async function loadHabits() {
  habitList.innerHTML = '<p>Cargando...</p>';
  const res = await fetch(API_URL, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const habits = await res.json();

  if (!habits.length) {
    habitList.innerHTML = '<p>No tienes hábitos aún.</p>';
    return;
  }

  habitList.innerHTML = habits.map(h => `
    <div class="habit-card">
      <h3>${h.nombre}</h3>
      <p>${h.descripcion || 'Sin descripción'}</p>
      <small>${h.frecuencia || ''} ${h.hora ? `· ${h.hora}` : ''}</small>
      <div class="actions">
        <button onclick="editHabit(${h.id})">✏️</button>
        <button onclick="deleteHabit(${h.id})">🗑️</button>
      </div>
    </div>
  `).join('');
}

async function editHabit(id) {
  const res = await fetch(`${API_URL}/${id}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const h = await res.json();
  document.getElementById('habitId').value = h.id;
  document.getElementById('habitNombre').value = h.nombre;
  document.getElementById('habitCategoria').value = h.categoria;
  document.getElementById('habitFrecuencia').value = h.frecuencia;
  document.getElementById('habitHora').value = h.hora;
  document.getElementById('habitDescripcion').value = h.descripcion;
  document.getElementById('habitModalTitle').textContent = 'Editar hábito';
  modal.classList.remove('hidden');
}

async function deleteHabit(id) {
  if (!confirm('¿Eliminar este hábito?')) return;
  await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  loadHabits();
}
