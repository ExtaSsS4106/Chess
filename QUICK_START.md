# 🚀 Chess Mobile - Quick Start Guide

## ⚡ 5-минутный старт

### Шаг 1: Клонирование и подготовка (2 мин)
```bash
cd Chess_mobile
./gradlew clean
./gradlew build
```

### Шаг 2: Настройка IP сервера (1 мин)
Если сервер на другом адресе, отредактируйте:
```
app/src/main/java/com/example/chess/api/endPoints.java
```
Измените:
```java
private String IP = "192.168.31.229";  // ← Ваш IP сервера
private String PORT = "8000";           // ← Ваш порт сервера
```

### Шаг 3: Запуск (2 мин)
```bash
# На эмуляторе
./gradlew installDebug

# Или в Android Studio: Run → Run 'app'
```

### Шаг 4: Тестирование
- Откройте приложение
- Нажмите "Register" чтобы создать аккаунт
- Или нажмите "Login" если аккаунт уже есть

---

## 🗂️ СТРУКТУРА ФАЙЛОВ (самое важное)

```
Chess_mobile/
├── app/src/main/java/com/example/chess/
│   ├── api/endPoints.java ...................... ⚙️ КОНФИГУРАЦИЯ (IP, PORT, endpoints)
│   ├── authorisation/
│   │   ├── LoginActivity.java
│   │   ├── RegActivity.java
│   │   └── core/
│   │       ├── Login.java ....................... 🔐 Логика входа
│   │       └── Register.java ................... 🔐 Логика регистрации
│   ├── main_fragments/
│   │   ├── home_fragment.java .................. 🏠 Главное меню
│   │   ├── friends_fragment.java ............... 👥 Друзья
│   │   ├── requests_fragment.java .............. 📨 Запросы
│   │   └── core/
│   │       ├── FriendsCore.java ................ API для друзей
│   │       └── RequestsCore.java .............. API для запросов
│   ├── MainActivity.java ....................... 📱 Главная активность
│   ├── GameActivity.java ....................... 🎮 Игровая доска + WebSocket
│   └── Loading.java ........................... ⏳ Поиск игры (WebSocket)
│
└── [ДОКУМЕНТАЦИЯ]
    ├── README_DOCS.md .......................... 📚 Этот файл (навигация)
    ├── ARCHITECTURE_ANALYSIS.md ............... 🏗️ Полный анализ
    ├── CLASS_REFERENCE.md ..................... 📖 Справочник классов
    ├── API_DOCUMENTATION.md ................... 📡 REST API + WebSocket
    └── PROJECT_SUMMARY.md ..................... 📊 Резюме проекта
```

---

## 🔑 КЛЮЧЕВЫЕ ФАЙЛЫ ДЛЯ ИЗМЕНЕНИЙ

### Если нужно изменить...

| Что | Где |
|-----|-----|
| **IP сервера** | `api/endPoints.java` линия ~2-4 |
| **Endpoints API** | `api/endPoints.java` линии ~9-23 |
| **Логику входа** | `authorisation/core/Login.java` |
| **Логику регистрации** | `authorisation/core/Register.java` |
| **Управление друзьями** | `main_fragments/core/FriendsCore.java` |
| **Управление запросами** | `main_fragments/core/RequestsCore.java` |
| **Главное меню** | `main_fragments/home_fragment.java` |
| **Игровую логику** | `GameActivity.java` |

---

## 📡 ГЛАВНЫЕ ENDPOINTS

```
Login:          POST   /mobile_api/app_functional/login
Register:       POST   /mobile_api/app_functional/register
Get Friends:    GET    /mobile_api/app_functional/friends/get
Add Friend:     POST   /mobile_api/app_functional/friends/add
Delete Friend:  POST   /mobile_api/app_functional/friends/delete
Get Requests:   GET    /mobile_api/app_functional/requests/get
Cancel Request: POST   /mobile_api/app_functional/requests/cancel
Game Session:   WS     /mobile_api/session/{channel_id}
```

Полная документация → [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 💾 ЛОКАЛЬНОЕ ХРАНИЛИЩЕ

### Где хранится token пользователя?
```
/data/data/com.example.chess/files/user_data.json
```

**Структура файла:**
```json
{
  "token": "eyJ...",
  "refresh": "eyJ...",
  "user": {
    "username": "player1",
    "email": "player1@example.com"
  }
}
```

**Класс для работы:** `data/loadUser.java`

---

## 🔐 АУТЕНТИФИКАЦИЯ

### Поток логина:
```
1. Пользователь вводит username + password
2. Login.perfomLogin() отправляет на сервер
3. Сервер возвращает token + refresh + user data
4. Сохраняется в user_data.json
5. Переход на MainActivity
```

### Все последующие запросы требуют:
```
Header: Authorization: Bearer {token}
```

Это делается автоматически в `Requests.java`

---

## 🎮 ИГРОВОЙ ПРОЦЕСС

### Поток игры:
```
home_fragment
  ↓ (кнопка "Start Game")
Loading (WebSocket поиск противника)
  ↓ (противник найден)
GameActivity (WebSocket сессия)
  ├─ Отрисовка доски
  ├─ Обмен ходами
  └─ WebSocket → другому игроку
  ↓ (игра завершена)
GameOverDialog (результаты)
  ↓ (кнопка "Exit")
MainActivity
```

---

## 🔄 АСИНХРОННОСТЬ И CALLBACKS

Все сетевые операции асинхронные:

```java
// Пример: получение друзей
FriendsCore.getFriends(new FriendsCore.FriendsCallback() {
    @Override
    public void onSuccess(List<Friend> friends) {
        // Здесь работать с полученными друзьями
        adapter.updateList(friends);
    }
    
    @Override
    public void onError(String error) {
        // Обработать ошибку
        Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
    }
});
```

---

## 🐛 ЧАСТЫЕ ПРОБЛЕМЫ И РЕШЕНИЯ

### Проблема 1: "Connection refused" / "Unable to connect to server"
**Решение:**
```
1. Проверить, что сервер запущен на 192.168.31.229:8000
2. Убедиться, что сетевые интерфейсы доступны
3. Изменить IP в endPoints.java если нужно
```

### Проблема 2: "Неверные credentials" при логине
**Решение:**
```
1. Убедиться, что пользователь зарегистрирован на сервере
2. Проверить правильность username/password
3. Посмотреть логи сервера
```

### Проблема 3: Токен истёк ("401 Unauthorized")
**Решение:**
```
1. Это автоматически обновляется через метаGET() / metaPOST()
2. Если не помогает, нужно перелогиниться
3. Проверить timeout токенов на сервере
```

### Проблема 4: WebSocket не подключается
**Решение:**
```
1. Убедиться, что серверу поддерживает WebSocket
2. Проверить URL: ws://192.168.31.229:8000/...
3. Посмотреть логи OkHttp в Android Studio
```

### Проблема 5: "Null Pointer Exception" при работе с друзьями
**Решение:**
```
1. Проверить, что список инициализирован
2. Убедиться, что адаптер получил данные
3. Добавить null check перед работой с данными
```

---

## 📊 ОСНОВНЫЕ КЛАССЫ И ИХ РОЛЬ

### REST API:
- **Requests.java** — главный класс для всех HTTP запросов
- **endPoints.java** — конфигурация всех endpoints

### Аутентификация:
- **Login.java** — логика входа
- **Register.java** — логика регистрации
- **LoginActivity.java** — UI входа
- **RegActivity.java** — UI регистрации

### Социальные функции:
- **FriendsCore.java** — логика работы с друзьями
- **RequestsCore.java** — логика работы с запросами
- **friends_fragment.java** — UI друзей
- **requests_fragment.java** — UI запросов

### Игра:
- **GameActivity.java** — главная активность игры + WebSocket
- **Loading.java** — поиск игры
- **GameOverDialog.java** — результаты
- **Pause.java** — пауза

---

## 📚 КОГДА ЧТО СМОТРЕТЬ

| Задача | Смотрите |
|--------|----------|
| Изменить IP сервера | `endPoints.java` |
| Добавить новый endpoint | `endPoints.java` + `API_DOCUMENTATION.md` |
| Изменить логику друзей | `FriendsCore.java` + `friends_fragment.java` |
| Фиксить ошибки при логине | `Login.java` + `LoginActivity.java` |
| Разобраться с WebSocket | `GameActivity.java` + `API_DOCUMENTATION.md` раздел WebSocket |
| Понять архитектуру | `ARCHITECTURE_ANALYSIS.md` |
| Найти класс по названию | `CLASS_REFERENCE.md` |

---

## ✅ ПРОВЕРОЧНЫЙ СПИСОК ПЕРЕД COMMIT

- [ ] Изменения скомпилировались без ошибок?
- [ ] IP сервера верный?
- [ ] Тестировал на эмуляторе/устройстве?
- [ ] Нет null pointer exceptions в логах?
- [ ] WebSocket подключается?
- [ ] Все callbacks работают?
- [ ] Обновил документацию если менял архитектуру?

---

## 🔗 БЫСТРЫЕ ССЫЛКИ

| Ссылка | Содержание |
|--------|-----------|
| [README_DOCS.md](README_DOCS.md) | 📚 Навигация по документации |
| [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md) | 🏗️ Полная архитектура |
| [CLASS_REFERENCE.md](CLASS_REFERENCE.md) | 📖 Справочник всех классов |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | 📡 REST API + WebSocket |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | 📊 Резюме и статистика |

---

## 🎓 ДЛЯ НОВИЧКОВ

### Дневной 1: Знакомство
1. Прочитайте [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) (20 мин)
2. Запустите приложение (10 мин)
3. Создайте аккаунт и пройдите по UI (20 мин)

### Дневной 2-3: Архитектура
1. Прочитайте [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md) разделы 1-3 (40 мин)
2. Посмотрите на файлы, которые описаны (30 мин)
3. Попробуйте найти каждый класс в IDE (20 мин)

### Дневной 4-5: API
1. Прочитайте [API_DOCUMENTATION.md](API_DOCUMENTATION.md) (60 мин)
2. Используйте Postman или curl для тестирования endpoints (30 мин)
3. Отследите вызовы в коде (30 мин)

### Дневной 6-7: Практика
1. Попробуйте добавить новый endpoint
2. Внесите изменения в логику друзей
3. Добавьте обработку ошибок

---

## 💬 ВОПРОСЫ?

Ищите ответы в:

**Как работает X?**
- [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md) → посмотрите нужный раздел

**Где находится класс Y?**
- [CLASS_REFERENCE.md](CLASS_REFERENCE.md) → используйте таблицу поиска

**Какой endpoint для Z?**
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) → используйте быстрый справочник

**Что означает W?**
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) → ищите в глоссарии

---

## 🚀 ГОТОВЫ К РАЗРАБОТКЕ?

1. ✅ Клонировали репо
2. ✅ Настроили IP сервера
3. ✅ Запустили на эмуляторе/устройстве
4. ✅ Прочитали этот Quick Start
5. ✅ Открыли документацию для справки

**Поехали кодить!** 🎉

---

**Последнее обновление: 18.06.2026**  
**Версия: 1.0**  
**Статус: Готов к использованию ✅**

