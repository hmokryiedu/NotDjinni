package not.djinni.presentation.router.routes.seeker.resources

import io.ktor.resources.*

@Resource("/seeker")
class Seeker {
    @Resource("profile")
    class Profile(val parent: Seeker = Seeker())

    @Resource("profile/experience")
    class Experience(val parent: Seeker = Seeker())

    @Resource("profile/experience/{id}")
    class ExperienceById(val parent: Seeker = Seeker(), val id: Long)

    @Resource("vacancy")
    class Vacancies(val parent: Seeker = Seeker())
}
