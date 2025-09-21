package not.djinni.database.impl.user

import not.djinni.database.api.user.UserEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

class UserTableEntity(id: EntityID<Long>) : LongEntity(id) {
    var email by UserTable.email
    var password by UserTable.password

    companion object Companion : LongEntityClass<UserTableEntity>(UserTable)
}

object UserTable : LongIdTable("user", "id") {
    val email = varchar("email", MAX_VARCHAR_LENGTH)
    val password = varchar("password", MAX_VARCHAR_LENGTH)

    private const val MAX_VARCHAR_LENGTH = 255
}

fun UserTableEntity.toEntity(): UserEntity {
    return UserEntity(
        id = id.value,
        email = email,
        password = password,
    )
}