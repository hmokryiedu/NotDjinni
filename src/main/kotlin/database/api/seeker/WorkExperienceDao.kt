package not.djinni.database.api.seeker

interface WorkExperienceDao {
    suspend fun createWorkExperience(experience: WorkExperienceEntity): Long
    suspend fun getWorkExperience(id: Long): WorkExperienceEntity?
    suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity>
    suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean
    suspend fun deleteWorkExperience(id: Long): Boolean
}
