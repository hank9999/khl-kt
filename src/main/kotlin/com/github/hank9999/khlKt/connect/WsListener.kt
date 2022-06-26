package com.github.hank9999.khlKt.connect

import com.github.hank9999.khlKt.connect.Type.Status
import com.github.hank9999.khlKt.connect.Utils.Companion.decompressZlib
import com.github.hank9999.khlKt.connect.WebSocket.Companion.addQueue
import com.github.hank9999.khlKt.connect.WebSocket.Companion.logger
import okhttp3.Response
import okhttp3.WebSocketListener
import okio.ByteString

class WsListener : WebSocketListener() {
    override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        logger.debug("[WebSocket] Closed $code, $reason")
        WebSocket.status = Status.CLOSED
    }

    override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        logger.debug("[WebSocket] Closing $code, $reason")
    }

    override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        logger.error("[WebSocket] Failure ${t.cause}, ${t.message}, ${response?.message}")
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        addQueue(decompressZlib(bytes.toByteArray()))
    }

    override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        logger.debug("[WebSocket] Connected")
    }

}