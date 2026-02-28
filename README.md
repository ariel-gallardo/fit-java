# ZonaFit Spring - Gestión de Clientes

Aplicación Spring Boot para la gestión de clientes de un gimnasio con autenticación JWT y control de acceso basado en roles.

## Descripción

ZonaFit es una API REST construida con Spring Boot para administrar clientes y sus membresías con seguridad integrada.

Funcionalidades principales:

- CRUD completo de clientes.
- Asociación opcional de membresía al cliente (solo administrador).
- Tipo de membresía manejado con `enum` (`SILVER`, `GOLD`, `BRONZE`).
- Persistencia del `enum` como `STRING` en base de datos.
- Expiración de membresía por cliente (`membresiaExpiraEn`).
- **Autenticación JWT**: registro e inicio de sesión de usuarios.
- **OAuth2**: integración con proveedores externos (configurado).
- **Autenticación por roles**: `ADMIN` y `CLIENTE` con control de acceso granular.
- Middleware centralizado para manejo de errores.
- Suite de pruebas unitarias e integración con cobertura de autorización.

## Tecnología

- **Framework**: Spring Boot 3.2.2
- **Lenguaje**: Java 21
- **ORM**: JPA/Hibernate
- **Base de datos (tests)**: H2 en memoria
- **Seguridad**: Spring Security, JWT (jjwt), OAuth2
- **Testing**: JUnit 5 + Mockito + Spring Security Test
- **Build**: Maven

## Estructura del proyecto

```
src/
├── main/java/gm/zona_fit/
│   ├── application/
│   │   ├── dto/
│   │   │   ├── ClientDTO.java
│   │   │   ├── ClienteMembresiaPatchDTO.java
│   │   │   ├── MembresiaDTO.java
│   │   │   ├── UserRegisterDTO.java
│   │   │   ├── UserLoginDTO.java
│   │   │   ├── AuthResponseDTO.java
│   │   │   └── UserDTO.java
│   │   └── services/
│   │       ├── ClienteService.java
│   │       ├── IClienteService.java
│   │       ├── UsuarioService.java
│   │       ├── IUsuarioService.java
│   │       ├── AuthService.java
│   │       ├── IAuthService.java
│   │       ├── JwtService.java
│   │       └── CustomUserDetailsService.java
│   ├── domain/Entities/
│   │   ├── Cliente.java
│   │   ├── Membresia.java
│   │   ├── MembresiaTipo.java
│   │   ├── Usuario.java
│   │   └── RolUsuario.java
│   ├── infrastructure/
│   │   ├── IClienteRepository.java
│   │   ├── IMembresiaRepository.java
│   │   └── IUsuarioRepository.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   └── JwtAuthenticationFilter.java
│   └── presentation/
│       ├── controllers/
│       │   ├── ClienteController.java
│       │   ├── AuthController.java
│       │   └── UsuarioController.java
│       └── middlewares/EntityNotFoundMiddleware.java
└── test/
    ├── java/gm/zona_fit/
    │   ├── unit/
    │   │   ├── services/ClienteServiceUnitTest.java
    │   │   └── repositories/
    │   │       ├── ClienteRepositoryUnitTest.java
    │   │       └── MembresiaRepositoryUnitTest.java
    │   └── integration/
    │       ├── ClienteIntegrationTest.java
    │       └── AuthIntegrationTest.java
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

## Autenticación y Autorización

### Roles

- **ADMIN**: Acceso completo a todas las operaciones (crear, leer, actualizar, eliminar). Puede asociar y desasociar membresías.
- **CLIENTE**: Acceso limitado a lectura de datos propios. No puede modificar membresías ni datos de otros clientes.

### Filtro global de Usuario por token

- La entidad `Usuario` tiene un filtro global dinámico de Hibernate.
- El JWT incluye los claims `role` y `userId`.
- Si el token es de `CLIENTE`, todas las consultas al repositorio de usuarios se limitan automáticamente a `id = userId` del token.
- Si el token es de `ADMIN`, no se aplica restricción por id y puede consultar cualquier usuario.
- `GET /usuarios` está habilitado para `ADMIN` y `CLIENTE`, pero para `CLIENTE` devuelve solo su propio usuario.
- `GET /usuarios/{id}` para `CLIENTE` devuelve `404` cuando intenta acceder a otro usuario.

### Flujo de autenticación

1. **Registro**: `POST /auth/register` con `username`, `email` y `password`.
   - Se crea automáticamente un usuario con rol `CLIENTE`.
   - Se asocia un cliente sin membresía por defecto.

2. **Login**: `POST /auth/login` con `username` y `password`.
   - Retorna un JWT válido por 24 horas.

3. **Solicitudes autenticadas**: Incluir JWT en header `Authorization: Bearer <token>`.

### Endpoints de autenticación

#### Registro
```http
POST /auth/register
Content-Type: application/json

{
  "username": "juan_perez",
  "email": "juan@example.com",
  "password": "MiSegura123!"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "juan_perez",
  "password": "MiSegura123!"
}

Respuesta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

## API REST - Clientes

### Endpoints

#### Listar clientes
```http
GET /clientes
Authorization: Bearer <token>
```

#### Obtener cliente por id
```http
GET /clientes?id={id}
Authorization: Bearer <token>
```

#### Listar clientes con membresía vencida
```http
GET /clientes/membresias-vencidas
Authorization: Bearer <token>
```

#### Crear cliente
```http
POST /clientes
Content-Type: application/json
Authorization: Bearer <token>

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
Authorization: Bearer <token>

{
  "nombre": "Juan",
  "apellido": "García",
  "membresia": 3,
  "membresiaExpiraEn": "2032-08-15T10:00:00"
}
```

#### Asociar/desasociar membresía (solo ADMIN)
```http
PATCH /clientes/{id}/membresia
Content-Type: application/json
Authorization: Bearer <token>

{
  "membresiaId": 1,
  "membresiaExpiraEn": "2033-01-01T00:00:00"
}
```

**Nota**: Solo usuarios con rol `ADMIN` pueden modificar membresías. Un intento de `CLIENTE` resultará en `403 Forbidden`.

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
Authorization: Bearer <token>
```

## API REST - Usuarios

### Endpoints

#### Listar usuarios
```http
GET /usuarios
Authorization: Bearer <token>
```

- `ADMIN`: obtiene todos los usuarios.
- `CLIENTE`: obtiene solo su propio usuario (filtrado por `userId` del token).

#### Obtener usuario por id
```http
GET /usuarios/{id}
Authorization: Bearer <token>
```

- `ADMIN`: puede consultar cualquier id.
- `CLIENTE`: solo su propio id; otro id responde `404 Not Found`.

## Casos de uso

### 1) Registro de nuevo usuario

- Se registra el usuario en `POST /auth/register`.
- Se crea automáticamente un cliente sin membresía asociada con rol `CLIENTE`.
- El usuario recibe un JWT para autenticarse en solicitudes posteriores.

### 2) Alta de cliente con membresía (solo ADMIN)

- El administrador crea el cliente con `POST /clientes` con membresía inicial.
- Se valida que la membresía exista.
- Si `membresia` viene en `null`, la expiración también queda en `null`.

### 3) Renovación de membresía (solo ADMIN)

- El administrador usa `PATCH /clientes/{id}/membresia` con el mismo `membresiaId` y una nueva fecha en `membresiaExpiraEn`.
- Permite extender vigencia sin tener que actualizar todos los datos del cliente.
- Un intento de `CLIENTE` resulta en `403 Forbidden`.

### 4) Cambio de plan (solo ADMIN)

- El administrador usa `PATCH /clientes/{id}/membresia` con un `membresiaId` distinto (`SILVER`, `GOLD` o `BRONZE`) y su nueva expiración.
- El cliente conserva sus datos personales y solo cambia su relación de membresía.

### 5) Baja de membresía (solo ADMIN)

- El administrador envía `membresiaId: null` y `membresiaExpiraEn: null`.
- El cliente queda activo en el sistema pero sin membresía asociada.

### 6) Consulta de membresías vencidas

- Se usa `GET /clientes/membresias-vencidas`.
- Devuelve clientes con membresía asignada cuya `membresiaExpiraEn` es anterior al momento actual.
- Requiere autenticación (cualquier rol).

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

Total esperado (según la última ejecución): **39 tests**.

- **Unitarios**: 16 tests (service + repository).
- **Integración**: 23 tests (Cliente + Autenticación - incluye validaciones de autorización).

#### Nuevos tests de autorización

- Verificación de que `CLIENTE` recibe `403 Forbidden` al intentar modificar membresías.
- Validación de que `ADMIN` puede ejecutar operaciones protegidas.
- Confirmación de que nuevos usuarios se registran sin membresía.

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
