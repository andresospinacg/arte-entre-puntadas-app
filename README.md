# 🧵 Arte Entre Puntadas - Aplicación Móvil

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0-blue)
![Language](https://img.shields.io/badge/language-Kotlin-orange)
![Android](https://img.shields.io/badge/android-13%2B-green)

Una aplicación Android moderna y atractiva para promocionar amigurumis artesanales. Diseñada con una interfaz intuitiva y fluida.

## 📱 Características

- 🎨 **Interfaz hermosa**: Diseño moderno con CardViews y colores coordinados
- 📸 **Onboarding interactivo**: 4 pantallas de introducción con ViewPager2
- 🔄 **Navegación fluida**: Transiciones suaves entre pantallas
- 💾 **Persistencia**: Recuerda si ya viste la bienvenida
- 🌍 **Español**: Interfaz completamente en español
- ⚡ **Rápida**: Optimizada para rendimiento máximo
- 📱 **Responsive**: Adaptable a diferentes tamaños de pantalla

## 🚀 Inicio Rápido

### Requisitos
- Android Studio (Arctic Fox o superior)
- Java 11+
- Android SDK 33+
- Gradle 8.13+

### Instalación

1. Clonar el repositorio
```bash
git clone https://github.com/andresospinacg/arte-entre-puntadas-app.git
cd arte-entre-puntadas-app
```

2. Abrir en Android Studio
```bash
# O simplemente abre la carpeta en Android Studio
```

3. Compilar el proyecto
```bash
./gradlew assembleDebug
```

4. Instalar en emulador/dispositivo
```bash
./gradlew installDebug
```

## 📋 Pantallas Implementadas

### 1. **SplashActivity** - Pantalla de Carga
- Duración: 3 segundos
- Animación del logo
- Progress bar visual
- Transición a pantalla de bienvenida

### 2. **Bienvenida** - Onboarding (4 Pantallas)

#### Página 1: Bienvenida
Presentación principal de la aplicación con descripción del concepto

#### Página 2: Productos Únicos
Muestra los tipos de productos disponibles:
- Personajes y muñecas
- Animales adorables
- Diseños personalizados
- Accesorios especiales

#### Página 3: Hazlo Personalizado
Explica la funcionalidad de personalización

#### Página 4: Calidad Garantizada
Destaca los valores de la marca:
- Materiales premium
- Hecho completamente a mano
- Cada pieza es única
- Con amor desde Ibagué

### 3. **MainActivity** - Pantalla Principal
- Toolbar personalizado
- Tarjeta de bienvenida destacada
- 3 secciones informativas
- ScrollView para contenido expandible
- Menú de opciones

## 🛠️ Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/app_arte_entre_puntadas/
│   │   │   ├── MainActivity.kt
│   │   │   ├── activities/
│   │   │   │   ├── Bienvenida.kt
│   │   │   │   └── SplashActivity.kt
│   │   │   └── adapters/
│   │   │       └── BienvenidaPagerAdapter.kt
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── activity_bienvenida.xml
│   │       │   ├── activity_splash.xml
│   │       │   ├── page_bienvenida.xml
│   │       │   ├── page_productos.xml
│   │       │   ├── page_personalizado.xml
│   │       │   └── page_calidad.xml
│   │       ├── menu/
│   │       │   └── menu_main.xml
│   │       ├── values/
│   │       │   ├── colors.xml
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       └── drawable/
│   └── test/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 🎨 Paleta de Colores

| Color | Código | Uso |
|-------|--------|-----|
| Marca (Amarillo) | #FFC107 | Botones, toolbar |
| Fondo Oscuro | #1C1C1E | Fondo de app |
| Blanco | #FFFFFF | Textos claros |
| Negro | #000000 | Textos oscuros |
| Verde | #E8F5E9 | Cards de productos |
| Azul | #E3F2FD | Cards de personalización |
| Púrpura | #F3E5F5 | Cards de calidad |

## 📚 Dependencias

```kotlin
// Core
implementation("androidx.core:core-ktx:1.x.x")
implementation("androidx.appcompat:appcompat:1.x.x")

// UI
implementation("com.google.android.material:material:1.x.x")
implementation("androidx.activity:activity:1.x.x")
implementation("androidx.constraintlayout:constraintlayout:2.x.x")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.viewpager2:viewpager2:1.0.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
```

## 🔐 Persistencia

La app usa **SharedPreferences** para almacenar:
- `has_seen_welcome`: Boolean que indica si ya se vio la pantalla de bienvenida

Esto evita mostrar el onboarding en cada apertura de la app.

## 🎯 Funcionalidades Futuras

- [ ] Crear pantalla de catálogo de productos
- [ ] Implementar carrito de compras
- [ ] Agregar sistema de personalización
- [ ] Integrar base de datos (Room)
- [ ] API REST para productos
- [ ] Sistema de autenticación de usuarios
- [ ] Galería de fotos
- [ ] Sistema de comentarios y reseñas
- [ ] Pago en línea
- [ ] Seguimiento de órdenes

## 📝 Compilación

### Debug
```bash
./gradlew assembleDebug
# APK ubicado en: app/build/outputs/apk/debug/
```

### Release
```bash
./gradlew assembleRelease
# APK ubicado en: app/build/outputs/apk/release/
```

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## 📱 Especificaciones

- **Min SDK**: 33
- **Target SDK**: 36
- **Versión**: 1.0
- **Lenguaje**: Kotlin
- **Build Tool**: Gradle 8.13

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/AmazingFeature`)
3. Commit los cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la licencia MIT - ver archivo LICENSE para más detalles.

## 📧 Contacto

- **Autor**: Andrés Ospina
- **Email**: andresospinacg@example.com
- **GitHub**: [@andresospinacg](https://github.com/andresospinacg)

## 🙏 Agradecimientos

- Material Design 3 por Google
- Komunitas Android Ibaguense
- Todos los que comparten amigurumis artesanales

---

**Hecho con ❤️ para los amigurumi lovers**

Última actualización: Octubre 2024 | v1.0.0
