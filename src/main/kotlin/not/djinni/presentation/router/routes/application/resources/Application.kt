package not.djinni.presentation.router.routes.application.resources

import io.ktor.resources.*

@Resource("/application")
class Application {
    @Resource("{id}")
    data class ById(val parent: Application = Application(), val id: Long)

    @Resource("vacancy/{vacancyId}")
    data class ByVacancy(val parent: Application = Application(), val vacancyId: Long)

    @Resource("{id}/status")
    data class Status(val parent: Application = Application(), val id: Long)

    @Resource("{id}/withdraw")
    data class Withdraw(val parent: Application = Application(), val id: Long)

    @Resource("check/vacancy/{vacancyId}")
    data class CheckByVacancy(val parent: Application = Application(), val vacancyId: Long)
}
