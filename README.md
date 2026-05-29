# IoT Ambiental TEC

Aplicación Android nativa para un proyecto académico de IoT ambiental del TEC San Carlos. Permite consultar datos simulados de temperatura, humedad y viento, visualizar estadísticas, revisar gráficos básicos, copiar resultados como CSV y explorar un mapa interactivo con puntos de interés.

## Tecnologías usadas

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- StateFlow / MutableStateFlow
- MVVM
- Firebase Firestore (repositorio preparado)
- Gradle Kotlin DSL

## Cómo correr el proyecto

1. Abra el repositorio en Android Studio.
2. Espere la sincronización de Gradle.
3. Ejecute la configuración `app` en un emulador o dispositivo Android con API 26+.
4. La app funciona inicialmente con `MockEnvironmentalRepository`, por lo que no necesita Firebase para la primera ejecución.

También puede compilar por terminal:

```bash
gradle :app:assembleDebug
```

## Configuración de Firebase Firestore

La arquitectura ya incluye `FirebaseEnvironmentalRepository` para leer desde Firestore.

1. Cree un proyecto en Firebase Console.
2. Registre una app Android con el package name `com.example.iotambientaltec`.
3. Descargue `google-services.json` y colóquelo en:

```text
app/google-services.json
```

4. Active el plugin en `app/build.gradle.kts` descomentando:

```kotlin
id("com.google.gms.google-services")
```

5. Cambie la inyección del repositorio en `MainActivity` o en una capa DI para usar `FirebaseEnvironmentalRepository` en lugar de `MockEnvironmentalRepository`.

## Colección esperada en Firestore

Colección: `environmental_data`

Documento de ejemplo:

```json
{
  "timestamp": 1760000000000,
  "date": "2026-10-01",
  "time": "08:00",
  "variable": "temperature",
  "value": 24.5,
  "unit": "°C",
  "location": "TEC San Carlos",
  "sensorId": "SENSOR_001"
}
```

Variables esperadas:

- `temperature`
- `humidity`
- `wind`

Reglas básicas sugeridas solo para pruebas académicas:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /environmental_data/{document} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## Funcionalidades implementadas

- Home moderno con accesos claros.
- Dashboard con últimos valores, promedio diario, máximo y mínimo histórico.
- Consultas con validación en tiempo real.
- Promedio por hora para un día.
- Promedio por día en rango de fechas.
- Máximo y mínimo histórico.
- Gráficos básicos (líneas y comparación) con Canvas.
- Comparación visual entre variables.
- Exportación CSV básica copiando el texto al portapapeles.
- Mapa interactivo tipo placeholder con puntos del TEC San Carlos.
- Tema Material 3 con soporte para modo oscuro del sistema.
- Datos mock realistas de 10 días cada 30 minutos para temperatura, humedad y viento.

## Estructura principal

```text
app/src/main/java/com/example/iotambientaltec/
├── data/
│   ├── mock/
│   ├── model/
│   ├── remote/
│   └── repository/
├── domain/usecase/
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   └── theme/
└── utils/
```

## Pendientes y mejoras futuras

- Agregar autenticación Firebase para escritura segura.
- Sustituir campos de texto de fecha por `DatePicker` Material 3.
- Exportar CSV a archivo mediante Storage Access Framework.
- Usar una imagen real del mapa del campus si se cuenta con permiso.
- Agregar pruebas unitarias para estadísticas y validaciones.
- Crear índices Firestore si se agregan consultas remotas más complejas.
