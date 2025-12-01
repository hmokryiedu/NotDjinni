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
}
