package not.djinni.domain.usecase.core

interface UseCase<out T> {
    suspend operator fun invoke(): T
}