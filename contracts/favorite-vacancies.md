## Favorite vacancies

1. endpoint -> `/favorite/vacancy`
type -> `GET`
response ->
```json
{
  "200": {
    "vacancies": [
      {
        "id": "Long",
        "company": {
          "id": "Long",
          "company_name": "String",
          "website": "String?",
          "description": "String"
        },
        "title": "String",
        "description": "String",
        "salary_min": "Int",
        "salary_max": "Int",
        "min_experience_years": "Int?",
        "employment_type": "String?",
        "category": "String?",
        "status": "String",
        "created_at": "String",
        "updated_at": "String",
        "applications_count": "Int",
        "views_count": "Int",
        "is_favorite": "Boolean"
      }
    ]
  },
  "401": {
    "message": "String"
  },
  "404": {
    "message": "String"
  }
}
```
request ->
```json
{
  "query": {
    "limit": "Int?",
    "offset": "Int?"
  }
}
```

2. endpoint -> `/favorite/vacancy/{vacancyId}`
type -> `POST`
response ->
```json
{
  "201": {
    "message": "String"
  },
  "401": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "409": {
    "message": "String"
  }
}
```
request ->
```json
{
  "path": {
    "vacancyId": "Long"
  }
}
```

3. endpoint -> `/favorite/vacancy/{vacancyId}`
type -> `DELETE`
response ->
```json
{
  "200": {
    "message": "String"
  },
  "401": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "409": {
    "message": "String"
  }
}
```
request ->
```json
{
  "path": {
    "vacancyId": "Long"
  }
}
```
