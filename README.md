# 🎯 Chess Server

Многопользовательский сервер для игры в шахматы в реальном времени с использованием WebSocket для синхронизации игровых сессий между игроками.

**Стек:** Django + Django REST Framework + Django Channels + Daphne

---

## 📋 Содержание

- [Возможности](#возможности)
- [Требования](#требования)
- [Установка](#установка)
- [Запуск](#запуск)
- [Архитектура](#архитектура)
- [API Документация](#api-документация)
- [WebSocket API](#websocket-api)
- [Структура проекта](#структура-проекта)

---

## ✨ Возможности

- ✅ Аутентификация пользователей через JWT токены
- ✅ Поиск противника в реальном времени
- ✅ Синхронизация игровой доски через WebSocket
- ✅ Анализ ходов (проверка корректности, выявление шаха/мата)
- ✅ Отслеживание статуса игры (playing, waiting, disabled)
- ✅ Переподключение при разрыве соединения (таймаут 10 минут)
- ✅ История игр и статистика игроков
- ✅ Система друзей

---

## 📦 Требования

```
Python 3.8+
Django 4.0+
djangorestframework 3.13+
djangorestframework-simplejwt 5.0+
channels 3.0+
daphne
```

Подробнее см. [requirements.txt](requirements.txt)

---

## 🚀 Установка

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd Chess_server
```

### 2. Создание виртуального окружения

```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
# или
venv\Scripts\activate  # Windows
```

### 3. Установка зависимостей

```bash
pip install -r requirements.txt
```

### 4. Миграции базы данных

```bash
python manage.py migrate
```

### 5. Создание суперпользователя (опционально)

```bash
python manage.py createsuperuser
```

---

## ▶️ Запуск

### Режим разработки (с Daphne)

```bash
python manage.py runserver
```

Сервер запустится на `http://localhost:8000`

### Или с использованием Daphne напрямую

```bash
daphne -b 0.0.0.0 -p 8000 conf.asgi:application
```

**Примечание:** Приложение использует `InMemoryChannelLayer` для хранения данных каналов в памяти процесса. Это подходит для разработки, но для production рекомендуется использовать Redis.

---

## 🏗️ Архитектура

```
┌─────────────────┐
│   Mobile App    │
│   (Android)     │
└────────┬────────┘
         │ REST API + WebSocket
         ▼
┌─────────────────────────┐
│   Django Channels       │
│   (Async WebSocket)     │
└────────┬────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌──────────────────┐
│ Django │ │  InMemory Layer  │
│  ORM   │ │  (Channels)      │
└────┬───┘ └──────────────────┘
     ▼
┌─────────────────┐
│  PostgreSQL     │
└─────────────────┘
```

**Текущая конфигурация:**
- **Channel Layer:** InMemoryChannelLayer (для разработки)
- **Database:** PostgreSQL
- **WebSocket:** Django Channels + Daphne

### Компоненты:

1. **REST API** - для аутентификации, профилей, статистики
2. **WebSocket** - для синхронизации игровых сессий в реальном времени
3. **ChessAnalyzer** - ядро анализа ходов и состояния игры
4. **Room Manager** - управление игровыми комнатами

---

## 📡 REST API Документация

### Аутентификация

#### Получение токена

```http
POST /api/token/
Content-Type: application/json

{
  "username": "user1",
  "password": "password123"
}
```

**Ответ:**
```json
{
  "access": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refresh": "eyJ0eXAiOiJKV1QiLCJhbGc..."
}
```

#### Обновление токена

```http
POST /api/token/refresh/
Content-Type: application/json

{
  "refresh": "eyJ0eXAiOiJKV1QiLCJhbGc..."
}
```

### Пользователь

#### Получение профиля

```http
GET /mobile_api/app_functional/profile/
Authorization: Bearer <access_token>
```

**Ответ:**
```json
{
  "id": 1,
  "username": "user1",
  "email": "user1@example.com",
  "avatar": null,
  "created_at": "2024-01-15T10:30:00Z"
}
```

### Ping/Pong (Keepalive)

```http
POST /mobile_api/app_functional/ping/
Authorization: Bearer <access_token>
```

**Ответ:**
```json
{
  "status": "pong"
}
```

### Поиск игры

```http
POST /mobile_api/app_functional/start_game_search/
Authorization: Bearer <access_token>

{
  "time_control": "blitz"
}
```

**Ответ:**
```json
{
  "room_id": "cb5a467d-5f10-4335-957b-1",
  "status": "searching"
}
```

#### Отмена поиска

```http
POST /mobile_api/app_functional/cancel_game_search/
Authorization: Bearer <access_token>
```

---

## 🔌 WebSocket API

### Подключение к сессии

```
ws://localhost:8000/mobile_api/session/{room_id}/?token={access_token}
```

### Сообщения от сервера

#### 1. `connected` - Успешное подключение

```json
{
  "type": "connected",
  "is_white": true,
  "user1": "player1_username",
  "user2": "player2_username",
  "desck": null,
  "message": "Вы подключены к игре"
}
```

#### 2. `waiting` - Ожидание противника

```json
{
  "type": "waiting",
  "message": "Ожидание соперника..."
}
```

#### 3. `start_game` - Игра началась

```json
{
  "type": "start_game",
  "message": "Соперник подключился!"
}
```

#### 4. `opponent_move` - Ход противника

```json
{
  "type": "opponent_move",
  "desck": {
    "grid": [...],
    "isWhiteTurn": true
  },
  "status": "playing",
  "winner": null,
  "reason": "",
  "white_pieces": 16,
  "black_pieces": 16,
  "is_white_turn": false,
  "king_in_check": false
}
```

#### 5. `info` - Информация о состоянии доски

```json
{
  "type": "info",
  "status": "playing",
  "winner": null,
  "reason": "",
  "white_pieces": 16,
  "black_pieces": 16,
  "is_white_turn": true,
  "king_in_check": false
}
```

#### 6. `opponent_disconnected` - Противник отключился

```json
{
  "type": "opponent_disconnected",
  "message": "Игрок player2 покинул игру. Ожидание возвращения..."
}
```

#### 7. `opponent_reconnected` - Противник вернулся

```json
{
  "type": "opponent_reconnected",
  "message": "Соперник вернулся в игру"
}
```

#### 8. `error` - Ошибка

```json
{
  "type": "error",
  "message": "Соперник не вернулся. Игра завершена."
}
```

### Сообщения от клиента

#### 1. `save` - Отправить ход

```json
{
  "type": "save",
  "grid": [...],
  "isWhiteTurn": false
}
```

#### 2. `getInfo` - Запросить информацию о доске

```json
{
  "type": "getInfo",
  "grid": [...],
  "isWhiteTurn": true
}
```

---

## 📂 Структура проекта

```
Chess_server/
├── conf/                          # Django конфигурация
│   ├── settings.py               # Основные настройки
│   ├── asgi.py                   # ASGI конфигурация (Channels)
│   ├── wsgi.py                   # WSGI конфигурация
│   ├── urls.py                   # URL маршруты
│   └── WSurls.py                 # WebSocket URL маршруты
│
├── mobile_api/                    # Основное приложение
│   ├── models.py                 # Модели БД (User, Room, Game)
│   ├── serializers.py            # DRF сериализаторы
│   ├── admin.py                  # Django Admin
│   │
│   ├── app_functional/           # Функционал приложения
│   │   ├── urls.py              # REST API маршруты
│   │   ├── views/               # REST API обработчики
│   │   │   ├── auth.py          # Аутентификация
│   │   │   ├── home.py          # Профиль, статистика
│   │   │   ├── friends.py       # Система друзей
│   │   │   ├── requests.py      # Запросы в друзья
│   │   │   ├── pingpong.py      # Keepalive сигнал
│   │   │   └── core/
│   │   │       └── homeCore.py  # Бизнес логика профиля
│   │   │
│   │   └── status_codes.py       # Коды статусов и ошибок
│   │
│   ├── game_functional/          # Игровой функционал
│   │   ├── urls.py              # Game API маршруты
│   │   ├── views/
│   │   │   └── gameSession.py   # WebSocket Session консьюмер
│   │   │
│   │   └── core/
│   │       └── analyze.py       # Анализ ходов (ChessAnalyzer)
│   │
│   ├── migrations/               # БД миграции
│   └── tests.py
│
├── test_box/                      # Утилиты для тестирования
│   └── home.py
│
├── manage.py                      # Django CLI
├── requirements.txt               # Зависимости
├── api.txt                        # Документация API (legacy)
└── README.md                      # Этот файл
```

---

## 🔐 Безопасность

- ✅ JWT токены для аутентификации
- ✅ CORS настройки для мобильного приложения
- ✅ Валидация пользователя перед подключением WebSocket
- ✅ Проверка авторизации в каждой игровой сессии

### Переменные окружения (.env)

```env
DEBUG=False
SECRET_KEY=your-secret-key
ALLOWED_HOSTS=your-domain.com

DB_ENGINE=django.db.backends.postgresql
DB_NAME=chess_db
DB_USER=postgres
DB_PASSWORD=password
DB_HOST=localhost
DB_PORT=5432

CORS_ALLOWED_ORIGINS=https://your-app.com
```

---

## 📊 Модели данных

### Rooms (Игровая комната)

```python
{
  "channel_id": "UUID",           # Уникальный ID комнаты
  "user_1": User,                 # Первый игрок (белые)
  "user_2": User,                 # Второй игрок (чёрные)
  "user_1_in": bool,              # Первый игрок онлайн?
  "user_2_in": bool,              # Второй игрок онлайн?
  "status": "waiting|playing|disabled",
  "data": JSON,                   # Состояние доски
  "info": JSON,                   # Информация об игре
  "winner": User | null,          # Победитель
  "created_at": datetime,
  "updated_at": datetime
}
```

---

## 🎮 Статусы игры

| Статус | Описание |
|--------|---------|
| `waiting` | Ожидание второго игрока |
| `playing` | Игра в процессе |
| `disabled` | Игра завершена или отменена |

---

## ⏱️ Таймауты

- **Переподключение** - 600 секунд (10 минут)
- **Keepalive ping** - Рекомендуется отправлять каждые 30 секунд

---

## 🐛 Известные проблемы и решения

### Проблема: WebSocket отключение при диконнекте первого игрока

**Причина:** В Android клиенте неправильное условие при обработке `opponent_disconnected`

**Решение:** Исправьте логику в `GameActivity.java`:
```java
// ❌ НЕПРАВИЛЬНО
if (gameOverDiaolg == null && !gameOverDiaolg.isShowing()) { }

// ✅ ПРАВИЛЬНО
if (gameOverDiaolg == null || !gameOverDiaolg.isShowing()) { }
```

### Проблема: Потеря сообщений WebSocket после перезагрузки

**Причина:** Используется `InMemoryChannelLayer`, который хранит данные только в памяти

**Решение (для production):** Переключитесь на Redis Channel Layer в `settings.py`:
```python
CHANNEL_LAYERS = {
    "default": {
        "BACKEND": "channels_redis.core.RedisChannelLayer",
        "CONFIG": {
            "hosts": [("localhost", 6379)],
        },
    },
}
```

### Проблема: JWT токен истёк

**Решение:** Используйте refresh токен для получения нового access токена

---

## 🚢 Deploy на Production

### 1. Переключитесь на Redis Channel Layer

Обновите `settings.py`:
```python
CHANNEL_LAYERS = {
    "default": {
        "BACKEND": "channels_redis.core.RedisChannelLayer",
        "CONFIG": {
            "hosts": [(os.environ.get('REDIS_HOST', 'localhost'), int(os.environ.get('REDIS_PORT', 6379)))],
        },
    },
}
```

Установите зависимость:
```bash
pip install channels-redis
```

### 2. Запустите сервер через Daphne

```bash
daphne -b 0.0.0.0 -p 8000 conf.asgi:application
```

### 3. Proxy через Nginx

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

---

## 👨‍💻 Разработка

### Запуск тестов

```bash
python manage.py test mobile_api.tests
```

### Создание миграции после изменения модели

```bash
python manage.py makemigrations mobile_api
python manage.py migrate
```

---

**Последнее обновление:** 2024-01-15