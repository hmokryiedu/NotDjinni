package not.djinni

import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.resources.*
import not.djinni.database.NotDjinniDatabase
import not.djinni.presentation.plugins.installAuthentication
import not.djinni.presentation.plugins.installKoin
import not.djinni.presentation.plugins.installSerialization
import not.djinni.presentation.router.Router
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    NotDjinniDatabase.init()
    installKoin()
    installAuthentication()
    install(Resources)
    installSerialization()
    get<Router>().install(this)
}
