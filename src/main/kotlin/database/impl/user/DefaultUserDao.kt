package not.djinni.database.impl.user

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import org.koin.core.annotation.Single

@Single([UserDao::class])
class DefaultUserDao : UserDao {

    override suspend fun getUser(id: Long): UserEntity? = runQuery {
        UserTableEntity.find { UserTable.id eq id }.firstOrNull()?.toEntity()
    }

    override suspend fun upsertUser(user: UserEntity): Long = runQuery {
        UserTableEntity.new {
            this.email = user.email
            this.password = user.password
        }.id.value
    }

    override suspend fun getUserByEmail(email: String): UserEntity? = runQuery {
        UserTableEntity.find { UserTable.email eq email }.firstOrNull()?.toEntity()
    }
}