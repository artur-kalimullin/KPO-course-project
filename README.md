# Анализ успеваемости студентов

Веб-приложение для автоматизации учёта успеваемости с разграничением доступа
для администраторов, преподавателей и студентов.

**Тема:** «Разработка веб-приложения для анализа успеваемости студентов
с разграничением доступа для сотрудников и студентов»

**Студент:** Калимуллин А.Р., группа ПИбд-41

## Возможности

- администратор: группы, предметы, студенты, преподаватели, привязка к группам;
- преподаватель: свои группы и студенты, выставление/редактирование оценок,
  статистика по группе и предмету;
- студент: свои оценки, средний балл, долги, рейтинг в группе;
- JWT-аутентификация, роли, валидация данных и ограничения на уровне PostgreSQL.

## Стек

Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL 17,
Liquibase, React 18 + Vite + Bootstrap.

## Запуск

Нужны **JDK 21** и **PostgreSQL 17**. Node.js отдельно ставить не нужно —
фронтенд собирается Maven-плагином.

1. Создайте базу:

   ```sql
   CREATE DATABASE grade_analysis;
   ```

2. Скопируйте `.env.example` в `.env` и заполните значения:

   ```powershell
   Copy-Item .env.example .env
   ```

   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/grade_analysis
   DB_USERNAME=postgres
   DB_PASSWORD=Ваш_пароль
   JWT_SECRET=случайная_строка_не_короче_32_символов
   ```

3. Запустите приложение:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   или соберите и запустите jar:

   ```powershell
   .\mvnw.cmd -DskipTests package
   java -jar target\grade-analysis-1.0.0.jar
   ```

   Liquibase при старте сам создаст таблицы, ограничения и загрузит демо-данные.

4. Откройте <http://localhost:8080>.

## Демо-доступы

| Логин | Пароль | Роль |
|---|---|---|
| `admin` | `123456` | администратор |
| `kalimullin` | `password123` | студент, ПИбд-14 |
| `eredavkin` | `password123` | студент, ПИбд-14 |
| `kenobi` | `password123` | студент, ПИМбд-22 |
| `ankilov` | `password123` | преподаватель |
| `m.koval` | `password123` | преподаватель |

## Структура

```
src/main/java/ru/ulstu/grade_analysis/
  controller/   REST-контроллеры
  service/      бизнес-логика и транзакции
  repository/   Spring Data JPA
  entity/       сущности
  dto/          запросы и ответы API
  mapper/       Entity -> DTO
  config/       Security, SPA-fallback
  security/     JWT
src/main/resources/db/changelog/   миграции Liquibase
front/src/                         React-приложение
```
