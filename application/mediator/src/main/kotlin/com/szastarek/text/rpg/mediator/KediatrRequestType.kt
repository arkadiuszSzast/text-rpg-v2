package com.szastarek.text.rpg.mediator

enum class KediatrRequestType(val code: String) {
    Command("command"),
    CommandWithResult("command-with-result"),
    Query("query"),
    Notification("notification"),
    Unknown("unknown")
}
