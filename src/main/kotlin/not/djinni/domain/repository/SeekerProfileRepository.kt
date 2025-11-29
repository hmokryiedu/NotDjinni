package not.djinni.domain.repository

import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience

interface SeekerProfileRepository {

    suspend fun getProfile(userId: Long): Result<SeekerProfile>
    suspend fun createProfile(userId: Long, profile: SeekerProfile): Result<SeekerProfile>
    suspend fun updateProfile(userId: Long, profile: SeekerProfile): Result<Unit>
    suspend fun deleteProfile(userId: Long): Result<Unit>

    suspend fun addWorkExperience(userId: Long, experience: WorkExperience): Result<Long>
    suspend fun updateWorkExperience(userId: Long, experienceId: Long, experience: WorkExperience): Result<Unit>
    suspend fun deleteWorkExperience(userId: Long, experienceId: Long): Result<Unit>
}
