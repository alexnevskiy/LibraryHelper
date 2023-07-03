# Приложение-помощник библиотекаря

## Стек

- Java
- Spring Boot
- Docker
- PostgreSQL
- Testcontainers
- JUnit 5

## Описание

В системе существуют две основные сущности: читатель и книга.

В приложении реализованы:

- REST-сервис для создания, редактирования, получения информации о читателях
- REST-сервис для создания, редактирования, получения информации о книгах
- REST-сервис для сохранения событий о том, что читатель взял книгу домой / отдал её назад
- Поиск самой популярной книги за определённый период времени
- Поиск читателя, прочитавшего самое большое количество книг за определённый период времени

В качестве базы данных используется PostgreSQL.

## Сборка и развёртывание

```
1. git clone https://github.com/alexnevskiy/LibraryHelper.git
2. cd LibraryHelper
3. docker-compose --env-file environments/.env up
```

Для развёртывания программы используется Docker. В проекте есть Dockerfile для реализованного приложения. Для управления контейнером приложения и контейнером PostgreSQL используется docker-compose.

Задать значения аргументов можно в environments/.env.

## Use cases

### Читатель

#### Создание

```http
POST http://localhost:8080/api/v1/reader/create
Content-Type: application/json

{ "firstName": "Ivan", "lastName": "Ivanov" }

Response:
{
    "id": 1,
    "firstName": "Ivan",
    "lastName": "Ivanov"
}
```

#### Редактирование

```http
PUT http://localhost:8080/api/v1/reader/update
Content-Type: application/json

{ "id": 1, "firstName": "Alexey", "lastName": "Alexeev" }

Response:
{
  "id": 1,
  "firstName": "Alexey",
  "lastName": "Alexeev"
}
```

#### Получение

```http
GET http://localhost:8080/api/v1/reader/

Response:
[
  {
    "id": 1,
    "firstName": "Ivan",
    "lastName": "Ivanov"
  },
  ...
  {
    "id": 100,
    "firstName": "Alexey",
    "lastName": "Alexeev"
  }
]
```

#### Читатель, прочитавший самое большое количество книг за определённый период времени

```http
GET http://localhost:8080/api/v1/reader/most?start=2023-06-30T12:00:00&end=2023-07-02T18:00:00

Response:
{
    "id": 1,
    "firstName": "Ivan",
    "lastName": "Ivanov"
}
```

### Книга

#### Создание

```http
POST http://localhost:8080/api/v1/book/create
Content-Type: application/json

{ "name": "The Witcher", "author": "Andrzej Sapkowski" }

Response:
{
    "id": 1,
    "name": "The Witcher",
    "author": "Andrzej Sapkowski"
}
```

#### Редактирование

```http
PUT http://localhost:8080/api/v1/book/update
Content-Type: application/json

{ "id": 1, "name": "Dubrovskiy", "author": "Aleksandr Pushkin" }

Response:
{
  "id": 1,
  "name": "Dubrovskiy",
  "author": "Aleksandr Pushkin"
}
```

#### Получение

```http
GET http://localhost:8080/api/v1/book/

Response:
[
  {
    "id": 1,
    "name": "The Witcher",
    "author": "Andrzej Sapkowski"
  },
  ...
  {
    "id": 100,
    "name": "Dubrovskiy",
    "author": "Aleksandr Pushkin"
  }
]
```

#### Самая популярная книга за определённый период времени

```http
GET http://localhost:8080/api/v1/book/popular?start=2023-06-30T12:00:00&end=2023-07-02T18:00:00

Response:
{
    "id": 1,
    "name": "The Witcher",
    "author": "Andrzej Sapkowski"
}
```

### Событие

#### Создание

```http
POST http://localhost:8080/api/v1/event/create
Content-Type: application/json

{ "idReader": 1, "idBook": 1, "eventType": "TAKE_BOOK", "eventDatetime": "2023-07-01T14:35:22" }

Response:
{
  "id": 1,
  "idReader": 1,
  "idBook": 1,
  "eventType": "TAKE_BOOK",
  "eventDatetime": "2023-07-01T14:35:22.000000"
}
```

#### Получение типов событий

```http
GET http://localhost:8080/api/v1/event/

Response:
[
  {
    "type": "TAKE_BOOK"
  },
  {
    "type": "RETURN_BOOK"
  }
]
```

