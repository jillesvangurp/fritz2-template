import dev.fritz2.binding.storeOf
import dev.fritz2.dom.*
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.ScopeContext
import dev.fritz2.dom.html.render
import dev.fritz2.identification.Id
import kotlinx.browser.document
import kotlinx.coroutines.*
import org.w3c.dom.DocumentFragment
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.js.Promise

class Transition(
    val enter: String? = null,
    val enterStart: String? = null,
    val enterEnd: String? = null,
    val leave: String? = null,
    val leaveStart: String? = null,
    val leaveEnd: String? = null
) {
    companion object {
        val leaveTransition: DomLifecycleHandler = { target, payload ->
            val transition = payload.unsafeCast<Transition?>()
            if (transition?.leave != null) {
                val classes = target.domNode.getAttribute("class").orEmpty()
                target.domNode.setAttribute("class", "$classes ${transition.leaveStart.orEmpty()}")
                target.domNode.setAttribute("class", "$classes ${transition.leave} ${transition.leaveEnd.orEmpty()}")
                animationDone(target.domNode).asDeferred()
            } else null
        }

        val enterTransition: DomLifecycleHandler = { target, payload ->
            val transition = payload.unsafeCast<Transition?>()
            if (transition?.enter != null) {
                val classes = target.domNode.getAttribute("class").orEmpty()
                target.domNode.setAttribute("class", "$classes ${transition.enterStart.orEmpty()}")
                //TODO: is this needed a second time in some browsers?
                kotlinx.browser.window.requestAnimationFrame {
                    target.domNode.setAttribute(
                        "class",
                        "$classes ${transition.enter} ${transition.enterEnd.orEmpty()}"
                    )
                    animationDone(target.domNode).then {
                        target.domNode.setAttribute("class", classes)
                    }
                }
            }
            null
        }
    }
}

val fade = Transition(
    "transition-all duration-100",
    "opacity-0",
    "opacity-100",
    "transition-all ease-out duration-100",
    "opacity-100",
    "opacity-0"
)

//FIXME: use from WebComponents?
@JsName("Function")
private external fun <T> nativeFunction(vararg params: String, block: String): T

val animationDone = nativeFunction<(Node) -> Promise<Unit>>(
    "_node", block = """
         return Promise.all(
           _node.getAnimations().map(
             function(animation) {
               return animation.finished
             }
           )
         )
    """.trimIndent()
)


class Fragment(override val job: Job, override val scope: Scope, private val apply: WithDomNode<Element>.() -> Unit) :
    RenderContext, WithDomNode<DocumentFragment> {
    override val domNode: DocumentFragment = document.createDocumentFragment()

    override fun <E : Node, T : WithDomNode<E>> register(element: T, content: (T) -> Unit): T {
        if (element.domNode is Element) this.apply.invoke(element.unsafeCast<WithDomNode<Element>>())
        content(element)
        domNode.appendChild(element.domNode)
        return element
    }
}

fun RenderContext.fragment(
    scopeContext: (ScopeContext.() -> Unit) = {},
    content: RenderContext.() -> Unit,
    apply: WithDomNode<Element>.() -> Unit = {},
): Fragment {
    //FIXME: replace by evalScope
    val s = ScopeContext(this.scope).apply(scopeContext).scope
    return register(Fragment(job, s, apply), content)
}


fun RenderContext.transition(transition: Transition, content: RenderContext.() -> Tag<*>) =
    content().apply {
        if (transition.leave != null) beforeUnmount(transition, Transition.leaveTransition)
        if (transition.enter != null) afterMount(transition, Transition.enterTransition)
    }

fun RenderContext.transition(
    enter: String? = null,
    enterStart: String? = null,
    enterEnd: String? = null,
    leave: String? = null,
    leaveStart: String? = null,
    leaveEnd: String? = null, content: RenderContext.() -> Tag<*>
) = transition(Transition(enter, enterStart, enterEnd, leave, leaveStart, leaveEnd), content)


fun main() {
    val visible = storeOf(true)

    val list = storeOf(listOf(Id.next(), Id.next(), Id.next()))
    val addItem = list.handle { it + Id.next() }
    val addMany = list.handle { it + Id.next() + Id.next() + Id.next() }
    val removeMany = list.handle { it.take(1) }
    val removeItem = list.handle { list, value: String -> list - value }

    MainScope().launch {
        for (i in 1..10000) {
            console.log("$i")
            delay(150)
            if (i % 2 == 0) {
                addMany()
            } else {
                removeMany()
            }
        }
    }

    render {
        div {
            button("p-4 border") {
                +"add"
                clicks handledBy addItem
            }
            button("p-4 border") {
                +"add many"
                clicks handledBy addMany
            }
            button("p-4 border") {
                +"shuffle"
                clicks handledBy list.handle { it.shuffled() }
            }
            button("p-4 border") {
                +"remove many"
                clicks handledBy removeMany
            }

            list.data.renderEach({ it }) { item ->
                div { //FIXME: get rid of this?
                    transition(fade) {
                        div(id = "myDiv") {
                            inlineStyle("margin-top: 10px; width: 200px; height: 50px; background-color: lightblue;")
                            +item
                            clicks.map { item } handledBy removeItem
                        }
                    }
                }
            }
        }

        div {
            inlineStyle("margin-top: 20px;")
            button("p-4 border") {
                +"toggle"
                clicks handledBy visible.handle { !it }
            }
            visible.data.render {
                if (it) {
                    transition(fade) {
                        div {
                            div(id = "myDiv") {
                                inlineStyle("margin-top: 10px; width: 200px; height: 200px; background-color: red;")
                            }

                            div(id = "myDiv2") {
                                inlineStyle("margin-top: 10px; width: 200px; height: 200px; background-color: red;")
                            }

                            div(id = "myDiv3") {
                                inlineStyle("margin-top: 10px; width: 200px; height: 200px; background-color: red;")
                            }
                        }
                    }
                }
            }
        }
    }
}