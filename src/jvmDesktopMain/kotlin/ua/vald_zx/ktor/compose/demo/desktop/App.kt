package ua.vald_zx.ktor.compose.demo.desktop

import androidx.compose.material.Text
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.common.foundation.layout.Row
import ua.vald_zx.ktor.compose.counter.CounterAction
import ua.vald_zx.ktor.compose.counter.CounterStore

val store = CounterStore()
fun main() = Window {
    val state = store.observeState().collectAsState()
    Row {
        Button(onClick = { store.dispatch(CounterAction.Sub) }) {
            Text("-")
        }
        Text(state.value.count.toString(), modifier = Modifier.padding(16.dp))
        Button(onClick = { store.dispatch(CounterAction.Add) }) {
            Text("+")
        }
    }
}