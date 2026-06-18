# Chess Mobile - API Reference и Game Protocol

## 📡 REST API ДОКУМЕНТАЦИЯ

### 🔐 АВТОРИЗАЦИЯ

#### 1. LOGIN — Вход пользователя
```
POST /mobile_api/app_functional/login

Request Body:
{
  "username": "player1",
  "password": "password123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "username": "player1",
    "email": "player1@example.com"
  }
}

Response (401 Unauthorized):
{
  "status": "error",
  "message": "Invalid credentials"
}

Обработка: Login.perfomLogin()
Сохранение: user_data.json (private files)
```

#### 2. REGISTER — Регистрация пользователя
```
POST /mobile_api/app_functional/register

Request Body:
{
  "username": "newplayer",
  "email": "newplayer@example.com",
  "password": "password123",
  "password2": "password123"
}

Response (201 Created):
{
  "status": "ok",
  "message": "User registered successfully"
}

Response (400 Bad Request):
{
  "status": "error",
  "message": "Username already exists" / "Email already exists" / "Passwords do not match"
}

Обработка: Register.performRegister()
```

#### 3. REFRESH TOKEN — Обновление токена доступа
```
GET /mobile_api/app_functional/token/refresh/{refreshToken}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (401 Unauthorized):
{
  "status": "error",
  "message": "Invalid refresh token"
}

Использование: Автоматическое в Requests.metaGET() и Requests.metaPOST()
```

#### 4. PING — Проверка соединения
```
GET /mobile_api/app_functional/ping

Response (200 OK):
{
  "status": "pong"
}

Использование: Requests.PingPong() — проверка перед основными запросами
```

---

### 👥 УПРАВЛЕНИЕ ДРУЗЬЯМИ

#### 1. GET FRIENDS — Получить список друзей
```
GET /mobile_api/app_functional/friends/get
Header: Authorization: Bearer {token}

Response (200 OK):
{
  "friends": [
    {
      "id": 1,
      "username": "friend1",
      "name": "Friend One"
    },
    {
      "id": 2,
      "username": "friend2",
      "name": "Friend Two"
    }
  ]
}

Response (401 Unauthorized):
{
  "status": "error",
  "message": "Authentication required"
}

Обработка: FriendsCore.getFriends(callback)
Результат: List<Friend> в FriendsFragment через RecyclerView
```

#### 2. ADD FRIEND — Добавить друга
```
POST /mobile_api/app_functional/friends/add
Header: Authorization: Bearer {token}

Request Body:
{
  "rid": 5  // Request ID или User ID
}

Response (200 OK):
{
  "status": "ok",
  "code": "friend_added",
  "message": "Successfully added to friends"
}

Response (400 Bad Request):
{
  "status": "error",
  "code": "already_friends",
  "message": "Already in friends list"
}

Response (404 Not Found):
{
  "status": "error",
  "code": "user_not_found",
  "message": "User not found"
}

Обработка: FriendsCore.addFriend(RID, callback)
Сценарий: Клик на "Add Friend" после ввода username
```

#### 3. DELETE FRIEND — Удалить друга
```
POST /mobile_api/app_functional/friends/delete
Header: Authorization: Bearer {token}

Request Body:
{
  "rid": 1  // Friend ID
}

Response (200 OK):
{
  "status": "ok",
  "code": "friend_removed",
  "message": "Successfully removed from friends"
}

Response (404 Not Found):
{
  "status": "error",
  "code": "friend_not_found",
  "message": "Friend not found"
}

Обработка: FriendsCore.deleteFriend(friendId, callback)
Сценарий: Клик на delete button рядом с другом в RecyclerView
UI обновление: FriendAdapter удаляет элемент из списка
```

#### 4. SEND INVITE — Отправить приглашение в друзья
```
POST /mobile_api/app_functional/friends/send_invite
Header: Authorization: Bearer {token}

Request Body:
{
  "rid": 10  // User ID получателя приглашения
}

Response (200 OK):
{
  "status": "ok",
  "code": "invite_sent",
  "message": "Invite sent successfully"
}

Response (400 Bad Request):
{
  "status": "error",
  "code": "already_sent",
  "message": "Invite already sent to this user"
}

Обработка: FriendsCore.sendInvite(RID, callback)
Сценарий: Alternative way to add friends (отправить приглашение)
```

---

### 📨 УПРАВЛЕНИЕ ЗАПРОСАМИ

#### 1. GET REQUESTS — Получить входящие запросы
```
GET /mobile_api/app_functional/requests/get
Header: Authorization: Bearer {token}

Response (200 OK):
{
  "data": [
    {
      "id": 1,
      "type": "friend_request",
      "user_from": {
        "id": 5,
        "name": "player5",
        "username": "player5"
      }
    },
    {
      "id": 2,
      "type": "game_request",
      "user_from": {
        "id": 8,
        "name": "player8",
        "username": "player8"
      }
    }
  ]
}

Response (401 Unauthorized):
{
  "status": "error",
  "message": "Authentication required"
}

Обработка: RequestsCore.GetRequests(callback)
Результат: List<RequestOb> в RequestsFragment через RecyclerView

Типы запросов (type):
- "friend_request" — запрос на добавление в друзья
- "game_request" — запрос на игру
- "challenge" — вызов на игру
```

#### 2. CANCEL REQUEST — Отменить/отклонить запрос
```
POST /mobile_api/app_functional/requests/cancel
Header: Authorization: Bearer {token}

Request Body:
{
  "rid": 1  // Request ID
}

Response (200 OK):
{
  "status": "ok",
  "code": "request_cancelled",
  "message": "Request cancelled successfully"
}

Response (404 Not Found):
{
  "status": "error",
  "code": "request_not_found",
  "message": "Request not found"
}

Обработка: RequestsCore.CancelRequests(RID, callback)
Сценарий: Клик на "Reject" button в RequestsFragment
UI обновление: RequestAdapter удаляет элемент из списка
```

#### 3. APPROVE REQUEST — Одобрить запрос
```
POST /mobile_api/app_functional/requests/aproove
Header: Authorization: Bearer {token}

Request Body:
{
  "rid": 1  // Request ID
}

Response (200 OK):
{
  "status": "ok",
  "code": "request_approved",
  "message": "Request approved successfully"
}

Response (404 Not Found):
{
  "status": "error",
  "code": "request_not_found",
  "message": "Request not found"
}

Обработка: RequestsCore.AprooveRequests(RID, callback)
Сценарий: Клик на "Accept" button в RequestsFragment
Результат: Если это friend_request, пользователь добавляется в друзья
           Если это game_request, начинается игра
```

---

### 🎮 УПРАВЛЕНИЕ ИГРОЙ

#### 1. ACTIVE GAME — Получить активную игру
```
GET /mobile_api/app_functional/active_game
Header: Authorization: Bearer {token}

Response (200 OK) — есть активная игра:
{
  "channel_id": "game_session_12345",
  "opponent": "player2",
  "game_started": "2024-06-18T10:30:00Z",
  "your_color": "white"  // "white" или "black"
}

Response (204 No Content) — нет активной игры:
(пустой ответ)

Response (404 Not Found):
{
  "status": "error",
  "message": "No active game"
}

Обработка: MainCore.getChannelID(callback)
Использование: При загрузке MainActivity — проверка наличия активной игры
Если есть, сразу переход на GameActivity
```

#### 2. SEARCH GAME START — Начать поиск игры
```
GET /mobile_api/search/game_start/
Header: Authorization: Bearer {token}

Response (202 Accepted) — поиск начался:
{
  "status": "searching",
  "message": "Searching for opponent..."
}

Response (200 OK) — противник найден:
{
  "status": "found",
  "channel_id": "game_session_12346",
  "opponent": "player3",
  "your_color": "black"
}

Response (408 Request Timeout) — timeout при поиске:
{
  "status": "timeout",
  "message": "Search timeout - no opponent found"
}

Обработка: WebSocket подключение в Loading.java
Сценарий: Нажата кнопка "Start Game"
WebSocket URL: ws://192.168.31.229:8000/mobile_api/search/game_start/
```

#### 3. GAME SESSION — Получить данные сессии
```
GET /mobile_api/session/{channel_id}
Header: Authorization: Bearer {token}

Response (200 OK):
{
  "channel_id": "game_session_12345",
  "white_player": "player1",
  "black_player": "player2",
  "current_turn": "white",
  "board": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",  // FEN notation
  "moves": [
    "e2e4",
    "e7e5",
    "g1f3"
  ],
  "status": "playing"  // "playing", "checkmate", "stalemate", "draw"
}

Response (404 Not Found):
{
  "status": "error",
  "message": "Game session not found"
}

Обработка: Загрузка в GameActivity при инициализации
Использование: Получение начального состояния доски
```

---

## 🔌 WEBSOCKET ПРОТОКОЛ

### Подключение для поиска игры
```
URL: ws://192.168.31.229:8000/mobile_api/search/game_start/
Header: Authorization: Bearer {token}

Клиент → Сервер (при подключении):
{
  "type": "search",
  "action": "start_search"
}

Сервер → Клиент (противник найден):
{
  "type": "game_found",
  "channel_id": "game_session_12345",
  "opponent": "player2",
  "your_color": "white",
  "board_fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
}

Обработка: Loading.java
Переход: Если "game_found", то переход на GameActivity с channel_id
```

### Подключение для игровой сессии
```
URL: ws://192.168.31.229:8000/mobile_api/session/{channel_id}
Header: Authorization: Bearer {token}

Клиент → Сервер (отправка хода):
{
  "type": "move",
  "from": "e2",
  "to": "e4",
  "player": "white",
  "timestamp": 1718681400000
}

Альтернативный формат (algebraic notation):
{
  "type": "move",
  "notation": "e2e4",
  "player": "white"
}

Сервер → Клиент (ход принят):
{
  "type": "move_accepted",
  "from": "e2",
  "to": "e4",
  "player": "white",
  "new_board": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR"
}

Сервер → Оппонент (ход противника):
{
  "type": "opponent_move",
  "from": "e2",
  "to": "e4",
  "player": "white",
  "board": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR"
}

Сервер → Клиент (ошибка хода):
{
  "type": "move_error",
  "reason": "Invalid move",
  "message": "This piece cannot move to that square"
}

Сервер → Клиент (проверка):
{
  "type": "board_state",
  "current_board": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR",
  "current_turn": "black",
  "check": false,
  "possible_moves": ["e7e5", "e7e6", "c7c6"]
}

Сервер → Оба (окончание игры):
{
  "type": "game_over",
  "winner": "white",  // или "black" для поражения, null для ничьи
  "reason": "checkmate",  // "checkmate", "resign", "timeout", "stalemate", "draw"
  "final_board": "...",
  "status": "finished"
}

Сервер → Клиент (отключение противника):
{
  "type": "opponent_disconnected",
  "message": "Opponent has disconnected"
}

Обработка: GameActivity.java
Методы: WebSocketListener.onOpen(), onMessage(), onFailure(), onClosed()
```

### Сообщения паузы и управления
```
Клиент → Сервер (пауза):
{
  "type": "pause",
  "player": "white"
}

Сервер → Оба (пауза установлена):
{
  "type": "paused",
  "paused_by": "white"
}

Клиент → Сервер (возобновление):
{
  "type": "resume",
  "player": "white"
}

Сервер → Оба (игра возобновлена):
{
  "type": "resumed",
  "resumed_by": "white"
}

Клиент → Сервер (сдача):
{
  "type": "resign",
  "player": "white"
}

Сервер → Оба (сдача):
{
  "type": "game_over",
  "winner": "black",
  "reason": "resign",
  "resigned_by": "white"
}

Клиент → Сервер (предложение ничьи):
{
  "type": "draw_offer",
  "player": "white"
}

Сервер → Оппонент (предложение ничьи):
{
  "type": "draw_offer_received",
  "offered_by": "white"
}

Клиент → Сервер (принятие ничьи):
{
  "type": "draw_accept",
  "player": "black"
}

Сервер → Оба (ничья согласована):
{
  "type": "game_over",
  "winner": null,
  "reason": "draw",
  "draw_agreed": true
}

Клиент → Сервер (отклонение ничьи):
{
  "type": "draw_decline",
  "player": "black"
}
```

### Heartbeat / Keep-alive
```
Сервер → Клиент (периодический ping):
{
  "type": "ping"
}

Клиент → Сервер (ответный pong):
{
  "type": "pong"
}

Использование: Проверка соединения каждые 30 сек
Обработка: GameActivity.java — автоматическое отправление pong
```

---

## 🔄 ТИПИЧНЫЕ СЦЕНАРИИ API ВЫЗОВОВ

### Сценарий 1: Запуск приложения (Login)
```
1. User вводит username и password
2. LoginActivity.onClick(loginBtn)
3. Login.perfomLogin(username, password, callback)
4. Requests.POST(/login, {username, password})
5. Сервер возвращает token + refresh + user data
6. Сохранение в user_data.json
7. startActivity(MainActivity)
```

### Сценарий 2: Загрузка главного экрана
```
1. MainActivity.onCreate()
2. loadUser.loadUserData(context) → читает user_data.json
3. MainCore.getChannelID(callback) → GET /active_game
4. Если есть активная игра → startActivity(GameActivity)
5. Иначе → показать home_fragment
6. home_fragment загружается с тремя кнопками
```

### Сценарий 3: Просмотр друзей
```
1. friends_fragment.onCreateView()
2. FriendsCore.getFriends(callback)
3. Requests.metaGET(/friends/get)
4. Сервер возвращает список друзей
5. FriendAdapter показывает список в RecyclerView
6. При скролле видны все друзья + delete button
```

### Сценарий 4: Добавление друга
```
1. friends_fragment: пользователь вводит username в textAreaFriends
2. Клик на кнопку поиска/добавления
3. FriendsCore.addFriend(RID, callback)
4. Requests.metaPOST(/friends/add, {rid})
5. Сервер возвращает {status: "ok"}
6. Toast: "Успешно добавлено"
7. Список друзей обновляется (reload нужен)
```

### Сценарий 5: Поиск и начало игры
```
1. home_fragment: клик на "Start Game"
2. startActivity(Loading)
3. Loading.onCreate() → WebSocket.connect(SEARCH_ROOM)
4. Сервер начинает поиск противника
5. WebSocketListener.onMessage() → "game_found"
6. Получен channel_id
7. startActivity(GameActivity, channel_id)
8. GameActivity.onCreate() → WebSocket.connect(SESSION)
9. Загружается доска, начинается игра
```

### Сценарий 6: Ход в игре
```
1. GameActivity: пользователь тапает на фигуру e2
2. Возможные ходы выделяются
3. Пользователь тапает на e4 (целевой квадрат)
4. WebSocket.send({type: "move", from: "e2", to: "e4"})
5. Сервер: валидирует ход, отправляет оппоненту
6. WebSocketListener.onMessage() → ход принят
7. Доска обновляется
8. Ход противника: WebSocket.onMessage() → {type: "opponent_move"}
9. Обновление доски для противника
```

### Сценарий 7: Завершение игры
```
1. Одна из сторон: мат, сдача, timeout
2. WebSocket.onMessage() → {type: "game_over", winner: "white"}
3. GameActivity: показ GameOverDialog
4. Диалог показывает: WIN/LOSE/DRAW + статистика
5. Кнопка Exit → dismiss диалог
6. Возврат на MainActivity
```

---

## ⚠️ ОБРАБОТКА ОШИБОК

### HTTP ошибки:
- **400 Bad Request** — неверные параметры
- **401 Unauthorized** — токен невалиден/истёк
- **403 Forbidden** — доступ запрещен
- **404 Not Found** — ресурс не найден
- **500 Internal Server Error** — ошибка сервера

### WebSocket ошибки:
- **Connection Failed** — не удалось подключиться
- **Connection Lost** — соединение разорвано
- **Invalid Message** — неверный формат сообщения
- **Timeout** — нет ответа за 30 сек

### Обработка в коде:
```java
// REST API
Requests.POST(path, data, new ApiCallback() {
    @Override
    public void onSuccess(String response) {
        // Обработка успеха
    }
    
    @Override
    public void onError(String error) {
        // Обработка ошибки
        Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
    }
});

// WebSocket
webSocket.send(message);
// При ошибке: WebSocketListener.onFailure() вызывается
```

---

## 📊 ПРИМЕРЫ ОТВЕТОВ

### Успешный login:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VybmFtZSI6InBsYXllcjEiLCJleHAiOjE3MTg3Njc4MDB9.signature",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ0eXBlIjoicmVmcmVzaCIsImV4cCI6MTcxOTM3MjYwMH0.signature",
  "user": {
    "id": 1,
    "username": "player1",
    "email": "player1@example.com"
  }
}
```

### Список друзей:
```json
{
  "friends": [
    {
      "id": 2,
      "username": "player2",
      "name": "John Doe"
    },
    {
      "id": 3,
      "username": "player3",
      "name": "Jane Smith"
    }
  ]
}
```

### Список запросов:
```json
{
  "data": [
    {
      "id": 1,
      "type": "friend_request",
      "user_from": {
        "id": 5,
        "name": "player5",
        "username": "player5"
      }
    },
    {
      "id": 2,
      "type": "game_request",
      "user_from": {
        "id": 8,
        "name": "player8",
        "username": "player8"
      }
    }
  ]
}
```

### Активная игра:
```json
{
  "channel_id": "game_session_12345",
  "opponent": "player2",
  "game_started": "2024-06-18T10:30:00Z",
  "your_color": "white"
}
```

---

## 🔐 ЗАГОЛОВКИ И АУТЕНТИФИКАЦИЯ

Все защищённые endpoints требуют:
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

Пример в Volley:
```java
@Override
public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + token);
    headers.put("Content-Type", "application/json");
    return headers;
}
```

