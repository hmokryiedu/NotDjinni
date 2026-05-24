package not.djinni.database

import kotlinx.coroutines.Dispatchers
import io.ktor.server.config.yaml.YamlConfigLoader
import not.djinni.database.impl.application.ApplicationTable
import not.djinni.database.impl.token.RefreshTokenTable
import not.djinni.database.impl.template.TemplateTable
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.seeker.WorkExperienceTable
import not.djinni.database.impl.user.UserTable
import not.djinni.database.impl.employer.CompanyTable
import not.djinni.database.impl.employer.EmployerProfileTable
import not.djinni.database.impl.favorite.FavoriteVacancyTable
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.database.impl.viewed.ViewedVacancyTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object NotDjinniDatabase {

    private val database: Database by lazy {
        val config = YamlConfigLoader().load("application.yaml") ?: error("Failed to load application.yaml")
        Database.connect(
            config.property("database.jdbcUrl").getString(),
            driver = "org.postgresql.Driver",
            user = config.property("database.username").getString(),
            password = config.property("database.password").getString()
        )
    }

    fun init() {
        transaction(database) {
            SchemaUtils.create(
                UserTable,
                RefreshTokenTable,
                SeekerProfileTable,
                WorkExperienceTable,
                CompanyTable,
                EmployerProfileTable,
                VacancyTable,
                ApplicationTable,
                FavoriteVacancyTable,
                ViewedVacancyTable,
                TemplateTable,
            )
            addLogger(StdOutSqlLogger)
        }
    }

    suspend fun <T> runQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
