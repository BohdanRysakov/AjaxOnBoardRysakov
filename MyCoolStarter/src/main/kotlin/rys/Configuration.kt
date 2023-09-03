package rys

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import rys.annotation.ConditionOnChoice


@Configuration
open class Configuration {
    @Bean
    @ConditionOnChoice
    open fun startListener() : AppLoadListnener {
        return AppLoadListnener();
    }
}