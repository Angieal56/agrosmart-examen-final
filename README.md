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








> **Modalidad:** Individual · **Online**
> **Sesión sincrónica:** el día del examen, a la hora indicada en Moodle
> **🔴 Plazo de entrega: viernes 31 de julio de 2026, 23:00** (hora de Ecuador)
> **Trabajo estimado:** ~4 horas efectivas
> **Entrega en Moodle:** únicamente la URL pública de tu repositorio de GitHub
> (el video de defensa se declara **dentro** del repo, en `IDENTIDAD.md`)
> **Puntaje:** 40 puntos → **Nota final = puntaje ÷ 2 (sobre 20)**

> 🕐 **Sobre el tiempo.** El examen inicia en una **sesión sincrónica**, pero tienes hasta
> el día siguiente para entregar. Los tiempos por fase que verás más adelante son
> **estimaciones para que te organices**, no un cronómetro: puedes desarrollar en varias
> sesiones, descansar y retomar. Lo que **sí** se evalúa es que el trabajo esté
> **distribuido en commits** y no aparezca todo de golpe al final.

---

## 📌 Índice

1. [Qué se evalúa y de dónde viene](#1-qué-se-evalúa-y-de-dónde-viene)
2. [Reglas de integridad académica](#2-reglas-de-integridad-académica-léelas-antes-de-escribir-código)
3. [Tu semilla personal](#3-tu-semilla-personal-obligatoria)
4. [El caso: AgroSmart](#4-el-caso-agrosmart)
5. [Arquitectura exigida](#5-arquitectura-exigida)
6. [Fases del examen](#6-fases-del-examen)
   - [Fase 0 — Identidad y arranque](#fase-0--identidad-y-arranque-10-min)
   - [Fase 1 — Configuración y perfiles](#fase-1--configuración-y-perfiles-25-min)
   - [Fase 2 — Persistencia con JPA/Hibernate](#fase-2--persistencia-con-jpahibernate-30-min)
   - [Fase 3 — Modelo inmutable y lógica funcional](#fase-3--modelo-inmutable-y-lógica-funcional-25-min)
   - [Fase 4 — Servicio reactivo y aislamiento del bloqueo](#fase-4--servicio-reactivo-y-aislamiento-del-bloqueo-40-min)
   - [Fase 5 — Módulo de IA con LangChain4j](#fase-5--módulo-de-ia-con-langchain4j-25-min)
   - [Fase 6 — API reactiva con WebFlux](#fase-6--api-reactiva-con-webflux-20-min)
   - [Fase 7 — Pruebas unitarias](#fase-7--pruebas-unitarias-40-min)
   - [Fase 8 — Integración, documentación y defensa](#fase-8--integración-documentación-y-defensa-25-min)
7. [Entregables](#7-entregables)
8. [Rúbrica de evaluación](#8-rúbrica-de-evaluación-40-puntos)
9. [Penalizaciones y bonus](#9-penalizaciones-y-bonus)
10. [Anexos técnicos](#10-anexos-técnicos)

> 🔴 **Fecha límite de entrega: viernes 31 de julio de 2026, 23:00.**

---

## 1. Qué se evalúa y de dónde viene

Este examen es **acumulativo**. Fusiona en un solo proyecto los dos bloques trabajados
durante el semestre:

| Origen | Temas que aporta a este examen |
|--------|-------------------------------|
| **Examen Parcial — EduSmart** | Perfiles de Spring (`application-prod.properties`), ORM con JPA/Hibernate (`@Entity`, `@Table`, `@Id`, `IDENTITY`, `unique`, `length`, `BigDecimal`), integración de IA con **LangChain4j** (`@AiService`, `@UserMessage`, `@V`), endpoint **GET** con parámetros por URL, commits semánticos secuenciales |
| **Tarea Práctica — MediTrack** | **Inmutabilidad** con copias defensivas, **programación funcional** (`Predicate`, `Consumer`), **Project Reactor** (`Mono`/`Flux`, `filter`, `map`, `doOnNext`, `defaultIfEmpty`, `switchIfEmpty`), controlador **WebFlux** no bloqueante, **JUnit + `StepVerifier`** con patrón AAA, rama por actividad, evidencia con `curl` y `mvn test` |

**Lo nuevo que integra este examen** (y que es el corazón de la evaluación): JPA/Hibernate
y LangChain4j son **bloqueantes**, mientras que WebFlux corre sobre un *event loop* de Netty que **no se puede bloquear**. Tu trabajo es hacerlos convivir correctamente aislando lo bloqueante en `Schedulers.boundedElastic()`. Quien bloquee el event loop pierde el criterio más pesado de la rúbrica.

### Resultados de aprendizaje

1. **RA1.** Configurar perfiles de ejecución y conectar una aplicación Spring Boot a
   PostgreSQL mediante el ORM JPA/Hibernate.
2. **RA2.** Diseñar un modelo de dominio **100 % inmutable** con copias defensivas y
   lógica funcional con interfaces funcionales de Java.
3. **RA3.** Construir un flujo **reactivo no bloqueante** con Project Reactor que
   consuma datos de un origen bloqueante sin comprometer el event loop.
4. **RA4.** Integrar un modelo de lenguaje mediante **LangChain4j** con contrato
   declarativo y manejo reactivo de fallos.
5. **RA5.** Verificar el comportamiento con **JUnit 5** y **`StepVerifier`** bajo el
   patrón AAA.
6. **RA6.** Evidenciar el proceso completo con **ramas, commits semánticos y trazabilidad
   verificable**, y **sustentar oralmente** las decisiones de diseño tomadas.

---

## 2. Reglas de integridad académica (léelas ANTES de escribir código)

Este examen es **online**. No se te pide que demuestres que no usaste IA: se te pide que
demuestres que **entiendes y puedes sustentar cada línea que entregas**. Los siguientes
controles son **obligatorios** y su incumplimiento tiene penalización explícita
(ver [sección 9](#9-penalizaciones-y-bonus)).

### 2.1 Sobre el uso de asistentes de IA

- El producto que construyes **sí** integra IA (LangChain4j). Eso es parte del examen.
- Usar un asistente (ChatGPT, Copilot, Gemini, Claude…) para que **escriba tu código por   ti** está prohibido y se detecta en la defensa oral.
- **No existe penalización por consultar documentación oficial.** Sí la hay por no poder explicar tu propio código.

### 2.2 Control 1 — Commit inicial con el nonce

Durante la **sesión sincrónica** el docente publica un **código aleatorio** (*nonce*),
por ejemplo `AGS-7F4K-2026`. Antes de que termine esa sesión debes hacer tu **primer
commit** con el archivo `IDENTIDAD.md`, que contiene ese código, tu nombre completo, tu
cédula y tu semilla personal.

Es lo único que se te pide en un horario fijo, y toma **menos de 5 minutos**: crear el
repositorio, copiar la plantilla `IDENTIDAD.md`, llenarla y hacer `push`. Después
trabajas a tu ritmo hasta el plazo del día siguiente.

> Este commit es lo que ancla tu trabajo a la sesión del examen. Un repositorio cuyo
> primer commit aparece recién al día siguiente **no acredita** haber iniciado en la
> sesión evaluada.

### 2.3 Control 2 — Trabajo distribuido en commits

- **Mínimo 9 commits** (uno por fase, como mínimo), hechos **a medida que avanzas**.
- **Historial lineal.** Prohibido `git push --force`, `git rebase -i` para reescribir
  fechas, y prohibido *squash* al integrar.
- Se auditará con:
  ```bash
  git log --format='%h | %ad | %cd | %s' --date=iso
  ```
----------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------
