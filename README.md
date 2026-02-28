# ZonaFit Spring - Gestión de Clientes

Aplicación Spring Boot para la gestión de clientes de un gimnasio.

## Descripción

ZonaFit es una aplicación Java construida con Spring Boot que proporciona una API REST para la gestión integral de clientes de un centro de fitness, incluyendo funcionalidades de:

- CRUD completo de clientes
- Gestión de membresías
- Aplicación de middleware para manejo centralizado de excepciones
- Suite de pruebas completa (unitarias e integración)

## Tecnología

- **Framework**: Spring Boot 3.2.2
- **Lenguaje**: Java 21
- **Base de Datos**: H2 (desarrollo/testing), extensible a otras bases de datos
- **ORM**: JPA/Hibernate
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Build**: Maven
- **Utilidades**: Lombok

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/gm/zona_fit/
│   │   ├── ZonaFitApplication.java          # Punto de entrada
│   │   ├── application/
│   │   │   ├── dto/
│   │   │   │   └── ClientDTO.java           # Data Transfer Object
│   │   │   └── services/
│   │   │       ├── ClienteService.java      # Lógica de negocio
│   │   │       └── IClienteService.java     # Interfaz del servicio
│   │   ├── domain/
│   │   │   └── Entities/
│   │   │       └── Cliente.java             # Entidad JPA
│   │   ├── infrastructure/
│   │   │   └── IClienteRepository.java      # Repositorio JPA
│   │   └── presentation/
│   │       ├── controllers/
│   │       │   └── ClienteController.java   # Endpoints REST
│   │       └── middlewares/
│   │           └── EntityNotFoundMiddleware.java # Manejo de excepciones
│   └── resources/
│       ├── application.properties
│       └── logback-spring.xml
└── test/
    ├── java/gm/zona_fit/
    │   ├── unit/
    │   │   ├── services/
    │   │   │   └── ClienteServiceUnitTest.java      # Tests del servicio
    │   │   └── repositories/
    │   │       └── ClienteRepositoryUnitTest.java   # Tests del repositorio
    │   └── integration/
    │       └── ClienteIntegrationTest.java          # Tests de API
    └── resources/
        ├── application-tests.properties
        └── fake-db.sql                              # Datos de prueba
```

## Suite de Pruebas

### Arquitectura de Testing

El proyecto implementa una estrategia de testing con dos tipos claramente separados:

#### Tests Unitarios (`@Tag("unit")`)
- **Ubicación**: `src/test/java/gm/zona_fit/unit/`
- **Alcance**: Lógica de negocio y persistencia sin contexto HTTP
- **Componentes probados**:
  - **ClienteServiceUnitTest**: Valida la lógica del servicio con repositorio mockeado
  - **ClienteRepositoryUnitTest**: Prueba las operaciones JPA con H2 en memoria
- **Aislamiento**: No requieren contexto completo de Spring
- **Cobertura**: 11 tests unitarios

#### Tests de Integración (`@Tag("integration")`)
- **Ubicación**: `src/test/java/gm/zona_fit/integration/`
- **Alcance**: API REST completa y persistencia en base de datos
- **Componentes probados**:
  - **ClienteIntegrationTest**: Valida endpoints REST (GET, POST, PUT, DELETE) con MockMvc
- **Variables de entorno**: Contexto Spring completo, H2 en memoria, datos preconfigurados
- **Cobertura**: 7 tests de integración

### Configuración de Tests

**Archivo**: `src/test/resources/application-tests.properties`
```properties
# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.main.web-application-type=servlet
```

**Datos precargados**: `src/test/resources/fake-db.sql`
- 3 clientes de prueba (Ana, Luis, Marta)
- Identidad de secuencia reiniciada para evitar conflictos

### Ejecución de Pruebas

#### Ejecutar todos los tests
```bash
mvn test
```
Resultado esperado: 18 tests (11 unitarios + 7 integración)

#### Ejecutar solo tests unitarios
```bash
mvn test -Punit-tests
```
- Filtra por `@Tag("unit")`
- Excluye paquete `**/controllers/**`
- Resultado esperado: 11 tests (services + repositories)

#### Ejecutar solo tests de integración
```bash
mvn test -Pintegration-tests
```
- Filtra por `@Tag("integration")`
- Incluye solo paquete `**/integration/**`
- Resultado esperado: 7 tests (endpoints REST)

#### Ejecutar test específico
```bash
mvn test -Dtest=ClienteServiceUnitTest
mvn test -Dtest=ClienteIntegrationTest
```

### Cobertura de Tests

#### ClienteServiceUnitTest (7 tests)
- `getAll_shouldReturnAllClientes` - Lista todas los clientes
- `getById_shouldReturnCliente_whenExists` - Obtiene un cliente existente
- `getById_shouldThrow_whenNotExists` - Lanza excepción si no existe
- `create_shouldPersistAndReturnId` - Crea nuevo cliente
- `update_shouldPersistChanges_whenExists` - Actualiza cliente existente
- `update_shouldThrow_whenNotExists` - Lanza excepción en actualización fallida
- `delete_shouldDelete_whenExists` y `delete_shouldThrow_whenNotExists` - Elimina cliente

#### ClienteRepositoryUnitTest (4 tests)
- `findAll_shouldReturnSeededData` - Verifica datos precargados
- `findById_shouldReturnCliente_whenExists` - Búsqueda por ID
- `save_shouldPersistCliente` - Persistencia de nuevo cliente
- `deleteById_shouldRemoveCliente` - Eliminación por ID

#### ClienteIntegrationTest (7 tests)
- `getAll_shouldReturnSeededClientes` - GET /clientes
- `getById_shouldReturnOneCliente` - GET /clientes?id=2
- `getById_shouldReturn404_whenMissing` - Manejo de errores
- `create_shouldPersistAndReturnLocation` - POST /clientes
- `update_shouldModifyExistingCliente` - PUT /clientes/3
- `delete_shouldRemoveCliente` - DELETE /clientes/1

## API REST

### Endpoints

#### Listar todos los clientes
```http
GET /clientes
```

#### Obtener cliente por ID
```http
GET /clientes?id={id}
```

#### Crear nuevo cliente
```http
POST /clientes
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "membresia": 1
}
```

#### Actualizar cliente
```http
PUT /clientes/{id}
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "García",
  "membresia": 2
}
```

#### Eliminar cliente
```http
DELETE /clientes/{id}
```

## Compilación y Ejecución

### Compilar el proyecto
```bash
mvn clean compile
```

### Empaquetar la aplicación
```bash
mvn clean package
```

### Ejecutar la aplicación
```bash
# Con Maven
mvn spring-boot:run

# O con el JAR generado
java -jar target/zona_fit-0.0.1-SNAPSHOT.jar
```

## Configuración

### Propiedades de Aplicación

**Archivo**: `src/main/resources/application.properties`

Personaliza según tu entorno:
- Configuración de base de datos
- Perfil activo (development, production)
- Nivel de logging

## Notas Arquitectónicas

### Separación de Responsabilidades
- **Domain**: Entidades y modelos de negocio
- **Application**: Servicios y DTOs
- **Infrastructure**: Repositorios y acceso a datos
- **Presentation**: Controladores y middleware

### Manejo de Excepciones
- `EntityNotFoundMiddleware`: Captura excepciones de entidades no encontradas
- Respuestas HTTP consistentes con códigos de estado apropiados

### Buenas Prácticas
- Use de DTOs para transferencia de datos
- Interfaces para contratación de servicios
- Tests aislados sin dependencias externas
- Uso de JUnit 5 con anotaciones modernas

## Desarrollo Futuro

- Implementación de más entidades (Membresías, Servicios)
- Autenticación y autorización (JWT)
- Documentación con Swagger/OpenAPI
- Caching
- Paginación en listados
- Validaciones adicionales

## Contacto y Contribuciones

Para reportar bugs o contribuir, por favor abre un issue en el repositorio.
