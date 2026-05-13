package not.djinni.presentation.router.routes.vacancy.resources

import io.ktor.resources.*

@Resource("/vacancy")
class Vacancy {
    @Resource("{id}")
    class ById(val parent: Vacancy = Vacancy(), val id: Long)

    @Resource("{id}/status")
    class Status(val parent: Vacancy = Vacancy(), val id: Long)

    @Resource("recent")
    class Recent(val parent: Vacancy = Vacancy(), val limit: Int = 10)

    @Resource("applied")
    class Applied(val parent: Vacancy = Vacancy())
}

@Resource("/company/{companyId}/vacancies")
class CompanyVacancies(val companyId: Long)
