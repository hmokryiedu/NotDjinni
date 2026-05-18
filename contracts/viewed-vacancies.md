## Viewed vacancies

1. endpoint -> `/seeker/viewed-vacancies`
type -> `GET`
response ->
```json
{
  "200": {
    "viewed_vacancies": [
      {
        "viewed_at": "String",
        "views_count": "Int",
        "vacancy": {
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

2. endpoint -> `/vacancy/{id}`
type -> `GET`
response ->
```json
{
  "200": {
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
  "path": {
    "id": "Long"
  }
}
```
