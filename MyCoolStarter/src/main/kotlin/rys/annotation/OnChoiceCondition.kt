package rys.annotation


import org.apache.logging.log4j.kotlin.logger
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import java.awt.HeadlessException
import javax.swing.JOptionPane


class OnChoiceCondition : Condition {

    //TODO spring is coming
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        //logger().log()
        try {
            return JOptionPane.showConfirmDialog(null, "Подключаем бин?") == 0

        } catch (e: HeadlessException) {
            println("No GUI support")
            return false
        }
    }

}
