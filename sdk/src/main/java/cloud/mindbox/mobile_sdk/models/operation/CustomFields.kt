package cloud.mindbox.mobile_sdk.models.operation

import cloud.mindbox.mobile_sdk.models.operation.adapters.CustomerFieldsAdapter
import cloud.mindbox.mobile_sdk.returnOnException
import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import androidx.core.util.Pair as AndroidXPair

@JsonAdapter(CustomerFieldsAdapter::class)
class CustomFields(val fields: Map<String, Any?>? = null) {

    /**
     * Convert [CustomFields] value to [T] typed object.
     *
     * @param classOfT Class type for result [CustomFields] object.
     */
    fun <T> convertTo(classOfT: Class<T>): T? = runCatching {
        val gson = Gson()
        return gson.fromJson(gson.toJson(fields), classOfT)
    }.returnOnException { null }

    constructor(vararg pairs: Pair<String, Any?>) : this(pairs.toMap())

    constructor(
        vararg pairs: AndroidXPair<String, Any?>
    ) : this(*pairs.mapNotNull { pair -> pair.first?.let { Pair(it, pair.second) } }.toTypedArray())


    override fun toString() = "CustomFields(fields=$fields)"

}
