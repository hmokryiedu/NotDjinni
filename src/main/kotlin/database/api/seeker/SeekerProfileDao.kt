package not.djinni.database.api.seeker

interface SeekerProfileDao {
    suspend fun createProfile(profile: SeekerProfileEntity): Long
    suspend fun getProfile(id: Long): SeekerProfileEntity?
    suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity?
    suspend fun updateProfile(profile: SeekerProfileEntity): Boolean
    suspend fun profileExists(userId: Long): Boolean
    suspend fun getProfileWithWorkExperience(userId: Long): Pair<SeekerProfileEntity, List<WorkExperienceEntity>>?
}
