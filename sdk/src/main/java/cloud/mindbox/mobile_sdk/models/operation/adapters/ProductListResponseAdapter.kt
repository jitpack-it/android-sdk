package cloud.mindbox.mobile_sdk.models.operation.adapters

import cloud.mindbox.mobile_sdk.models.operation.response.CatalogProductListResponse
import cloud.mindbox.mobile_sdk.models.operation.response.ProductListItemResponse
import cloud.mindbox.mobile_sdk.returnOnException
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.*

class ProductListResponseAdapter : TypeAdapter<Any>() {

    private val gson by lazy { Gson() }

    override fun write(out: JsonWriter?, value: Any?) {
        runCatching {
            if (value == null) {
                out?.nullValue()
            } else {
                out?.jsonValue(gson.toJson(value))
            }
        }.returnOnException { out }
    }

    override fun read(`in`: JsonReader?): Any? = `in`?.let { reader ->
        runCatching {
            when (reader.peek()) {
                JsonToken.NULL -> {
                    reader.nextNull()
                    return@let null
                }
                JsonToken.BEGIN_ARRAY -> {
                    gson.fromJson<List<ProductListItemResponse>?>(
                        reader,
                        object : TypeToken<List<ProductListItemResponse>?>() {}.type
                    )
                }
                JsonToken.BEGIN_OBJECT -> {
                    gson.fromJson<CatalogProductListResponse?>(
                        reader,
                        CatalogProductListResponse::class.java
                    )
                }
                else -> null
            }
        }.returnOnException { null }
    }

}
