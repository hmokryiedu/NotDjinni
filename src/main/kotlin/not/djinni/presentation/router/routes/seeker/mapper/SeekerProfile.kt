package not.djinni.presentation.router.routes.seeker.mapper

import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience
import not.djinni.model.vacancy.JobCategoryCode
import not.djinni.presentation.router.routes.common.request.JobCategoryRequest
import not.djinni.presentation.router.routes.seeker.request.CreateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.UpdateProfileRequest
import not.djinni.presentation.router.routes.seeker.request.WorkExperienceRequest
import not.djinni.presentation.router.routes.seeker.response.SeekerProfileResponse
import not.djinni.presentation.router.routes.seeker.response.WorkExperienceResponse

fun JobCategoryRequest.toDomain() = JobCategoryCode.valueOf(name)

fun JobCategoryCode.toRequest() = JobCategoryRequest.valueOf(name)

fun SeekerProfile.toResponse(): SeekerProfileResponse {
    return SeekerProfileResponse(
        id = id,
        aboutMe = aboutMe,
        specialty = speciality,
        desiredSalary = desiredSalary,
        experienceYears = experienceYears,
        jobCategory = jobCategory.toRequest().name,
        workExperience = workExperience.map { it.toResponse() }
    )
}

fun WorkExperience.toResponse(): WorkExperienceResponse {
    return WorkExperienceResponse(
        id = id,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate,
        isCurrent = isCurrent
    )
}

fun CreateProfileRequest.toDomain(): SeekerProfile {
    return SeekerProfile(
        id = 0,
        aboutMe = aboutMe,
        speciality = specialty,
        desiredSalary = desiredSalary,
        experienceYears = experienceYears,
        jobCategory = jobCategory.toDomain(),
        workExperience = workExperience.map { it.toDomain() }
    )
}

fun UpdateProfileRequest.toDomain(): SeekerProfile {
    return SeekerProfile(
        id = 0,
        aboutMe = aboutMe,
        speciality = specialty,
        desiredSalary = desiredSalary,
        experienceYears = experienceYears,
        jobCategory = jobCategory.toDomain(),
        workExperience = workExperience.map { it.toDomain() }
    )
}

fun WorkExperienceRequest.toDomain(): WorkExperience {
    return WorkExperience(
        id = 0,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}
