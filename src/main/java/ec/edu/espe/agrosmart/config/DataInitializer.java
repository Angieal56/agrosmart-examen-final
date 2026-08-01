package ec.edu.espe.agrosmart.config;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // 3 Productos Validos (precioUsd > 0 y con correos)
                ProductoEntity p1 = new ProductoEntity(null, "Cafe Arabigo Especial Loja", new BigDecimal("18.50"), 250, "Cafe", "ventas@cafeloja.ec,export@cafeloja.ec");
                ProductoEntity p2 = new ProductoEntity(null, "Cafe Premium Sucumbios", new BigDecimal("12.00"), 500, "Cafe", "pedidos@premium.ec");
                ProductoEntity p3 = new ProductoEntity(null, "Cafe de Altura Pichincha Organico", new BigDecimal("22.00"), 120, "Cafe", "contacto@cafepichincha.com");

                // 1 Producto Invalido (precioUsd = 0)
                ProductoEntity p4 = new ProductoEntity(null, "Cafe Muestra Gratis", new BigDecimal("0.00"), 50, "Cafe", "muestras@cafeec.com");

                // 1 Producto Invalido (sin correos de notificacion)
                ProductoEntity p5 = new ProductoEntity(null, "Cafe Arabigo Lote 1", new BigDecimal("15.00"), 80, "Cafe", "");

                repository.saveAll(List.of(p1, p2, p3, p4, p5));
                System.out.println(">>> Siembra de datos completado (5 productos de Cafe creados).");
            }
        };
    }
}