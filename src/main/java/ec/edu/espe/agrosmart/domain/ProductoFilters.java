package ec.edu.espe.agrosmart.domain;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProductoFilters {

    public static final Predicate<Producto> IS_VALID = producto ->
            producto != null
                    && producto.getPrecioUsd() != null
                    && producto.getPrecioUsd().compareTo(BigDecimal.ZERO) > 0
                    && producto.getCorreosNotificacion() != null
                    && !producto.getCorreosNotificacion().isEmpty();

    public static final Consumer<Producto> LOG_PRODUCTO = producto ->
            System.out.println("[AgroSmart Log] Procesando ID: " + producto.getId() + " - Nombre: " + producto.getNombre());

    public static final Function<Producto, Producto> A_MAYUSCULAS = producto ->
            new Producto(
                    producto.getId(),
                    producto.getNombre() != null ? producto.getNombre().toUpperCase() : null,
                    producto.getCategoria(),
                    producto.getPrecioUsd(),
                    producto.getCorreosNotificacion()
            );
}