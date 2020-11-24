package com.dazhi.http.retrofit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 功能：源数据直接通过异步流来处理
 * 描述：官方目前未提供协程流的适配器
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-12 下午5:43
 * 用法：
 * Retrofit.Builder()
 *         ...
 *         .addCallAdapterFactory(FlowCallAdapterFactory())
 *         .build()
 * //
 * @POST
 * fun login(@Body any: Any): Flow<String>
 */
class FlowCallAdapterFactory : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) {
            return null
        }
        val type = getParameterUpperBound(0, returnType as ParameterizedType)
        return FlowCallAdapter<Any>(type)
    }
}

private class FlowCallAdapter<R>(private val responseType: Type) : CallAdapter<R, Flow<R?>> {
    override fun responseType(): Type {
        return responseType
    }

    @ExperimentalCoroutinesApi
    override fun adapt(call: Call<R>): Flow<R?> {
        return callbackFlow {
            val started = AtomicBoolean(false)
            if (started.compareAndSet(false, true)) {
                call.enqueue(object : Callback<R> {
                    override fun onResponse(call: Call<R>, response: Response<R>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            // 206是文件下载时，回待下载文件的总长度用
                            if (body == null || (response.code()!=206 && response.code() != 200)) {
                                close(Throwable("HTTP status code: ${response.code()}"))
                            } else {
                                offer(body)
                            }
                        } else {
                            close(Throwable(errorMsg(response) ?: "unknown error"))
                        }
                    }

                    override fun onFailure(call: Call<R>, throwable: Throwable) {
                        close(throwable)
                    }
                })
            }
            awaitClose {
                call.cancel()
            }
        }
    }

    private fun errorMsg(response: Response<R>): String? {
        val msg = response.errorBody()?.string()
        return if (msg.isNullOrEmpty()) {
            response.message()
        } else {
            msg
        }
    }

}