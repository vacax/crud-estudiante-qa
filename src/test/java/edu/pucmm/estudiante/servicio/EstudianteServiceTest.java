package edu.pucmm.estudiante.servicio;

import static edu.pucmm.estudiante.pruebas.EstudianteBuilder.unEstudiante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import edu.pucmm.estudiante.dominio.EstudianteDuplicadoException;
import edu.pucmm.estudiante.dominio.EstudianteNoEncontradoException;
import edu.pucmm.estudiante.dominio.ValidacionException;
import edu.pucmm.estudiante.repositorio.EstudianteRepositorio;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de estudiantes")
class EstudianteServiceTest {

  @Mock private EstudianteRepositorio repositorio;
  @InjectMocks private EstudianteService servicio;

  @Test
  @DisplayName("listar delega en el repositorio")
  void listar() {
    var existentes = List.of(unEstudiante().conId(1L).construir());
    when(repositorio.listar()).thenReturn(existentes);

    assertThat(servicio.listar()).isEqualTo(existentes);
  }

  @Test
  @DisplayName("obtener devuelve el estudiante existente")
  void obtenerExistente() {
    var estudiante = unEstudiante().conId(7L).construir();
    when(repositorio.buscarPorId(7L)).thenReturn(Optional.of(estudiante));

    assertThat(servicio.obtener(7L)).isEqualTo(estudiante);
  }

  @Test
  @DisplayName("obtener un id inexistente lanza EstudianteNoEncontradoException")
  void obtenerInexistente() {
    when(repositorio.buscarPorId(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> servicio.obtener(99L))
        .isInstanceOfSatisfying(
            EstudianteNoEncontradoException.class, e -> assertThat(e.getId()).isEqualTo(99L));
  }

  @Test
  @DisplayName("crear un estudiante válido lo guarda en el repositorio y devuelve el id asignado")
  void crearValido() {
    var sinId = unEstudiante().construir();
    var conId = unEstudiante().conId(1L).construir();
    when(repositorio.guardar(sinId)).thenReturn(conId);

    Estudiante creado = servicio.crear(unEstudiante().solicitud());

    assertThat(creado).isEqualTo(conId);
    assertThat(creado.carrera()).isEqualTo(Carrera.ISC);
    verify(repositorio).guardar(sinId);
  }

  @Test
  @DisplayName("crear un estudiante inválido lanza ValidacionException y no toca el repositorio")
  void crearInvalido() {
    var invalida = unEstudiante().conNombre("a").conCarrera("XXX").solicitud();

    assertThatThrownBy(() -> servicio.crear(invalida))
        .isInstanceOfSatisfying(
            ValidacionException.class, e -> assertThat(e.getErrores()).hasSize(2));

    verify(repositorio, never()).guardar(org.mockito.ArgumentMatchers.any());
    verifyNoInteractions(repositorio);
  }

  @Test
  @DisplayName("actualizar un estudiante existente devuelve los datos nuevos con su id")
  void actualizarExistente() {
    var esperado = unEstudiante().conId(5L).conCarrera("IIS").construir();
    when(repositorio.actualizar(esperado)).thenReturn(true);

    var resultado = servicio.actualizar(5L, unEstudiante().conCarrera("IIS").solicitud());

    assertThat(resultado).isEqualTo(esperado);
  }

  @Test
  @DisplayName("actualizar un id inexistente lanza EstudianteNoEncontradoException")
  void actualizarInexistente() {
    when(repositorio.actualizar(unEstudiante().conId(99L).construir())).thenReturn(false);

    assertThatThrownBy(() -> servicio.actualizar(99L, unEstudiante().solicitud()))
        .isInstanceOf(EstudianteNoEncontradoException.class);
  }

  @Test
  @DisplayName("actualizar con datos inválidos lanza ValidacionException y no toca el repositorio")
  void actualizarInvalido() {
    var invalida = unEstudiante().conCorreo("malo").solicitud();

    assertThatThrownBy(() -> servicio.actualizar(1L, invalida))
        .isInstanceOf(ValidacionException.class);

    verifyNoInteractions(repositorio);
  }

  @Test
  @DisplayName("eliminar un estudiante existente delega en el repositorio")
  void eliminarExistente() {
    when(repositorio.eliminar(3L)).thenReturn(true);

    servicio.eliminar(3L);

    verify(repositorio).eliminar(3L);
  }

  @Test
  @DisplayName("eliminar un id inexistente lanza EstudianteNoEncontradoException")
  void eliminarInexistente() {
    when(repositorio.eliminar(99L)).thenReturn(false);

    assertThatThrownBy(() -> servicio.eliminar(99L))
        .isInstanceOf(EstudianteNoEncontradoException.class);
  }

  // ---- Regla: no se permiten dos estudiantes con el mismo nombre en la misma carrera ----

  @Test
  @DisplayName("crear con nombre repetido en la misma carrera lanza EstudianteDuplicadoException")
  void crearDuplicado() {
    when(repositorio.existeNombreEnCarrera("Ana Pérez", Carrera.ISC, null)).thenReturn(true);

    assertThatThrownBy(() -> servicio.crear(unEstudiante().solicitud()))
        .isInstanceOf(EstudianteDuplicadoException.class);

    verify(repositorio, never()).guardar(any());
  }

  @Test
  @DisplayName("crear sin duplicado consulta la regla y guarda")
  void crearSinDuplicado() {
    var sinId = unEstudiante().construir();
    when(repositorio.existeNombreEnCarrera("Ana Pérez", Carrera.ISC, null)).thenReturn(false);
    when(repositorio.guardar(sinId)).thenReturn(unEstudiante().conId(1L).construir());

    servicio.crear(unEstudiante().solicitud());

    verify(repositorio).existeNombreEnCarrera("Ana Pérez", Carrera.ISC, null);
    verify(repositorio).guardar(sinId);
  }

  @Test
  @DisplayName("crear con el mismo nombre en otra carrera se permite")
  void crearMismoNombreOtraCarrera() {
    var sinId = unEstudiante().conCarrera("ITT").construir();
    when(repositorio.existeNombreEnCarrera("Ana Pérez", Carrera.ITT, null)).thenReturn(false);
    when(repositorio.guardar(sinId)).thenReturn(unEstudiante().conId(2L).construir());

    servicio.crear(unEstudiante().conCarrera("ITT").solicitud());

    verify(repositorio).existeNombreEnCarrera("Ana Pérez", Carrera.ITT, null);
    verify(repositorio).guardar(sinId);
  }

  @Test
  @DisplayName(
      "actualizar con nombre y carrera de otro estudiante lanza EstudianteDuplicadoException")
  void actualizarDuplicadoDeOtro() {
    when(repositorio.existeNombreEnCarrera("Ana Pérez", Carrera.ISC, 5L)).thenReturn(true);

    assertThatThrownBy(() -> servicio.actualizar(5L, unEstudiante().solicitud()))
        .isInstanceOf(EstudianteDuplicadoException.class);

    verify(repositorio, never()).actualizar(any());
  }

  @Test
  @DisplayName("actualizar conservando su propio nombre y carrera no lanza (se excluye a sí mismo)")
  void actualizarConservandoSuPropioNombre() {
    var esperado = unEstudiante().conId(5L).construir();
    when(repositorio.existeNombreEnCarrera("Ana Pérez", Carrera.ISC, 5L)).thenReturn(false);
    when(repositorio.actualizar(esperado)).thenReturn(true);

    var resultado = servicio.actualizar(5L, unEstudiante().solicitud());

    assertThat(resultado).isEqualTo(esperado);
    verify(repositorio).existeNombreEnCarrera("Ana Pérez", Carrera.ISC, 5L);
  }
}
