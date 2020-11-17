package com.dazhi.http.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 功能：Retrofit请求直接回字符串的转换工厂类
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-12 下午6:28
 * 用法：
 * Retrofit.Builder()
 *         ...
 *         .addConverterFactory(StringConverterFactory.own())
 *         .build()
 */
class StringConverterFactory private constructor(): Converter.Factory() {
    private val INSTANCE_CONVERTER = StringConverter()

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        return if (type === String::class.java) {
            INSTANCE_CONVERTER
        } else null //其它类型我们不处理，返回null就行
    }

    companion object {
        private val INSTANCE = StringConverterFactory()

        @JvmStatic
        fun own(): StringConverterFactory {
            return INSTANCE
        }
    }
}

private class StringConverter : Converter<ResponseBody, String> {
    override fun convert(responseBody: ResponseBody): String? {
        return responseBody.string()
    }
}