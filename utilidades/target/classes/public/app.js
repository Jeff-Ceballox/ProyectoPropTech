// =============================================
// Insignia Inmo — Frontend Application
// =============================================

function initApp() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (usuario.nombre) {
        const navNombre = document.getElementById('nombre-usuario-nav');
        if (navNombre) navNombre.textContent = usuario.nombre.split(' ')[0];
    }
    cargarInmuebles();
}

document.addEventListener("DOMContentLoaded", initApp);

// ---- NAVEGACIÓN ENTRE SECCIONES ----

function mostrarSeccion(seccion) {
    const secciones = ['inmuebles', 'clientes', 'operaciones', 'reportes'];
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
    if (seccion === 'operaciones') cargarOperaciones();
    if (seccion === 'reportes') cargarReportes();
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
                const colores = ['var(--color-primario)', 'var(--color-secundario)'];
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
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });;
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

function buscarOperacionPorTipo() {
    const tipo = document.getElementById('input-buscar-operacion').value.trim();
    const resultDiv = document.getElementById('resultado-busqueda');
    resultDiv.innerHTML = '<div class="text-white-50">Buscando...</div>';

    fetch(`/api/operaciones/tipo/${encodeURIComponent(tipo)}`)
        .then(res => {
            if (!res.ok) throw new Error("No encontrado");
            return res.json();
        })
        .then(operaciones => {
            if (operaciones.length === 0) {
                resultDiv.innerHTML = `<div class="result-card" style="border-color: rgba(233,69,96,0.5);"><span>❌ No se encontraron operaciones de tipo "${tipo}".</span></div>`;
                return;
            }
            let html = `<div class="result-card"><h5 class="fw-bold mb-2">✅ ${operaciones.length} operaciones de tipo "${tipo}"</h5><hr><div class="row g-2">`;
            operaciones.forEach(op => {
                html += `<div class="col-md-4"><div class="border rounded p-2"><strong>${op.idOperacion}</strong><br><small>Monto: $${op.monto}M</small></div></div>`;
            });
            html += '</div></div>';
            resultDiv.innerHTML = html;
        })
        .catch(() => {
            resultDiv.innerHTML = `<div class="result-card" style="border-color: rgba(233,69,96,0.5);"><span>❌ Error al buscar operaciones.</span></div>`;
        });
}

// ---- OPERACIONES ----

function cargarOperaciones() {
    fetch('/api/operaciones')
        .then(res => {
            if (!res.ok) throw new Error("Servidor rechazó la conexión");
            return res.json();
        })
        .then(operaciones => {
            const contenedor = document.getElementById('contenedor-operaciones');
            contenedor.innerHTML = '';
            document.getElementById('contador-operaciones').textContent = operaciones.length + ' operaciones';

            if (operaciones.length === 0) {
                contenedor.innerHTML = `
                    <div class="col-12 text-center text-muted py-5">
                        <h4>💼 No hay operaciones registradas aún</h4>
                        <p>Usa el botón "Nueva Operación" para agregar la primera.</p>
                    </div>`;
                return;
            }

            operaciones.forEach((operacion, idx) => {
                const colorTipo = operacion.tipo === 'Venta' ? 'var(--color-primario)' : 
                                  operacion.tipo === 'Arriendo' ? 'var(--color-secundario)' : 'var(--color-primario)';
                
                const tarjeta = `
                    <div class="col-md-4 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-proptech shadow-sm h-100">
                            <div class="card-header-gradient text-white py-3 px-4">
                                <h5 class="mb-1 fw-bold">${operacion.tipo}</h5>
                                <small class="text-white-50">ID: ${operacion.idOperacion}</small>
                            </div>
                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="badge rounded-pill px-3 py-1" style="background: var(--color-fondo-claro); color: var(--color-primario);">
                                        ${operacion.tipo}
                                    </span>
                                    <span class="price-tag mb-2">$${operacion.monto}M</span>
                                </div>
                                <hr class="my-3">
                                <div class="d-flex flex-column gap-2 small">
                                    <span>👤 Cliente: ${operacion.cliente ? operacion.cliente.nombre : operacion.idCliente}</span>
                                    <span>🏠 Inmueble: ${operacion.inmueble ? operacion.inmueble.codigo : operacion.idInmueble}</span>
                                    <span>💼 Asesor: ${operacion.asesor ? operacion.asesor.nombre : operacion.idAsesor}</span>
                                    <span>📅 ${new Date(operacion.fecha).toLocaleDateString()}</span>
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
            document.getElementById('contenedor-operaciones').innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor.</p>
                </div>
            `;
        });
}

function abrirModalOperacion() {
    new bootstrap.Modal(document.getElementById('modalOperacion')).show();
}

function registrarOperacion() {
    const datos = {
        idOperacion: document.getElementById('nueva-op-id').value.trim(),
        tipo: document.getElementById('nueva-op-tipo').value,
        idCliente: document.getElementById('nueva-op-cliente').value.trim(),
        idInmueble: document.getElementById('nueva-op-inmueble').value.trim(),
        idAsesor: document.getElementById('nueva-op-asesor').value.trim(),
        monto: parseFloat(document.getElementById('nueva-op-monto').value) || 0
    };

    // Validaciones
    if (!datos.idOperacion) {
        mostrarError('El ID de la operación es obligatorio');
        return;
    }
    if (!datos.idCliente) {
        mostrarError('El ID del cliente es obligatorio');
        return;
    }
    if (!datos.idInmueble) {
        mostrarError('El ID del inmueble es obligatorio');
        return;
    }
    if (isNaN(datos.monto) || datos.monto <= 0) {
        mostrarError('El monto debe ser un número positivo');
        return;
    }

    fetch('/api/operaciones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al registrar');
        return res.json();
    })
    .then(operacion => {
        bootstrap.Modal.getInstance(document.getElementById('modalOperacion')).hide();
        ['nueva-op-id','nueva-op-cliente','nueva-op-inmueble','nueva-op-asesor','nueva-op-monto']
            .forEach(id => document.getElementById(id).value = '');
        cargarOperaciones();
        mostrarExito('Operación registrada exitosamente');
    })
    .catch(err => {
        mostrarError('Error al registrar operación: ' + err.message);
    });
}

// ---- REPORTES ----

function cargarReportes() {
    mostrarReporte('rendimiento');
}

function mostrarReporte(tipo) {
    // Actualizar tabs
    if (event && event.target) {
        document.querySelectorAll('#report-tabs .nav-link').forEach(btn => {
            btn.classList.remove('active');
        });
        event.target.classList.add('active');
    }

    const contenedor = document.getElementById('contenedor-reportes');
    contenedor.innerHTML = '<div class="text-center text-muted"><div class="spinner-border text-primary mb-2"></div><p>Cargando...</p></div>';

    switch(tipo) {
        case 'rendimiento':
            fetch('/api/reportes/rendimiento-inmuebles')
                .then(res => res.json())
                .then(data => {
                    contenedor.innerHTML = `
                        <div class="row g-4">
                            <div class="col-md-3">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Total Inmuebles</h6>
                                    <h3 class="fw-bold text-primary">${data.totalInmuebles}</h3>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Total Visitas</h6>
                                    <h3 class="fw-bold text-info">${data.totalVisitas}</h3>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Operaciones</h6>
                                    <h3 class="fw-bold text-success">${data.totalOperaciones}</h3>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Tasa Conversión</h6>
                                    <h3 class="fw-bold" style="background: linear-gradient(135deg, #0f3460, #533483); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">${data.tasaConversion}</h3>
                                </div>
                            </div>
                        </div>
                    `;
                });
            break;
        
        case 'asesores':
            fetch('/api/reportes/asesores')
                .then(res => res.json())
                .then(data => {
                    let html = '<div class="row g-3">';
                    data.forEach(a => {
                        html += `
                            <div class="col-md-4">
                                <div class="card card-proptech p-3">
                                    <h5 class="fw-bold mb-2">${a.asesor || a.nombre}</h5>
                                    <span class="badge bg-primary rounded-pill mb-2">${a.idAsesor || 'N/A'}</span>
                                    <p class="mb-1"><strong>Operaciones:</strong> ${a.operacionesCerradas}</p>
                                    <p class="mb-0 fw-bold">${a.totalVentas}</p>
                                    <small class="text-muted">Calificación: ${a.calificacion || 'N/A'}</small>
                                </div>
                            </div>
                        `;
                    });
                    html += '</div>';
                    contenedor.innerHTML = html;
                });
            break;
        
        case 'precios':
            fetch('/api/reportes/precios-zona')
                .then(res => res.json())
                .then(data => {
                    let html = '<div class="row g-3">';
                    for (const [zona, precio] of Object.entries(data)) {
                        html += `
                            <div class="col-md-4">
                                <div class="card card-proptech p-3 text-center">
                                    <h5 class="fw-bold mb-2">${zona}</h5>
                                    <span class="price-tag mb-2">$${precio.toFixed(2)}M</span>
                                    <small class="text-muted">Precio promedio</small>
                                </div>
                            </div>
                        `;
                    }
                    html += '</div>';
                    contenedor.innerHTML = html;
                });
            break;
        
        case 'clientes':
            fetch('/api/reportes/clientes-activos')
                .then(res => res.json())
                .then(data => {
                    contenedor.innerHTML = `
                        <div class="row g-4">
                            <div class="col-md-4">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Total Clientes</h6>
                                    <h3 class="fw-bold text-primary">${data.totalClientes}</h3>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Con Visitas</h6>
                                    <h3 class="fw-bold text-info">${data.clientesConVisitas}</h3>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="card card-proptech p-3 text-center">
                                    <h6 class="text-muted">Con Operaciones</h6>
                                    <h3 class="fw-bold text-success">${data.clientesConOperaciones}</h3>
                                </div>
                            </div>
                        </div>
                        <div class="mt-4 text-center">
                            <span class="badge bg-primary fs-6 px-4 py-2 rounded-pill">Tasa de Actividad: ${data.tasaActividad}</span>
                        </div>
                    `;
                });
            break;
        
        case 'anomalias':
            Promise.all([
                fetch('/api/anomalias/total').then(r => r.json()),
                fetch('/api/anomalias/visitas-sin-cierre').then(r => r.json()),
                fetch('/api/anomalias/sobrecarga-asesores').then(r => r.json())
            ]).then(([total, visitas, sobrecarga]) => {
                let html = `
                    <div class="alert alert-warning text-center mb-4">
                        <h4 class="fw-bold">⚠️ Total Anomalías Detectadas: ${total.totalAnomalias}</h4>
                    </div>
                    <h5 class="fw-bold mb-3">Visitas sin Cierre</h5>
                    <div class="row g-3 mb-4">
                `;
                if (visitas.length === 0) {
                    html += '<p class="text-muted">No se detectaron visitas sin cierre</p>';
                } else {
                    visitas.forEach(a => {
                        html += `
                            <div class="col-md-6">
                                <div class="card card-proptech p-3 border-warning">
                                    <p class="mb-1"><strong>${a.cliente}</strong></p>
                                    <small class="text-muted">${a.mensaje}</small>
                                </div>
                            </div>
                        `;
                    });
                }
                html += `
                    </div>
                    <h5 class="fw-bold mb-3">Sobrecarga de Asesores</h5>
                    <div class="row g-3">
                `;
                if (sobrecarga.length === 0) {
                    html += '<p class="text-muted">No se detectó sobrecarga de asesores</p>';
                } else {
                    sobrecarga.forEach(a => {
                        html += `
                            <div class="col-md-6">
                                <div class="card card-proptech p-3 border-warning">
                                    <p class="mb-1"><strong>${a.asesor}</strong></p>
                                    <small class="text-muted">${a.mensaje}</small>
                                </div>
                            </div>
                        `;
                    });
                }
                html += '</div>';
                contenedor.innerHTML = html;
            });
            break;
    }
}

// ---- FILTROS DE REPORTES ----

function aplicarFiltros() {
    // Se implementará cuando se tengan datos filtrables
    // Por ahora recargamos el reporte actual
    const activeTab = document.querySelector('#report-tabs .nav-link.active');
    if (activeTab) {
        const tabText = activeTab.textContent.toLowerCase();
        if (tabText.includes('rendimiento')) mostrarReporte('rendimiento');
        else if (tabText.includes('asesores')) mostrarReporte('asesores');
        else if (tabText.includes('precios')) mostrarReporte('precios');
        else if (tabText.includes('clientes')) mostrarReporte('clientes');
        else if (tabText.includes('anomalias')) mostrarReporte('anomalias');
    }
}

function limpiarFiltros() {
    document.getElementById('filtro-tipo').value = '';
    document.getElementById('filtro-zona').value = '';
    document.getElementById('filtro-precio-min').value = '';
    cargarReportes();
}

// ---- EXPORTACIÓN DE REPORTES ----

function exportarReporte() {
    // Obtener el contenido del reporte actual
    const contenido = document.getElementById('contenedor-reportes').innerHTML;
    
    // Crear una ventana para imprimir (solución simple para PDF)
    const ventana = window.open('', '_blank');
    ventana.document.write(`
        <html>
        <head>
            <title>Reporte Insignia Inmo</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <style>
                body { font-family: 'Inter', sans-serif; padding: 20px; }
                .card-proptech { border: 1px solid #ddd; border-radius: 12px; margin-bottom: 15px; }
            </style>
        </head>
        <body>
            <h1 class="fw-bold mb-4">Reporte Insignia Inmo - ${new Date().toLocaleDateString()}</h1>
            ${contenido}
        </body>
        </html>
    `);
    ventana.document.close();
    setTimeout(() => {
        ventana.print();
    }, 500);
}

// ---- MENSAJES DE ERROR Y ÉXITO ----

function mostrarError(mensaje) {
    // Crear toast de error
    const toast = document.createElement('div');
    toast.className = 'position-fixed bottom-0 end-0 p-3';
    toast.style.zIndex = '1055';
    toast.innerHTML = `
        <div class="toast show align-items-center text-bg-danger border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-exclamation-triangle me-2"></i>${mensaje}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" onclick="this.parentElement.parentElement.parentElement.remove()"></button>
            </div>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

function mostrarExito(mensaje) {
    const toast = document.createElement('div');
    toast.className = 'position-fixed bottom-0 end-0 p-3';
    toast.style.zIndex = '1055';
    toast.innerHTML = `
        <div class="toast show align-items-center text-bg-success border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-check-circle me-2"></i>${mensaje}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" onclick="this.parentElement.parentElement.parentElement.remove()"></button>
            </div>
        </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

function initApp() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (usuario.nombre) {
        const navNombre = document.getElementById('nombre-usuario-nav');
        if (navNombre) navNombre.textContent = usuario.nombre.split(' ')[0];
    }
    cargarInmuebles();
}

document.addEventListener("DOMContentLoaded", initApp);