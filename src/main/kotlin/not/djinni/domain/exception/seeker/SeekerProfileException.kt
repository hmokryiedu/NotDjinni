package not.djinni.domain.exception.seeker

sealed class SeekerProfileException(override val message: String) : Throwable() {
    class ProfileNotFound : SeekerProfileException("Profile not found")
    class ProfileAlreadyExists : SeekerProfileException("Profile already exists")
    class WorkExperienceNotFound : SeekerProfileException("Work experience not found")
    class Unauthorized : SeekerProfileException("Unauthorized to access this resource")
    data class InvalidProfileData(override val message: String) : SeekerProfileException(message)
}
