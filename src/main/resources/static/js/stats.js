import Chart from "https://cdn.jsdelivr.net/npm/chart.js";

(async function loadStats() {
  const token = localStorage.getItem('token');
  const res = await fetch('/api/progress', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const data = await res.json();

  const ctx = document.getElementById('habitChart');
  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: data.map(d => d.fecha),
      datasets: [{
        label: 'Hábitos completados',
        data: data.map(d => d.completados)
      }]
    }
  });
})();
