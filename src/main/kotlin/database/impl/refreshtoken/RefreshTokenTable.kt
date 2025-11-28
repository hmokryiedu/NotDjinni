package not.djinni.database.impl.refreshtoken

import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class RefreshTokenTableEntity(id: EntityID<Long>) : LongEntity(id) {
    var userId by RefreshTokenTable.userId
    var token by RefreshTokenTable.token
    var expiresAt by RefreshTokenTable.expiresAt

    companion object : LongEntityClass<RefreshTokenTableEntity>(RefreshTokenTable)
}

object RefreshTokenTable : LongIdTable("refresh_tokens", "id") {
    val userId = long("user_id").index()
    val token = varchar("token", MAX_TOKEN_LENGTH).uniqueIndex()
    val expiresAt = timestamp("expires_at")

    private const val MAX_TOKEN_LENGTH = 512
}

fun RefreshTokenTableEntity.toEntity() = RefreshTokenEntity(
    id = id.value,
    userId = userId,
    token = token,
    expiresAt = expiresAt
)
