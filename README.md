# TuTranquilo - MVP 1.0 (SoportApp)

**TuTranquilo** es una plataforma profesional de soporte técnico diseñada para ofrecer una experiencia de usuario honesta, segura y altamente accesible, inspirada en los estándares de calidad de aplicaciones líderes como Uber y Rappi.

## 🚀 Propósito del Proyecto
El objetivo principal es transformar el servicio técnico informal en Cali, Colombia, en una experiencia de bienestar y respaldo, garantizando que cada interacción sea justa, cumplida e íntegra.

## 📱 Flujo Maestro de Solicitud (10 Pasos)
Hemos diseñado un proceso lineal y lógico que reduce la fricción inicial y asegura el compromiso del técnico:

1.  **Perfil de Usuario:** Selección entre servicio de Hogar o Empresa.
2.  **Selección de Servicio:** Catálogo dinámico de soluciones técnicas.
3.  **Descripción del Problema:** Formulario accesible con carga de evidencia opcional y gestión de horarios (Lun-Sáb).
4.  **Modalidad del Servicio:** Evaluación informativa de las opciones (Remoto, Sitio, Laboratorio).
5.  **Resumen de Solicitud:** Revisión transparente de los datos ingresados.
6.  **Pago Seguro (Base):** Cobro preventivo de $35.000 COP para asegurar la atención del experto.
7.  **Identificación del Cliente:** Registro silencioso del usuario (Nombre/Celular) tras el compromiso de pago.
8.  **Asignación de Técnico:** Búsqueda y asignación dinámica de un experto certificado.
9.  **Seguimiento en Tiempo Real:** Panel de control inteligente que gestiona estados y pagos de excedentes ($30.000 por traslado/recogida).
10. **Calificación Final:** Cierre del ciclo con feedback del usuario y construcción de reputación.

## 🛠️ Fase 4 — Tecnologías

Arquitectura elegida (información técnica permanece en local vía Ollama):

```
                ┌─────────────────────┐
                │      Flutter        │
                │  App Android/iOS    │
                └──────────┬──────────┘
                           │
                          HTTPS
                           │
                ┌──────────▼──────────┐
                │       FastAPI       │
                │       Backend       │
                └─────┬──────┬───────┘
                      │      │
            ┌─────────┘      └──────────┐
            ▼                            ▼
     ┌──────────────┐             ┌──────────────┐
     │ PostgreSQL   │             │ IA local     │
     │ Base datos   │             │ Ollama       │
     └──────────────┘             └──────────────┘
```

| Capa | Tecnología | Rol |
| --- | --- | --- |
| Frontend | **Flutter + Dart** (`frontend/`) | Un mismo código para Android y, después, iOS |
| Backend | **Python + FastAPI** (`backend/`) | API REST, docs en `/docs`, integración con IA |
| Base de datos | **PostgreSQL 15** | Persistencia de usuarios, tickets y diagnósticos |
| IA | **Ollama + modelo local** (`llama3` por defecto) | Preguntas y diagnóstico sin enviar datos a una API externa |

### Cómo levantar el stack

```bash
# 1. Backend + PostgreSQL + Ollama (el primer pull del modelo puede tardar)
docker compose up --build

# 2. API: http://localhost:8000  |  Docs: http://localhost:8000/docs  |  Salud: http://localhost:8000/health

# 3. App Flutter (desde frontend/)
flutter pub get
flutter run
```

La app Flutter apunta por defecto a `http://10.0.2.2:8000` (emulador Android). En iOS simulator usa `--dart-define=API_BASE_URL=http://127.0.0.1:8000`.

El módulo Android nativo (`app/`, Jetpack Compose) se conserva como prototipo local; el producto multiplataforma es Flutter + FastAPI.

## 📍 Potencial de Mercado
Proyecto validado bajo las necesidades del mercado caleño:
- Eliminación de la desconfianza en el servicio técnico.
- Foco en la población de la tercera edad desatendida.
- Modelo de negocio protegido con cobros base y dinámicos.

---
*"Trabajen de buena gana en todo lo que hagan, como si fuera para el Señor..." (Colosenses 3:23)*
