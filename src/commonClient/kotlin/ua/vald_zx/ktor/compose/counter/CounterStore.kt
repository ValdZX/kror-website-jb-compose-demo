package ua.vald_zx.ktor.compose.counter

import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ua.vald_zx.ktor.compose.nanoredux.Action
import ua.vald_zx.ktor.compose.nanoredux.State
import ua.vald_zx.ktor.compose.nanoredux.Store

data class CounterState(
    val count: Int = 0,
) : State

sealed class CounterAction : Action {
    object Add : CounterAction()
    object Sub : CounterAction()
    data class Extertal(val count: Int) : CounterAction()
}

class CounterStore : Store<CounterState, CounterAction>, CoroutineScope by CoroutineScope(Dispatchers.Main) {
    val client = HttpClient {
        install(Logging)
        install(WebSockets)
    }

    init {
        launch(Dispatchers.Default) {
            client.webSocket(method = HttpMethod.Get, host = "0.0.0.0", port = 8180, path = "/counter") {
                val messageOutputRoutine = launch { outputMessages() }
                val userInputRoutine = launch { inputMessages() }
                userInputRoutine.join()
                messageOutputRoutine.cancelAndJoin()
            }
        }
    }

    private val state: MutableStateFlow<CounterState> = MutableStateFlow(CounterState())

    override fun observeState(): StateFlow<CounterState> = state

    override fun dispatch(action: CounterAction) {
        val oldState = state.value
        val newState = when (action) {
            is CounterAction.Add -> {
                oldState.copy(count = oldState.count + 1)
            }
            is CounterAction.Sub -> {
                oldState.copy(count = oldState.count - 1)
            }
            is CounterAction.Extertal -> {
                oldState.copy(count = action.count)
            }
        }
        if (newState != oldState) {
            state.value = newState
        }
    }

    suspend fun DefaultClientWebSocketSession.outputMessages() {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            val count = message.readText().toInt()
            if (state.value.count != count) {
                dispatch(CounterAction.Extertal(count))
            }
        }
    }

    suspend fun DefaultClientWebSocketSession.inputMessages() {
        while (true) {
            state.collect {
                send(state.value.count.toString())
            }
        }
    }
}