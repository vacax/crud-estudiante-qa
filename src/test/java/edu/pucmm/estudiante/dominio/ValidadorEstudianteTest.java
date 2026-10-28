package edu.pucmm.estudiante.dominio;

import static edu.pucmm.estudiante.pruebas.EstudianteBuilder.unEstudiante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Validador de estudiante")
class ValidadorEstudianteTest {

  private static List<ErrorValidacion> validarNombre(String nombre) {
    return ValidadorEstudiante.validar(unEstudiante().conNombre(nombre).solicitud());
  }

  @Test
  @DisplayName("una solicitud válida no produce errores")
  void solicitudValida() {
    assertThat(ValidadorEstudiante.validar(unEstudiante().solicitud())).isEmpty();
  }

  @ParameterizedTest(name = "nombre de {0} caracteres es válido")
  @DisplayName("nombres con longitud en el límite permitido son válidos")
  @ValueSource(ints = {2, 3, 99, 100})
  void nombreLongitudValida(int longitud) {
    assertThat(validarNombre("a".repeat(longitud))).isEmpty();
  }

  @ParameterizedTest(name = "nombre de {0} caracteres es inválido")
  @DisplayName("nombres fuera del rango 2..100 son inválidos")
  @ValueSource(ints = {1, 101, 500})
  void nombreLongitudInvalida(int longitud) {
    assertThat(validarNombre("a".repeat(longitud)))
        .containsExactly(
            new ErrorValidacion("nombre", "El nombre debe tener entre 2 y 100 caracteres"));
  }

  @ParameterizedTest(name = "nombre null o vacío ({0}) es obligatorio")
  @DisplayName("nombre null o vacío es obligatorio")
  @NullAndEmptySource
  void nombreObligatorio(String nombre) {
    assertThat(validarNombre(nombre))
        .containsExactly(new ErrorValidacion("nombre", "El nombre es obligatorio"));
  }

  @ParameterizedTest(name = "[{0}] tiene espacios en los bordes")
  @DisplayName("nombres con espacios al inicio o al final son inválidos")
  @ValueSource(strings = {" Ana", "Ana ", " Ana ", "\tAna", "Ana\n"})
  void nombreConEspaciosEnBordes(String nombre) {
    assertThat(validarNombre(nombre))
        .containsExactly(
            new ErrorValidacion(
                "nombre", "El nombre no debe tener espacios al inicio ni al final"));
  }

  @Test
  @DisplayName("espacios internos en el nombre son válidos")
  void nombreConEspaciosInternos() {
    assertThat(validarNombre("Ana María de los Santos")).isEmpty();
  }

  @ParameterizedTest(name = "carrera {0} pertenece al catálogo")
  @DisplayName("todas las carreras del catálogo son válidas")
  @ValueSource(strings = {"ISC", "ITT", "IME", "IIS", "ICI"})
  void carreraValida(String carrera) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCarrera(carrera).solicitud()))
        .isEmpty();
  }

  @ParameterizedTest(name = "carrera [{0}] no pertenece al catálogo")
  @DisplayName("carreras fuera del catálogo o en minúsculas son inválidas")
  @ValueSource(strings = {"XXX", "isc", "Isc", "ISC ", "MED", "INFORMATICA"})
  void carreraFueraDelCatalogo(String carrera) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCarrera(carrera).solicitud()))
        .containsExactly(
            new ErrorValidacion("carrera", "La carrera no pertenece al catálogo: " + carrera));
  }

  @ParameterizedTest(name = "carrera null o vacía ({0}) es obligatoria")
  @DisplayName("carrera null o vacía es obligatoria")
  @NullAndEmptySource
  void carreraObligatoria(String carrera) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCarrera(carrera).solicitud()))
        .containsExactly(new ErrorValidacion("carrera", "La carrera es obligatoria"));
  }

  @ParameterizedTest(name = "correo [{0}] es válido")
  @DisplayName("correos con formato válido son aceptados")
  @ValueSource(
      strings = {
        "a@b.co",
        "ana.perez@ejemplo.edu",
        "ana+qa@ejemplo.edu.do",
        "A_B-C%d@sub.dominio.com"
      })
  void correoValido(String correo) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCorreo(correo).solicitud())).isEmpty();
  }

  @ParameterizedTest(name = "correo [{0}] es inválido")
  @DisplayName("correos con formato inválido son rechazados")
  @ValueSource(
      strings = {
        "sinarroba",
        "@ejemplo.edu",
        "ana@",
        "ana@ejemplo",
        "ana@ejemplo.c",
        "ana perez@ejemplo.edu",
        "ana@@ejemplo.edu",
        "ana@ejemplo..edu",
        " ana@ejemplo.edu"
      })
  void correoInvalido(String correo) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCorreo(correo).solicitud()))
        .containsExactly(new ErrorValidacion("correo", "El correo no tiene un formato válido"));
  }

  @ParameterizedTest(name = "correo null o vacío ({0}) es obligatorio")
  @DisplayName("correo null o vacío es obligatorio")
  @NullAndEmptySource
  void correoObligatorio(String correo) {
    assertThat(ValidadorEstudiante.validar(unEstudiante().conCorreo(correo).solicitud()))
        .containsExactly(new ErrorValidacion("correo", "El correo es obligatorio"));
  }

  @ParameterizedTest(name = "nombre=[{0}] carrera=[{1}] correo=[{2}] -> {3} errores")
  @DisplayName("acumula todos los errores en lugar de detenerse en el primero")
  @CsvSource(
      nullValues = "NULL",
      value = {
        "NULL, NULL, NULL, 3",
        "a, XXX, malo, 3",
        "Ana, XXX, malo, 2",
        "Ana, ISC, malo, 1",
        "Ana, ISC, a@b.co, 0"
      })
  void acumulaErrores(String nombre, String carrera, String correo, int esperados) {
    var solicitud = new EstudianteSolicitud(nombre, carrera, correo);
    assertThat(ValidadorEstudiante.validar(solicitud)).hasSize(esperados);
  }

  @Test
  @DisplayName("validarOLanzar lanza ValidacionException con todos los errores")
  void validarOLanzarConErrores() {
    var solicitud = new EstudianteSolicitud(null, null, null);
    assertThatThrownBy(() -> ValidadorEstudiante.validarOLanzar(solicitud))
        .isInstanceOfSatisfying(
            ValidacionException.class,
            e ->
                assertThat(e.getErrores())
                    .extracting(ErrorValidacion::campo)
                    .containsExactly("nombre", "carrera", "correo"));
  }

  @Test
  @DisplayName("validarOLanzar no lanza nada con una solicitud válida")
  void validarOLanzarSinErrores() {
    ValidadorEstudiante.validarOLanzar(unEstudiante().solicitud());
  }
}
