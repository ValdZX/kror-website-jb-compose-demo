import androidx.compose.runtime.collectAsState
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import ua.vald_zx.ktor.compose.counter.CounterAction
import ua.vald_zx.ktor.compose.counter.CounterStore

fun main() {
    val store = CounterStore()
    renderComposable(rootElementId = "root") {
        val state = store.observeState().collectAsState()
        Div({ style { padding(25.px) } }) {
            Button(attrs = {
                onClick { store.dispatch(CounterAction.Sub) }
            }) {
                Text("-")
            }
            Span({ style { padding(15.px) } }) {
                Text(state.value.count.toString())
            }
            Button(attrs = {
                onClick { store.dispatch(CounterAction.Add) }
            }) {
                Text("+")
            }
        }
    }
}