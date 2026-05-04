package not.djinni.presentation.router.routes.favorite.resources

import io.ktor.resources.Resource

@Resource("/favorite")
class FavoriteVacancy {
    @Resource("vacancy/{vacancyId}")
    data class ByVacancy(val parent: FavoriteVacancy = FavoriteVacancy(), val vacancyId: Long)
}
