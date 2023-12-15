package com.szastarek.text.rpg.mediator

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.Query

object KediatrRequestTypeExtractor {
	fun <TRequest> extract(request: TRequest) =
		when (request) {
			is Command -> KediatrRequestType.Command
			is CommandWithResult<*> -> KediatrRequestType.CommandWithResult
			is Query<*> -> KediatrRequestType.Query
			is Notification -> KediatrRequestType.Notification
			else -> KediatrRequestType.Unknown
		}
}
