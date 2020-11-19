package com.dazhi.http.retrofit

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-12 下午6:21
 * 实例：createApi<IApkUpgrade>()
 */
val okHttpClient: OkHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

inline fun <reified T> createApi(
        baseUrl: String = "http://127.0.0.1/",
        client: OkHttpClient = okHttpClient,
        callAdapterFactory: CallAdapter.Factory = FlowCallAdapterFactory(),
        converterFactory: Converter.Factory? = StringConverterFactory.own()
): T {
    val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(callAdapterFactory)
            .apply {
                if(converterFactory!=null) {
                    addConverterFactory(converterFactory)
                }
            }
            .build()
    return retrofit.create(T::class.java)
}