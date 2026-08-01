package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void getters_cuandoSeInstancia_debenRetornarValoresIniciales() {
        // Arrange
        Long id = 1L;
        String nombre = "Cafe Especial";
        String categoria = "Cafe";
        BigDecimal precio = new BigDecimal("15.50");
        List<String> correos = List.of("contacto@agrosmart.ec");

        // Act
        Producto producto = new Producto(id, nombre, categoria, precio, correos);

        // Assert
        assertEquals(id, producto.getId());
        assertEquals(nombre, producto.getNombre());
        assertEquals(categoria, producto.getCategoria());
        assertEquals(precio, producto.getPrecioUsd());
        assertEquals(1, producto.getCorreosNotificacion().size());
    }

    @Test
    void constructor_alMutarListaEntrada_noDebeAfectarEstadoInterno() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("ventas@agrosmart.ec");
        Producto producto = new Producto(1L, "Cacao fino", "Cacao", new BigDecimal("120.50"), correosOriginales);

        // Act
        correosOriginales.add("intruso@mail.com");

        // Assert (Copia defensiva de ENTRADA)
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertNotSame(correosOriginales, producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alIntentarMutarSalida_debeLanzarExcepcion() {
        // Arrange
        Producto producto = new Producto(1L, "Cafe", "Cafe", new BigDecimal("10.00"), List.of("info@agrosmart.ec"));

        // Act & Assert (Copia defensiva de SALIDA)
        List<String> correosRetornados = producto.getCorreosNotificacion();
        assertThrows(UnsupportedOperationException.class, () -> correosRetornados.add("intruso@mail.com"));
    }
}