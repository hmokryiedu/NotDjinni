package not.djinni.domain.usecase.core

interface UseCaseWithParams<out T, in P> {
    suspend operator fun invoke(params: P): T
}