import dev.fritz2.binding.storeOf
import dev.fritz2.tailwind.renderTailwind
import dev.fritz2.tailwind.require
import dev.fritz2.tailwind.ui.alert
import dev.fritz2.tailwind.ui.card
import dev.fritz2.tailwind.ui.icons.Solid
import model.Framework

fun main() {
    require("./styles.css")
    val frameworkStore = storeOf(Framework("fritz2"))

    renderTailwind {
        card("w-60 mb-8") {
            header("Header")
            content("Lorem ipsum, dolor sit amet...")
            footer("Footer")
        }

        alert("w-72 my-4") {
            icon(Solid.check)
            header("This is my second alert!")
            details {
                p { +"And here go some details" }
            }
            color = success
        }
    }
}