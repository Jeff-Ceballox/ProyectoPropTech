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

                // Construimos la tarjeta HTML
                const tarjetaHTML = `
                    <div class="col-md-4">
                        <div class="card h-100 shadow-sm border-0">
                            <div class="card-header bg-primary text-white">
                                <h5 class="card-title mb-0">${inmueble.tipo} en ${inmueble.direccion}</h5>
                            </div>
                            <div class="card-body">
                                <p class="card-text text-muted">
                                    <strong>Código:</strong> ${inmueble.codigo} <br>
                                    <strong>Área:</strong> ${inmueble.area} m² <br>
                                    <strong>Estado:</strong> <span class="badge ${colorEstado}">${inmueble.estado}</span>
                                </p>
                                <h3 class="text-primary fw-bold">$${inmueble.precio}M</h3>
                            </div>
                            <div class="card-footer bg-white border-0 pb-3 text-center">
                                <button class="btn btn-outline-primary btn-sm w-100 fw-bold">Ver Detalles</button>
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