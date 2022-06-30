package com.github.hank9999.kook

data class Config(
    val token: String,
    val cmd_prefix: List<String> = listOf(".", "。", "/"),
    val verify_token: String = "",
    val host: String = "",
    val port: Int = 3000,
    val path: String = "/webhook"
)