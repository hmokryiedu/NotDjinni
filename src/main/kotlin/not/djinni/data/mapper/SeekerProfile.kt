package not.djinni.data.mapper

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience

fun SeekerProfileEntity.toDomain(workExperiences: List<WorkExperience> = emptyList()): SeekerProfile {
    return SeekerProfile(
        id = id,
        speciality = specialty,
        experienceYears = experienceYears,
        desiredSalary = desiredSalary,
        aboutMe = aboutMe,
        workExperience = workExperiences
    )
}

fun SeekerProfile.toEntity(userId: Long): SeekerProfileEntity {
    return SeekerProfileEntity(
        id = id,
        userId = userId,
        specialty = speciality,
        experienceYears = experienceYears,
        desiredSalary = desiredSalary,
        aboutMe = aboutMe
    )
}
