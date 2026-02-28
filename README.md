# ZonaFit Spring - Gestión de Clientes

Aplicación Spring Boot para la gestión de clientes de un gimnasio.

## Descripción

ZonaFit es una API REST construida con Spring Boot para administrar clientes y sus membresías.

Funcionalidades principales:

- CRUD completo de clientes.
- Asociación opcional de membresía al cliente.
- Tipo de membresía manejado con `enum` (`SILVER`, `GOLD`, `BRONZE`).
- Persistencia del `enum` como `STRING` en base de datos.
- Expiración de membresía por cliente (`membresiaExpiraEn`).
- Middleware centralizado para manejo de errores.
- Suite de pruebas unitarias e integración.

## Tecnología

- **Framework**: Spring Boot 3.2.2
- **Lenguaje**: Java 21
- **ORM**: JPA/Hibernate
- **Base de datos (tests)**: H2 en memoria
- **Testing**: JUnit 5 + Mockito
- **Build**: Maven

## Estructura del proyecto

```
src/
├── main/java/gm/zona_fit/
│   ├── application/
│   │   ├── dto/
│   │   │   ├── ClientDTO.java
│   │   │   ├── ClienteMembresiaPatchDTO.java
│   │   │   └── MembresiaDTO.java
│   │   └── services/
│   │       ├── ClienteService.java
│   │       └── IClienteService.java
│   ├── domain/Entities/
│   │   ├── Cliente.java
│   │   ├── Membresia.java
│   │   └── MembresiaTipo.java
│   ├── infrastructure/
│   │   ├── IClienteRepository.java
│   │   └── IMembresiaRepository.java
│   └── presentation/
│       ├── controllers/ClienteController.java
│       └── middlewares/EntityNotFoundMiddleware.java
└── test/
    ├── java/gm/zona_fit/
    │   ├── unit/
    │   │   ├── services/ClienteServiceUnitTest.java
    │   │   └── repositories/
    │   │       ├── ClienteRepositoryUnitTest.java
    │   │       └── MembresiaRepositoryUnitTest.java
    │   └── integration/ClienteIntegrationTest.java
    └── resources/
        ├── application-tests.properties
        └── fake-db.sql
```

## Modelo de membresía

- `Membresia.tipo` usa `MembresiaTipo` (`SILVER`, `GOLD`, `BRONZE`).
- Se persiste como texto (`EnumType.STRING`).
- Tiene índice en DB sobre la columna `tipo`.
- `Cliente` tiene relación opcional con `Membresia`.
- `Cliente` almacena fecha/hora de expiración en `membresiaExpiraEn`.

## API REST

### Endpoints

#### Listar clientes
```http
GET /clientes
```

#### Obtener cliente por id
```http
GET /clientes?id={id}
```

#### Listar clientes con membresía vencida
```http
GET /clientes/membresias-vencidas
```

#### Crear cliente
```http
POST /clientes
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "membresia": 2,
  "membresiaExpiraEn": "2031-12-31T00:00:00"
}
```

#### Actualizar cliente
```http
PUT /clientes/{id}
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "García",
  "membresia": 3,
  "membresiaExpiraEn": "2032-08-15T10:00:00"
}
```

#### Asociar/desasociar membresía (opcional)
```http
PATCH /clientes/{id}/membresia
Content-Type: application/json

{
  "membresiaId": 1,
  "membresiaExpiraEn": "2033-01-01T00:00:00"
}
```

Para quitar membresía y expiración:
```json
{
  "membresiaId": null,
  "membresiaExpiraEn": null
}
```

#### Eliminar cliente
```http
DELETE /clientes/{id}
```

## Casos de uso

### 1) Alta de cliente con membresía

- Se crea el cliente con `membresia` (id de la membresía) y `membresiaExpiraEn`.
- El backend valida que la membresía exista.
- Si `membresia` viene en `null`, la expiración también queda en `null`.

### 2) Renovación de membresía

- Se usa `PATCH /clientes/{id}/membresia` con el mismo `membresiaId` y una nueva fecha en `membresiaExpiraEn`.
- Permite extender vigencia sin tener que actualizar todos los datos del cliente.

### 3) Cambio de plan

- Se usa `PATCH /clientes/{id}/membresia` con un `membresiaId` distinto (`SILVER`, `GOLD` o `BRONZE`) y su nueva expiración.
- El cliente conserva sus datos personales y solo cambia su relación de membresía.

### 4) Baja de membresía

- Se envía `membresiaId: null` y `membresiaExpiraEn: null`.
- El cliente queda activo en el sistema pero sin membresía asociada.

### 5) Consulta de membresías vencidas

- Se usa `GET /clientes/membresias-vencidas`.
- Devuelve clientes con membresía asignada cuya `membresiaExpiraEn` es anterior al momento actual.

## Pruebas

### Arquitectura

- **Unitarias service**: `@Tag("unit/service")`
- **Unitarias repository**: `@Tag("unit/repository")`
- **Integración**: `@Tag("integration")`

### Comandos

```bash
# Todos los tests
mvn test

# Unitarios (service + repository)
mvn test -Punit-tests

# Solo unitarios de service
mvn test -Punit-service-tests

# Solo unitarios de repository
mvn test -Punit-repository-tests

# Solo integración
mvn test -Pintegration-tests
```

### Estado actual de cobertura

Total esperado (según la última ejecución): **35 tests**.

## Configuración de tests

- Archivo: `src/test/resources/application-tests.properties`
- Seed: `src/test/resources/fake-db.sql`
  - Inserta membresías (`SILVER`, `GOLD`, `BRONZE`)
  - Inserta clientes con y sin membresía
  - Define expiración inicial para clientes con membresía

## Compilación y ejecución

```bash
mvn clean compile
mvn clean package
mvn spring-boot:run
```

O ejecuta el jar:

```bash
java -jar target/zona_fit-0.0.1-SNAPSHOT.jar
```
