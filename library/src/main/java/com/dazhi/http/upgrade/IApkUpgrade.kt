package com.dazhi.http.upgrade

import com.dazhi.http.retrofit.createApi
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 功能：Apk 升级版本接口定义
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-14 上午9:05
 */
interface IApkUpgrade {
    @GET
    @Streaming
    @Headers("range: bytes={loadedLen}-") // mapOf("RANGE" to "bytes=${dnLen}-")
    fun download(@Url url: String, @Path("loadedLen")loadedLen: Long): Flow<Response<ResponseBody>>
}

/**
 * 作者：WangZezhi  (20-11-17  下午3:15)
 * 功能：库请求构建实例
 * 描述：实际开发中尽量结合自己的业务逻辑请求去构建一个，因为createApi()函数最后项目中调用一次
 */
object IApkUpgradeImpl {
    private val api by lazy {
        // 这里不使用默认的Sting转换工厂
        createApi<IApkUpgrade>(converterFactory = null)
    }

    // loadedLen 已下载的文件大小
    @JvmStatic
    fun download(url: String, loadedLen: Long=0): Flow<Response<ResponseBody>> {
        return api.download(url, loadedLen)
    }
}