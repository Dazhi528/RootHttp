package com.dazhi.http.upgrade

import android.util.Log
import com.dazhi.http.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.BufferedSink
import okio.Okio
import okio.Source
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
@Suppress("unused", "BlockingMethodInNonBlockingContext")
internal class DialogDownloadTask(val call: Call) {
    private var mJob: Job? = null
    private var boCanceled = false

    companion object {
        const val DOWNLOAD_FILE = "download.apk"
    }

    interface Call {
        fun progress(progress: Long, fileSize: Long)
        fun succ()
        fun fail()
    }
    
    fun cancel() {
        boCanceled=true
        mJob?.cancel()
        mJob = null
    }

    private fun log(e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("DialogDownloadTask", "msg：${e.message}；cause：${e.cause}")
        }
    }

    fun execute(boReLoad: Boolean, url: String, saveDir: String) {
        if(mJob!=null) {
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
        mJob = GlobalScope.launch {
            var size: Long = 0
            // 获得下载文件总大小
            IApkUpgradeImpl.download(url, 0)
                    .flowOn(Dispatchers.IO)
                    .take(1)
                    .onEach {
                        size = it.contentLength()
                    }.catch {
                        log(it)
                    }
                    .collect()
            // 初始化已下载文件长度
            var dnLen = 0L
            if (file.exists()) {
                dnLen = file.length()
            }
            // 判断下载进度
            if (size == 0L || dnLen >= size) {
                call.fail()
                return@launch
            }
            // 真正的下载逻辑
            var mSource: Source? = null
            var mBufferedSink: BufferedSink? = null
            IApkUpgradeImpl.download(url, dnLen)
                    .onEach {
                        // 构建输入
                        mSource = Okio.source(it.byteStream())
                        if (mSource == null) {
                            call.fail()
                            return@onEach
                        }
                        // 构建输出
                        val saveFile = RandomAccessFile(file, "rw")
                        saveFile.seek(dnLen) // 跳过已下载的字节
                        val mSink = Okio.sink(FileOutputStream(saveFile.fd))
                        mBufferedSink = Okio.buffer(mSink)
                        if (mBufferedSink == null) {
                            call.fail()
                            return@onEach
                        }
                        // 循环读出并写入到文件
                        var tempLen: Long
                        var tempSum = 0L
                        while (mSource!!.read(mBufferedSink!!.buffer(),
                                        DEFAULT_BUFFER_SIZE.toLong()).apply {
                                    tempLen = this
                                } != -1L) {
                            if (boCanceled) {
                                call.fail()
                                return@onEach
                            }
                            mBufferedSink?.emit()
                            tempSum += tempLen
                            withContext(Dispatchers.Main) {
                                call.progress(tempSum+dnLen, size)
                            }
                        }
                        mSource?.close()
                        mBufferedSink?.close()
                        cancel()
                        call.succ()
                    }.catch {
                        try {
                            log(it)
                            mSource?.close()
                            mBufferedSink?.close()
                        } catch (e: Exception) {
                        }
                    }
                    .flowOn(Dispatchers.IO)
                    .launchIn(this)
        }
    }

}