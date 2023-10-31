import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate

@SpringBootTest
class KafkaTest {
    @Autowired
    private val kafka: ReactiveKafkaProducerTemplate<String, String>? = null
}
