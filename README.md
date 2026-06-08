# Школьный дневник
![structure](./docs/molang.png)

# ⚠️!В процессе!⚠️

## Микросервисное приложение школьный дневник

### Стек:
- Backend
  - Spring Cloud (Eureka, Api Gateway, Feign)
  - Jpa (psql)
  - Spring security (Oauth resource server - Keycloak)
  - Keycloak
  - Psql
  - Flyway
  - Минимально настроены логи: Logback Loki Alloy
  - RabbitMQ (планируется)
- Frontend
  - Vite
  - React
  - react-dom
  - TanStackQuery
  - Redux Toolkit
  - tailwindcss
  - keycloak-js
  - shadcn-ui
  - react-router-dom

### Структура бд
#### Academic-service DB
![structure](./docs/diagram_academic.png)
#### User-service DB
![structure](./docs/diagram_user.png)

### Роуты
### keycloak
    1. Адрес: localhost:9090
    2. Логин: `admin`
    3. Пароль: `password`

### backend
    Swagger, общий для сервисов ApiGateway 
    localhost:8080/swagger-ui/index.html

### frontend роуты
    `/student/home` Домашняя страница ученика
    `/student/diary` Страница дневник ученика
    `/student/grades` Страница со всеми оценками ученика
    `/teacher/journal` Страница с успеваемостью учеников для учителя
    `/teacher/homework` Страница с дз для учителя
    `/admin/subject` Модификация предметов админ
    `/admin/period` Модификация четвертей админ
    `/admin/user` Модификация пользователей админ
    `/admin/school-class` Модификация школьных классов
    `/admin/schedule` Модификация школьных классов

### Макеты фронта
#### Scope `Ученик`
![student_home](./docs/student/1.png)
![student_diary](./docs/student/2.png)
![student_grades](./docs/student/3.png)
![student_grades](./docs/student/4.png)

#### Scope `Учитель`
![teacher_academic_performance](./docs/teacher/1.png)
![teacher_homework](./docs/teacher/2.png)
![teacher_homework](./docs/teacher/3.png)
![teacher_homework](./docs/teacher/4.png)

#### Scope `Админ`
![admin_subjects](./docs/admin/1.png)
![admin_academic_period](./docs/admin/2.png)
![admin_users](./docs/admin/3.png)
![admin_users2](./docs/admin/4.png)
![admin_users2](./docs/admin/5.png)
![admin_users2](./docs/admin/6.png)
![admin_users2](./docs/admin/7.png)