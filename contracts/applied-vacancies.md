## Applied vacancies

1. endpoint -> `/vacancy/applied`
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
  "403": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "400": {
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

2. endpoint -> `/application`
type -> `POST`
response ->
```json
{
  "201": {
    "id": "Long",
    "vacancy_id": "Long",
    "job_seeker_id": "Long",
    "status": "ApplicationStatus",
    "cover_letter": "String?",
    "created_at": "Instant",
    "updated_at": "Instant"
  },
  "401": {
    "message": "String"
  },
  "403": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "409": {
    "message": "String"
  },
  "400": {
    "message": "String"
  }
}
```
request ->
```json
{
  "vacancy_id": "Long",
  "cover_letter": "String?"
}
```

3. endpoint -> `/application/check/vacancy/{vacancyId}`
type -> `GET`
response ->
```json
{
  "200": {
    "has_applied": "Boolean"
  },
  "401": {
    "message": "String"
  },
  "403": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "400": {
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

4. endpoint -> `/application/vacancy/{vacancyId}/mine`
type -> `GET`
response ->
```json
{
  "200": {
    "id": "Long",
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
    },
    "job_seeker": {
      "id": "Long",
      "about_me": "String?",
      "speciality": "String",
      "desired_salary": "Int",
      "experience_years": "Int",
      "job_category": "String",
      "work_experience": "Array"
    },
    "status": "ApplicationStatus",
    "cover_letter": "String?",
    "created_at": "Instant",
    "updated_at": "Instant"
  },
  "401": {
    "message": "String"
  },
  "403": {
    "message": "String"
  },
  "404": {
    "message": "String"
  },
  "400": {
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
