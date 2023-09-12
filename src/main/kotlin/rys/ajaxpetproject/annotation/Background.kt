package rys.ajaxpetproject.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Background(
    val Policy:PolicyType = PolicyType.INFINITE,
    val delay: Long = 10000,
    val iterations:Int = -1)

