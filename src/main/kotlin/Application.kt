package not.djinni

import io.ktor.server.application.*
import io.ktor.server.netty.*
import not.djinni.presentation.plugins.installKoin
import not.djinni.presentation.router.Router
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    installKoin()
    get<Router>().install(this)
}
