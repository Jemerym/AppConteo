# AppConteo 📱

**AppConteo** es una aplicación móvil nativa desarrollada en Android con **Jetpack Compose** y **SQLite**, diseñada para el control de aforo, gestión de accesos, turnos y usuarios en eventos o recintos.

---

## 👥 Roles del Sistema

1. **Administrador (`rolId = 1`)**
   - Acceso al panel de control general.
   - Gestión de usuarios y observadores (crear, editar, eliminar).
   - Programación y gestión de turnos por punto de acceso.
   - Supervisión del historial completo de registros de aforo.

2. **Observador / Guardia (`rolId = 2`)**
   - Acceso al panel de inicio de observador.
   - Registro de aforo en tiempo real (Clientes y Transeúntes, tipo de grupo, rango etario).
   - Escaneo de códigos de barras / QR de boletas con la cámara integrada (**CameraX** y **Google ML Kit**).
   - Consulta de turnos asignados.

---

## 🔑 Credenciales de Prueba

| Rol | Correo | Contraseña | Palabra Secreta |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@demo.cl` | `1234` | `duoc` |
| **Observador** | `camila@duocuc.cl` | `123456` | `duoc` |
| **Observador** | `diego@duocuc.cl` | `123456` | `duoc` |

---

## 🚀 Funcionalidades Principales

- **Arquitectura MVVM Limpia:** Separación estricta entre estados de UI (`UiState`), ViewModels puros de UI y ViewModel centralizador de persistencia (`UsuariosViewModel`).
- **Base de datos SQLite local:** Persistencia robusta mediante `SQLiteOpenHelper` con soporte de claves foráneas e índices optimizados.
- **Escáner QR y de Boletas:** Integración con **CameraX** y **Google ML Kit** para lectura óptica de códigos.
- **Navegación Fluida:** Animaciones de transición (*Slide & Fade*) y protección de rutas por rol (*Route Guards*).
- **Gestión de Perfil:** Cambio de foto de perfil (desde cámara o galería) procesada y comprimida a Bitmap/Blob.

---

---

## 🛠️ Cómo Ejecutar

1. Abre el proyecto en **Android Studio** (versión Jellyfish o superior recomendada).
2. Sincroniza las dependencias de Gradle (`Sync Project with Gradle Files`).
3. Conecta un dispositivo físico o inicia un emulador Android (API 24 o superior).
4. Ejecuta la aplicación (`Run 'app'`).
