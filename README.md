# AI Shopping Assistant — Android

Android-клієнт (Java) для [AI Shopping Assistant](../PROJECT_DOCUMENTATION.md). Повна архітектура й теорія — у `PROJECT_DOCUMENTATION.md` у корені проєкту, покроковий план — у `ROADMAP.md`.

## Стек
Java 17, min SDK 26, target/compile SDK 35, MVVM (ViewModel/LiveData), AndroidX, Material Components. Room, Retrofit, WorkManager тощо додаються по мірі проходження спринтів (ROADMAP.md).

## Структура пакетів
```
com.shoppingassistant
├── ui            (Activity/Fragment, адаптери)
├── model         (дата-класи)
├── network       (Retrofit-інтерфейси, DTO)
├── repository    (джерело правди: Room + мережа)
├── database      (Room: Entity, Dao, AppDatabase)
├── service       (WorkManager, SpeechRecognizer-обгортки тощо)
└── util
```

## Як запустити

1. Відкрити папку `shopping-assistant-android` в Android Studio — вона синхронізує Gradle і підхопить локальний `local.properties` (не комітиться, там шлях до Android SDK).
2. Переконатись, що для запуску проти backend локально він піднятий (`shopping-assistant-backend`, `docker compose up -d` + `./gradlew bootRun`).
3. Запустити на емуляторі/пристрої. Debug-збірка звертається до `http://10.0.2.2:8080/` (localhost хостової машини з емулятора).

Або з командного рядка:
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## CI
GitHub Actions (`.github/workflows/android-ci.yml`): lint → unit tests → build debug APK.
