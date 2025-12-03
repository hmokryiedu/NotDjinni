package not.djinni.model.role

import not.djinni.model.vacancy.JobCategoryCode

data class SeekerProfile(
    val id: Long,
    val aboutMe: String?,
    val speciality: String,
    val desiredSalary: Int,
    val experienceYears: Int,
    val jobCategory: JobCategoryCode,
    val workExperience: List<WorkExperience> = emptyList()
)
