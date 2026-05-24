// =============================================
// PropTech Analytics — Frontend Application
// =============================================

document.addEventListener("DOMContentLoaded", () => {
    cargarInmuebles();
});

// ---- NAVEGACIÓN ENTRE SECCIONES ----

function mostrarSeccion(seccion) {
    const secciones = ['inmuebles', 'clientes'];
    secciones.forEach(s => {
        document.getElementById('seccion-' + s).style.display = s === seccion ? 'block' : 'none';
        document.getElementById('buscador-' + s).style.display = s === seccion ? 'block' : 'none';
        document.getElementById('btn-nav-' + s).classList.toggle('active', s === seccion);
    });
    // Limpiar resultado de búsqueda al cambiar sección
    document.getElementById('resultado-busqueda').innerHTML = '';

    // Cargar datos según la sección
    if (seccion === 'inmuebles') cargarInmuebles();
    if (seccion === 'clientes') cargarClientes();
}

// ---- INMUEBLES ----

function cargarInmuebles() {
    fetch('/api/inmuebles')
        .then(res => {
            if (!res.ok) throw new Error("Servidor rechazó la conexión");
            return res.json();
        })
        .then(inmuebles => {
            const contenedor = document.getElementById('contenedor-inmuebles');
            contenedor.innerHTML = '';
            document.getElementById('contador-inmuebles').textContent = inmuebles.length + ' propiedades';

            inmuebles.forEach((inmueble, idx) => {
                let colorEstado = inmueble.estado === 'Disponible' ? 'bg-success' : 'bg-secondary';
                let extras = [];
                if (inmueble.habitaciones > 0) extras.push(`🛏️ ${inmueble.habitaciones} Hab`);
                if (inmueble.banos > 0) extras.push(`🛁 ${inmueble.banos} Baños`);
                if (inmueble.tieneParqueadero) extras.push(`🚗 Parqueo`);
                let descripcion = inmueble.descripcion || "Propiedad exclusiva y moderna.";

                const tarjeta = `
                    <div class="col-md-4 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-proptech shadow-sm h-100">
                            <div class="card-header-gradient text-white py-3 px-4">
                                <h5 class="mb-1 fw-bold">${inmueble.tipo}</h5>
                                <small class="text-white-50">📍 ${inmueble.direccion}</small>
                            </div>
                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="badge ${colorEstado} rounded-pill px-3 py-1">${inmueble.estado}</span>
                                    <span class="text-muted small fw-bold">Cod: ${inmueble.codigo}</span>
                                </div>
                                <div class="price-tag mb-2">$${inmueble.precio}M</div>
                                <p class="text-muted small mb-3">${descripcion}</p>
                                <div class="d-flex flex-wrap gap-2">
                                    <span class="extra-pill">📐 ${inmueble.area} m²</span>
                                    ${extras.map(e => `<span class="extra-pill">${e}</span>`).join('')}
                                </div>
                            </div>
                            <div class="card-footer bg-white border-0 p-3 text-center">
                                <button class="btn btn-primary rounded-pill w-100 fw-bold shadow-sm"
                                        onclick="verDetalleInmueble('${inmueble.codigo}')">
                                    Ver Detalles
                                </button>
                            </div>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });
        })
        .catch(error => {
            console.error("Error:", error);
            document.getElementById('contenedor-inmuebles').innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor. Verifica que Java esté corriendo.</p>
                </div>
            `;
        });
}

function buscarInmueblePorCodigo() {
    const codigo = document.getElementById('input-buscar-inmueble').value.trim();
    if (!codigo) return;

    const resultDiv = document.getElementById('resultado-busqueda');
    resultDiv.innerHTML = '<div class="text-white-50">Buscando...</div>';

    fetch(`/api/inmuebles/${encodeURIComponent(codigo)}`)
        .then(res => {
            if (!res.ok) throw new Error("No encontrado");
            return res.json();
        })
        .then(inmueble => {
            resultDiv.innerHTML = `
                <div class="result-card">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="fw-bold mb-1">✅ ${inmueble.tipo} — ${inmueble.codigo}</h5>
                            <p class="mb-1 small text-white-50">📍 ${inmueble.direccion}</p>
                            <p class="mb-0"><strong>$${inmueble.precio}M</strong> · ${inmueble.area} m² · ${inmueble.estado}</p>
                        </div>
                        <span class="badge bg-success rounded-pill px-3 py-2 fs-6">Encontrado</span>
                    </div>
                </div>
            `;
        })
        .catch(() => {
            resultDiv.innerHTML = `
                <div class="result-card" style="border-color: rgba(233,69,96,0.5);">
                    <span>❌ No se encontró un inmueble con código "<strong>${codigo}</strong>".</span>
                </div>
            `;
        });
}

function verDetalleInmueble(codigo) {
    fetch(`/api/inmuebles/${encodeURIComponent(codigo)}`)
        .then(res => res.json())
        .then(inmueble => {
            document.getElementById('detalle-inmueble-titulo').textContent =
                `${inmueble.tipo} — ${inmueble.codigo}`;

            let extras = [];
            if (inmueble.habitaciones > 0) extras.push(`🛏️ ${inmueble.habitaciones} Habitaciones`);
            if (inmueble.banos > 0) extras.push(`🛁 ${inmueble.banos} Baños`);
            if (inmueble.tieneParqueadero) extras.push(`🚗 Parqueadero incluido`);
            let descripcion = inmueble.descripcion || "Propiedad exclusiva y moderna.";

            document.getElementById('detalle-inmueble-body').innerHTML = `
                <div class="row">
                    <div class="col-md-6">
                        <h6 class="text-muted fw-bold mb-3">INFORMACIÓN GENERAL</h6>
                        <table class="table table-borderless">
                            <tr><td class="fw-bold">Tipo:</td><td>${inmueble.tipo}</td></tr>
                            <tr><td class="fw-bold">Dirección:</td><td>📍 ${inmueble.direccion}</td></tr>
                            <tr><td class="fw-bold">Código:</td><td>${inmueble.codigo}</td></tr>
                            <tr><td class="fw-bold">Estado:</td><td><span class="badge ${inmueble.estado === 'Disponible' ? 'bg-success' : 'bg-secondary'} rounded-pill">${inmueble.estado}</span></td></tr>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h6 class="text-muted fw-bold mb-3">DETALLES FINANCIEROS</h6>
                        <div class="price-tag mb-3">$${inmueble.precio}M</div>
                        <div class="mb-3">
                            <span class="extra-pill me-2">📐 ${inmueble.area} m²</span>
                            ${extras.map(e => `<span class="extra-pill me-2">${e}</span>`).join('')}
                        </div>
                    </div>
                </div>
                <hr>
                <h6 class="text-muted fw-bold mb-2">DESCRIPCIÓN</h6>
                <p class="text-muted">${descripcion}</p>
            `;

            new bootstrap.Modal(document.getElementById('modalDetalleInmueble')).show();
        })
        .catch(err => console.error('Error al cargar detalle:', err));
}

// ---- CLIENTES ----

function cargarClientes() {
    fetch('/api/clientes')
        .then(res => {
            if (!res.ok) throw new Error("Servidor rechazó la conexión");
            return res.json();
        })
        .then(clientes => {
            const contenedor = document.getElementById('contenedor-clientes');
            contenedor.innerHTML = '';
            document.getElementById('contador-clientes').textContent = clientes.length + ' registrados';

            if (clientes.length === 0) {
                contenedor.innerHTML = `
                    <div class="col-12 text-center text-muted py-5">
                        <h4>👥 No hay clientes registrados aún</h4>
                        <p>Usa el botón "Registrar Cliente" para agregar el primero.</p>
                    </div>`;
                return;
            }

            clientes.forEach((cliente, idx) => {
                const iniciales = cliente.nombre.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
                const colores = ['#e94560', '#0f3460', '#533483', '#16213e', '#1a1a2e'];
                const color = colores[idx % colores.length];

                const tarjeta = `
                    <div class="col-md-4 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-cliente shadow-sm h-100">
                            <div class="card-header-cliente text-white py-3 px-4 d-flex align-items-center gap-3">
                                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold"
                                     style="width:48px; height:48px; background: rgba(255,255,255,0.2); font-size:1.1rem;">
                                    ${iniciales}
                                </div>
                                <div>
                                    <h5 class="mb-0 fw-bold">${cliente.nombre}</h5>
                                    <small class="text-white-50">ID: ${cliente.identificacion}</small>
                                </div>
                            </div>
                            <div class="card-body p-4">
                                <div class="budget-tag mb-2">$${cliente.presupuestoMaximo}M</div>
                                <p class="text-muted small mb-1">Presupuesto máximo</p>
                                <hr class="my-3">
                                <div class="d-flex flex-column gap-2 small">
                                    <span>📞 ${cliente.telefono}</span>
                                    <span>📧 ${cliente.email}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });
        })
        .catch(error => {
            console.error("Error:", error);
            document.getElementById('contenedor-clientes').innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor.</p>
                </div>
            `;
        });
}

function buscarClientePorId() {
    const id = document.getElementById('input-buscar-cliente').value.trim();
    if (!id) return;

    const resultDiv = document.getElementById('resultado-busqueda');
    resultDiv.innerHTML = '<div class="text-white-50">Buscando...</div>';

    fetch(`/api/clientes/${encodeURIComponent(id)}`)
        .then(res => {
            if (!res.ok) throw new Error("No encontrado");
            return res.json();
        })
        .then(cliente => {
            resultDiv.innerHTML = `
                <div class="result-card">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="fw-bold mb-1">✅ ${cliente.nombre}</h5>
                            <p class="mb-1 small text-white-50">ID: ${cliente.identificacion} · 📞 ${cliente.telefono}</p>
                            <p class="mb-0">📧 ${cliente.email} · <strong>Presupuesto: $${cliente.presupuestoMaximo}M</strong></p>
                        </div>
                        <span class="badge bg-success rounded-pill px-3 py-2 fs-6">Encontrado</span>
                    </div>
                </div>
            `;
        })
        .catch(() => {
            resultDiv.innerHTML = `
                <div class="result-card" style="border-color: rgba(233,69,96,0.5);">
                    <span>❌ No se encontró un cliente con ID "<strong>${id}</strong>".</span>
                </div>
            `;
        });
}

// ---- REGISTRO DE CLIENTE ----

function abrirModalCliente() {
    new bootstrap.Modal(document.getElementById('modalCliente')).show();
}

function registrarCliente() {
    const datos = {
        identificacion: document.getElementById('nuevo-cliente-id').value.trim(),
        nombre: document.getElementById('nuevo-cliente-nombre').value.trim(),
        telefono: document.getElementById('nuevo-cliente-telefono').value.trim(),
        email: document.getElementById('nuevo-cliente-email').value.trim(),
        presupuestoMaximo: parseFloat(document.getElementById('nuevo-cliente-presupuesto').value) || 0
    };

    if (!datos.identificacion || !datos.nombre) {
        alert('Identificación y nombre son obligatorios.');
        return;
    }

    fetch('/api/clientes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al registrar');
        return res.json();
    })
    .then(cliente => {
        // Cerrar modal
        bootstrap.Modal.getInstance(document.getElementById('modalCliente')).hide();
        // Limpiar formulario
        ['nuevo-cliente-id','nuevo-cliente-nombre','nuevo-cliente-telefono','nuevo-cliente-email','nuevo-cliente-presupuesto']
            .forEach(id => document.getElementById(id).value = '');
        // Recargar lista
        cargarClientes();
    })
    .catch(err => {
        alert('Error al registrar cliente: ' + err.message);
    });
}