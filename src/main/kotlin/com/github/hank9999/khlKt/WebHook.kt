package com.github.hank9999.khlKt

import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.javalin.http.Context
import com.github.hank9999.khlKt.json.JSON.Companion.json
import com.github.hank9999.khlKt.json.JSON.Companion.t
import com.github.hank9999.khlKt.json.JSON.Operator.get
import com.github.hank9999.khlKt.json.JSON.Operator.invoke
import com.github.hank9999.khlKt.types.types.MessageTypes
import com.github.hank9999.khlKt.types.types.MessageTypes.*
import com.github.hank9999.khlKt.http.exceptions.HttpException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.zip.InflaterInputStream

class WebHook {

    private val logger: Logger = LoggerFactory.getLogger(WebHook::class.java)
    private val config: Config = Bot.config

    fun initialize() {
        val app = Javalin.create().start(config.host, config.port)
        app.post(config.path) { ctx -> khlHandler(ctx) }
    }

    private fun decompressZlib(content: ByteArray): String {
        return InflaterInputStream(content.inputStream()).bufferedReader().use { it.readText() }
    }

    private fun khlHandler(ctx: Context) {
        val body = decompressZlib(ctx.bodyAsBytes())
        val element = json.parseToJsonElement(body)
        if (element["s"](t.int) != 0) {
            logger.warn("[Khl] Unknown signaling, ignored")
            return
        }
        val dObject = element["d"]
        if (dObject["verify_token"](t.string) != config.verify_token) {
            logger.warn("[Khl] Wrong Verify Token, message may be fake, ignored")
            return
        }

        try {
            when (MessageTypes.fromInt(dObject["type"](t.int))) {
                KMD, TEXT, CARD, VIDEO, IMG, AUDIO, FILE -> Bot.khlHandler.messageHandler(dObject)
                SYS -> khlEventHandler(ctx, dObject)
                ALL -> TODO()
            }
        } catch(e: HttpException) {
            logger.error("${e.javaClass.name} ${e.message}")
            ctx.status(200)
        } catch (e: Exception) {
            // 如果遇到什么奇怪的bug 打印json全文
            logger.error(body)
            logger.error("${e.javaClass.name} ${e.message}")
            // logger.error(e.stackTraceToString())
            ctx.status(200)
        }
    }

    private fun khlEventHandler(ctx: Context, element: JsonElement) {
        when (element["channel_type"](t.string)) {
            "WEBHOOK_CHALLENGE" -> {
                val challenge = element["challenge"](t.string)
                val resp = buildJsonObject { put("challenge", challenge) }.toString()
                ctx.contentType("application/json").result(resp)
                logger.info("[Khl] Received WEBHOOK_CHALLENGE request, challenge: $challenge, Responded")
            }
            else -> Bot.khlHandler.eventHandler(element)
        }
    }


}