# Open Beats

Reproductor de música **local**, **open source** y **sin anuncios invasivos**.

Open Beats nació porque su autor, **soviet117**, necesitaba un reproductor de
música local que no lo bombardeera con anuncios ni con promociones que
interrumpen la reproducción. Y como todo programador... lo implementó :).

## Características

- Reproduce la música de tu dispositivo (MediaStore), sin conexión a servicios.
- **Inicio**: recents, favoritas y todas tus canciones.
- **Buscar** y **Biblioteca**.
- **Now Playing** con posición, shuffle y repetir.
- **Cola de reproducción** ("up next") desde Now Playing: salta a cualquier canción de la cola.
- **Mini player** persistente sobre la barra de navegación.
- **Favoritas** con persistencia local (por canción).
- **Notificación media** con controles (anterior / play-pause / siguiente) y el
  logo de la app en la pantalla de bloqueo.
- Reproducción en **segundo plano** (foreground service de media playback).
- **Audio focus**: pausa/resume automáticos con WhatsApp, llamadas o videos, y
  detención al desconectar auriculares.

## Stack

- **Kotlin Multiplatform** con **Compose Multiplatform** (UI compartida).
- **Android**: ExoPlayer / media3 (reproducción + MediaSession/notificación).
- **iOS**: targets `iosArm64` / `iosSimulatorArm64`. Reproductor con **AVPlayer** y biblioteca con **MediaPlayer/MPMediaLibrary** (el build se verifica vía CI en GitHub Actions; requiere macOS para compilar).

## Estructura

```
shared/       Lógica + UI compartida (Compose). Source sets: commonMain,
              androidMain, iosMain (+ tests)
  audio/      PlayerController, PlayerState, AudioLibrary, MockPlayerController
              AndroidPlayerController (MediaController → PlaybackService),
              MediaStoreAudioLibrary, PlaybackService (MediaSessionService)
              iOS: AppleMusicLibrary (MPMediaLibrary), ApplePlayerController (AVPlayer)
  ui/         screens (Home, Search, Library, NowPlaying, Permission),
              components (MiniPlayer, ArtworkCache, Rows, Sections), data, theme
  composeResources/  recursos Compose (logo de la app: ic_ob_logo)
androidApp/   Aplicación Android (MainActivity + manifest + iconos/launcher)
iosApp/       Proyecto Xcode (SwiftUI) que embebe el framework estático Shared
```

## Cómo ejecutar

- **Android**: `./gradlew :androidApp:assembleDebug` y `./gradlew :androidApp:installDebug`
  (con un dispositivo conectado por ADB). Requiere JDK 21.
- **iOS**: abrir [`iosApp`](./iosApp) en Xcode y ejecutarlo.

## Tests

```bash
./gradlew :shared:testAndroidHostTest          # unit tests en host
./gradlew :shared:iosSimulatorArm64Test        # tests iOS (simulador, requiere macOS)
```

CI en GitHub Actions: `.github/workflows/ci.yml` compila y testea Android
(ubuntu) e iOS (macOS) en cada push/PR, y sube el APK debug y el framework.

## Licencia

Open source — ver el repositorio para más detalles.
