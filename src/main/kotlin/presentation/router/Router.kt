package not.djinni.presentation.router

import io.ktor.server.application.Application

interface Router {
    fun install(application: Application)
}