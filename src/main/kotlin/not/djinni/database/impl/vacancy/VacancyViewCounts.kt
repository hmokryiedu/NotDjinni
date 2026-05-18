package not.djinni.database.impl.vacancy

import not.djinni.database.impl.viewed.ViewedVacancyTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.count

internal fun countViewsByVacancyIds(vacancyIds: Collection<Long>): Map<Long, Int> {
    if (vacancyIds.isEmpty()) return emptyMap()

    val vacancyEntityIds = vacancyIds.distinct().map { EntityID(it, VacancyTable) }
    val countExpression = ViewedVacancyTable.id.count()
    return ViewedVacancyTable
        .select(ViewedVacancyTable.vacancyId, countExpression)
        .where { ViewedVacancyTable.vacancyId inList vacancyEntityIds }
        .groupBy(ViewedVacancyTable.vacancyId)
        .associate { row ->
            row[ViewedVacancyTable.vacancyId].value to row[countExpression].toInt()
        }
}
