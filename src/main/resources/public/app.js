// Interfaz mínima del CRUD de estudiantes: fetch a /api/estudiantes, sin frameworks.
const API = "/api/estudiantes";
const CAMPOS = ["nombre", "carrera", "correo"];

const formulario = document.getElementById("formulario");
const cuerpoTabla = document.getElementById("cuerpo-tabla");
const mensajeError = document.getElementById("mensaje-error");
const mensajeExito = document.getElementById("mensaje-exito");
const mensajeVacio = document.getElementById("mensaje-vacio");
const titulo = document.getElementById("titulo-formulario");
const entradas = {
  nombre: document.getElementById("campo-nombre"),
  carrera: document.getElementById("select-carrera"),
  correo: document.getElementById("campo-correo"),
};

let idEnEdicion = null;

function mostrar(elemento, texto) {
  elemento.textContent = texto || "";
  elemento.hidden = !texto;
}

function limpiarErrores() {
  mostrar(mensajeError, "");
  CAMPOS.forEach((c) => {
    document.getElementById("error-" + c).textContent = "";
  });
}

function mostrarErrores(errores) {
  (errores || []).forEach((e) => {
    const destino = document.getElementById("error-" + e.campo);
    if (destino) {
      destino.textContent = e.mensaje;
    } else {
      mostrar(mensajeError, e.mensaje);
    }
  });
}

async function leerErrores(respuesta) {
  try {
    const cuerpo = await respuesta.json();
    return cuerpo.errores || [];
  } catch (e) {
    return [{ campo: "servidor", mensaje: "Error inesperado (" + respuesta.status + ")" }];
  }
}

function celda(texto) {
  const td = document.createElement("td");
  td.textContent = texto;
  return td;
}

function boton(texto, testid, alPulsar) {
  const b = document.createElement("button");
  b.type = "button";
  b.textContent = texto;
  b.dataset.testid = testid;
  b.addEventListener("click", alPulsar);
  return b;
}

function dibujarTabla(estudiantes) {
  cuerpoTabla.replaceChildren();
  estudiantes.forEach((e) => {
    const fila = document.createElement("tr");
    fila.dataset.testid = "fila-estudiante-" + e.id;
    fila.append(celda(e.id), celda(e.nombre), celda(e.carrera), celda(e.correo));
    const acciones = document.createElement("td");
    acciones.append(
      boton("Editar", "boton-editar-" + e.id, () => iniciarEdicion(e)),
      " ",
      boton("Eliminar", "boton-eliminar-" + e.id, () => eliminar(e.id)),
    );
    fila.append(acciones);
    cuerpoTabla.append(fila);
  });
  mensajeVacio.hidden = estudiantes.length > 0;
}

async function cargar() {
  const respuesta = await fetch(API);
  if (!respuesta.ok) {
    mostrar(mensajeError, "No se pudo cargar la lista de estudiantes");
    return;
  }
  dibujarTabla(await respuesta.json());
}

function reiniciarFormulario() {
  idEnEdicion = null;
  formulario.reset();
  titulo.textContent = "Nuevo estudiante";
  limpiarErrores();
}

function iniciarEdicion(e) {
  idEnEdicion = e.id;
  entradas.nombre.value = e.nombre;
  entradas.carrera.value = e.carrera;
  entradas.correo.value = e.correo;
  titulo.textContent = "Editar estudiante " + e.id;
  limpiarErrores();
  mostrar(mensajeExito, "");
}

async function guardar(evento) {
  evento.preventDefault();
  limpiarErrores();
  mostrar(mensajeExito, "");
  const cuerpo = {
    nombre: entradas.nombre.value,
    carrera: entradas.carrera.value,
    correo: entradas.correo.value,
  };
  const editando = idEnEdicion !== null;
  const respuesta = await fetch(editando ? API + "/" + idEnEdicion : API, {
    method: editando ? "PUT" : "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(cuerpo),
  });
  // Cualquier 2xx se considera éxito (no se exige un código exacto).
  if (respuesta.ok) {
    reiniciarFormulario();
    mostrar(mensajeExito, editando ? "Estudiante actualizado" : "Estudiante creado");
    await cargar();
  } else {
    mostrarErrores(await leerErrores(respuesta));
  }
}

async function eliminar(id) {
  limpiarErrores();
  mostrar(mensajeExito, "");
  const respuesta = await fetch(API + "/" + id, { method: "DELETE" });
  if (respuesta.ok) {
    if (idEnEdicion === id) {
      reiniciarFormulario();
    }
    mostrar(mensajeExito, "Estudiante eliminado");
    await cargar();
  } else {
    mostrarErrores(await leerErrores(respuesta));
  }
}

formulario.addEventListener("submit", guardar);
document.getElementById("boton-cancelar").addEventListener("click", reiniciarFormulario);
cargar();
