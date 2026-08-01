package ec.edu.espe.agrosmart.controller;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.service.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class AgroSmartController {

    private final ProductoService service;

    public AgroSmartController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/api/productos")
    public Flux<Producto> obtenerProductos() {
        return service.obtenerProductosComercializables();
    }

    @GetMapping("/api/productos/{id}")
    public Mono<Producto> obtenerPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping(value = "/api/agrosmart/publicidad", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> generarPublicidad(@RequestParam String producto, @RequestParam String audiencia) {
        return service.generarPublicidad(producto, audiencia);
    }
}