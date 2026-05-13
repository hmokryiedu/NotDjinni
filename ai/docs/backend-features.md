## Applied Vacancies API
Description: Verify or align the current seeker applications endpoint so it returns only the authenticated seeker's applications with card-ready vacancy/application data: application ID, vacancy ID, title, company, salary, employment type, experience, status, applied date, and updated date. Support status filtering including `WITHDRAWN`, pagination, and newest-first sorting. Cover access control, filtering, withdrawn visibility, pagination, and sorting.

## Cover Letter Templates API
Description: Add seeker-owned cover letter template CRUD. Store title, content, seeker profile owner, created date, and updated date. Validate non-empty title and content, return only the current seeker's templates, sort by latest update or creation date, and cover CRUD, ownership, authentication, and missing seeker profile cases.

## Duplicate Vacancy Support
Description: Verify vacancy detail response has all fields needed to prefill the create vacancy form, and reuse the existing create vacancy endpoint for duplicate submission. Do not add a dedicated duplicate endpoint, and do not copy generated/system fields such as ID, status, posted date, updated date, application count, or applications. Align create vacancy validation only where copied values expose existing gaps.

## Vacancy Application Count
Description: Verify employer vacancy list and detail responses include `applicationCount`. Count applications per vacancy and company, include `WITHDRAWN` unless a separate active-only field is later added, and keep the count stable across application status updates. Cover zero, multiple applications, cross-vacancy, cross-company, list, and detail scenarios.

## Withdraw Application
Description: Add `PATCH /application/{id}/withdraw` for authenticated seekers to withdraw only their own applications. Transition `APPLIED`, `REVIEWING`, `INTERVIEW`, `TEST_TASK`, and `OFFER` to `WITHDRAWN`; reject `HIRED` and `REJECTED`; make repeated withdraw from `WITHDRAWN` idempotent. Preserve the application record, employer visibility, application count, and history. Cover ownership, authentication, missing seeker profile, terminal statuses, idempotency, and employer visibility.

## Public Vacancy Read
Description: Verify `GET /vacancy` and `GET /vacancy/{id}` work for guests without JWT and expose only public active vacancy data. Support guest search, filters, pagination, and sorting on vacancy list. Do not include seeker-specific fields in guest vacancy detail response, and keep mutations, application, and favorite actions protected. Cover guest reads and protected guest actions.

## Favorite Vacancies API
Description: Verify favorite add/remove contracts: `POST /favorite/vacancy/{vacancyId}` and `DELETE /favorite/vacancy/{vacancyId}`. Verify favorite list contract: `GET /favorite/vacancy`. Return only the current seeker's favorite active vacancies for MVP, add `isFavorite` to authenticated seeker vacancy list/detail responses if absent, avoid N+1 favorite state queries, and cover add, remove, duplicate add, list ownership, authentication, and cross-user access.

## Viewed Vacancies API
Description: Add backend persistence for seeker viewed vacancy history. Track views with `POST /seeker/viewed-vacancies/{vacancyId}`, list history with `GET /seeker/viewed-vacancies`, update `viewedAt` on repeated view instead of creating duplicates, and return latest viewed first with pagination and vacancy card fields. Cover tracking, repeated view update, sorting, ownership, authentication, and missing seeker profile.

## Profile Editing MVP
Description: Align `PUT /seeker/profile` to update the current seeker's own profile and return the updated seeker profile. Align seeker work experience create, update, and delete mutations for owned records with field/date validation and updated seeker profile responses. Align `PUT /employer/profile` for role-only employer profile editing. Keep employer company, account name, email, password, profile deletion, and new endpoints out of MVP scope. Cover seeker update, experience ownership, invalid dates, and employer role update.
