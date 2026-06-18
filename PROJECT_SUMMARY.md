# Chess Mobile - Резюме проекта

## 📋 КРАТКОЕ РЕЗЮМЕ

**Название**: Chess Mobile  
**Тип**: Android приложение для игры в шахматы  
**Язык программирования**: Java  
**Build система**: Gradle (Kotlin DSL)  
**Минимальная версия Android**: [Не указана в проекте]  
**Целевая версия Android**: [Не указана в проекте]  

---

## 🎯 ОСНОВНАЯ ФУНКЦИОНАЛЬНОСТЬ

1. **Аутентификация**
   - Регистрация новых пользователей
   - Вход в аккаунт
   - Refresh токены для сохранения сессии

2. **Социальные функции**
   - Управление списком друзей (добавление, удаление)
   - Просмотр входящих запросов (друзья, игры)
   - Принятие/отклонение запросов

3. **Игровой процесс**
   - Поиск противника через WebSocket
   - Real-time игра в шахматы (8x8 доска)
   - Отслеживание ходов
   - Определение результата (победа, поражение, ничья)

4. **UI/UX**
   - Bottom navigation (3 вкладки)
   - RecyclerView для списков
   - Диалоги для результатов и паузы

---

## 📦 СТРУКТУРА МОДУЛЕЙ

| Модуль | Файлы | Назначение |
|--------|-------|-----------|
| **api/** | endPoints.java, Requests.java | REST API и конфигурация |
| **authorisation/** | LoginActivity, RegActivity, Login, Register | Аутентификация пользователей |
| **core/** | MainCore.java | Ядро приложения (активные игры) |
| **data/** | loadUser.java | Управление данными пользователя |
| **gameCore/** | GameOverDialog, Pause | Диалоги игры |
| **main_fragments/** | 3 фрагмента + адаптеры + объекты | UI главного экрана |

---

## 🗂️ ИЕРАРХИЯ ФАЙЛОВ

```
Chess_mobile/
├── app/
│   ├── src/main/java/com/example/chess/
│   │   ├── api/                    [2 файла]  Сетевое взаимодействие
│   │   ├── authorisation/          [4 файла]  Логин и регистрация
│   │   ├── core/                   [1 файл]   Ядро приложения
│   │   ├── data/                   [1 файл]   Управление данными
│   │   ├── gameCore/               [2 файла]  Диалоги игры
│   │   ├── main_fragments/         [6 файлов] + подпапки
│   │   │   ├── adapters/           [2 файла]  Адаптеры
│   │   │   ├── core/               [2 файла]  Бизнес-логика
│   │   │   └── objects/            [2 файла]  Модели данных
│   │   └── Activity классы         [3 файла]  MainActivity, GameActivity, Loading
│   │
│   ├── src/main/res/layout/        [~11 файлов] Layout XML
│   ├── src/main/res/values/        [~3 файла]  Ресурсы
│   ├── AndroidManifest.xml
│   └── build.gradle.kts
│
├── build.gradle.kts                [Конфигурация Gradle]
├── settings.gradle.kts             [Мульти-модульный проект]
├── gradle.properties               [Свойства Gradle]
└── README.md                       [Документация проекта]

ИТОГО: ~31 Java файл + конфиги
```

---

## 🔄 ОСНОВНЫЕ ПОТОКИ ДАННЫХ

### 1️⃣ Аутентификация
```
LoginActivity → Login.perfomLogin() 
    → Requests.POST(/login) 
    → Сохранение в user_data.json 
    → MainActivity
```

### 2️⃣ Загрузка друзей
```
friends_fragment.onCreateView() 
    → FriendsCore.getFriends() 
    → Requests.metaGET(/friends/get) 
    → FriendAdapter показывает список
```

### 3️⃣ Начало игры
```
home_fragment 
    → Loading (WebSocket поиск) 
    → GameActivity (WebSocket сессия) 
    → GameOverDialog 
    → MainActivity
```

### 4️⃣ Ход в игре
```
GameActivity.onTouch() 
    → WebSocket.send({type: "move", from, to}) 
    → Сервер → Оппонент 
    → GameActivity обновляет доску
```

---

## 📊 КОЛИЧЕСТВО КОДА

| Компонент | Количество файлов |
|-----------|-------------------|
| Java классы | 23 |
| Layout XML | ~11 |
| Ресурсы (values) | ~3 |
| Конфигурационные | 4 |
| **Итого** | **~41** |

---

## 🔌 ТЕХНОЛОГИИ И БИБЛИОТЕКИ

### Основное:
- **Android API 30+** — минимум не указан
- **Java 8+** — лямбда выражения используются

### Сетевые:
- **Volley** — HTTP клиент для REST API
- **OkHttp3** — WebSocket клиент

### UI:
- **AndroidX** — Fragment, RecyclerView, AppCompatActivity
- **RecyclerView** — списки друзей и запросов

### Утилиты:
- **org.json** — парсинг JSON

### Не используются:
- ❌ Retrofit (использован Volley)
- ❌ RxJava (использованы callbacks)
- ❌ Room Database (всё на сервере)
- ❌ Dagger (нет DI)
- ❌ LiveData / ViewModel (нет MVVM)
- ❌ Data Binding

---

## 🎮 ЗАПУСК И ТЕСТИРОВАНИЕ

### Требования:
- Android SDK 30+
- Gradle 7.0+
- JDK 8+

### Конфигурация сервера:
```
IP: 192.168.31.229
PORT: 8000
BASE URL: http://192.168.31.229:8000
```

⚠️ **Это значения по умолчанию в `endPoints.java`**

### Перед запуском:
1. Проверить наличие сервера на `192.168.31.229:8000`
2. При необходимости изменить IP в `endPoints.java`
3. Скомпилировать: `./gradlew build`
4. Запустить: `./gradlew installDebug` или из IDE

---

## 🔐 БЕЗОПАСНОСТЬ

### ✅ Реализовано:
- JWT токены для доступа
- Refresh токены для обновления сессии
- Private storage для user_data.json

### ⚠️ Потенциальные проблемы:
- IP сервера захардкодирован
- Используется HTTP вместо HTTPS
- Используется ws:// вместо wss://
- Отсутствует шифрование при передаче
- Токены хранятся в plaintext файле

### 🛡️ Рекомендации:
1. Использовать HTTPS и WSS
2. Добавить SSL certificate pinning
3. Зашифровать user_data.json
4. Использовать SharedPreferences с EncryptedSharedPreferences
5. Обфускировать IP адрес сервера

---

## 📱 АКТИВНОСТИ И ФРАГМЕНТЫ

### Activities:
1. **LoginActivity** — экран входа
2. **RegActivity** — экран регистрации
3. **MainActivity** — главный экран (с фрагментами)
4. **GameActivity** — экран игры (доска)
5. **Loading** — экран поиска игры

### Fragments (в MainActivity):
1. **home_fragment** — главное меню
2. **friends_fragment** — список друзей
3. **requests_fragment** — входящие запросы

### Dialogs:
1. **GameOverDialog** — результаты игры
2. **Pause** — пауза игры

---

## 🔄 ЖИЗНЕННЫЙ ЦИКЛ ПРИЛОЖЕНИЯ

```
[Start]
  ↓
[LoginActivity / RegActivity]
  ↓
[MainActivity] ← загрузка друзей, запросов
  ├─ home_fragment (по умолчанию)
  ├─ friends_fragment (по нажатию кнопки)
  └─ requests_fragment (по нажатию кнопки)
  ↓
[Home Fragment - нажата кнопка "Start Game"]
  ↓
[Loading] ← WebSocket поиск противника
  ↓
[GameActivity] ← WebSocket сессия игры
  ├─ Отрисовка доски
  ├─ Обмен ходами
  └─ Показ результатов (GameOverDialog)
  ↓
[MainActivity] ← возврат на главный экран
```

---

## 📡 API ENDPOINTS

### Auth:
- `POST /mobile_api/app_functional/login` — вход
- `POST /mobile_api/app_functional/register` — регистрация
- `GET /mobile_api/app_functional/token/refresh/{token}` — обновление токена

### Friends:
- `GET /mobile_api/app_functional/friends/get` — получить друзей
- `POST /mobile_api/app_functional/friends/add` — добавить друга
- `POST /mobile_api/app_functional/friends/delete` — удалить друга
- `POST /mobile_api/app_functional/friends/send_invite` — отправить приглашение

### Requests:
- `GET /mobile_api/app_functional/requests/get` — получить запросы
- `POST /mobile_api/app_functional/requests/cancel` — отменить запрос
- `POST /mobile_api/app_functional/requests/aproove` — одобрить запрос

### Game:
- `GET /mobile_api/app_functional/active_game` — активная игра
- `GET /mobile_api/search/game_start/` — начать поиск
- `WS /mobile_api/session/{channel_id}` — WebSocket игровой сессии

---

## 📂 КОНФИГУРАЦИОННЫЕ ФАЙЛЫ

### build.gradle.kts (Проект)
```kotlin
plugins {
    id("com.android.application")
}

android {
    compileSdk = 34  // или другое значение
    defaultConfig {
        applicationId = "com.example.chess"
        minSdk = 21     // предполагаемо
        targetSdk = 34  // предполагаемо
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.x.x")
    implementation("androidx.fragment:fragment:1.x.x")
    implementation("androidx.recyclerview:recyclerview:1.x.x")
    
    // Network
    implementation("com.android.volley:volley:1.x.x")
    implementation("com.squareup.okhttp3:okhttp:4.x.x")
    
    // JSON
    // org.json встроена в Android
}
```

### AndroidManifest.xml (примерно)
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application>
        <activity android:name="com.example.chess.authorisation.LoginActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity android:name="com.example.chess.MainActivity" />
        <activity android:name="com.example.chess.GameActivity" />
        <activity android:name="com.example.chess.Loading" />
        <!-- и т.д. -->
    </application>
</manifest>
```

---

## 🐛 ИЗВЕСТНЫЕ ПРОБЛЕМЫ И УЛУЧШЕНИЯ

### Текущие ограничения:
1. Только одна активная игра одновременно
2. Отсутствует offline режим
3. Нет истории ходов
4. Нет рейтинговой системы
5. Нет чата между игроками

### Рекомендуемые улучшения:
1. Реализовать MVVM архитектуру
2. Добавить Room Database для кеша
3. Добавить пушнотификации (FCM)
4. Реализовать Observer паттерн (LiveData)
5. Добавить Unit тесты
6. Добавить Instrumentation тесты
7. Обновить на Kotlin
8. Использовать Retrofit вместо Volley
9. Добавить обработку ошибок сетевых запросов
10. Реализовать Snackbar вместо Toast

---

## 📝 ДОКУМЕНТАЦИЯ ПРОЕКТА

В директории проекта должны присутствовать:

1. **ARCHITECTURE_ANALYSIS.md** — подробный анализ архитектуры (этот файл)
2. **CLASS_REFERENCE.md** — справочник всех классов и интерфейсов
3. **API_DOCUMENTATION.md** — документация REST API и WebSocket протокола
4. **PROJECT_SUMMARY.md** — этот файл (краткое резюме)
5. **README.md** — инструкции по установке и запуску

---

## 🎓 КЛЮЧЕВЫЕ ПАТТЕРНЫ ПРОЕКТИРОВАНИЯ

### Используемые:
1. **Callback паттерн** — для асинхронных операций
2. **Adapter паттерн** — для RecyclerView
3. **Fragment паттерн** — для модульного UI
4. **Singleton паттерн** — endPoints класс
5. **Observer паттерн** — WebSocket listeners

### Не используемые:
- ❌ MVP/MVVM архитектура
- ❌ Repository паттерн
- ❌ Dependency Injection
- ❌ Factory паттерн

---

## 🌐 СЕТЕВАЯ АРХИТЕКТУРА

```
┌─────────────────┐
│  Mobile Client  │ (Android приложение)
└────────┬────────┘
         │
    ┌────┴──────────────┬──────────────┐
    │                   │              │
    │                   │              │
[REST API]         [WebSocket]    [Local Storage]
(Volley)          (OkHttp3)       (user_data.json)
    │                   │              │
    │                   │              │
    │          ┌────────┴────────┐     │
    │          │                 │     │
    ↓          ↓                 ↓     ↓
    └──────────┴────────┬────────┘─────┘
                        │
                        │
              ┌─────────▼──────────┐
              │  Backend Server    │
              │ (192.168.31.229:8000)
              └────────┬───────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ↓              ↓              ↓
    [Database]  [Game Logic]  [User Auth]
```

---

## 📊 СТАТИСТИКА ПРОЕКТА

| Метрика | Значение |
|---------|----------|
| Основных Java файлов | 23 |
| Layout XML файлов | ~11 |
| Пакетов | 8 |
| Activities | 5 |
| Fragments | 3 |
| Adapters | 2 |
| Models | 3 |
| Services | ~6 |
| Callback интерфейсов | ~10 |
| REST endpoints | 17 |
| WebSocket сообщений | ~20 |

---

## 🚀 БЫСТРЫЙ СТАРТ

### 1. Установка
```bash
cd Chess_mobile
./gradlew build
```

### 2. Запуск на эмуляторе
```bash
./gradlew installDebug
```

### 3. Запуск на устройстве
```bash
./gradlew installRelease
```

### 4. Изменение IP сервера
Отредактировать `/app/src/main/java/com/example/chess/api/endPoints.java`:
```java
private String IP = "192.168.31.229";  // Изменить на нужный IP
private String PORT = "8000";            // Изменить порт если нужно
```

---

## 📞 ОСНОВНЫЕ КОНТАКТЫ

### REST API параметры:
- **Base URL**: `http://192.168.31.229:8000`
- **WebSocket URL**: `ws://192.168.31.229:8000`
- **Аутентификация**: JWT Bearer токены

### Основные endpoints:
- Login: `/mobile_api/app_functional/login`
- Friends: `/mobile_api/app_functional/friends/*`
- Game: `/mobile_api/search/game_start/`, `/mobile_api/session/{id}`

---

## ✅ ЧЕКЛИСТ ФУНКЦИОНАЛЬНОСТИ

- ✅ Регистрация и вход
- ✅ Управление друзьями
- ✅ Просмотр запросов
- ✅ Поиск противника
- ✅ Real-time игра в шахматы
- ✅ Отслеживание ходов
- ✅ Диалоги результатов
- ✅ Bottom navigation
- ✅ RecyclerView списки
- ✅ WebSocket коммуникация
- ⚠️ Отсутствует: история ходов
- ⚠️ Отсутствует: рейтинговая система
- ⚠️ Отсутствует: чат
- ⚠️ Отсутствует: offline режим

---

**Документация создана: 18.06.2026**  
**Версия проекта: mobile_0.1**  
**Статус: В разработке**

