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


**Hecho con ❤️ para los amigurumi lovers**
