# 🧵 Arte Entre Puntadas

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-orange)
![Android](https://img.shields.io/badge/Android-API%2033%2B-green)
![Supabase](https://img.shields.io/badge/Supabase-2.1.4-3ECF8E)

Aplicación Android de e-commerce para amigurumis artesanales.

## ✨ Características

- ✅ Autenticación (Email/Password + Google OAuth)
- ✅ Gestión de productos (CRUD con imágenes)
- ✅ Carrito de compras completo
- ✅ Roles de usuario (Cliente/Admin)
- ✅ Gestión de perfil

## 🚀 Instalación

### Requisitos
- Android Studio Hedgehog+
- JDK 11+
- Cuenta en [Supabase](https://supabase.com)

### Configuración

1. **Clonar**
```bash
git clone https://github.com/andresospinacg/arte-entre-puntadas-app.git
cd arte-entre-puntadas-app
```

2. **Configurar Supabase**
   - Crear proyecto en Supabase
   - Ejecutar `database/init_supabase.sql`

3. **Credenciales (`local.properties`)**
```properties
supabase.url=https://tu-proyecto.supabase.co
supabase.key=tu-anon-key
google.web.client.id=tu-web-client-id
```

4. **Compilar**
```bash
./gradlew assembleDebug
```

## 🏗️ Stack Tecnológico

- **Lenguaje**: Kotlin 2.0.21
- **Arquitectura**: MVVM + Repository Pattern
- **UI**: Material Design 3
- **Backend**: Supabase (PostgreSQL + Auth + Storage)
- **Async**: Coroutines + LiveData
- **Imágenes**: Glide

##  Especificaciones

- Min SDK: 33 (Android 13)
- Target SDK: 36
- Gradle: 8.13.0

## 🔑 Usuario Admin

```sql
-- Después de registrar un usuario
UPDATE profiles 
SET role = 'admin' 
WHERE email = 'tu-email@example.com';
```

##  Contacto

**GitHub**: [@andresospinacg](https://github.com/andresospinacg)

---

*Hecho con ❤️ para los amigurumi lovers*