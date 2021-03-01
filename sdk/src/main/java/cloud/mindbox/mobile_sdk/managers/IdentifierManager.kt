package cloud.mindbox.mobile_sdk.managers

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import cloud.mindbox.mobile_sdk.Logger
import cloud.mindbox.mobile_sdk.repository.MindboxPreferences
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

internal object IdentifierManager {

    fun isNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager?.areNotificationsEnabled() != true) {
                return false
            }
            return manager.notificationChannels.firstOrNull { channel ->
                channel.importance == NotificationManager.IMPORTANCE_NONE
            } == null
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun registerFirebaseToken(): String? {
        return try {
            val token: String? = Tasks.await(FirebaseMessaging.getInstance().token)
            if (!token.isNullOrEmpty() && token != MindboxPreferences.firebaseToken) {
                Logger.i(this, "Token gets or updates from firebase")
            }
            token
        } catch (e: Exception) {
            Logger.w(this, "Fetching FCM registration token failed with exception $e")
            null
        }
    }

    fun getAdsIdentification(context: Context): String {
        return try {
            val advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            if (!advertisingIdInfo.isLimitAdTrackingEnabled && !advertisingIdInfo.id.isNullOrEmpty()) {
                val id = advertisingIdInfo.id
                Logger.d(
                    this, "Received from AdvertisingIdClient: device uuid - $id"
                )
                id
            } else {
                val id = generateRandomUuid()
                Logger.d(
                    this,
                    "Device uuid cannot be received from AdvertisingIdClient. Will be generated from Random - $id"
                )
                id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val id = generateRandomUuid()
            Logger.d(
                this,
                "Device uuid cannot be received from AdvertisingIdClient. Will be generated from Random - $id"
            )
            id
        }
    }

    private fun generateRandomUuid() = UUID.randomUUID().toString()
}