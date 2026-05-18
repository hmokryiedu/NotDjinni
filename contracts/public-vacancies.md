## Public vacancies

1. endpoint -> `/vacancy`
type -> `GET`
response ->
```json
{
  "200": {
    "without_auth": {
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
          "views_count": "Int"
        }
      ]
    },
    "with_auth": {
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
    }
  },
  "400": {
    "message": "String"
  },
  "403": {
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
    "offset": "Int?",
    "category": "String[]?",
    "status": "String[]?",
    "employment_type": "String[]?",
    "sort_by": "String?",
    "sort_direction": "String?",
    "search": "String?",
    "company_id": "Long?",
    "salary_min": "Int?",
    "salary_max": "Int?",
    "experience_years": "Int?"
  },
  "auth": "Optional JWT"
}
```

2. endpoint -> `/vacancy/{id}`
type -> `GET`
response ->
```json
{
  "200": {
    "without_auth": {
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
      "views_count": "Int"
    },
    "with_auth": {
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
  },
  "400": {
    "message": "String"
  },
  "403": {
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
  },
  "auth": "Optional JWT"
}
```

3. endpoint -> `/vacancy/recent`
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
        "views_count": "Int"
      }
    ]
  },
  "400": {
    "message": "String"
  },
  "403": {
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
    "limit": "Int?"
  }
}
```

4. endpoint -> `/company/{companyId}/vacancies`
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
        "views_count": "Int"
      }
    ]
  },
  "400": {
    "message": "String"
  },
  "403": {
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
    "companyId": "Long"
  },
  "query": {
    "limit": "Int?",
    "offset": "Int?"
  }
}
```
