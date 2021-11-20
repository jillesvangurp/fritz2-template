import dev.fritz2.binding.storeOf
import dev.fritz2.dom.afterMount
import dev.fritz2.dom.beforeUnmount
import dev.fritz2.dom.html.render
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow


fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}


@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val runs = flow<Unit> {
        for (i in 1..50) {
            console.log(i)
            delay(1000)
            emit(Unit)
        }
    }

    val b = flow {
        for (i in 1..100) {
            delay(10)
            val s = "s$i" + getRandomString(1024)
            emit(s)
        }
    }


    val s = storeOf(listOf(1, 2, 3))
    val modify = s.handle {
        val length = (0..5).random()
        buildList {
            (0 until length).onEach {
                add((0..255).random())
            }
        }
    }


    render {
        runs handledBy modify

        button {
            +"change"
//            clicks handledBy modify
        }
        ul {
            s.data.renderEach(into = this) { num ->
                li {
                    afterMount { _, _ ->
                        val x = getRandomString(1024)
                        null
                    }
                    +num.toString()
                    +" value = "
//                    b.asText()
                    b.render(into = this) {
                        beforeUnmount { _, _ ->
                            val x = it.length
                            null
                        }
                        span { +it }
                    }
                }
            }
        }


        div {
            s.data.asText()
        }
    }
}