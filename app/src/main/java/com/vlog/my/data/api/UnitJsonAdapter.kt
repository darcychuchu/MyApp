package com.vlog.my.data.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException

/**
 * Moshi 适配器，用于处理 Kotlin Unit 类型
 * 这是必要的，因为 Moshi 默认不知道如何序列化/反序列化 Unit 类型
 */
class UnitJsonAdapter : JsonAdapter<Unit>() {
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Unit {
        // 消费 JSON 值但不做任何事情
        reader.skipValue()
        return Unit
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Unit?) {
        // Unit 类型序列化为空对象
        writer.beginObject()
        writer.endObject()
    }
}
