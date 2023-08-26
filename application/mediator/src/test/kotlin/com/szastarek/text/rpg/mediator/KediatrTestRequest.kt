package com.szastarek.text.rpg.mediator

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.Query

internal class SimpleCommand : Command

internal class SimpleCommandWithResult : CommandWithResult<String>

internal class SimpleQuery : Query<String>

internal class SimpleNotification : Notification

internal class UnknownRequest
