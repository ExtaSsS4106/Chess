# 📚 Chess Mobile - Индекс документации

## 🎯 Быстрая навигация

### Если вы хотите...

**Узнать, как устроен проект** → [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md)
- Полная архитектура приложения
- Описание каждого модуля
- Структура папок и назначение файлов
- Жизненный цикл приложения
- Диаграммы взаимодействия

**Найти нужный класс или интерфейс** → [CLASS_REFERENCE.md](CLASS_REFERENCE.md)
- Справочник всех 23 Java классов
- Таблицы с назначением каждого класса
- Callback интерфейсы
- Быстрые ссылки на основные компоненты

**Работать с API и WebSocket** → [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- Все 17 REST endpoints с примерами
- WebSocket протокол для игры
- Примеры запросов и ответов
- Сценарии API вызовов
- Обработка ошибок

**Получить обзор проекта** → [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
- Краткое резюме
- Ключевые особенности
- Технологический стек
- Статистика проекта
- Чеклист функциональности

---

## 📖 ПОЛНЫЙ СПИСОК ДОКУМЕНТОВ

| Документ | Размер | Назначение | Для кого |
|----------|--------|-----------|---------|
| **ARCHITECTURE_ANALYSIS.md** | ~45 KB | Полный анализ архитектуры | Архитекторы, lead разработчики |
| **CLASS_REFERENCE.md** | ~25 KB | Справочник классов | Разработчики, code reviewers |
| **API_DOCUMENTATION.md** | ~35 KB | Документация API | Backend разработчики, QA |
| **PROJECT_SUMMARY.md** | ~20 KB | Резюме проекта | Менеджеры, новые разработчики |
| **README_DOCS.md** | этот файл | Навигация по документам | Все |

---

## 🗂️ БЫСТРЫЙ СПРАВОЧНИК

### 📁 Основные папки

```
api/                    → [API_DOCUMENTATION.md] + [ARCHITECTURE_ANALYSIS.md]
authorisation/          → [ARCHITECTURE_ANALYSIS.md] раздел 2.2
core/                   → [ARCHITECTURE_ANALYSIS.md] раздел 2.5
data/                   → [ARCHITECTURE_ANALYSIS.md] раздел 2.3
gameCore/               → [ARCHITECTURE_ANALYSIS.md] раздел 2.4
main_fragments/         → [ARCHITECTURE_ANALYSIS.md] раздел 2.6
```

### 🔑 Ключевые классы

```
MainActivity           → [ARCHITECTURE_ANALYSIS.md] раздел 3.1
GameActivity           → [ARCHITECTURE_ANALYSIS.md] раздел 3.2
Login                  → [CLASS_REFERENCE.md] таблица авторизации
Requests               → [API_DOCUMENTATION.md] раздел "REST API"
FriendsCore            → [ARCHITECTURE_ANALYSIS.md] раздел 2.6.3
RequestsCore           → [ARCHITECTURE_ANALYSIS.md] раздел 2.6.3
```

### 🌐 API Endpoints

```
/login                 → [API_DOCUMENTATION.md] раздел "АВТОРИЗАЦИЯ"
/friends/*             → [API_DOCUMENTATION.md] раздел "УПРАВЛЕНИЕ ДРУЗЬЯМИ"
/requests/*            → [API_DOCUMENTATION.md] раздел "УПРАВЛЕНИЕ ЗАПРОСАМИ"
/session/*             → [API_DOCUMENTATION.md] раздел "WEBSOCKET ПРОТОКОЛ"
```

---

## 📝 ПО ТЕМАМ

### Аутентификация
- **Классы**: [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-авторизация-authorisation)
- **API**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-авторизация)
- **Архитектура**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#-22--модуль-аутентификации)
- **Поток**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#поток-аутентификации)

### Управление друзьями
- **Классы**: [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главный-экран-main_fragments)
- **API**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-управление-друзьями)
- **Архитектура**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#-26--ui-фрагменты-главного-экрана)
- **Поток**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#2️⃣-загрузка-друзей)

### Игровой процесс
- **Классы**: [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главные-активности)
- **API**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-управление-игрой)
- **WebSocket**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-websocket-протокол)
- **Архитектура**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#5-логика-игры-в-шахматы)
- **Поток**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#3️⃣-начало-игры)

### Сетевое взаимодействие
- **REST API**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-rest-api-документация)
- **WebSocket**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-websocket-протокол)
- **Классы**: [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-api-api)
- **Безопасность**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-безопасность)

### UI компоненты
- **Activities**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#3-основные-activity-и-flow)
- **Fragments**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#-26--ui-фрагменты-главного-экрана)
- **Adapters**: [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главный-экран-main_fragments)
- **Layouts**: [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#6-ui-компоненты-и-layouts)

---

## 🔍 ПОИСК ПО СЛОВАМ

### Если ищете...

**"token"** → [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-авторизация) + [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#41-userdata-loaderjava)

**"WebSocket"** → [API_DOCUMENTATION.md](API_DOCUMENTATION.md#-websocket-протокол) + [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главные-активности)

**"RecyclerView"** → [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главный-экран-main_fragments) + [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#-adapters--адаптеры-recyclerview)

**"Fragment"** → [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#-26--ui-фрагменты-главного-экрана) + [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-главный-экран-main_fragments)

**"Callback"** → [CLASS_REFERENCE.md](CLASS_REFERENCE.md#-авторизация-authorisation) + [API_DOCUMENTATION.md](API_DOCUMENTATION.md#⚠️-обработка-ошибок)

**"Ошибка"** → [API_DOCUMENTATION.md](API_DOCUMENTATION.md#⚠️-обработка-ошибок) + [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-безопасность)

**"Безопасность"** → [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-безопасность) + [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md#11-безопасность)

---

## 📊 СТАТИСТИКА ДОКУМЕНТАЦИИ

| Документ | Строк кода | Разделов | Таблиц | Примеров |
|----------|-----------|----------|---------|----------|
| ARCHITECTURE_ANALYSIS.md | ~1200 | 14 | 8 | 15+ |
| CLASS_REFERENCE.md | ~600 | 10 | 10 | 5+ |
| API_DOCUMENTATION.md | ~800 | 12 | 5 | 20+ |
| PROJECT_SUMMARY.md | ~450 | 15 | 8 | 3+ |
| **ИТОГО** | **~3050** | **51** | **31** | **40+** |

---

## 🎓 ПУТЬ ОБУЧЕНИЯ

### Для новых разработчиков:
1. Начните с [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) — получите общее представление
2. Прочитайте [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md) разделы 1-3 — поймите структуру
3. Изучите интересующий модуль в [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md)
4. Используйте [CLASS_REFERENCE.md](CLASS_REFERENCE.md) для деталей конкретных классов
5. Смотрите [API_DOCUMENTATION.md](API_DOCUMENTATION.md) при работе с API

### Для backend разработчиков:
1. Прочитайте [API_DOCUMENTATION.md](API_DOCUMENTATION.md) всю документацию API
2. Посмотрите примеры запросов и ответов
3. Изучите WebSocket протокол (раздел "WEBSOCKET ПРОТОКОЛ")
4. Обратитесь к [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md) раздел 7 для примеров использования

### Для QA/тестировщиков:
1. Изучите [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-чеклист-функциональности) — список функций
2. Прочитайте [API_DOCUMENTATION.md](API_DOCUMENTATION.md) — все endpoint'ы для тестирования
3. Смотрите сценарии в [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-основные-потоки-взаимодействия)
4. Проверьте обработку ошибок в [API_DOCUMENTATION.md](API_DOCUMENTATION.md#⚠️-обработка-ошибок)

### Для архитекторов/техлидов:
1. Прочитайте полностью [ARCHITECTURE_ANALYSIS.md](ARCHITECTURE_ANALYSIS.md)
2. Изучите диаграммы и паттерны в разделах 4-6
3. Обратите внимание на рекомендации в [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md#-известные-проблемы-и-улучшения)
4. Просмотрите [CLASS_REFERENCE.md](CLASS_REFERENCE.md) для понимания связей между классами

---

## 🔗 ПЕРЕКРЁСТНЫЕ ССЫЛКИ

### ARCHITECTURE_ANALYSIS.md ↔ CLASS_REFERENCE.md
- Раздел 2 (Папки) ↔ Таблицы классов
- Раздел 3 (Activity) ↔ Раздел Activities
- Раздел 4 (Models) ↔ Раздел Objects

### ARCHITECTURE_ANALYSIS.md ↔ API_DOCUMENTATION.md
- Раздел 7 (Endpoints) ↔ REST API документация
- Раздел 5 (Game Logic) ↔ WebSocket протокол
- Раздел 8 (Обработка ошибок) ↔ Раздел "Обработка ошибок"

### CLASS_REFERENCE.md ↔ API_DOCUMENTATION.md
- Таблица Requests класса ↔ REST API методы
- Callback интерфейсы ↔ Примеры использования

### PROJECT_SUMMARY.md ↔ Все документы
- Быстрые ссылки на основные компоненты во все документы

---

## 🚀 РЕКОМЕНДУЕМЫЕ КОМАНДЫ

### Поиск в документах (grep):
```bash
# Найти все упоминания "WebSocket"
grep -r "WebSocket" *.md

# Найти все endpoints
grep -E "^(GET|POST|PUT|DELETE|WS)" API_DOCUMENTATION.md

# Найти все классы
grep "^###" CLASS_REFERENCE.md
```

### Навигация в IDE:
```
Ctrl+F → найти текст в документе
Ctrl+Shift+F → найти во всех документах
```

---

## 📝 ВЕРСИОНИРОВАНИЕ ДОКУМЕНТАЦИИ

| Версия | Дата | Изменения |
|--------|------|-----------|
| 1.0 | 18.06.2026 | Первая версия с полным анализом |

---

## 💡 СОВЕТЫ ПО ИСПОЛЬЗОВАНИЮ

1. **Откройте несколько документов** — используйте split screen в IDE для быстрого переключения

2. **Используйте Ctrl+F** — поиск по ключевым словам ускорит навигацию

3. **Следуйте диаграммам** — они помогают визуализировать архитектуру

4. **Проверяйте перекрёстные ссылки** — часто нужна информация из нескольких документов

5. **Обновляйте документы** — при изменении кода обновляйте соответствующие разделы

---

## 🎯 ЗАКЛЮЧЕНИЕ

Эта документация содержит:
- ✅ **Полный анализ архитектуры** (51 раздел)
- ✅ **Справочник всех 23 классов** (31 таблица)
- ✅ **Полную документацию API** (17 endpoints + WebSocket)
- ✅ **Краткое резюме проекта** (чеклисты и статистика)
- ✅ **Примеры кода и сценарии** (40+ примеров)

**Всё необходимое для полного понимания проекта Chess Mobile!** 🎮♟️

---

**Последнее обновление: 18.06.2026**  
**Статус: ✅ Полная документация**  
**Версия проекта: mobile_0.1**

