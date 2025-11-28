package not.djinni.data.mapper

import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.model.role.WorkExperience

fun WorkExperienceEntity.toDomain(): WorkExperience {
    return WorkExperience(
        id = id,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}

fun WorkExperience.toEntity(profileId: Long): WorkExperienceEntity {
    return WorkExperienceEntity(
        id = id,
        profileId = profileId,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}
