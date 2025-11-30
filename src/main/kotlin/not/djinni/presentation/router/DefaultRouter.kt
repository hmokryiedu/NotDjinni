package not.djinni.presentation.router

import io.ktor.server.application.*
import io.ktor.server.routing.*
import not.djinni.presentation.router.routes.auth.AuthRoute
import not.djinni.presentation.router.routes.seeker.SeekerRoute
import not.djinni.presentation.router.routes.user.UserRoute
import not.djinni.presentation.router.routes.company.CompanyRoute
import not.djinni.presentation.router.routes.employer.EmployerRoute
import org.koin.core.annotation.Single

@Single([Router::class])
class DefaultRouter(
    private val authRoute: AuthRoute,
    private val userRoute: UserRoute,
    private val seekerRoute: SeekerRoute,
    private val companyRoute: CompanyRoute,
    private val employerRoute: EmployerRoute
) : Router {

    override fun install(application: Application) {
        application.routing {
            authRoute.install(this)
            userRoute.install(this)
            seekerRoute.install(this)
            companyRoute.install(this)
            employerRoute.install(this)
        }
    }
}