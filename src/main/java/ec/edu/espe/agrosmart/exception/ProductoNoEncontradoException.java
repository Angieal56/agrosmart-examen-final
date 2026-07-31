package ec.edu.espe.agrosmart.exception;

public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("Producto con ID " + id + " no fue encontrado en el sistema.");
    }
}