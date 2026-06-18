# Chess Mobile - Анализ архитектуры проекта

## Обзор проекта
**Chess Mobile** — это Android приложение для игры в шахматы с возможностью сетевого взаимодействия. Приложение использует REST API (Volley) и WebSocket для связи с серверной частью.

---

## 1. СТРУКТУРА ПАПОК И ОСНОВНЫЕ МОДУЛИ

### 📦 **com.example.chess** (корневой пакет)
Основные Activity классы приложения.

#### Основные файлы:
- **MainActivity.java** — Главная активность (домашний экран)
- **GameActivity.java** — Активность для отображения игровой доски
- **Loading.java** — Экран загрузки при поиске игры

---

## 2. ДЕТАЛЬНЫЙ АНАЛИЗ ПО ПАПКАМ

### 2.1. 📡 **api/** — Модуль сетевого взаимодействия

#### `endPoints.java` — Конфигурация всех API endpoints
- **Назначение**: Хранит конфигурацию сервера (IP, PORT) и пути всех API endpoints
- **Ключевые параметры**:
  - `IP = "192.168.31.229"` — IP адрес сервера
  - `PORT = "8000"` — Порт сервера
  - `URL = "http://192.168.31.229:8000"` — Полный URL базы
  - `WS_URL = "ws://192.168.31.229:8000"` — WebSocket URL

- **Endpoints**:
  ```
  Авторизация:
  - /mobile_api/app_functional/login — Вход
  - /mobile_api/app_functional/register — Регистрация
  
  Друзья:
  - /mobile_api/app_functional/friends/get — Получить список друзей
  - /mobile_api/app_functional/friends/add — Добавить друга
  - /mobile_api/app_functional/friends/delete — Удалить друга
  - /mobile_api/app_functional/friends/send_invite — Отправить приглашение
  
  Запросы на игру:
  - /mobile_api/app_functional/requests/get — Получить запросы
  - /mobile_api/app_functional/requests/cancel — Отменить запрос
  - /mobile_api/app_functional/requests/aproove — Одобрить запрос
  
  Игра:
  - /mobile_api/search/game_start/ — Поиск игры
  - /mobile_api/session/ — Сессия игры
  - /mobile_api/app_functional/active_game — Активная игра
  - /mobile_api/app_functional/ping — Проверка соединения
  - /mobile_api/app_functional/token/refresh/ — Обновление токена
  ```

#### `Requests.java` — HTTP клиент для REST запросов
- **Назначение**: Единая точка для всех HTTP запросов к серверу
- **Технология**: Использует Volley для HTTP запросов

- **Основные методы**:
  ```java
  public void POST(String path, JSONObject data, ApiCallback callback)
  // Отправляет POST запрос
  
  public void GET(String path, ApiCallback callback)
  // Отправляет GET запрос
  
  public void metaPOST(String path, JSONObject data, ApiCallback callback)
  // POST с проверкой ping-pong перед отправкой
  
  public void metaGET(String path, ApiCallback callback)
  // GET с проверкой ping-pong перед отправкой
  
  public void PingPong(ApiCallback callback)
  // Проверка соединения с сервером
  ```

- **Интерфейс callback**:
  ```java
  public interface ApiCallback {
      void onSuccess(String response);
      void onError(String error);
  }
  ```

---

### 2.2. 🔐 **authorisation/** — Модуль аутентификации

#### `LoginActivity.java` — Activity экран входа
- **Назначение**: UI для входа пользователя
- **Функциональность**:
  - Ввод username и password
  - Валидация полей (email, пароль)
  - Кнопка входа → вызов `Login.perfomLogin()`
  - Кнопка переключения на регистрацию

#### `RegActivity.java` — Activity экран регистрации
- **Назначение**: UI для регистрации нового пользователя
- **Функциональность**:
  - Ввод username, email, пароль, подтверждение пароля
  - Кнопка регистрации → вызов `Register.performRegister()`
  - Переключение на экран входа

#### **core/** — Бизнес-логика аутентификации

##### `Login.java` — Логика входа
- **Назначение**: Обработка процесса логина
- **Ключевые методы**:
  ```java
  public void perfomLogin(String username, String password, ApiCallback callback)
  // Отправляет credentials на сервер
  // При успехе сохраняет token и refresh token в файл user_data.json
  
  public void logout()
  // Удаляет файл user_data.json и выходит из аккаунта
  ```

##### `Register.java` — Логика регистрации
- **Назначение**: Обработка регистрации нового пользователя
- **Ключевые методы**:
  ```java
  public void performRegister(String username, String email, String password1, String password2, RegisterCallback callback)
  // Отправляет данные регистрации на сервер
  ```

- **Интерфейс callback**:
  ```java
  public interface RegisterCallback {
      void onSuccess(String response);
      void onError(String error);
  }
  ```

---

### 2.3. 💾 **data/** — Модуль работы с данными

#### `loadUser.java` — Управление данными пользователя
- **Назначение**: Загрузка и сохранение данных пользователя локально

- **Вложенный класс `UserData`**:
  ```java
  public static class UserData {
      private String token;        // JWT токен доступа
      private String refresh;      // Refresh токен
      private String username;     // Имя пользователя
      private String email;        // Email пользователя
  }
  ```

- **Ключевые методы**:
  ```java
  public UserData loadUserData(Context context)
  // Читает user_data.json из локального хранилища
  // Возвращает объект UserData с токенами и данными пользователя
  
  public String getFILE()
  // Возвращает имя файла: "user_data.json"
  ```

- **Путь хранения**: `/data/data/com.example.chess/files/user_data.json`

---

### 2.4. 🎮 **gameCore/** — Модуль логики игры (диалоги)

#### `GameOverDialog.java` — Диалог завершения игры
- **Назначение**: Отображение результатов игры (победа/поражение/ничья)
- **Параметры диалога**:
  ```java
  String title     // "YOU WIN" / "YOU LOSE" / "DRAW"
  String icon      // Иконка результата
  String message   // Описание результата
  String stats     // Статистика игры
  Runnable onExit  // Callback при выходе
  ```
- **Layout**: `dialog_game_over`

#### `Pause.java` — Диалог паузы
- **Назначение**: Отображение экрана паузы игры
- **Параметры**:
  ```java
  String message   // Сообщение паузы
  Runnable onExit  // Callback выхода из паузы
  ```
- **Layout**: `pause`

---

### 2.5. ⚙️ **core/** — Модуль ядра приложения

#### `MainCore.java` — Ядро основной активности
- **Назначение**: Управление активной игрой и канала WebSocket

- **Ключевые методы**:
  ```java
  public void getChannelID(ChannelCallback callback)
  // Получает channel_id активной игры с сервера
  // Нужен для подключения к WebSocket сессии игры
  ```

- **Интерфейс callback**:
  ```java
  public interface ChannelCallback {
      void onSuccess(String ID);     // Получен channel_id
      void onError(String error);    // Ошибка получения
  }
  ```

---

### 2.6. 🏠 **main_fragments/** — UI Фрагменты главного экрана

#### Основные фрагменты:

##### `home_fragment.java` — Фрагмент Домашнего экрана
- **Назначение**: Главный экран с опциями игры
- **Функциональность**:
  - Кнопка "Start Game" → переход на `Loading.java` (поиск игры)
  - Кнопка "Start with Friend" → (не реализовано)
  - Кнопка "Logout" → выход из аккаунта

##### `friends_fragment.java` — Фрагмент Друзья
- **Назначение**: Управление списком друзей
- **Функциональность**:
  - Отображение списка друзей через `RecyclerView`
  - Поле ввода для поиска/добавления друга
  - Загрузка друзей с сервера через `FriendsCore`
- **Адаптер**: `FriendAdapter`

##### `requests_fragment.java` — Фрагмент Запросы
- **Назначение**: Управление запросами на добавление в друзья и на игру
- **Функциональность**:
  - Отображение списка запросов
  - Загрузка запросов с сервера через `RequestsCore`
- **Адаптер**: `RequestAdapter`

---

#### **adapters/** — Адаптеры RecyclerView

##### `FriendAdapter.java` — Адаптер списка друзей
- **Назначение**: Отображение списка друзей в RecyclerView
- **Элемент списка layout**: `item_friend`
- **Функциональность**:
  - Отображение имени друга
  - Кнопка удаления друга
  - Обновление списка через `FriendsCore.deleteFriend()`

##### `RequestAdapter.java` — Адаптер списка запросов
- **Назначение**: Отображение запросов в RecyclerView
- **Элемент списка layout**: `item_request`
- **Функциональность**:
  - Отображение имени и типа запроса
  - Кнопка отмены/отклонения запроса
  - Обновление через `RequestsCore.CancelRequests()`

---

#### **objects/** — Модели данных

##### `Friend.java` — Класс для представления друга
```java
public class Friend {
    private Integer id;      // ID друга в системе
    private String name;     // Имя/username друга
    
    public Friend(Integer id, String name)
    public int getId()
    public String getName()
    public void setId(Integer id)
    public void setName(String name)
}
```

##### `RequestOb.java` — Класс для представления запроса
```java
public class RequestOb {
    private Integer id;      // ID запроса
    private String name;     // Имя пользователя, отправившего запрос
    private String type;     // Тип запроса ("friend" / "game" / и т.д.)
    
    public RequestOb(Integer id, String name, String type)
    public int getId()
    public String getName()
    public String getType()
}
```

---

#### **core/** — Бизнес-логика для фрагментов

##### `FriendsCore.java` — Логика управления друзьями
- **Назначение**: REST API вызовы для операций с друзьями

- **Ключевые методы**:
  ```java
  public void getFriends(FriendsCallback callback)
  // GET /mobile_api/app_functional/friends/get
  // Возвращает List<Friend> в callback
  
  public void addFriend(Integer RID, AddFriendCallback callback)
  // POST /mobile_api/app_functional/friends/add
  // RID = ID пользователя для добавления
  
  public void deleteFriend(Integer friendId, DeleteFriendCallback callback)
  // POST /mobile_api/app_functional/friends/delete
  // Удаляет друга из списка
  
  public void sendInvite(Integer RID, SendInviteCallback callback)
  // POST /mobile_api/app_functional/friends/send_invite
  // Отправляет приглашение пользователю
  ```

- **Интерфейсы callback**:
  ```java
  public interface FriendsCallback {
      void onSuccess(List<Friend> friends);
      void onError(String error);
  }
  
  public interface AddFriendCallback {
      void onSuccess(String message);
      void onError(String error);
  }
  
  public interface DeleteFriendCallback {
      void onSuccess(String message);
      void onError(String error);
  }
  
  public interface SendInviteCallback {
      void onSuccess(String message);
      void onError(String error);
  }
  ```

##### `RequestsCore.java` — Логика управления запросами
- **Назначение**: REST API вызовы для операций с запросами

- **Ключевые методы**:
  ```java
  public void GetRequests(RequestsCallback callback)
  // GET /mobile_api/app_functional/requests/get
  // Возвращает List<RequestOb> в callback
  
  public void CancelRequests(Integer RID, CancelCallback callback)
  // POST /mobile_api/app_functional/requests/cancel
  // Отменяет/отклоняет запрос
  
  public void AprooveRequests(Integer RID, AprooveCallback callback)
  // POST /mobile_api/app_functional/requests/aproove
  // Одобряет запрос на добавление в друзья/игру
  ```

- **Интерфейсы callback**:
  ```java
  public interface RequestsCallback {
      void onSuccess(List<RequestOb> requests);
      void onError(String error);
  }
  
  public interface CancelCallback {
      void onSuccess(String message);
      void onError(String error);
  }
  
  public interface AprooveCallback {
      void onSuccess(String message);
      void onError(String error);
  }
  ```

---

## 3. ОСНОВНЫЕ ACTIVITY И FLOW

### 3.1. **MainActivity.java** — Главная активность
- **Путь активации**: После успешного логина
- **Функциональность**:
  - Отображение имени пользователя
  - Навигация между фрагментами (Home, Friends, Requests)
  - Проверка активной игры через `MainCore.getChannelID()`
  - 3 кнопки навигации:
    - 🏠 Home Button → `home_fragment`
    - 👥 Requests Button → `requests_fragment`
    - 👫 Friends Button → `friends_fragment`

- **Layout**: `main`

### 3.2. **GameActivity.java** — Активность игры
- **Путь активации**: После поиска и подключения к игре
- **Функциональность**:
  - Отображение игровой доски (шахматные клетки)
  - WebSocket соединение для real-time игры
  - Отрисовка фигур и ходов
  - Сенсорное управление (выбор фигуры, перемещение)
  - Диалоги паузы и завершения игры
  - Отображение имен игроков

- **Основные компоненты**:
  ```java
  private ImageView gameTable;     // Canvas для отрисовки доски
  private Bitmap boardBitmap;      // Битмап для рисования
  private WebSocket webSocket;     // WebSocket соединение
  private TextView name_p1_g;      // Имя первого игрока
  private TextView name_p2_g;      // Имя второго игрока
  private int cellWidth, cellHeight; // Размеры клеток доски
  ```

- **WebSocket функциональность**:
  - Подключение к сессии игры
  - Отправка ходов
  - Получение ходов противника
  - Обновление состояния доски в real-time

### 3.3. **Loading.java** — Экран загрузки (поиск игры)
- **Путь активации**: Кнопка "Start Game" в `home_fragment`
- **Функциональность**:
  - WebSocket соединение для поиска игры
  - Отправка запроса на поиск игры
  - Ожидание подключения противника
  - Переход на `GameActivity` при найденной игре

- **Основные компоненты**:
  ```java
  private WebSocket webSocket;     // WebSocket клиент
  private boolean isConnected;     // Статус соединения
  private TextView labelL;         // Label статуса загрузки
  ```

---

## 4. МОДЕЛИ ДАННЫХ (Data Models)

### 4.1. **UserData** (loadUser.java)
```java
public class UserData {
    String token;        // JWT токен для доступа к API
    String refresh;      // Refresh токен для обновления сессии
    String username;     // Никнейм пользователя
    String email;        // Email пользователя
}
```

### 4.2. **Friend** (Friend.java)
```java
public class Friend {
    Integer id;          // ID в системе
    String name;         // Username
}
```

### 4.3. **RequestOb** (RequestOb.java)
```java
public class RequestOb {
    Integer id;          // ID запроса
    String name;         // Username отправителя
    String type;         // Тип запроса (friend_request, game_request и т.д.)
}
```

---

## 5. ЛОГИКА ИГРЫ В ШАХМАТЫ

### 5.1. **Поток игры**

1. **Поиск противника** (`Loading.java`):
   - Пользователь нажимает "Start Game"
   - Приложение отправляет запрос на поиск игры
   - WebSocket ожидает подключения противника

2. **Подключение** (`GameActivity.java`):
   - При подключении противника получается `channel_id`
   - WebSocket подключается к сессии игры
   - Загружается доска и данные игры

3. **Игровой процесс** (`GameActivity.java`):
   - Отрисовка доски 8x8 клеток
   - Сенсорное управление ходами
   - WebSocket отправляет ход на сервер
   - Сервер передает ход противнику
   - Обновление доски в real-time

4. **Завершение** (`GameActivity.java`):
   - Мат, пат или одна сторона сдалась
   - Показывается диалог `GameOverDialog`
   - Результат: WIN / LOSE / DRAW
   - Возврат на `MainActivity`

### 5.2. **Отрисовка доски**
- **Format**: 8x8 GridView/Canvas
- **Цвета**: Черные и белые клетки
- **Фигуры**: Отрисовываются графически
- **Ходы**: Отмечаются на доске визуально
- **Интерактивность**: Touch события для выбора фигуры и перемещения

### 5.3. **Обмен ходами по WebSocket**
```
Client → Server: { "move": "e2e4", "sessionId": "..." }
Server → Opponent: { "move": "e2e4", "player": "white" }
Opponent → Client: { "move": "e7e5", "player": "black" }
```

---

## 6. UI КОМПОНЕНТЫ И LAYOUTS

### Activity Layouts:
| Layout файл | Активность | Назначение |
|---|---|---|
| `login` | LoginActivity | Форма входа |
| `registr` | RegActivity | Форма регистрации |
| `main` | MainActivity | Главный экран с навигацией |
| `loading` | Loading | Экран поиска игры |
| (Game layout) | GameActivity | Игровая доска |

### Fragment Layouts:
| Layout файл | Фрагмент | Назначение |
|---|---|---|
| `home` | home_fragment | Главное меню |
| `friends` | friends_fragment | Список друзей |
| `requests` | requests_fragment | Список запросов |

### Dialog/Item Layouts:
| Layout файл | Использование | Назначение |
|---|---|---|
| `item_friend` | FriendAdapter | Элемент в списке друзей |
| `item_request` | RequestAdapter | Элемент в списке запросов |
| `dialog_game_over` | GameOverDialog | Диалог завершения игры |
| `pause` | Pause | Диалог паузы |

---

## 7. СЕТЕВОЕ ВЗАИМОДЕЙСТВИЕ

### 7.1. **REST API Endpoints**

#### Авторизация:
- `POST /mobile_api/app_functional/login`
  - Request: `{ "username": "...", "password": "..." }`
  - Response: `{ "token": "jwt...", "refresh": "...", "user": { "username": "...", "email": "..." } }`

- `POST /mobile_api/app_functional/register`
  - Request: `{ "username": "...", "email": "...", "password": "...", "password2": "..." }`
  - Response: `{ "status": "ok" }`

#### Друзья:
- `GET /mobile_api/app_functional/friends/get`
  - Response: `{ "friends": [{ "id": 1, "username": "..." }, ...] }`

- `POST /mobile_api/app_functional/friends/add`
  - Request: `{ "rid": 123 }`
  - Response: `{ "status": "ok", "code": "..." }`

- `POST /mobile_api/app_functional/friends/delete`
  - Request: `{ "rid": 123 }`
  - Response: `{ "status": "ok", "code": "..." }`

- `POST /mobile_api/app_functional/friends/send_invite`
  - Request: `{ "rid": 123 }`
  - Response: `{ "status": "ok", "code": "..." }`

#### Запросы:
- `GET /mobile_api/app_functional/requests/get`
  - Response: `{ "data": [{ "id": 1, "type": "friend_request", "user_from": { "name": "..." } }, ...] }`

- `POST /mobile_api/app_functional/requests/cancel`
  - Request: `{ "rid": 123 }`
  - Response: `{ "status": "ok", "code": "..." }`

- `POST /mobile_api/app_functional/requests/aproove`
  - Request: `{ "rid": 123 }`
  - Response: `{ "status": "ok", "code": "..." }`

#### Игра:
- `GET /mobile_api/app_functional/active_game`
  - Response: `{ "channel_id": "..." }` (если есть активная игра)

- `GET /mobile_api/search/game_start/`
  - Response: `{ "status": "searching" }` или `{ "channel_id": "...", "status": "found" }`

- `GET /mobile_api/session/{sessionId}`
  - Response: Данные сессии игры (доска, ходы, и т.д.)

- `GET /mobile_api/app_functional/ping`
  - Response: `{ "status": "pong" }`

- `GET /mobile_api/app_functional/token/refresh/{refreshToken}`
  - Response: `{ "token": "new_jwt_token" }`

### 7.2. **WebSocket для Real-time Игры**

#### Подключение:
```
ws://192.168.31.229:8000/mobile_api/session/{channel_id}
```

#### Сообщения:
```json
{
  "type": "move",
  "from": "e2",
  "to": "e4",
  "player": "white"
}

{
  "type": "game_over",
  "winner": "white",
  "reason": "checkmate"
}

{
  "type": "board_update",
  "board": [...]
}
```

---

## 8. ОБРАБОТКА ОШИБОК И ИСКЛЮЧЕНИЙ

### API Обработка:
- Использует callback интерфейсы для асинхронной обработки
- Обычно показывает Toast сообщения об ошибках
- Логирует ошибки через `Log.e()`, `Log.d()`

### Валидация данных:
- **LoginActivity/RegActivity**: Проверка на пустые поля, валидность email
- **API Requests**: Проверка JSON структуры перед парсингом

---

## 9. СОХРАНЕНИЕ ДАННЫХ

### Локальное хранилище:
- **Файл**: `user_data.json` (в private директории приложения)
- **Содержимое**: 
  ```json
  {
    "token": "eyJ...",
    "refresh": "eyJ...",
    "user": {
      "username": "...",
      "email": "..."
    }
  }
  ```

### SharedPreferences:
- Не используется в текущей версии

### Database:
- Не используется (все данные на сервере)

---

## 10. ТЕХНОЛОГИЧЕСКИЕ СТЕКИ

### Основные библиотеки:
- **Volley** — HTTP клиент для REST API
- **OkHttp3** — HTTP клиент для WebSocket
- **Android Support Libraries** — AppCompatActivity, Fragment и т.д.
- **JSON** — Парсинг JSON ответов

### Android версия:
- Min SDK: Не указано (нужно проверить AndroidManifest.xml)
- Target SDK: Не указано (нужно проверить build.gradle)

---

## 11. БЕЗОПАСНОСТЬ

### Текущая реализация:
- JWT токены для аутентификации
- Refresh токены для обновления сессии
- Данные сохраняются в private files (доступны только приложению)

### Потенциальные проблемы:
- ⚠️ Server IP захардкодирован в коде
- ⚠️ WebSocket использует незащищённый ws:// вместо wss://
- ⚠️ HTTP вместо HTTPS (уязвимость MITM атак)
- ⚠️ Отсутствует SSL/TLS сертификат

---

## 12. ЖИЗНЕННЫЙ ЦИКЛ ПРИЛОЖЕНИЯ

```
↓
LoginActivity / RegActivity
↓ (успешная авторизация)
↓
MainActivity (LoadUserData)
    ├─ home_fragment (показывается по умолчанию)
    ├─ friends_fragment
    └─ requests_fragment
↓ (нажата кнопка "Start Game")
↓
Loading.java (поиск игры через WebSocket)
↓ (найден противник)
↓
GameActivity (игровая сессия)
    ├─ WebSocket подключение
    ├─ Отрисовка доски
    ├─ Обработка ходов
    └─ Показ GameOverDialog при завершении
↓
MainActivity (возврат на главный экран)
```

---

## 13. ФАЙЛЫ КОНФИГУРАЦИИ

- **build.gradle.kts** — Конфигурация Gradle
- **AndroidManifest.xml** — Манифест приложения
- **gradle.properties** — Свойства проекта
- **settings.gradle.kts** — Настройки мульти-модульного проекта

---

## 14. РЕЗЮМЕ АРХИТЕКТУРЫ

**Архитектурный паттерн**: MVC (Model-View-Controller) с элементами MVP

**Слои приложения**:
1. **Presentation Layer** — Activities, Fragments, Adapters (UI)
2. **Business Logic Layer** — *Core классы (FriendsCore, RequestsCore, MainCore, Login, Register)
3. **Data Layer** — loadUser (локальное хранилище)
4. **Network Layer** — Requests, endPoints (REST API и WebSocket)

**Ключевые компоненты**:
- 📡 **API Module** — Сетевое взаимодействие
- 🔐 **Auth Module** — Аутентификация пользователей
- 👥 **Social Module** — Управление друзьями и запросами
- 🎮 **Game Module** — Логика и UI игры
- 💾 **Data Module** — Управление данными пользователя

---

## СТРУКТУРА ВСЕХ ФАЙЛОВ (Быстрый справочник)

```
com.example.chess/
├── api/
│   ├── endPoints.java ........................ Конфигурация endpoints
│   └── Requests.java ........................ HTTP клиент (Volley)
│
├── authorisation/
│   ├── LoginActivity.java ................... UI логина
│   ├── RegActivity.java .................... UI регистрации
│   └── core/
│       ├── Login.java ....................... Логика логина
│       └── Register.java ................... Логика регистрации
│
├── core/
│   └── MainCore.java ........................ Ядро главной активности
│
├── data/
│   └── loadUser.java ........................ Работа с UserData (локальное хранилище)
│
├── gameCore/
│   ├── GameOverDialog.java ................. Диалог завершения игры
│   └── Pause.java .......................... Диалог паузы
│
├── main_fragments/
│   ├── home_fragment.java .................. Фрагмент главного меню
│   ├── friends_fragment.java ............... Фрагмент друзей
│   ├── requests_fragment.java .............. Фрагмент запросов
│   │
│   ├── adapters/
│   │   ├── FriendAdapter.java .............. Адаптер для списка друзей
│   │   └── RequestAdapter.java ............ Адаптер для списка запросов
│   │
│   ├── core/
│   │   ├── FriendsCore.java ................ Бизнес-логика друзей
│   │   └── RequestsCore.java .............. Бизнес-логика запросов
│   │
│   └── objects/
│       ├── Friend.java ..................... Модель друга
│       └── RequestOb.java ................. Модель запроса
│
├── MainActivity.java ........................ Главная активность
├── GameActivity.java ........................ Активность игры (доска + WebSocket)
└── Loading.java ............................ Экран поиска игры (WebSocket)
```

---

## РЕКОМЕНДАЦИИ ДЛЯ ДОКУМЕНТИРОВАНИЯ

1. **API Documentation** — Добавить подробное описание всех REST endpoints
2. **Game Protocol** — Описать WebSocket протокол для игры
3. **Error Handling** — Документировать все возможные ошибки
4. **Code Comments** — Добавить комментарии к сложной логике
5. **Architecture Diagram** — Создать диаграмму взаимодействия компонентов
6. **Test Cases** — Добавить примеры тест-кейсов

