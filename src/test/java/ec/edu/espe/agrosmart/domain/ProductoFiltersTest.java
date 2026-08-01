package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoFiltersTest {

    @Test
    void isValid_conProductoValido_debeRetornarTrue() {
        // Arrange
        Producto productoValido = new Producto(1L, "Cafe", "Cafe", new BigDecimal("10.00"), List.of("cliente@mail.com"));

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(productoValido);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto productoInvalido = new Producto(2L, "Gratis", "Cafe", BigDecimal.ZERO, List.of("cliente@mail.com"));

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(productoInvalido);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_conCorreosVacios_debeRetornarFalse() {
        // Arrange
        Producto productoInvalido = new Producto(3L, "Sin Correo", "Cafe", new BigDecimal("15.00"), Collections.emptyList());

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(productoInvalido);

        // Assert
        assertFalse(resultado);
    }
}