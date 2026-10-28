package edu.pucmm.estudiante.servicio;

import static edu.pucmm.estudiante.pruebas.EstudianteBuilder.unEstudiante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
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
}
