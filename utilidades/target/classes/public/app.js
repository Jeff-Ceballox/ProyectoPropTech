// =============================================
// Insignia Inmo — Frontend Application
// =============================================

function initApp() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (usuario.nombre) {
        const navNombre = document.getElementById('nombre-usuario-nav');
        if (navNombre) navNombre.textContent = usuario.nombre.split(' ')[0];
    }

    // Ocultar secciones según rol
    const rol = (usuario.rol || 'cliente').toLowerCase();
    const btnOperaciones = document.getElementById('btn-nav-operaciones');
    const btnReportes = document.getElementById('btn-nav-reportes');
    const btnRegistrarCliente = document.getElementById('btn-registrar-cliente');
    const btnNuevaOperacion = document.getElementById('btn-nueva-operacion');

    if (rol === 'cliente') {
        if (btnOperaciones) btnOperaciones.style.display = 'none';
        if (btnReportes) btnReportes.style.display = 'none';
        if (btnRegistrarCliente) btnRegistrarCliente.style.display = 'none';
        if (btnNuevaOperacion) btnNuevaOperacion.style.display = 'none';
    }

    const btnPendientes = document.getElementById('btn-nav-pendientes');
    if (rol === 'admin') {
        if (btnPendientes) btnPendientes.style.display = 'inline-block';
    } else {
        if (btnPendientes) btnPendientes.style.display = 'none';
    }

    // Dropdown items según rol
    const miVisitasItem = document.getElementById('dropdown-mis-visitas');
    const misVisitasAsignadasItem = document.getElementById('dropdown-visitas-asignadas');
    const dashboardItem = document.getElementById('dropdown-dashboard');
    if (miVisitasItem) miVisitasItem.style.display = 'block';
    if (misVisitasAsignadasItem) misVisitasAsignadasItem.style.display = (rol === 'vendedor' || rol === 'admin') ? 'block' : 'none';
    if (dashboardItem) dashboardItem.style.display = (rol === 'vendedor' || rol === 'admin') ? 'block' : 'none';

    if (usuario.email) {
        cargarFavoritos();
    }
    cargarInmuebles();
}

document.addEventListener("DOMContentLoaded", initApp);

// ---- NAVEGACIÓN ENTRE SECCIONES ----

function mostrarSeccion(seccion, skipLoad) {
    const secciones = ['inmuebles', 'clientes', 'operaciones', 'reportes', 'pendientes'];
    secciones.forEach(s => {
        const secEl = document.getElementById('seccion-' + s);
        if (secEl) secEl.style.display = s === seccion ? 'block' : 'none';
        const busEl = document.getElementById('buscador-' + s);
        if (busEl) busEl.style.display = s === seccion ? 'block' : 'none';
        const navEl = document.getElementById('btn-nav-' + s);
        if (navEl) navEl.classList.toggle('active', s === seccion);
    });
    // Limpiar resultado de búsqueda al cambiar sección
    const resBusqueda = document.getElementById('resultado-busqueda');
    if (resBusqueda) resBusqueda.innerHTML = '';

    if (skipLoad) return;

    // Cargar datos según la sección
    if (seccion === 'inmuebles') cargarInmuebles();
    if (seccion === 'clientes') cargarClientes();
    if (seccion === 'operaciones') cargarOperaciones();
    if (seccion === 'reportes') cargarReportes();
    if (seccion === 'pendientes') cargarPendientes();
}

// ---- INMUEBLES ----

function cargarInmuebles() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const estaLogueado = !!usuario.email;
    const rol = (usuario.rol || '').toLowerCase();
    const puedeEditar = rol === 'admin' || rol === 'vendedor';

    const btnAgregar = document.getElementById('btn-agregar-inmueble');
    if (btnAgregar) btnAgregar.style.display = puedeEditar ? 'inline-block' : 'none';

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

                const esFav = window.favoritos && window.favoritos.includes(inmueble.codigo);
                const corazon = esFav ? '❤️' : '🤍';
                const favActive = esFav ? 'active' : '';

                const tarjeta = `
                    <div class="col-md-4 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-proptech shadow-sm h-100">
                            <div class="card-header-gradient text-white py-3 px-4 d-flex justify-content-between align-items-center">
                                <div>
                                    <h5 class="mb-1 fw-bold">${inmueble.tipo}</h5>
                                    <small class="text-white-50">📍 ${inmueble.direccion}</small>
                                </div>
                                ${estaLogueado ? `
                                <button class="btn-fav ${favActive}" onclick="toggleFavorito('${inmueble.codigo}', this)" title="Favorito">${corazon}</button>
                                ` : ''}
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
                            <div class="card-footer bg-white border-0 p-3">
                                <div class="d-flex gap-2">
                                    <button class="btn btn-nav active rounded-pill fw-bold flex-grow-1 shadow-sm"
                                            onclick="verDetalleInmueble('${inmueble.codigo}')">
                                        Ver Detalles
                                    </button>
                                    ${puedeEditar ? `
                                    <button class="btn btn-custom-outline rounded-pill px-3" onclick="editarInmueble('${inmueble.codigo}')" title="Editar">✏️</button>
                                    <button class="btn btn-outline-danger rounded-pill px-3" onclick="eliminarInmueble('${inmueble.codigo}')" title="Eliminar">🗑️</button>
                                    ` : ''}
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
            document.getElementById('contenedor-inmuebles').innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor. Verifica que Java esté corriendo.</p>
                </div>
            `;
        });
}

// ---- FAVORITOS ----

window.favoritos = [];

function cargarFavoritos() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!usuario.email) return;
    fetch(`/api/favoritos?email=${encodeURIComponent(usuario.email)}`)
        .then(res => res.json())
        .then(data => {
            window.favoritos = data || [];
        })
        .catch(() => {});
}

function toggleFavorito(codigoInmueble, btn) {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!usuario.email) {
        mostrarError('Debes iniciar sesión para guardar favoritos.');
        return;
    }

    fetch(`/api/favoritos/${encodeURIComponent(codigoInmueble)}?email=${encodeURIComponent(usuario.email)}`, {
        method: 'POST'
    })
    .then(res => res.json())
    .then(data => {
        if (data.favorito) {
            if (!window.favoritos.includes(codigoInmueble)) {
                window.favoritos.push(codigoInmueble);
            }
            btn.textContent = '❤️';
            btn.classList.add('active');
            mostrarExito('✅ Agregado a favoritos');
        } else {
            window.favoritos = window.favoritos.filter(f => f !== codigoInmueble);
            btn.textContent = '🤍';
            btn.classList.remove('active');
            mostrarExito('🗑 Quitado de favoritos');
        }
    })
    .catch(err => {
        mostrarError('Error al actualizar favorito: ' + err.message);
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

            const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
            const footer = document.getElementById('detalle-inmueble-footer');
            if (footer) {
                if (usuario.email) {
                    footer.innerHTML = `
                        <button type="button" class="btn btn-secondary rounded-pill" data-bs-dismiss="modal">Cerrar</button>
                        <button type="button" class="btn btn-primary rounded-pill fw-bold px-4 shadow-sm"
                            onclick="abrirModalAgendarVisita('${inmueble.codigo}')">
                            📅 Agendar Visita
                        </button>
                    `;
                } else {
                    footer.innerHTML = `
                        <button type="button" class="btn btn-secondary rounded-pill" data-bs-dismiss="modal">Cerrar</button>
                    `;
                }
            }

            new bootstrap.Modal(document.getElementById('modalDetalleInmueble')).show();
        })
        .catch(err => console.error('Error al cargar detalle:', err));
}

// ---- AGENDAR VISITA ----

function abrirModalAgendarVisita(codigoInmueble) {
    document.getElementById('visita-inmueble-codigo').value = codigoInmueble;
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const info = document.getElementById('visita-cliente-info');
    if (usuario.nombre) {
        info.innerHTML = `<small>📌 La visita se registrará a nombre de <strong>${usuario.nombre}</strong> (${usuario.email}).</small>`;
    }
    new bootstrap.Modal(document.getElementById('modalAgendarVisita')).show();
}

function registrarVisita() {
    const codigoInmueble = document.getElementById('visita-inmueble-codigo').value.trim();
    const fechaHoraRaw = document.getElementById('visita-fecha').value;
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');

    if (!codigoInmueble || !fechaHoraRaw) {
        mostrarError('Selecciona una fecha y hora para la visita.');
        return;
    }
    if (!usuario.email) {
        mostrarError('Debes iniciar sesión para agendar una visita.');
        return;
    }

    const fechaHora = fechaHoraRaw.replace('T', ' ') + ':00';

    fetch('/api/visitas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            codigoInmueble: codigoInmueble,
            fechaHora: fechaHora,
            emailCliente: usuario.email
        })
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al programar la visita');
        return res.json();
    })
    .then(data => {
        bootstrap.Modal.getInstance(document.getElementById('modalAgendarVisita')).hide();
        document.getElementById('visita-fecha').value = '';
        mostrarExito('✅ Visita programada para el ' + data.fechaHora);
    })
    .catch(err => {
        mostrarError('Error al agendar visita: ' + err.message);
    });
}

// ---- MIS VISITAS ----

function mostrarMisVisitas() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!usuario.email) {
        mostrarError('Debes iniciar sesión para ver tus visitas.');
        return;
    }

    mostrarSeccion('inmuebles', true);

    const contenedor = document.getElementById('contenedor-inmuebles');
    contenedor.innerHTML = '<div class="text-center text-muted"><div class="spinner-border text-primary mb-2"></div><p>Cargando tus visitas...</p></div>';
    document.getElementById('contador-inmuebles').textContent = 'Mis Visitas';

    fetch(`/api/visitas/cliente/${encodeURIComponent(usuario.email)}`)
        .then(res => res.json())
        .then(visitas => {
            contenedor.innerHTML = '';
            document.getElementById('contador-inmuebles').textContent = visitas.length + ' visita(s)';

            if (visitas.length === 0) {
                contenedor.innerHTML = `
                    <div class="col-12 text-center text-muted py-5">
                        <h4>📅 No tienes visitas agendadas</h4>
                        <p>Busca un inmueble y agenda una cita para verlo.</p>
                    </div>`;
                return;
            }

            visitas.forEach((v, idx) => {
                const colorEstado = v.estado === 'Pendiente' ? 'bg-warning text-dark' :
                                    v.estado === 'Realizada' ? 'bg-success' : 'bg-secondary';
                const puedeCancelar = v.estado === 'Pendiente';

                const tarjeta = `
                    <div class="col-md-6 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-proptech shadow-sm h-100">
                            <div class="card-header-gradient text-white py-3 px-4">
                                <h5 class="mb-1 fw-bold">📅 ${v.fechaHora}</h5>
                                <small class="text-white-50">ID: ${v.idVisita}</small>
                            </div>
                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="badge ${colorEstado} rounded-pill px-3 py-1">${v.estado}</span>
                                </div>
                                <hr class="my-3">
                                <div class="d-flex flex-column gap-2 small">
                                    <span>🏠 Inmueble: ${v.inmueble ? v.inmueble.tipo + ' (' + v.inmueble.codigo + ')' : v.codigoInmueble}</span>
                                    <span>📍 ${v.inmueble ? v.inmueble.direccion : ''}</span>
                                    <span>💼 Asesor: ${v.asesor ? v.asesor.nombre : 'Por asignar'}</span>
                                </div>
                            </div>
                            ${puedeCancelar ? `
                            <div class="card-footer bg-white border-0 p-3 text-center">
                                <button class="btn btn-outline-danger rounded-pill fw-bold px-4"
                                    onclick="cancelarVisita('${v.idVisita}')">
                                    🗑 Cancelar Visita
                                </button>
                            </div>` : ''}
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });
        })
        .catch(err => {
            contenedor.innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error</h5>
                    <p>No se pudieron cargar tus visitas.</p>
                </div>`;
        });
}

function cancelarVisita(idVisita) {
    if (!confirm('¿Estás seguro de cancelar esta visita?')) return;

    fetch(`/api/visitas/${encodeURIComponent(idVisita)}/cancelar`, {
        method: 'PUT'
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al cancelar');
        return res.json();
    })
    .then(data => {
        mostrarExito('✅ Visita cancelada exitosamente');
        mostrarMisVisitas();
    })
    .catch(err => {
        mostrarError('Error al cancelar visita: ' + err.message);
    });
}

// ---- CLIENTES ----

function cargarClientes() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const rol = (usuario.rol || '').toLowerCase();
    const puedeEditar = rol === 'admin' || rol === 'vendedor';
    const contenedor = document.getElementById('contenedor-clientes');
    contenedor.innerHTML = '<div class="col-12 text-center text-muted"><div class="spinner-border text-primary mb-2" role="status"></div><p>Cargando clientes...</p></div>';

    fetch('/api/clientes')
        .then(res => {
            if (!res.ok) throw new Error("Servidor rechazó la conexión");
            return res.json();
        })
        .then(clientes => {
            contenedor.innerHTML = '';
            document.getElementById('contador-clientes').textContent = clientes.length + ' registrados';

            if (clientes.length === 0) {
                const esAdmin = rol === 'admin';
                contenedor.innerHTML = `
                    <div class="col-12 text-center text-muted py-5">
                        <h4>👥 No hay clientes registrados aún</h4>
                        <p>Usa el botón "Registrar Cliente" para agregar el primero.</p>
                        ${esAdmin ? `
                        <button class="btn btn-custom rounded-pill fw-bold px-4 mt-3" onclick="sembrarClientes()">
                            🌱 Re-sembrar datos de prueba
                        </button>` : ''}
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
                                    <span>📞 ${cliente.telefono || '(sin teléfono)'}</span>
                                    <span>📧 ${cliente.email || '(sin email)'}</span>
                                </div>
                                ${puedeEditar ? `
                                <hr class="my-3">
                                <div class="d-flex gap-2">
                                    <button class="btn btn-custom-outline btn-sm rounded-pill flex-grow-1" onclick="editarCliente('${cliente.identificacion}')">✏️ Editar</button>
                                    <button class="btn btn-outline-danger btn-sm rounded-pill" onclick="eliminarCliente('${cliente.identificacion}', '${cliente.nombre.replace(/'/g, "\\'")}')">🗑️</button>
                                </div>` : ''}
                            </div>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });
        })
        .catch(error => {
            console.error("Error:", error);
            contenedor.innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error de conexión</h5>
                    <p>No se pudo conectar con el servidor: ${error.message}</p>
                    <button class="btn btn-custom rounded-pill fw-bold px-4 mt-2" onclick="cargarClientes()">🔄 Reintentar</button>
                </div>
            `;
        });
}

function sembrarClientes() {
    const btn = event.target;
    btn.disabled = true;
    btn.textContent = 'Sembrando...';
    fetch('/api/clientes/seed', { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            mostrarExito('✅ ' + data.mensaje);
            cargarClientes();
        })
        .catch(err => {
            mostrarError('Error: ' + err.message);
            btn.disabled = false;
            btn.textContent = '🌱 Re-sembrar datos de prueba';
        });
}

// ---- VISITAS ASIGNADAS (ASESOR) ----

function mostrarVisitasAsignadas() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!usuario.email) {
        mostrarError('Debes iniciar sesión.');
        return;
    }

    mostrarSeccion('inmuebles', true);

    const contenedor = document.getElementById('contenedor-inmuebles');
    contenedor.innerHTML = '<div class="text-center text-muted"><div class="spinner-border text-primary mb-2"></div><p>Cargando visitas asignadas...</p></div>';
    document.getElementById('contador-inmuebles').textContent = 'Mis Visitas Asignadas';

    // Necesitamos obtener el ID del asesor. Hacemos fetch al dashboard primero.
    fetch(`/api/asesor/dashboard?email=${encodeURIComponent(usuario.email)}`)
        .then(res => {
            if (!res.ok) throw new Error('No se encontró perfil de asesor');
            return res.json();
        })
        .then(dash => {
            return fetch(`/api/visitas/asesor/${encodeURIComponent(dash.idAsesor)}`);
        })
        .then(res => res.json())
        .then(visitas => {
            contenedor.innerHTML = '';
            document.getElementById('contador-inmuebles').textContent = visitas.length + ' visita(s) asignadas';

            if (visitas.length === 0) {
                contenedor.innerHTML = `
                    <div class="col-12 text-center text-muted py-5">
                        <h4>📋 No tienes visitas asignadas</h4>
                        <p>Las visitas aparecerán aquí cuando los clientes agenden citas.</p>
                    </div>`;
                return;
            }

            visitas.forEach((v, idx) => {
                const colorEstado = v.estado === 'Pendiente' ? 'bg-warning text-dark' :
                                    v.estado === 'Confirmada' ? 'bg-info text-dark' :
                                    v.estado === 'Realizada' ? 'bg-success' : 'bg-secondary';
                const puedeConfirmar = v.estado === 'Pendiente';
                const puedeRealizar = v.estado === 'Confirmada';
                const puedeCancelar = v.estado === 'Pendiente' || v.estado === 'Confirmada';

                const tarjeta = `
                    <div class="col-md-6 fade-in" style="animation-delay: ${idx * 0.1}s">
                        <div class="card card-proptech shadow-sm h-100">
                            <div class="card-header-gradient text-white py-3 px-4">
                                <h5 class="mb-1 fw-bold">📅 ${v.fechaHora}</h5>
                                <small class="text-white-50">ID: ${v.idVisita}</small>
                            </div>
                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="badge ${colorEstado} rounded-pill px-3 py-1">${v.estado}</span>
                                </div>
                                <hr class="my-3">
                                <div class="d-flex flex-column gap-2 small">
                                    <span>👤 Cliente: ${v.cliente ? v.cliente.nombre + ' (' + v.cliente.email + ')' : 'N/A'}</span>
                                    <span>🏠 Inmueble: ${v.inmueble ? v.inmueble.tipo + ' (' + v.inmueble.codigo + ')' : v.codigoInmueble}</span>
                                    <span>📍 ${v.inmueble ? v.inmueble.direccion : ''}</span>
                                </div>
                            </div>
                            <div class="card-footer bg-white border-0 p-3 text-center">
                                <div class="d-flex gap-2 justify-content-center">
                                    ${puedeConfirmar ? `<button class="btn btn-success btn-sm rounded-pill px-3" onclick="confirmarVisita('${v.idVisita}')">✅ Confirmar</button>` : ''}
                                    ${puedeRealizar ? `<button class="btn btn-primary btn-sm rounded-pill px-3" onclick="realizarVisita('${v.idVisita}')">🎯 Realizada</button>` : ''}
                                    ${puedeCancelar ? `<button class="btn btn-outline-danger btn-sm rounded-pill px-3" onclick="cancelarVisitaAsignada('${v.idVisita}')">🗑 Cancelar</button>` : ''}
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                contenedor.innerHTML += tarjeta;
            });
        })
        .catch(err => {
            contenedor.innerHTML = `
                <div class="alert alert-danger text-center w-100 shadow-sm rounded-4">
                    <h5>❌ Error</h5>
                    <p>${err.message}</p>
                </div>`;
        });
}

function confirmarVisita(idVisita) {
    fetch(`/api/visitas/${encodeURIComponent(idVisita)}/confirmar`, { method: 'PUT' })
        .then(res => { if (!res.ok) throw new Error('Error al confirmar'); return res.json(); })
        .then(data => { mostrarExito('✅ Visita confirmada'); mostrarVisitasAsignadas(); })
        .catch(err => mostrarError('Error: ' + err.message));
}

function realizarVisita(idVisita) {
    fetch(`/api/visitas/${encodeURIComponent(idVisita)}/realizar`, { method: 'PUT' })
        .then(res => { if (!res.ok) throw new Error('Error al marcar realizada'); return res.json(); })
        .then(data => { mostrarExito('✅ Visita marcada como realizada'); mostrarVisitasAsignadas(); })
        .catch(err => mostrarError('Error: ' + err.message));
}

function cancelarVisitaAsignada(idVisita) {
    if (!confirm('¿Cancelar esta visita?')) return;
    fetch(`/api/visitas/${encodeURIComponent(idVisita)}/cancelar`, { method: 'PUT' })
        .then(res => { if (!res.ok) throw new Error('Error al cancelar'); return res.json(); })
        .then(data => { mostrarExito('🗑 Visita cancelada'); mostrarVisitasAsignadas(); })
        .catch(err => mostrarError('Error: ' + err.message));
}

// ---- DASHBOARD ASESOR ----

function mostrarDashboard() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!usuario.email) {
        mostrarError('Debes iniciar sesión.');
        return;
    }

    mostrarSeccion('inmuebles', true);

    const contenedor = document.getElementById('contenedor-inmuebles');
    contenedor.innerHTML = '<div class="text-center"><div class="spinner-border text-primary mb-2"></div><p>Cargando dashboard...</p></div>';
    document.getElementById('contador-inmuebles').textContent = 'Dashboard';

    fetch(`/api/asesor/dashboard?email=${encodeURIComponent(usuario.email)}`)
        .then(res => {
            if (!res.ok) throw new Error('No se encontró perfil de asesor');
            return res.json();
        })
        .then(d => {
            contenedor.innerHTML = `
                <div class="col-12">
                    <div class="card card-proptech shadow-sm">
                        <div class="card-header-gradient text-white py-3 px-4">
                            <h4 class="mb-0 fw-bold">📊 Dashboard: ${d.nombre}</h4>
                            <small>${d.especialidad || 'Sin especialidad'}</small>
                        </div>
                        <div class="card-body p-4">
                            <div class="row g-4">
                                <div class="col-md-3 col-6">
                                    <div class="text-center p-3 rounded-4" style="background: var(--color-fondo-claro);">
                                        <div class="fs-1 fw-bold" style="color: var(--color-primario);">${d.operacionesCerradas}</div>
                                        <small class="text-muted">Operaciones cerradas</small>
                                    </div>
                                </div>
                                <div class="col-md-3 col-6">
                                    <div class="text-center p-3 rounded-4" style="background: var(--color-fondo-claro);">
                                        <div class="fs-1 fw-bold" style="color: var(--color-secundario);">$${d.totalVentas.toFixed(1)}M</div>
                                        <small class="text-muted">Total en ventas</small>
                                    </div>
                                </div>
                                <div class="col-md-3 col-6">
                                    <div class="text-center p-3 rounded-4" style="background: var(--color-fondo-claro);">
                                        <div class="fs-1 fw-bold" style="color: var(--color-primario);">${d.visitasAtendidas}</div>
                                        <small class="text-muted">Visitas realizadas</small>
                                    </div>
                                </div>
                                <div class="col-md-3 col-6">
                                    <div class="text-center p-3 rounded-4" style="background: var(--color-fondo-claro);">
                                        <div class="fs-1 fw-bold" style="color: var(--color-secundario);">${d.visitasPendientes}</div>
                                        <small class="text-muted">Visitas pendientes</small>
                                    </div>
                                </div>
                            </div>
                            <hr class="my-4">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <p><strong>📧 Email:</strong> ${d.email}</p>
                                    <p><strong>📞 Teléfono:</strong> ${d.telefono || 'N/A'}</p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>⭐ Calificación:</strong> ${d.calificacion || 'N/A'} / 5.0</p>
                                    <p><strong>🆔 ID:</strong> ${d.idAsesor}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>`;
        })
        .catch(err => {
            contenedor.innerHTML = `
                <div class="alert alert-warning text-center rounded-4 shadow-sm">
                    <h5>⚠️ No hay dashboard disponible</h5>
                    <p>${err.message}</p>
                    <p>Para usar el dashboard, debes tener un perfil de asesor registrado en la base de datos.</p>
                </div>`;
        });
}

// ---- CRUD INMUEBLES (ASESOR/ADMIN) ----

function editarInmueble(codigo) {
    fetch(`/api/inmuebles/${encodeURIComponent(codigo)}`)
        .then(res => res.json())
        .then(inm => {
            document.getElementById('edit-inm-codigo').value = inm.codigo;
            document.getElementById('edit-inm-codigo-original').value = inm.codigo;
            document.getElementById('edit-inm-tipo').value = inm.tipo || '';
            document.getElementById('edit-inm-direccion').value = inm.direccion || '';
            document.getElementById('edit-inm-precio').value = inm.precio || 0;
            document.getElementById('edit-inm-area').value = inm.area || 0;
            document.getElementById('edit-inm-estado').value = inm.estado || 'Disponible';
            document.getElementById('edit-inm-habitaciones').value = inm.habitaciones || 0;
            document.getElementById('edit-inm-banos').value = inm.banos || 0;
            document.getElementById('edit-inm-parqueadero').checked = inm.tieneParqueadero || false;
            document.getElementById('edit-inm-descripcion').value = inm.descripcion || '';
            new bootstrap.Modal(document.getElementById('modalEditarInmueble')).show();
        })
        .catch(err => mostrarError('Error al cargar inmueble: ' + err.message));
}

function guardarInmuebleEditado() {
    const codigo = document.getElementById('edit-inm-codigo-original').value;
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const datos = {
        codigo: codigo,
        tipo: document.getElementById('edit-inm-tipo').value,
        direccion: document.getElementById('edit-inm-direccion').value,
        precio: parseFloat(document.getElementById('edit-inm-precio').value) || 0,
        area: parseFloat(document.getElementById('edit-inm-area').value) || 0,
        estado: document.getElementById('edit-inm-estado').value,
        habitaciones: parseInt(document.getElementById('edit-inm-habitaciones').value) || 0,
        banos: parseInt(document.getElementById('edit-inm-banos').value) || 0,
        tieneParqueadero: document.getElementById('edit-inm-parqueadero').checked,
        descripcion: document.getElementById('edit-inm-descripcion').value
    };

    const params = usuario.email ? `?email=${encodeURIComponent(usuario.email)}` : '';

    fetch(`/api/inmuebles/${encodeURIComponent(codigo)}${params}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(datos)
    })
    .then(res => {
        if (!res.ok) return res.json().then(err => { throw new Error(err.error || 'Error al actualizar'); });
        return res.json();
    })
    .then(data => {
        mostrarExito('✅ Inmueble actualizado');
        bootstrap.Modal.getInstance(document.getElementById('modalEditarInmueble')).hide();
        cargarInmuebles();
    })
    .catch(err => mostrarError('Error: ' + err.message));
}

function eliminarInmueble(codigo) {
    if (!confirm(`¿Eliminar el inmueble ${codigo}? Esta acción no se puede deshacer.`)) return;
    fetch(`/api/inmuebles/${encodeURIComponent(codigo)}`, { method: 'DELETE' })
        .then(res => { if (!res.ok) throw new Error('Error al eliminar'); return res.json(); })
        .then(data => { mostrarExito('🗑 Inmueble eliminado'); cargarInmuebles(); })
        .catch(err => mostrarError('Error: ' + err.message));
}

function abrirModalNuevoInmueble() {
    document.getElementById('nuevo-inm-codigo').value = '';
    document.getElementById('nuevo-inm-tipo').value = '';
    document.getElementById('nuevo-inm-estado').value = 'Disponible';
    document.getElementById('nuevo-inm-direccion').value = '';
    document.getElementById('nuevo-inm-precio').value = '';
    document.getElementById('nuevo-inm-area').value = '';
    document.getElementById('nuevo-inm-habitaciones').value = '';
    document.getElementById('nuevo-inm-banos').value = '';
    document.getElementById('nuevo-inm-parqueadero').checked = false;
    document.getElementById('nuevo-inm-descripcion').value = '';
    new bootstrap.Modal(document.getElementById('modalNuevoInmueble')).show();
}

function guardarNuevoInmueble() {
    const datos = {
        codigo: document.getElementById('nuevo-inm-codigo').value.trim(),
        tipo: document.getElementById('nuevo-inm-tipo').value.trim(),
        estado: document.getElementById('nuevo-inm-estado').value,
        direccion: document.getElementById('nuevo-inm-direccion').value.trim(),
        precio: parseFloat(document.getElementById('nuevo-inm-precio').value) || 0,
        area: parseFloat(document.getElementById('nuevo-inm-area').value) || 0,
        habitaciones: parseInt(document.getElementById('nuevo-inm-habitaciones').value) || 0,
        banos: parseInt(document.getElementById('nuevo-inm-banos').value) || 0,
        tieneParqueadero: document.getElementById('nuevo-inm-parqueadero').checked,
        descripcion: document.getElementById('nuevo-inm-descripcion').value.trim()
    };

    if (!datos.codigo || !datos.tipo || !datos.direccion) {
        mostrarError('Código, Tipo y Dirección son obligatorios');
        return;
    }

    fetch('/api/inmuebles', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(datos)
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al crear inmueble');
        return res.json();
    })
    .then(data => {
        mostrarExito('✅ Inmueble creado: ' + data.codigo);
        bootstrap.Modal.getInstance(document.getElementById('modalNuevoInmueble')).hide();
        cargarInmuebles();
    })
    .catch(err => mostrarError('Error: ' + err.message));
}

// ---- CRUD CLIENTES (ASESOR/ADMIN) ----

function editarCliente(id) {
    fetch(`/api/clientes/${encodeURIComponent(id)}`)
        .then(res => res.json())
        .then(cli => {
            document.getElementById('edit-cli-id').value = cli.identificacion;
            document.getElementById('edit-cli-id-original').value = cli.identificacion;
            document.getElementById('edit-cli-nombre').value = cli.nombre || '';
            document.getElementById('edit-cli-telefono').value = cli.telefono || '';
            document.getElementById('edit-cli-email').value = cli.email || '';
            document.getElementById('edit-cli-presupuesto').value = cli.presupuestoMaximo || 0;
            new bootstrap.Modal(document.getElementById('modalEditarCliente')).show();
        })
        .catch(err => mostrarError('Error al cargar cliente: ' + err.message));
}

function guardarClienteEditado() {
    const idOriginal = document.getElementById('edit-cli-id-original').value;
    const datos = {
        identificacion: document.getElementById('edit-cli-id').value,
        nombre: document.getElementById('edit-cli-nombre').value,
        telefono: document.getElementById('edit-cli-telefono').value,
        email: document.getElementById('edit-cli-email').value,
        presupuestoMaximo: parseFloat(document.getElementById('edit-cli-presupuesto').value) || 0
    };

    fetch(`/api/clientes/${encodeURIComponent(idOriginal)}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(datos)
    })
    .then(res => {
        if (!res.ok) throw new Error('Error al actualizar');
        return res.json();
    })
    .then(data => {
        mostrarExito('✅ Cliente actualizado');
        bootstrap.Modal.getInstance(document.getElementById('modalEditarCliente')).hide();
        cargarClientes();
    })
    .catch(err => mostrarError('Error: ' + err.message));
}

function eliminarCliente(id, nombre) {
    if (!confirm(`¿Eliminar a "${nombre}" (${id})? Esta acción no se puede deshacer.`)) return;
    fetch(`/api/clientes/${encodeURIComponent(id)}`, { method: 'DELETE' })
        .then(res => { if (!res.ok) throw new Error('Error al eliminar'); return res.json(); })
        .then(data => { mostrarExito('🗑 Cliente eliminado'); cargarClientes(); })
        .catch(err => mostrarError('Error: ' + err.message));
}

function buscarClientePorId(id) {
    if (!id) {
        id = document.getElementById('input-buscar-cliente').value.trim();
    }
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
        mostrarError('Identificación y nombre son obligatorios.');
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
        mostrarError('Error al registrar cliente: ' + err.message);
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

// ---- APROBACIONES (ADMIN) ----

function cargarPendientes() {
    const contenedor = document.getElementById('contenedor-pendientes');
    contenedor.innerHTML = '<div class="text-center text-muted"><p>Cargando cambios pendientes...</p></div>';

    fetch('/api/admin/cambios-pendientes')
        .then(res => {
            if (!res.ok) throw new Error('Error del servidor');
            return res.json();
        })
        .then(cambios => {
            if (cambios.length === 0) {
                contenedor.innerHTML = `
                    <div class="alert alert-success text-center rounded-4 shadow-sm">
                        <h5>✅ No hay cambios pendientes de aprobación</h5>
                    </div>
                `;
                return;
            }

            // Agrupar por usuario
            const grupos = {};
            cambios.forEach(c => {
                if (!grupos[c.email]) grupos[c.email] = {nombre: c.nombreUsuario, email: c.email, cambios: []};
                grupos[c.email].cambios.push(c);
            });

            let html = '';
            Object.values(grupos).forEach(grupo => {
                html += `
                    <div class="card card-proptech shadow-sm mb-4">
                        <div class="card-header-gradient text-white py-3 px-4">
                            <h5 class="mb-0 fw-bold">👤 ${grupo.nombre}</h5>
                            <small>${grupo.email}</small>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Campo</th>
                                            <th>Valor Actual</th>
                                            <th>Valor Nuevo</th>
                                            <th>Fecha</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                `;
                grupo.cambios.forEach(c => {
                    const campoLabel = {nombre: 'Nombre', telefono: 'Teléfono', direccion: 'Dirección', email: 'Email'}[c.campo] || c.campo;
                    html += `
                        <tr>
                            <td><strong>${campoLabel}</strong></td>
                            <td class="text-muted">${c.valorAnterior || '(vacío)'}</td>
                            <td><span class="text-success fw-bold">${c.valorNuevo}</span></td>
                            <td><small class="text-muted">${c.fechaSolicitud}</small></td>
                            <td>
                                <button class="btn btn-success btn-sm rounded-pill px-3 me-1" onclick="aprobarCambio(${c.id}, this)">
                                    ✅ Aprobar
                                </button>
                                <button class="btn btn-outline-danger btn-sm rounded-pill px-3" onclick="rechazarCambio(${c.id}, this)">
                                    ❌ Rechazar
                                </button>
                            </td>
                        </tr>
                    `;
                });
                html += `
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                `;
            });
            contenedor.innerHTML = html;
        })
        .catch(err => {
            contenedor.innerHTML = `
                <div class="alert alert-danger text-center rounded-4 shadow-sm">
                    <h5>❌ Error al cargar cambios pendientes</h5>
                    <p>${err.message}</p>
                </div>
            `;
        });
}

function aprobarCambio(idCambio, btn) {
    const admin = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!admin.email) {
        mostrarError('Debes iniciar sesión como administrador.');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Aprobando...';

    fetch(`/api/admin/cambios-pendientes/${idCambio}/aprobar?adminEmail=${encodeURIComponent(admin.email)}`, {
        method: 'PUT'
    })
    .then(res => res.json())
    .then(data => {
        mostrarExito('✅ Cambio aprobado: ' + data.campo);
        cargarPendientes();
    })
    .catch(err => {
        mostrarError('Error al aprobar: ' + err.message);
        btn.disabled = false;
        btn.textContent = '✅ Aprobar';
    });
}

function rechazarCambio(idCambio, btn) {
    const admin = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (!admin.email) {
        mostrarError('Debes iniciar sesión como administrador.');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Rechazando...';

    fetch(`/api/admin/cambios-pendientes/${idCambio}/rechazar?adminEmail=${encodeURIComponent(admin.email)}`, {
        method: 'PUT'
    })
    .then(res => res.json())
    .then(data => {
        mostrarExito('🗑 Cambio rechazado');
        cargarPendientes();
    })
    .catch(err => {
        mostrarError('Error al rechazar: ' + err.message);
        btn.disabled = false;
        btn.textContent = '❌ Rechazar';
    });
}