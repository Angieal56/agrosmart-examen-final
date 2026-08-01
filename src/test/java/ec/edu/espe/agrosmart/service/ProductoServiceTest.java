package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class ProductoServiceTest {

    private ProductoEntity crearEntidad(Long id, String nombre, BigDecimal precio, String correos) {
        ProductoEntity entity = new ProductoEntity();
        entity.setIdProducto(id);
        entity.setNombreProducto(nombre);
        entity.setCategoria("Cafe");
        entity.setPrecioUsd(precio);
        entity.setCorreosNotificacion(correos);
        return entity;
    }

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirSoloLosValidos() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        List<ProductoEntity> entidades = List.of(
                crearEntidad(1L, "Cafe 1", new BigDecimal("10.00"), "a@mail.com"),
                crearEntidad(2L, "Cafe 2", new BigDecimal("12.00"), "b@mail.com"),
                crearEntidad(3L, "Cafe 3", new BigDecimal("15.00"), "c@mail.com"),
                crearEntidad(4L, "Invalido Precio", BigDecimal.ZERO, "d@mail.com"),
                crearEntidad(5L, "Invalido Correo", new BigDecimal("20.00"), "")
        );
        Mockito.when(repo.findAll()).thenReturn(entidades);
        ProductoService service = new ProductoService(repo, null);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        List<ProductoEntity> entidades = List.of(
                crearEntidad(4L, "Invalido Precio", BigDecimal.ZERO, "d@mail.com")
        );
        Mockito.when(repo.findAll()).thenReturn(entidades);
        ProductoService service = new ProductoService(repo, null);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextMatches(p -> p.getId().equals(0L) && p.getNombre().contains("GENERICO"))
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeLanzarExcepcion() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);
        Mockito.when(repo.findById(999L)).thenReturn(Optional.empty());
        ProductoService service = new ProductoService(repo, null);

        // Act
        Mono<Producto> mono = service.buscarPorId(999L);

        // Assert
        StepVerifier.create(mono)
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    @Test
    void generarPublicidad_cuandoCaminoFeliz_debeEmitirTextoPublicidad() {
        // Arrange
        AgroSmartAIService aiService = Mockito.mock(AgroSmartAIService.class);
        Mockito.when(aiService.generarPublicidad("Cafe", "Gourmet")).thenReturn("¡Prueba el mejor café!");
        ProductoService service = new ProductoService(null, aiService);

        // Act
        Mono<String> mono = service.generarPublicidad("Cafe", "Gourmet");

        // Assert
        StepVerifier.create(mono)
                .expectNext("¡Prueba el mejor café!")
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoElProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        AgroSmartAIService aiService = Mockito.mock(AgroSmartAIService.class);
        Mockito.when(aiService.generarPublicidad(any(), any()))
                .thenThrow(new RuntimeException("429 Too Many Requests"));
        ProductoService service = new ProductoService(null, aiService);

        // Act & Assert
        StepVerifier.create(service.generarPublicidad("Cacao", "Exportadores"))
                .expectNextMatches(texto -> texto.contains("Publicidad no disponible"))
                .verifyComplete();
    }
}