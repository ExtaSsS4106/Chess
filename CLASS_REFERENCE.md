# Chess Mobile - Справочник классов и модулей

## 📋 БЫСТРЫЙ СПИСОК ВСЕХ КЛАССОВ

### 🔐 АВТОРИЗАЦИЯ (authorisation/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **LoginActivity** | Activity | UI форма входа (username + password) |
| **RegActivity** | Activity | UI форма регистрации (username + email + password) |
| **Login** | Service | Обработка логика входа, сохранение токенов |
| **Register** | Service | Обработка логика регистрации |

**Callback интерфейсы:**
- `RegisterCallback` — для обработки результата регистрации

---

### 📡 API (api/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **endPoints** | Config | Конфигурация всех API endpoints и URLs |
| **Requests** | HTTP Client | Singleton для всех REST запросов (Volley) |

**Callback интерфейсы:**
- `ApiCallback` — для обработки успеха/ошибки любого запроса

**Методы Requests:**
- `POST()` — простой POST запрос
- `GET()` — простой GET запрос
- `metaPOST()` — POST с ping-pong проверкой
- `metaGET()` — GET с ping-pong проверкой
- `PingPong()` — проверка связи с сервером

---

### 💾 ДАННЫЕ (data/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **loadUser** | FileManager | Загрузка/сохранение данных пользователя из JSON |
| **loadUser.UserData** | Model | Модель данных пользователя (token, refresh, username, email) |

---

### 🎮 ЯДРО ИГРЫ (gameCore/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **GameOverDialog** | Dialog | Показ результатов игры (WIN/LOSE/DRAW) |
| **Pause** | Dialog | Диалог паузы игры |

---

### ⚙️ ЯДРО ПРИЛОЖЕНИЯ (core/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **MainCore** | Service | Получение активной игры (channel_id) |

**Callback интерфейсы:**
- `ChannelCallback` — для получения channel_id активной игры

---

### 🏠 ГЛАВНЫЙ ЭКРАН (main_fragments/)

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **home_fragment** | Fragment | Главное меню (Start Game, Start with Friend, Logout) |
| **friends_fragment** | Fragment | Список друзей + поле поиска для добавления |
| **requests_fragment** | Fragment | Список входящих запросов |

**Адаптеры:**
| Класс | Тип | Назначение |
|-------|-----|-----------|
| **FriendAdapter** | RecyclerView.Adapter | Отображение списка друзей |
| **RequestAdapter** | RecyclerView.Adapter | Отображение списка запросов |

**Бизнес-логика (core/):**
| Класс | Тип | Назначение |
|-------|-----|-----------|
| **FriendsCore** | Service | API вызовы для операций с друзьями |
| **RequestsCore** | Service | API вызовы для операций с запросами |

**Модели данных (objects/):**
| Класс | Тип | Назначение |
|-------|-----|-----------|
| **Friend** | Model | Модель друга (id, name) |
| **RequestOb** | Model | Модель запроса (id, name, type) |

**Callback интерфейсы (FriendsCore):**
- `FriendsCallback` — получение списка друзей
- `AddFriendCallback` — результат добавления друга
- `DeleteFriendCallback` — результат удаления друга
- `SendInviteCallback` — результат отправки приглашения

**Callback интерфейсы (RequestsCore):**
- `RequestsCallback` — получение списка запросов
- `CancelCallback` — результат отмены запроса
- `AprooveCallback` — результат одобрения запроса

---

### 🎮 ГЛАВНЫЕ АКТИВНОСТИ

| Класс | Тип | Назначение |
|-------|-----|-----------|
| **MainActivity** | Activity | Главный экран с навигацией между фрагментами |
| **GameActivity** | Activity | Игровая доска (8x8) + WebSocket для ходов |
| **Loading** | Activity | Экран поиска противника (WebSocket) |

---

## 📊 АРХИТЕКТУРНАЯ ДИАГРАММА

```
┌─────────────────────────────────────────────────┐
│         PRESENTATION LAYER (UI)                 │
├─────────────────────────────────────────────────┤
│ LoginActivity │ RegActivity │ MainActivity      │
│    GameActivity   │   Loading                   │
│ ┌──────────────────────────────────────────┐   │
│ │ Fragments:                               │   │
│ │  - home_fragment   - friends_fragment    │   │
│ │  - requests_fragment                     │   │
│ │                                          │   │
│ │ Adapters:                                │   │
│ │  - FriendAdapter   - RequestAdapter      │   │
│ └──────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────┐
│       BUSINESS LOGIC LAYER (Core)               │
├─────────────────────────────────────────────────┤
│ MainCore │ FriendsCore │ RequestsCore           │
│ Login    │ Register                              │
│ GameOverDialog │ Pause                           │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────┐
│        DATA LAYER                               │
├─────────────────────────────────────────────────┤
│ loadUser (JSON файл user_data.json)             │
│ Models: UserData, Friend, RequestOb             │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────┐
│       NETWORK LAYER                             │
├─────────────────────────────────────────────────┤
│ Requests (Volley для HTTP)                      │
│ endPoints (конфигурация)                        │
│ OkHttp3 (WebSocket для игры)                    │
│                                                 │
│ Endpoints:                                      │
│ ├─ /mobile_api/app_functional/login             │
│ ├─ /mobile_api/app_functional/register          │
│ ├─ /mobile_api/app_functional/friends/*         │
│ ├─ /mobile_api/app_functional/requests/*        │
│ └─ /mobile_api/session/ (WebSocket)             │
└─────────────────────────────────────────────────┘
```

---

## 🔄 ОСНОВНЫЕ ПОТОКИ ВЗАИМОДЕЙСТВИЯ

### Поток аутентификации:
```
LoginActivity 
    ↓
Login.perfomLogin() 
    ↓ 
Requests.POST(/login)
    ↓ (успех)
user_data.json сохранен (token + refresh)
    ↓
MainActivity
```

### Поток поиска игры:
```
home_fragment 
    → Loading.java (нажата кнопка Start Game)
        → WebSocket.connect(SEARCH_ROOM)
            ↓ (противник найден)
        → GameActivity
            → WebSocket.connect(SESSION)
                → GameOverDialog
                    → MainActivity
```

### Поток управления друзьями:
```
friends_fragment
    ├─ FriendsCore.getFriends() 
    │       → FriendAdapter отображает список
    │
    ├─ textAreaFriends + button (поиск нового)
    │       → FriendsCore.addFriend()
    │
    └─ FriendAdapter.deleteBtn (удаление)
            → FriendsCore.deleteFriend()
```

### Поток управления запросами:
```
requests_fragment
    ├─ RequestsCore.GetRequests()
    │       → RequestAdapter отображает список
    │
    └─ RequestAdapter.cancelBtn (отклонение)
            → RequestsCore.CancelRequests()
```

---

## 🗂️ СТРУКТУРА ФАЙЛОВ (Полная)

```
Chess_mobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/chess/
│   │   │   │   ├── api/
│   │   │   │   │   ├── endPoints.java
│   │   │   │   │   └── Requests.java
│   │   │   │   │
│   │   │   │   ├── authorisation/
│   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   ├── RegActivity.java
│   │   │   │   │   └── core/
│   │   │   │   │       ├── Login.java
│   │   │   │   │       └── Register.java
│   │   │   │   │
│   │   │   │   ├── core/
│   │   │   │   │   └── MainCore.java
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   └── loadUser.java
│   │   │   │   │
│   │   │   │   ├── gameCore/
│   │   │   │   │   ├── GameOverDialog.java
│   │   │   │   │   └── Pause.java
│   │   │   │   │
│   │   │   │   ├── main_fragments/
│   │   │   │   │   ├── home_fragment.java
│   │   │   │   │   ├── friends_fragment.java
│   │   │   │   │   ├── requests_fragment.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapters/
│   │   │   │   │   │   ├── FriendAdapter.java
│   │   │   │   │   │   └── RequestAdapter.java
│   │   │   │   │   │
│   │   │   │   │   ├── core/
│   │   │   │   │   │   ├── FriendsCore.java
│   │   │   │   │   │   └── RequestsCore.java
│   │   │   │   │   │
│   │   │   │   │   └── objects/
│   │   │   │   │       ├── Friend.java
│   │   │   │   │       └── RequestOb.java
│   │   │   │   │
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── GameActivity.java
│   │   │   │   └── Loading.java
│   │   │   │
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── login.xml
│   │   │       │   ├── registr.xml
│   │   │       │   ├── main.xml
│   │   │       │   ├── home.xml
│   │   │       │   ├── friends.xml
│   │   │       │   ├── requests.xml
│   │   │       │   ├── loading.xml
│   │   │       │   ├── item_friend.xml
│   │   │       │   ├── item_request.xml
│   │   │       │   ├── dialog_game_over.xml
│   │   │       │   └── pause.xml
│   │   │       │
│   │   │       └── values/
│   │   │           ├── strings.xml
│   │   │           ├── colors.xml
│   │   │           └── styles.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🔑 КЛЮЧЕВЫЕ КОНЦЕПЦИИ

### 1. **Асинхронность**
- Все API запросы выполняются асинхронно
- Callbacks используются для обработки результатов
- WebSocket используется для real-time коммуникации в игре

### 2. **Аутентификация**
- JWT токены для доступа к API
- Refresh токены для обновления сессии
- Данные сохраняются в файле user_data.json

### 3. **Навигация**
- Activity → Fragment (использует FragmentManager)
- 3 основные вкладки (Home, Friends, Requests)

### 4. **Модели данных**
- `UserData` — данные пользователя
- `Friend` — представление друга
- `RequestOb` — представление запроса

### 5. **WebSocket коммуникация**
- Для поиска игры (Loading.java)
- Для игровой сессии (GameActivity.java)
- Используется OkHttp3

---

## 📞 ОСНОВНЫЕ API ENDPOINTS

### Auth:
```
POST /mobile_api/app_functional/login
POST /mobile_api/app_functional/register
GET  /mobile_api/app_functional/token/refresh/{token}
GET  /mobile_api/app_functional/ping
```

### Friends:
```
GET  /mobile_api/app_functional/friends/get
POST /mobile_api/app_functional/friends/add
POST /mobile_api/app_functional/friends/delete
POST /mobile_api/app_functional/friends/send_invite
```

### Requests:
```
GET  /mobile_api/app_functional/requests/get
POST /mobile_api/app_functional/requests/cancel
POST /mobile_api/app_functional/requests/aproove
```

### Game:
```
GET  /mobile_api/app_functional/active_game
GET  /mobile_api/search/game_start/
WS   /mobile_api/session/{channel_id}
```

---

## 🎯 ТОЧКИ ВХОДА

1. **После установки**: `LoginActivity` или `RegActivity`
2. **После логина**: `MainActivity` (с загрузкой данных через `loadUser`)
3. **Нажатие кнопки "Start Game"**: `Loading` (поиск противника)
4. **При нахождении противника**: `GameActivity` (игровая сессия)
5. **При завершении игры**: Показ `GameOverDialog`, возврат на `MainActivity`

---

## 🛡️ БЕЗОПАСНОСТЬ (Текущее состояние)

✅ **Имеется:**
- JWT токены для аутентификации
- Refresh токены для обновления сессии
- Private storage для user_data.json

⚠️ **Рекомендуется добавить:**
- HTTPS вместо HTTP
- WSS вместо WS
- SSL/TLS сертификаты
- Обфускация IP адреса сервера
- Шифрование чувствительных данных

---

## 📦 ИСПОЛЬЗУЕМЫЕ БИБЛИОТЕКИ

- **androidx.appcompat** — AppCompatActivity
- **androidx.fragment** — Fragment, FragmentManager
- **androidx.recyclerview** — RecyclerView, Adapter
- **com.android.volley** — HTTP клиент для REST API
- **okhttp3** — HTTP клиент для WebSocket
- **org.json** — Парсинг JSON

---

## 💡 ИНТЕРЕСНЫЕ ДЕТАЛИ

1. **Файл user_data.json** сохраняется в приватной директории приложения
2. **Игровая доска** отрисовывается на Canvas/ImageView
3. **WebSocket** используется для real-time коммуникации (не Retrofit)
4. **Callback интерфейсы** используются для асинхронности вместо RxJava
5. **Отсутствуют** Room Database, Retrofit, Dagger
6. **простая архитектура** без MVVM/Clean Architecture

