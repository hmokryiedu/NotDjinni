package not.djinni.presentation.router.routes.company.resources

import io.ktor.resources.*

@Resource("/company")
class Company {
    @Resource("{id}")
    class ById(val parent: Company = Company(), val id: Long)

    @Resource("search")
    class Search(val parent: Company = Company(), val name: String)
}
