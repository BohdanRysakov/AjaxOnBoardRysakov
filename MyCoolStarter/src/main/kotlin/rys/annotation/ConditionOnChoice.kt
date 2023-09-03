package rys.annotation

import org.springframework.context.annotation.Conditional
import java.lang.annotation.RetentionPolicy

@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnChoiceCondition::class)
annotation class ConditionOnChoice
