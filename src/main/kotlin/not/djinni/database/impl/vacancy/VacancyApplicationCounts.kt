package not.djinni.database.impl.vacancy

import not.djinni.database.impl.application.ApplicationTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.count

internal fun countApplicationsByVacancyIds(vacancyIds: List<Long>): Map<Long, Int> {
    if (vacancyIds.isEmpty()) return emptyMap()

    val vacancyEntityIds = vacancyIds.distinct().map { EntityID(it, VacancyTable) }
    val countExpression = ApplicationTable.id.count()
    return ApplicationTable
        .select(ApplicationTable.vacancyId, countExpression)
        .where {
            ApplicationTable.vacancyId inList vacancyEntityIds
        }
        .groupBy(ApplicationTable.vacancyId)
        .associate { row ->
            row[ApplicationTable.vacancyId].value to row[countExpression].toInt()
        }
}
