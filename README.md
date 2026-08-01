# 🌾 Universidad de las Fuerzas Armadas ESPE

## Examen Final Práctico — Programación Avanzada

### Caso integrador: **AgroSmart** — Plataforma de Comercialización Agrícola

# 🌾 AgroSmart - Examen Final de Programación Avanzada

**Autora:** Angie Nicole Alvarado  
**Institución:** Universidad de las Fuerzas Armadas ESPE  
**Carrera:** Ingeniería en Tecnologías de la Información  
**Puerto Asignado:** `8172`  
**Perfil Activo:** `prod`

---

## 🔑 Semilla Personal y Cálculo del Puerto

### 1. Datos de Identificación
- **Nombre Legal:** Angie Nicole Alvarado
- **Cédula:** *1726955972*
- **Nonce / Semilla Calculada:** `72`

### 2. Algoritmo de Cálculo de Semilla y Puerto
La semilla se obtiene extrayendo los dos últimos dígitos de la cédula de identidad. A partir de la semilla calculada (`72`), se asignan las variables de entorno y parámetros de red del sistema:

$$\text{Semilla} = 72$$
$$\text{Puerto Netty} = 8100 + \text{Semilla} = 8172$$
$$\text{Tabla PostgreSQL} = \text{tbl\_productos\_base\_}72$$

---

## 🚀 Instrucciones de Ejecución

### 1. Requisitos Previos
- **Java 21 LTS** (Amazon Corretto / OpenJDK)
- **PostgreSQL 18**
- **Maven 3.9+** (o el wrapper `./mvnw` incluido)

### 2. Configuración de la Base de Datos PostgreSQL
Crea la base de datos necesaria para el perfil de producción:

```sql
CREATE DATABASE agrosmart_db;
2. Configuración de la Base de Datos

Crear la base de datos:

CREATE DATABASE agrosmart_db;
3. Variables de Entorno (Opcional)

Si deseas sobrescribir las credenciales configuradas en la aplicación, puedes utilizar las siguientes variables de entorno.

Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="prod"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/agrosmart_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="tu_password"
Linux / macOS
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/agrosmart_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=tu_password
4. Compilación y Ejecución
Ejecutar pruebas
./mvnw clean test
Iniciar la aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

Si todo fue exitoso, en consola aparecerá:

Netty started on port(s): 8172 (http)
## Endpoints Disponibles
Método	Endpoint	Descripción	Respuesta
GET	/api/productos	Obtiene la lista de productos comercializables	application/json (Flux)
GET	/api/productos/{id}	Obtiene un producto por ID	application/json (Mono)
GET	/api/agrosmart/publicidad	Genera una frase publicitaria utilizando IA	text/plain (Mono)
## Ejemplos de Uso
1. Obtener todos los productos
curl.exe http://localhost:8172/api/productos

Respuesta

[
  {
    "id": 1,
    "nombre": "CAFÉ LOJA ESPECIAL",
    "categoria": "Café",
    "precioUsd": 12.50,
    "correosNotificacion": [
      "ventas@cafeloja.ec",
      "export@cafeloja.ec"
    ]
  },
  {
    "id": 2,
    "nombre": "CHOCOLATE AMARGO 70%",
    "categoria": "Cacao",
    "precioUsd": 4.50,
    "correosNotificacion": [
      "info@cacaoec.com"
    ]
  }
]
2. Obtener un producto por ID
curl.exe http://localhost:8172/api/productos/1

Respuesta

{
  "id": 1,
  "nombre": "CAFÉ LOJA ESPECIAL",
  "categoria": "Café",
  "precioUsd": 12.50,
  "correosNotificacion": [
    "ventas@cafeloja.ec",
    "export@cafeloja.ec"
  ]
}
3. Probar un error 404
curl.exe -i http://localhost:8172/api/productos/9999

Respuesta

HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error":"Producto con ID 9999 no fue encontrado en el sistema."
}
4. Generar publicidad con IA
curl.exe "http://localhost:8172/api/agrosmart/publicidad?producto=Cacao%20fino%20de%20aroma&audiencia=exportadores%20europeos"

Respuesta

Descubre la excelencia del Cacao Fino de Aroma ecuatoriano, seleccionado exclusivamente para los paladares más exigentes de Europa.
## Justificación de los Operadores Reactivos
Mono.fromCallable(...)

Envuelve operaciones bloqueantes (como consultas JPA o llamadas HTTP síncronas) para diferir su ejecución hasta que exista una suscripción al flujo reactivo.

flatMapMany(Flux::fromIterable)

Transforma una colección (List<ProductoEntity>) en un flujo reactivo (Flux) para procesar cada elemento individualmente.

map(...)

Realiza transformaciones uno a uno de manera síncrona, como convertir entidades JPA en objetos de dominio.

filter(...)

Aplica reglas de negocio para permitir únicamente productos válidos, por ejemplo:

Precio mayor que cero.
Correos electrónicos válidos.
doOnNext(...)

Ejecuta efectos secundarios, como registrar información en logs, sin modificar los datos del flujo.

defaultIfEmpty(...)

Retorna un producto genérico cuando el flujo queda vacío después de aplicar los filtros.

switchIfEmpty(...)

Cuando no existe un producto para el ID solicitado, cambia el flujo hacia:

Mono.error(new ProductoNoEncontradoException(id))

permitiendo devolver un HTTP 404.

timeout(Duration.ofSeconds(30))

Cancela automáticamente la llamada a la IA si supera los 30 segundos de espera.

onErrorResume(...)

Captura excepciones (timeouts, errores de red o fallas de la IA) y genera una respuesta alternativa para evitar errores HTTP 500.

## Puente Bloqueante -> Reactivo con boundedElastic
El problema

Spring WebFlux trabaja sobre Netty utilizando un número reducido de hilos (Event Loop) capaces de atender miles de solicitudes concurrentes.

Si una consulta JPA o una llamada HTTP síncrona se ejecuta directamente sobre estos hilos, el Event Loop queda bloqueado, reduciendo drásticamente la capacidad de atender nuevas solicitudes.

## La solución

Para evitar este problema se utiliza:

Mono.fromCallable(...)
    .subscribeOn(Schedulers.boundedElastic())
¿Qué logra esta estrategia?
  -Aislamiento de hilos

Las operaciones bloqueantes se ejecutan en un pool especializado (boundedElastic) y no en el Event Loop principal.

  -Evaluación diferida

La operación solo comienza cuando existe un suscriptor, respetando el paradigma reactivo.

  -Mayor resiliencia

Mientras la consulta bloqueante se ejecuta en un hilo elástico, Netty permanece libre para seguir procesando nuevas peticiones HTTP, mejorando la escalabilidad y el rendimiento de la aplicación.

## Tecnologías Utilizadas
Java 21
Spring Boot 3
Spring WebFlux
Reactor Core
Spring Data JPA
PostgreSQL
Maven
Netty
LangChain4j
Docker (opcional)






