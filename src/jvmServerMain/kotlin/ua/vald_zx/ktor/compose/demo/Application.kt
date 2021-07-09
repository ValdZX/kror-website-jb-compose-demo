package ua.vald_zx.ktor.compose.demo

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(5)
    }
    routing {
        static("/") {
            resources("")
        }
        get("/") {
            call.respondText(
                this::class.java.classLoader.getResource("index.html")!!.readText(),
                ContentType.Text.Html
            )
        }
        val sessions = ConcurrentList<WebSocketServerSession>()
        webSocket("/counter") {
            sessions.add(this)
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    sessions.forEach { session ->
                        session.outgoing.send(Frame.Text(text))
                    }
                }
            }
        }
    }
}