package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.Collections;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    // Producto genérico de respaldo en caso de que el flujo quede vacío
    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L,
            "PRODUCTO GENERICO DE RESPALDO",
            "General",
            new BigDecimal("10.00"),
            Collections.singletonList("soporte@agrosmart.ec")
    );

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Flux<Producto> obtenerProductosComercializables() {
        // Mono.fromCallable: Difiere la consulta bloqueante a JPA para que no se ejecute hasta la suscripción
        return Mono.fromCallable(repository::findAll)
                // subscribeOn(Schedulers.boundedElastic()): Aísla la operación I/O bloqueante de JPA fuera del event loop de Netty
                .subscribeOn(Schedulers.boundedElastic())
                // flatMapMany: Convierte la lista síncrona materializada (List<ProductoEntity>) en un flujo asíncrono (Flux)
                .flatMapMany(Flux::fromIterable)
                // map: Transforma la entidad del ORM al modelo de dominio inmutable
                .map(ProductoMapper::toDominio)
                // map: Aplica la transformación funcional para convertir el nombre a mayúsculas retornando una nueva instancia
                .map(ProductoFilters.A_MAYUSCULAS)
                // filter: Filtra los productos descartando los que no cumplan la regla de negocio (precio > 0 y correos no vacíos)
                .filter(ProductoFilters.IS_VALID)
                // doOnNext: Permite ejecutar un efecto secundario no invasivo (log) sin alterar los elementos emitidos
                .doOnNext(ProductoFilters.LOG_PRODUCTO)
                // defaultIfEmpty: Emite un elemento por defecto si los filtros descartaron la totalidad del flujo
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {
        // Mono.fromCallable: Difiere la búsqueda síncrona de JPA
        return Mono.fromCallable(() -> repository.findById(id))
                // subscribeOn(Schedulers.boundedElastic()): Deriva el hilo de ejecución al pool elástico asignado a I/O
                .subscribeOn(Schedulers.boundedElastic())
                // flatMap(Mono::justOrEmpty): Transforma el Optional<ProductoEntity> de JPA en un Mono vacío si no existe
                .flatMap(Mono::justOrEmpty)
                // map: Convierte la entidad encontrada al modelo de dominio inmutable
                .map(ProductoMapper::toDominio)
                // switchIfEmpty: Maneja la ausencia de valor dentro del flujo reactivo sin usar llamadas bloqueantes, lanzando la excepción
                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)));
    }
}