Microservices: Spring Cloud (Netflix Eureka, Spring Cloud Gateway)

Database: PostgreSQL + Flyway

Auth: Keycloak (OIDC/OAuth2)

Observability: Loki + Grafana (логи), Micrometer + Prometheus (метрики) + Alloy

Frontend: React + Redux

1. User Service 
    [ ] Flyway: Миграции для teachers, students, parents, admins.
    
    [ ] Keycloak Sync: Настроен Event Listener или периодическая синхронизация keycloak_id.
    
    [ ] API: Добавлен эндпоинт /api/users/me (извлечение данных на основе JWT от Gateway).

2. 🛡️ Auth Strategy

    [ ] Keycloak Realm: Настройка ролей (ROLE_STUDENT, ROLE_TEACHER, и т.д.).
    
    [ ] Token Exchange: Настройка мапперов для включения user_id из БД в JWT токен.
    
    [ ] Security Config: Spring Security Resource Server настроен в каждом сервисе.

   3. 📚 Academic Service (Core)
   [ ] Flyway: Таблицы classes, subjects, academic_periods.

    [ ] Feign Clients: Интерфейс для получения данных об учителях из User Service.
    
    [ ] Logic: Привязка учеников к классам, распределение нагрузки учителей.

4. 📊 Grade Service
   [ ] Database: Таблицы оценок и типов работ.

    [ ] Calculations: Логика подсчета среднего балла (Weight-based).
    
    [ ] Events: Отправка события "Оценка выставлена" в Notification Service через брокер (опционально).

5. 📅 Schedule Service
   [ ] Algorithm: Проверка конфликтов (один учитель в двух кабинетах одновременно).

    [ ] Integration: Связь с Academic Service для получения списка предметов.

6. 📝 Homework & 📁 File Service
   Можно объединить или разнести.

    [ ] Storage: Интеграция с хранилищем (S3/MinIO) для загрузки файлов ДЗ.
    
    [ ] Relationship: Связь homework_id с schedule_id.

7. 🔔 Notification Service
   [ ] Tech: WebSocket (STOMP) для Real-time уведомлений на React.

    [ ] Integration: Слушает события от Grade Service и Homework Service.

⚙️ Инфраструктурный Чеклист (Spring Cloud)
17. 🛰️ Discovery Service (Eureka)
    [ ] Настроен сервер Eureka.

    [ ] Все микросервисы имеют spring-cloud-starter-netflix-eureka-client.
    
    [ ] Настроены Health Checks для корректного отображения статуса в Dashboard.

18. 🌉 API Gateway (Spring Cloud Gateway)
    [ ] Routing: Прописаны префиксы для всех сервисов (например, /api/v1/users/**).

    [ ] Security: Gateway выступает в роли OAuth2 Client/Resource Server (проверяет валидность JWT от Keycloak).
    
    [ ] Token Relay: Настроена передача токена в заголовках к нижележащим сервисам.
    
    [ ] CORS: Настроены правила для React (localhost:3000).

19. 🪵 Observability (Loki + Grafana)
    [ ] Logback: Настроен Loki4j аппендер в микросервисах для прямой отправки логов в Loki.

    [ ] Tracing: Добавлен Spring Cloud Sleuth / Micrometer Tracing (чтобы видеть traceId в логах от Gateway до БД).
    
    [ ] Dashboard: Создан дашборд в Grafana для мониторинга ошибок (4xx, 5xx).

20. 🏗️ База данных (Flyway + PSQL)
    [ ] В каждом сервисе настроен свой сепаратистский конфиг Flyway.

    [ ] Разделены схемы БД для каждого микросервиса (User DB, Grade DB и т.д.).

🗺️ Roadmap разработки (Твой стек)
    Phase 1 (Infra): Запустить Eureka + Gateway + Keycloak. Убедиться, что React может авторизоваться и получить JWT.
    
    Phase 2 (User/Academic): Допилить User Service и создать Academic Service. Настроить их общение через OpenFeign.
    
    Phase 3 (Journal): Реализовать Grade Service и Schedule Service. Подключить Flyway миграции.
    
    Phase 4 (Front): React + Redux Toolkit. Настроить RTK Query для работы с защищенными эндпоинтами через Gateway.
    
    Phase 5 (Monitoring): Подключить Loki и настроить алерты в Grafana на ошибки 500.
