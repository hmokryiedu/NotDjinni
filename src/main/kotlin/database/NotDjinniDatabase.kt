package not.djinni.database

import kotlinx.coroutines.Dispatchers
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object NotDjinniDatabase {

    private val database: Database by lazy {
        Database.connect(
            "jdbc:postgresql://localhost:5432/notdjinni",
            driver = "org.postgresql.Driver",
            user = "notdjinni",
            password = "notdjinnipassword"
        )
    }

    fun init() {
        transaction(database) {
            SchemaUtils.create(UserTable)
            addLogger(StdOutSqlLogger)
        }
    }

    suspend fun <T> runQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}