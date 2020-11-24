package com.dazhi.http.upgrade

import android.util.Log
import com.dazhi.http.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

/**
 * 功能：下载任务
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-4 下午5:16
 */
@Suppress("unused")
internal class DialogDownloadTask(private val call: Call) {
    private var mJob: Job? = null
    private var boCanceled = false

    companion object {
        const val DOWNLOAD_FILE = "download.apk"
    }

    interface Call {
        suspend fun progress(progress: Long, fileSize: Long)
        fun succ()
        fun fail()
    }

    fun cancel() {
        boCanceled = true
        mJob?.cancel()
        mJob = null
    }

    private fun log(e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("DialogDownloadTask", "msg：${e.message}；cause：${e.cause}")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun execute(boReLoad: Boolean, url: String, saveDir: String) {
        if (mJob != null) {
            return
        }
        // 指定下载文件
        val file = File("$saveDir/$DOWNLOAD_FILE")
        if (boReLoad && file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
            }
        }
        // 运行协程
        mJob = GlobalScope.launch(Dispatchers.IO) {
            // 获得下载文件总大小
            val size = IApkUpgradeImpl.download(url, 0)
                    .first().contentLength()
            // 初始化已下载文件长度
            var dnLen = 0L
            if (file.exists()) {
                dnLen = file.length()
            }
            // 判断下载进度
            if (size <= 0L || dnLen >= size) {
                if (size > 0 && dnLen == size) { // 本地已下载好的情况
                    call.progress(dnLen, size)
                    call.succ()
                } else {
                    call.fail() // 异常情况
                }
                cancel()
                return@launch
            }
            // 真正的下载逻辑
            var mSource: Source? = null
            var mBufferedSink: BufferedSink? = null
            IApkUpgradeImpl.download(url, dnLen)
                    .onEach {
                        // 构建输入
                        mSource = it.byteStream().source()
                        if (mSource == null) {
                            call.fail()
                            cancel()
                            return@onEach
                        }
                        // 构建输出
                        val saveFile = RandomAccessFile(file, "rw")
                        saveFile.seek(dnLen) // 跳过已下载的字节
                        mBufferedSink = FileOutputStream(saveFile.fd).sink().buffer()
                        if (mBufferedSink == null) {
                            call.fail()
                            cancel()
                            return@onEach
                        }
                        // 循环读出并写入到文件
                        var tempLen: Long
                        var tempSum = 0L
                        while (mSource!!.read(mBufferedSink!!.buffer,
                                        DEFAULT_BUFFER_SIZE.toLong()).apply {
                                    tempLen = this
                                } != -1L) {
                            if (boCanceled) {
                                call.fail()
                                cancel()
                                return@onEach
                            }
                            mBufferedSink?.emit()
                            tempSum += tempLen
                            call.progress(tempSum + dnLen, size)
                        }
                        mSource?.close()
                        mBufferedSink?.close()
                        call.succ()
                        cancel()
                    }.catch {
                        try {
                            mSource?.close()
                        } catch (e: Exception) {
                        }
                        try {
                            mBufferedSink?.close()
                        } catch (e: Exception) {
                        }
                        try {
                            log(it)
                            cancel()
                        } catch (e: Exception) {
                        }
                    }
                    .launchIn(this)
        }
    }

}