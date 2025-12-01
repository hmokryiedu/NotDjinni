package not.djinni.presentation.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*

fun Application.installLogging() {
    install(CallLogging)
}