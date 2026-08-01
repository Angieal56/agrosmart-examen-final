package ec.edu.espe.agrosmart.mapper;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductoMapper {

    public static Producto toDominio(ProductoEntity entity) {
        if (entity == null) {
            return null;
        }

        List<String> correosList;
        if (entity.getCorreosNotificacion() == null || entity.getCorreosNotificacion().trim().isEmpty()) {
            correosList = Collections.emptyList();
        } else {
            correosList = Arrays.stream(entity.getCorreosNotificacion().split(","))
                    .map(String::trim)
                    .filter(correo -> !correo.isEmpty())
                    .collect(Collectors.toList());
        }

        return new Producto(
                entity.getIdProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.getPrecioUsd(),
                correosList
        );
    }
}