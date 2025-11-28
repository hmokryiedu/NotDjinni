package not.djinni.database.impl.refreshtoken

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.refreshtoken.RefreshTokenDao
import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.koin.core.annotation.Single
import java.time.LocalDateTime

@Single([RefreshTokenDao::class])
class DefaultRefreshTokenDao : RefreshTokenDao {

    override suspend fun findByToken(token: String): RefreshTokenEntity? = runQuery {
        RefreshTokenTableEntity
            .find { RefreshTokenTable.token eq token }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun insert(entity: RefreshTokenEntity): Long = runQuery {
        RefreshTokenTableEntity.new {
            this.userId = entity.userId
            this.token = entity.token
            this.expiresAt = entity.expiresAt
        }.id.value
    }

    override suspend fun deleteByToken(token: String): Boolean = runQuery {
        val deletedCount = RefreshTokenTable.deleteWhere {
            RefreshTokenTable.token eq token
        }
        deletedCount > 0
    }

    override suspend fun deleteAllForUser(userId: Long): Int = runQuery {
        RefreshTokenTable.deleteWhere {
            RefreshTokenTable.userId eq userId
        }
    }

    override suspend fun deleteExpired(): Int = runQuery {
        RefreshTokenTable.deleteWhere {
            RefreshTokenTable.expiresAt less LocalDateTime.now()
        }
    }
}
