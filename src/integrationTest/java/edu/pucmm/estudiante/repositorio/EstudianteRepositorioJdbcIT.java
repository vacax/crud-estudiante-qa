package edu.pucmm.estudiante.repositorio;

import static edu.pucmm.estudiante.pruebas.EstudianteBuilder.unEstudiante;
import static org.assertj.core.api.Assertions.assertThat;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Repositorio JDBC de estudiantes (PostgreSQL real)")
class EstudianteRepositorioJdbcIT extends BasePostgresIT {

  private EstudianteRepositorioJdbc repositorio;

  @BeforeEach
  void crearRepositorio() {
    repositorio = new EstudianteRepositorioJdbc(DATA_SOURCE);
  }

  @Test
  @DisplayName("guardar asigna el id generado por la base de datos")
  void guardarGeneraId() {
    Estudiante guardado = repositorio.guardar(unEstudiante().construir());

    assertThat(guardado.id()).isEqualTo(1L);
  }

  @Test
  @DisplayName("los ids generados son crecientes")
  void idsCrecientes() {
    Estudiante primero = repositorio.guardar(unEstudiante().conNombre("Primero Uno").construir());
    Estudiante segundo = repositorio.guardar(unEstudiante().conNombre("Segundo Dos").construir());

    assertThat(segundo.id()).isGreaterThan(primero.id());
  }

  @Test
  @DisplayName("buscarPorId mapea cada campo leído de la base de datos")
  void buscarPorIdMapeaTodosLosCampos() {
    Estudiante guardado =
        repositorio.guardar(
            unEstudiante()
                .conNombre("María Núñez")
                .conCarrera("IIS")
                .conCorreo("maria.nunez@ejemplo.edu")
                .construir());

    Estudiante leido = repositorio.buscarPorId(guardado.id()).orElseThrow();

    // Campo a campo: así un mapeo defectuoso señala exactamente la columna rota.
    assertThat(leido.id()).isEqualTo(guardado.id());
    assertThat(leido.nombre()).isEqualTo("María Núñez");
    assertThat(leido.carrera()).isEqualTo(Carrera.IIS);
    assertThat(leido.correo()).isEqualTo("maria.nunez@ejemplo.edu");
    assertThat(leido).usingRecursiveComparison().isEqualTo(guardado);
  }

  @Test
  @DisplayName("buscarPorId de un id inexistente devuelve Optional vacío")
  void buscarInexistente() {
    assertThat(repositorio.buscarPorId(999L)).isEmpty();
  }

  @Test
  @DisplayName("listar devuelve todos los estudiantes ordenados por id con sus campos")
  void listarEnOrden() {
    Estudiante a =
        repositorio.guardar(unEstudiante().conNombre("Zoe Zeta").conCarrera("ICI").construir());
    Estudiante b =
        repositorio.guardar(
            unEstudiante()
                .conNombre("Ana Alfa")
                .conCarrera("ITT")
                .conCorreo("ana@alfa.edu")
                .construir());
    Estudiante c =
        repositorio.guardar(unEstudiante().conNombre("Mia Mu").conCarrera("IME").construir());

    assertThat(repositorio.listar())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(a, b, c);
    assertThat(repositorio.listar())
        .extracting(Estudiante::nombre)
        .containsExactly("Zoe Zeta", "Ana Alfa", "Mia Mu");
    assertThat(repositorio.listar())
        .extracting(Estudiante::carrera)
        .containsExactly(Carrera.ICI, Carrera.ITT, Carrera.IME);
    assertThat(repositorio.listar()).extracting(Estudiante::correo).contains("ana@alfa.edu");
  }

  @Test
  @DisplayName("listar sin datos devuelve una lista vacía")
  void listarVacio() {
    assertThat(repositorio.listar()).isEmpty();
  }

  @Test
  @DisplayName("actualizar un estudiante existente cambia todos sus campos y devuelve true")
  void actualizarExistente() {
    Estudiante guardado = repositorio.guardar(unEstudiante().construir());
    Estudiante cambios =
        unEstudiante()
            .conId(guardado.id())
            .conNombre("Luis Gómez")
            .conCarrera("IME")
            .conCorreo("luis@ejemplo.edu")
            .construir();

    assertThat(repositorio.actualizar(cambios)).isTrue();

    assertThat(repositorio.buscarPorId(guardado.id()).orElseThrow())
        .usingRecursiveComparison()
        .isEqualTo(cambios);
  }

  @Test
  @DisplayName("actualizar un id inexistente devuelve false")
  void actualizarInexistente() {
    assertThat(repositorio.actualizar(unEstudiante().conId(999L).construir())).isFalse();
  }

  @Test
  @DisplayName("eliminar un estudiante existente devuelve true y deja de aparecer")
  void eliminarExistente() {
    Estudiante guardado = repositorio.guardar(unEstudiante().construir());

    assertThat(repositorio.eliminar(guardado.id())).isTrue();

    assertThat(repositorio.buscarPorId(guardado.id())).isEmpty();
  }

  @Test
  @DisplayName("eliminar un id inexistente devuelve false")
  void eliminarInexistente() {
    assertThat(repositorio.eliminar(999L)).isFalse();
  }

  @Test
  @DisplayName("TRUNCATE ... RESTART IDENTITY reinicia los ids entre pruebas")
  void idsReiniciadosEntrePruebas() {
    assertThat(repositorio.guardar(unEstudiante().construir()).id()).isEqualTo(1L);
  }
}
