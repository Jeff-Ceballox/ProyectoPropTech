document.addEventListener("DOMContentLoaded", () => {
    cargarInmuebles();
});

function cargarInmuebles() {
    // Llamamos a nuestra propia API
    fetch('/api/inmuebles')
        .then(respuesta => {
            if (!respuesta.ok) throw new Error("El servidor Java rechazó la conexión");
            return respuesta.json(); 
        })
        .then(inmuebles => {
            const contenedor = document.getElementById('contenedor-inmuebles');
            contenedor.innerHTML = ''; // Borramos el mensajito de "Cargando..."

            // Recorremos los datos que nos mandó Java
            inmuebles.forEach(inmueble => {
                let colorEstado = inmueble.estado === 'Disponible' ? 'bg-success' : 'bg-secondary';

                let extras = [];
                if (inmueble.habitaciones > 0) extras.push(`🛏️ ${inmueble.habitaciones} Hab`);
                if (inmueble.banos > 0) extras.push(`🛁 ${inmueble.banos} Baños`);
                if (inmueble.tieneParqueadero) extras.push(`🚗 Parqueo`);
                let descripcion = inmueble.descripcion ? inmueble.descripcion : "Propiedad exclusiva y moderna.";

                // Construimos la tarjeta HTML
                const tarjetaHTML = `
                    <div class="col-md-4">
                        <div class="card h-100 shadow-lg border-0 rounded-4 overflow-hidden" style="transition: transform 0.3s;">
                            <div class="card-header bg-dark text-white py-3">
                                <h5 class="card-title mb-0 fw-bold">${inmueble.tipo}</h5>
                                <small class="text-light">📍 ${inmueble.direccion}</small>
                            </div>
                            <div class="card-body bg-light">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="badge ${colorEstado} fs-6">${inmueble.estado}</span>
                                    <span class="text-muted small">Cod: ${inmueble.codigo}</span>
                                </div>
                                <h2 class="text-primary fw-bold mb-3">$${inmueble.precio}M</h2>
                                <p class="card-text text-muted small mb-3">${descripcion}</p>
                                <div class="d-flex flex-wrap gap-2 text-secondary small fw-bold">
                                    <span>📐 ${inmueble.area} m²</span>
                                    ${extras.length > 0 ? ' | ' + extras.join(' | ') : ''}
                                </div>
                            </div>
                            <div class="card-footer bg-white border-0 p-3 text-center">
                                <button class="btn btn-primary rounded-pill w-100 fw-bold shadow-sm">Ver Detalles</button>
                            </div>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjetaHTML;
            });
        })
        .catch(error => {
            console.error("Hubo un error:", error);
            document.getElementById('contenedor-inmuebles').innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor. Verifica que Java esté corriendo.</p>
                </div>
            `;
        });
}