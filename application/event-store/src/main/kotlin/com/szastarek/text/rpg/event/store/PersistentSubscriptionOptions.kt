package com.szastarek.text.rpg.event.store

import com.eventstore.dbclient.CreatePersistentSubscriptionToStreamOptions
import com.eventstore.dbclient.NackAction
import com.eventstore.dbclient.SubscribePersistentSubscriptionOptions
import com.eventstore.dbclient.UserCredentials

data class PersistentSubscriptionOptions(
	val subscriptionOptions: SubscribePersistentSubscriptionOptions = SubscribePersistentSubscriptionOptions.get(),
	val nackAction: NackAction = NackAction.Park,
	val autoAcknowledge: Boolean = true,
	val autoCreateStreamGroup: Boolean = true,
	val maxRetries: Long = 5,
	val maxResubscribeAttempts: Long = 100,
	val createPersistentSubscriptionToStreamOptions: CreatePersistentSubscriptionToStreamOptions =
		CreatePersistentSubscriptionToStreamOptions.get().fromStart().resolveLinkTos(),
) {
	fun bufferSize(size: Int) = this.copy(subscriptionOptions = subscriptionOptions.bufferSize(size))

	fun authenticated(credentials: UserCredentials) = this.copy(subscriptionOptions = subscriptionOptions.authenticated(credentials))

	fun deadline(deadline: Long) = this.copy(subscriptionOptions = subscriptionOptions.deadline(deadline))

	fun notRequireLeader() = this.copy(subscriptionOptions = subscriptionOptions.notRequireLeader())

	fun requiresLeader() = this.copy(subscriptionOptions = subscriptionOptions.requiresLeader())

	fun autoCreateStreamGroup() = this.copy(autoCreateStreamGroup = true)

	fun notAutoCreateStreamGroup() = this.copy(autoCreateStreamGroup = false)

	fun nackPark() = this.copy(nackAction = NackAction.Park)

	fun nackRetry() = this.copy(nackAction = NackAction.Retry)

	fun nackSkip() = this.copy(nackAction = NackAction.Skip)

	fun nackStop() = this.copy(nackAction = NackAction.Stop)

	fun nackUnknown() = this.copy(nackAction = NackAction.Unknown)

	fun autoAcknowledge() = this.copy(autoAcknowledge = true)

	fun notAutoAcknowledge() = this.copy(autoAcknowledge = false)

	fun maxRetries(max: Long) = this.copy(maxRetries = max)
}
