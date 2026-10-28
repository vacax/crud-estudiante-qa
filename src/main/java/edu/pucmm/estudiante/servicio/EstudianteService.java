package edu.pucmm.estudiante.servicio;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import edu.pucmm.estudiante.dominio.EstudianteDuplicadoException;
import edu.pucmm.estudiante.dominio.EstudianteNoEncontradoException;
import edu.pucmm.estudiante.dominio.EstudianteSolicitud;
import edu.pucmm.estudiante.dominio.ValidadorEstudiante;
import edu.pucmm.estudiante.repositorio.EstudianteRepositorio;
import java.util.List;

/** Reglas de negocio del CRUD de estudiantes. Depende solo de la interfaz del repositorio. */
public class EstudianteService {

  private final EstudianteRepositorio repositorio;

  public EstudianteService(EstudianteRepositorio repositorio) {
    this.repositorio = repositorio;
  }

  public List<Estudiante> listar() {
    return repositorio.listar();
  }

  public Estudiante obtener(long id) {
    return repositorio.buscarPorId(id).orElseThrow(() -> new EstudianteNoEncontradoException(id));
  }

  public Estudiante crear(EstudianteSolicitud solicitud) {
    ValidadorEstudiante.validarOLanzar(solicitud);
    Estudiante nuevo = aEstudiante(null, solicitud);
    verificarNoDuplicado(nuevo, null);
    return repositorio.guardar(nuevo);
  }

  public Estudiante actualizar(long id, EstudianteSolicitud solicitud) {
    ValidadorEstudiante.validarOLanzar(solicitud);
    Estudiante actualizado = aEstudiante(id, solicitud);
    verificarNoDuplicado(actualizado, id);
    if (!repositorio.actualizar(actualizado)) {
      throw new EstudianteNoEncontradoException(id);
    }
    return actualizado;
  }

  public void eliminar(long id) {
    if (!repositorio.eliminar(id)) {
      throw new EstudianteNoEncontradoException(id);
    }
  }

  private void verificarNoDuplicado(Estudiante estudiante, Long excluirId) {
    if (repositorio.existeNombreEnCarrera(estudiante.nombre(), estudiante.carrera(), excluirId)) {
      throw new EstudianteDuplicadoException(estudiante.nombre(), estudiante.carrera());
    }
  }

  private static Estudiante aEstudiante(Long id, EstudianteSolicitud s) {
    return new Estudiante(id, s.nombre(), Carrera.desdeTexto(s.carrera()), s.correo());
  }
}
