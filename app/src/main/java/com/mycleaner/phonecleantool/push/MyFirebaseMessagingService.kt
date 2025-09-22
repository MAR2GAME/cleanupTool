package com.mycleaner.phonecleantool.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mycleaner.phonecleantool.utils.LogUtil


class MyFirebaseMessagingService : FirebaseMessagingService() {

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		PushManager.pushToken = token
		Log.e(TAG, "onNewToken: $token")
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		Log.e(TAG, "onMessageReceived: ${message.notification?.body}")
		Log.e(TAG, "onMessageReceived: ${message.notification?.title}")
		Log.e(TAG, "onMessageReceived: ${message.data}")
		val paramsAppOpenFrom = message.data["AppOpenFrom"] ?: "Push"
		val paramsNoticeId = message.data["NoticeId"] ?: ""
		val paramsDistinctId = message.data["DistinctId"] ?: ""

		LogUtil.log("notification_shown",mapOf(
			"msg_id" to paramsNoticeId,
		"target_user_id" to paramsDistinctId
		))
	}

	companion object {
		private const val TAG = "MyFirebaseMessagingServ"
	}

}