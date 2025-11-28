package not.djinni.presentation.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import not.djinni.di.AppModule
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.installKoin() {
    install(Koin) {
        slf4jLogger()
        modules(AppModule().module)
    }
}