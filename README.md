# TOTUM

App Android para **contar el flujo de personas** (clientes y transeúntes) en los accesos de un recinto. Un **administrador** programa los turnos de los **observadores**. Cada observador, durante su turno, registra a las personas que pasan por su acceso, ya sea escaneando la boleta (clientes) o con un registro rápido (tránsito).

El backend son **4 microservicios Spring Boot** con MySQL. La app se conecta a ellos con Retrofit.

📋 **Tablero del proyecto (Trello):** https://trello.com/b/9NETUH65/app-conteo

⚙️ **Repositorio del backend (microservicios):** https://github.com/Jemerym/Back-appconteo

---

## Funcionalidades

### Todos los usuarios
- Registro con nombre, correo, contraseña (con repetir contraseña), palabra secreta y términos.
- Inicio de sesión. La sesión queda guardada en el teléfono.
- Recuperar la contraseña con la palabra secreta.
- Perfil: foto (cámara o galería), modificar nombre y contraseña.
- **Tema** de la app: Sistema, Claro, Oscuro, Tokyo y Lila. Queda guardado en el teléfono.

### Observador
- Ve su **turno en curso**. La app lo consulta al servidor cada 15 s, con la hora del servidor.
- Registra personas solo durante su turno:
  - **Cliente:** escanea el código de barras o QR de la boleta. La boleta no se puede repetir.
  - **Registro rápido (tránsito).**
  - En ambos casos registra el tipo de grupo y el rango etario.
- **Mis Turnos:** sus turnos ordenados por día (Hoy, Mañana, …) y por hora.
- **Ver Registros:** sus registros. Puede **modificar y anular** los suyos solo mientras su turno está en curso.

### Administrador
- Panel con el conteo del día: clientes, transeúntes y boletas.
- **Usuarios:** buscar por nombre o correo, ver los registros de cada uno, **anular y restaurar** usuarios, y filtro **"Mostrar anulados"**.
- **Programar turnos:** elegir acceso, observador, día y tramo de 30 minutos. El servidor rechaza los choques de horario.
- **Turnos:** editar, **anular y restaurar**, filtro "Mostrar anulados".
- **Registros:** ver todos, filtrar por fecha, acceso y tipo, modificar, **anular y restaurar**.

> **Anular en vez de eliminar:** usuarios, turnos y registros no se borran. Se anulan: dejan de contar o de funcionar, pero se conservan y se pueden restaurar.

---

## Arquitectura

```
App Android (Kotlin + Jetpack Compose, MVVM)
   │  Retrofit (HTTP / JSON)
   ├──► roles-ms      :8081  ── MySQL roles_db
   ├──► usuarios-ms   :8082  ── MySQL usuarios_db   ──(WebClient)──► roles-ms
   ├──► turnos-ms     :8083  ── MySQL turnos_db     ──(WebClient)──► usuarios-ms
   └──► registros-ms  :8084  ── MySQL registros_db
```

| Microservicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| **roles-ms** | 8081 | `roles_db` | Catálogo de roles (Administrador, Observador). |
| **usuarios-ms** | 8082 | `usuarios_db` | Registro, login (BCrypt), perfil, foto, recuperar clave, anular y restaurar. |
| **turnos-ms** | 8083 | `turnos_db` | Programar turnos, validar choques de horario, turno en curso (hora de Chile), anular y restaurar. |
| **registros-ms** | 8084 | `registros_db` | Registros de aforo, boleta única, conteo del día, anular y restaurar. La fecha y la hora las pone el servidor. |

### Tecnologías
- **App:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel + StateFlow, Retrofit + Gson, Coil (fotos), CameraX + ML Kit (escáner), SharedPreferences (sesión y tema).
- **Backend:** Java 17, Spring Boot 4, Spring Data JPA, MySQL, Lombok, WebClient, BCrypt, springdoc-openapi (Swagger).

---

## Cómo ejecutarlo

### 1. Requisitos
- Java 17 o superior.
- MySQL (o XAMPP/MariaDB) corriendo en `localhost:3306`, con usuario `root` sin contraseña. Si es distinto, cambia `spring.datasource.*` en cada `application.properties`.
- Android Studio.

### 2. Levantar los microservicios
El código de los microservicios está en https://github.com/Jemerym/Back-appconteo. Clónalo y entra a la carpeta:

```bash
git clone https://github.com/Jemerym/Back-appconteo.git
```

```bash
cd Back-appconteo
```

Las bases de datos se crean solas (`createDatabaseIfNotExist=true`) y las tablas también (`ddl-auto=update`).

Levanta los 4 microservicios **en este orden**, porque usuarios-ms necesita a roles-ms y turnos-ms necesita a usuarios-ms. En cada carpeta, ejecuta su comando (en Windows usa `mvnw.cmd` en vez de `./mvnw`):

**roles-ms:**
```bash
./mvnw spring-boot:run
```

**usuarios-ms:**
```bash
./mvnw spring-boot:run
```

**turnos-ms:**
```bash
./mvnw spring-boot:run
```

**registros-ms:**
```bash
./mvnw spring-boot:run
```

También puedes ejecutar la clase `*MsApplication` de cada microservicio desde IntelliJ.

Cada microservicio tiene su documentación en Swagger: `http://localhost:<puerto>/swagger-ui.html`.

### 3. Configurar la app
En `data/remote/RetrofitClient.kt` pon la **IP del PC donde corren los microservicios**:
```kotlin
private const val URL_USUARIOS  = "http://192.168.1.5:8082/"
private const val URL_TURNOS    = "http://192.168.1.5:8083/"
private const val URL_REGISTROS = "http://192.168.1.5:8084/"
```
- **Emulador:** usa `10.0.2.2` en vez de la IP.
- **Celular físico:** usa la IP del PC en la misma WiFi. Puedes verla con `ipconfig` en Windows.
- **Firewall de Windows:** permite los puertos **8082, 8083 y 8084**. Si el login funciona pero los turnos no, casi siempre es el firewall.

### 4. Usuarios de prueba
usuarios-ms los crea solo la primera vez:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@demo.cl` | `1234` |
| Observador | `camila@duocuc.cl` | `123456` |
| Observador | `diego@duocuc.cl` | `123456` |

La palabra secreta de los tres es `duoc`. Los usuarios nuevos que se registren desde la app quedan siempre como **Observador**.

---

## Endpoints principales

### roles-ms (`/api/roles`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/roles` | Lista los roles |
| GET | `/api/roles/{id}` | Obtiene un rol |

### usuarios-ms (`/api/usuarios`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/usuarios?buscar=&incluirAnulados=` | Lista o busca usuarios |
| GET | `/api/usuarios/{id}` | Obtiene un usuario |
| POST | `/api/usuarios` | Registra un usuario (siempre como Observador) |
| POST | `/api/usuarios/login` | Inicia sesión |
| PUT | `/api/usuarios/{id}` | Actualiza nombre y correo |
| PUT | `/api/usuarios/{id}/contrasena` | Cambia la contraseña |
| POST | `/api/usuarios/recuperar-clave` | Recupera la clave con la palabra secreta |
| POST / GET | `/api/usuarios/{id}/avatar` | Sube o descarga la foto de perfil |
| PUT | `/api/usuarios/{id}/anular` · `/restaurar` | Anula o restaura un usuario |

### turnos-ms (`/api/turnos`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/turnos?usuarioId=` | Lista turnos (todos o los de un observador) |
| GET | `/api/turnos/actual?usuarioId=` | Turno en curso (204 si no tiene) |
| GET | `/api/turnos/{id}` | Obtiene un turno |
| POST | `/api/turnos` | Programa un turno (409 si choca con otro) |
| PUT | `/api/turnos/{id}` | Modifica un turno |
| PUT | `/api/turnos/{id}/anular` · `/restaurar` | Anula o restaura un turno |
| PUT | `/api/turnos/usuario/{usuarioId}/anular` | Anula los turnos de un usuario |

### registros-ms (`/api/registros`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/registros?usuarioId=&turnoId=&fecha=&incluirAnulados=` | Lista registros con filtros |
| GET | `/api/registros/{id}` | Obtiene un registro |
| POST | `/api/registros` | Registra una persona (409 si la boleta ya se usó) |
| PUT | `/api/registros/{id}` | Modifica acceso, grupo y rango etario |
| PUT | `/api/registros/{id}/anular` · `/restaurar` | Anula o restaura un registro |
| GET | `/api/registros/boleta/existe?codigo=` | ¿La boleta ya está registrada? |
| GET | `/api/registros/conteo?fecha=&usuarioId=` | Totales del día (clientes, tránsito, total) |

---

## Estructura de la app

```
appconteo/
├── data/            Repositories (Usuario, Turno, Registro, Avatar), SesionPreferencias, PreferenciasTema
│   └── remote/      Retrofit: RetrofitClient, *Api, ApiModelos (DTOs)
├── model/           Modelos y UiState (Usuario, Turno, HistorialRegistros, Validaciones…)
├── navigation/      AppNavegacion y Rutas
├── permisos/        Permisos de cámara y galería
├── ui/
│   ├── components/  Componentes reutilizables (botones, campos, tarjetas, ActualizarPeriodicamente…)
│   ├── screens/     Pantallas (Login, Registro, Admin, Inicio, Turnos, Registros, Perfil…)
│   ├── style/       Estilos de botones, campos y tarjetas
│   └── theme/       Colores, tipografía y temas (TemaApp)
└── viewmodel/       ViewModels (Usuarios, Turnos, Conteo, Login, Registro, Programar, Tema…)
```
