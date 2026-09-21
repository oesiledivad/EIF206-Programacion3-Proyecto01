<div align="center">

# 📅 Sistema de Reserva de Recursos

### EIF206 — Programación III · Proyecto 01

Sistema de escritorio para la **gestión integral de reservas de recursos institucionales**, desarrollado con Java y JavaFX. Incluye autenticación y roles, gestión de reservas y recursos, persistencia XML, generación de reportes PDF, pruebas automatizadas e integración con **Inteligencia Artificial (LLM)** mediante la API de Groq.

<p>
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-2f73c9?logo=java&logoColor=white" alt="JavaFX 21">
  <img src="https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white" alt="JUnit 5">
  <img src="https://img.shields.io/badge/Mockito-5.x-78C257" alt="Mockito">
  <img src="https://img.shields.io/badge/AI-Groq-00A67E" alt="Groq">
</p>

<p>
  <img src="https://img.shields.io/github/last-commit/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Último commit">
  <img src="https://img.shields.io/github/commit-activity/m/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Actividad del repositorio">
  <img src="https://img.shields.io/github/repo-size/oesiledivad/EIF206-Programacion3-Proyecto01?logo=github" alt="Tamaño del repositorio">
</p>

<p>
  🎓 <strong>Proyecto académico — Universidad Nacional de Costa Rica</strong>
</p>

</div>

---

## Características Principales

### 🔐 Autenticación y Gestión de Usuarios
- **Autenticación segura:** Inicio de sesión mediante credenciales de acceso.
- **Gestión de roles:** Soporte estructurado para perfiles de **Administrador** y **Funcionario**.
- **Gestión de sesiones:** Control dinámico de la sesión activa y restricciones según los privilegios del usuario.
- **Cambio de contraseña:** Opción segura para la actualización de credenciales.

### 📅 Gestión de Reservas
- **Creación de reservas:** Registro asistido de nuevos apartados de recursos institucionales.
- **Consulta de reservas:** Visualización en tiempo real y filtrado de reservas vigentes o pasadas.
- **Cancelación:** Opción exclusiva para que los funcionarios cancelen reservas futuras.
- **Validación de disponibilidad:** Control automático de conflictos e interbloqueos de recursos antes de confirmar.
- **Asignación inteligente:** Selección automatizada del primer recurso disponible que cumpla con los requisitos.

### 🤖 Asistente de Inteligencia Artificial (LLM)
- **Procesamiento de lenguaje natural:** Capacidad de describir actividades usando lenguaje cotidiano y descriptivo.
- **Generación automática:** Interpretación de solicitudes para rellenar de forma inteligente los formularios de reserva.
- **Extracción de recursos:** Identificación precisa de categorías y componentes solicitados en texto libre.
- **Integración con Groq:** Enlace directo con modelos LLM de alto rendimiento a través de la API de Groq.

### 🏢 Mantenimiento del Sistema (CRUD)
Operaciones completas de alta, consulta, modificación y baja para:
- **Funcionarios:** Información de usuario, nombre completo y número de teléfono.
- **Categorías de recursos:** Identificadores y descripciones generales.
- **Recursos físicos:** Control de números de activo, asociación por llave foránea (FK) y descripciones detalladas.

### 🗓️ Calendarización y Programación
- **Matriz de recursos:** Selección por fecha y categoría, visualizando filas de horarios y columnas de recursos físicos.
- **Programación semanal:** Vista matricial por horas y días de la semana con el detalle de los responsables y actividades.

### 📊 Estadísticas y Reportes PDF
- **Estadísticas dinámicas:** Rangos de fechas para recursos (categorías, cantidades) y actividades con gráficos de barras interactivos.
- **Reportes profesionales:** Generación automatizada de reportes en PDF integrados en todas las vistas principales del sistema.

### 💾 Persistencia y Calidad
- **Persistencia XML y JAXB:** Almacenamiento estructurado mediante wrappers especializados y adaptadores de fecha/hora.
- **Pruebas Automatizadas:** Suite completa de pruebas unitarias (JUnit 5) e integración (Mockito).

---

## Credenciales Iniciales
Para acceder al sistema por primera vez, utiliza el usuario administrador predeterminado:
- **ID:** `admin`
- **Clave:** `admin`

---

## Arquitectura del Sistema

El proyecto sigue una estricta **arquitectura por capas**, garantizando el desacoplamiento entre la interfaz gráfica, la lógica de negocio, el acceso a datos y las entidades de dominio.

```text
┌─────────────────────────────────────────────┐
│           INTERFAZ DE USUARIO               │
│          JavaFX · FXML · CSS                │
│            Views + Controllers               │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                 SERVICIOS                   │
│      Autenticación · Reservas · Sesiones    │
│       Usuarios · Recursos · Reportes        │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              LÓGICA DE NEGOCIO              │
│     Validaciones · Reglas · Disponibilidad  │
│              Integración con IA             │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                   DATOS                     │
│        XML · JAXB · Wrappers · Adaptadores  │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              MODELOS / DTO                  │
│       Entidades y objetos de transferencia  │
└─────────────────────────────────────────────┘

```

### Organización por Capas

| Capa | Responsabilidad |
| --- | --- |
| **Interfaz de Usuario** | Vistas JavaFX, archivos FXML, hojas de estilo CSS y controladores de eventos. |
| **Servicios** | Orquestación de operaciones de la aplicación y conexión lógica con controladores. |
| **Lógica** | Reglas de negocio, validaciones de disponibilidad y submódulo de IA (`aiGenerator`). |
| **Datos** | Operaciones CRUD y serialización JAXB sobre archivos XML estructurados. |
| **Modelos** | Clases de dominio principales (`Administrador`, `Categoria`, `Funcionario`, `Recurso`, `Reserva`, `Usuario`). |

### Tecnologías e Integraciones Clave

* 🤖 **Groq API:** Procesamiento e inferencia mediante modelos LLM avanzados.
* 💾 **Jakarta JAXB:** Mapeo y persistencia de objetos en formato XML.
* 📄 **Apache PDFBox + EasyTable:** Renderizado y exportación de reportes institucionales en PDF.
* 🔐 **Dotenv Java:** Manejo seguro de configuraciones y variables de entorno.

---

## Configuración del Archivo `.env`

Para habilitar la funcionalidad del asistente de Inteligencia Artificial, debes crear un archivo llamado **`.env`** en la raíz absoluta del proyecto (junto al archivo `pom.xml`).

El archivo debe tener la siguiente estructura exacta:

```env
GROQ_API_KEY=tu_api_key_de_groq_aqui
GROQ_URL=https://api.groq.com/openai/v1/chat/completions
GROQ_MODEL=nombre_del_modelo_qwen
```

### Descripción de Variables

* **`GROQ_API_KEY`**: Clave de autenticación personal obtenida en la plataforma de Groq.
* **`GROQ_URL`**: Endpoint oficial de la API de Groq consumido por la aplicación.
* **`GROQ_MODEL`**: Identificador del modelo de lenguaje utilizado por el sistema.

> **Nota Importante:** Se recomienda emplear modelos de la familia **Qwen** disponibles en Groq por su excelente desempeño en la extracción estructurada de datos. Como los catálogos cambian con el tiempo, verifica el modelo activo en tu cuenta de Groq antes de ejecutar. El archivo `.env` está protegido por defecto y **nunca debe subirse al control de versiones**.

---


## Requisitos Previos e Instalación de Maven

Asegúrate de tener instalado en tu entorno de desarrollo:

1. **Java Development Kit (JDK) 17 o superior** (se recomienda fuertemente JDK 21).
2. **Apache Maven** para la compilación y gestión de dependencias.

### ¿Cómo instalar Maven?

* **En Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install maven
```


* **En macOS (usando Homebrew):**
```bash
brew install maven
```


* **En Windows:**
1. Descarga el binario comprimido de Maven desde el [sitio web oficial](https://maven.apache.org/download.cgi).
2. Extrae el contenido en una ruta de tu preferencia (ej. `C:\Program Files\Apache\maven`).
3. Añade la ruta de la carpeta `bin` a las **Variables de Entorno** del sistema (`PATH`).
4. Verifica la instalación ejecutando en tu terminal:
```bash
mvn -version
```
---

## Estructura del Proyecto

```text
data/                           # Archivos XML base para la persistencia
src/
├── main/
│   ├── java/
│   │   └── una/proyecto/
│   │       ├── app/            # Configuración y MainApp
│   │       ├── controller/     # Controladores de vistas JavaFX
│   │       ├── datos/          # Capa de datos y Wrappers JAXB para XML
│   │       ├── logic/          # Lógica de negocio y módulo IA (aiGenerator)
│   │       ├── model/          # Entidades del dominio del sistema
│   │       ├── service/        # Servicios de aplicación y autenticación
│   │       └── utils/          # Utilidades (Navegación, PDF, Sesión, Adaptadores)
│   │
│   └── resources/
│       └── una/proyecto/
│           ├── css/            # Estilos visuales de la interfaz
│           ├── fonts/          # Recursos tipográficos
│           └── ui/             # Diseños FXML de las pantallas
│
└── test/
    └── java/
        ├── Integration/        # Pruebas de integración del sistema
        └── Unit/               # Pruebas unitarias de componentes
```

---

## Compilación y Ejecución

Si deseas clonar el repositorio desde cero, compilar el código, ejecutar la suite de pruebas automatizadas y arrancar la aplicación de escritorio mediante Maven, sigue estos pasos en tu terminal:

1. **Clonar el repositorio y acceder a la carpeta del proyecto:**

```bash
git clone https://github.com/oesiledivad/EIF206-Programacion3-Proyecto01.git
cd EIF206-Programacion3-Proyecto01
```

2. **Compilar el código y ejecutar todas las pruebas automatizadas (Unitarias e Integración):**

```bash
mvn clean test
```

3. **Ejecutar la interfaz gráfica con JavaFX:**

```bash
mvn javafx:run
```

