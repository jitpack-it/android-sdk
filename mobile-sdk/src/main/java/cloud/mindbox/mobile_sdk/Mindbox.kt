package cloud.mindbox.mobile_sdk

import android.content.Context
import cloud.mindbox.mobile_sdk.managers.GatewayManager
import cloud.mindbox.mobile_sdk.managers.IdentifierManager
import cloud.mindbox.mobile_sdk.models.FullInitData
import cloud.mindbox.mobile_sdk.models.PartialInitData
import cloud.mindbox.mobile_sdk.repository.MindboxPreferences
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object Mindbox {

    private var context: Context? = null
    private val mindboxJob = Job()
    private val mindboxScope = CoroutineScope(Default + mindboxJob)

    fun init(context: Context, configuration: Configuration, callback: (String?, String?) -> Unit) {
        this.context = context

        Hawk.init(context).build()
        mindboxScope.launch(Main) {
            callback.invoke(initDeviceId(), MindboxPreferences.installationId)
        }
    }

    private suspend fun initDeviceId(): String? {
        val adid = mindboxScope.async { IdentifierManager.getAdsIdentification(context) }
        return adid.await()
    }

    private fun setInstallationId(id: String) {
        if (id.isNotEmpty() && id != MindboxPreferences.installationId) {
            MindboxPreferences.installationId = id
        }
    }

    //todo validate fields
    fun registerSdk(
        context: Context,
        endpoint: String,
        deviceUuid: String,
        installationId: String
    ) {
        mindboxScope.launch {
            if (MindboxPreferences.isFirstInitialize) {
                firstInitialize(context, endpoint, deviceUuid, installationId)
            } else {
                secondaryInitialize()
            }
        }
    }

    private suspend fun firstInitialize(
        context: Context,
        endpoint: String,
        deviceUuid: String,
        installationId: String
    ) {
        val firebaseToken = mindboxScope.async { IdentifierManager.getFirebaseToken() }
        val adid = mindboxScope.async { IdentifierManager.getAdsIdentification(context) }
        setInstallationId(installationId)

        val deviceId = if (deviceUuid.isNotEmpty()) {
            deviceUuid
        } else {
            adid.await()
        }

        registerClient(
            firebaseToken.await(),
            endpoint,
            deviceId ?: "",
            MindboxPreferences.installationId ?: ""
        )
    }

    private suspend fun secondaryInitialize() {

    }

    private fun registerClient(
        firebaseToken: String?, endpoint: String, deviceUuid: String, installationId: String
    ) {
        MindboxPreferences.isFirstInitialize = false

        val isTokenAvailable = !firebaseToken.isNullOrEmpty()
        val initData = if (installationId.isNotEmpty()) {
            FullInitData(
                firebaseToken ?: "",
                isTokenAvailable,
                installationId,
                false //fixme
            )
        } else {
            PartialInitData(
                firebaseToken ?: "",
                isTokenAvailable,
                false //fixme
            )
        }

        GatewayManager.sendFirstInitialization(
            endpoint,
            deviceUuid,
            initData
        )
    }

    fun release() {
        context = null
        mindboxJob.cancel()
    }
}