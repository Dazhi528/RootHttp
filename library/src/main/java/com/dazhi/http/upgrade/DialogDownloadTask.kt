package com.dazhi.http.upgrade

import com.dazhi.http.temp.DownloadListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * 功能：下载任务
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-4 下午5:16
 */
@Suppress("unused")
class DialogDownloadTask(private val mDownloadListener: DownloadListener) {
    companion object {
        const val DOWNLOAD_FILE = "download.apk"
    }

    // 当前状态
    private val STATE_SUCCESS = 0
    private val STATE_PROGRESS = 1
    private val STATE_PAUSED = 2
    private val STATE_FAILED = 3
    private val STATE_CANCELED = 4

    //
    private val mainScope = MainScope()
    private var boPaused = false
    private var boCanceled = false
    private var inProgress = 0


    fun execute(boReLoad: Boolean, url: String, saveDir: String) {
        val file = File("$saveDir/$DOWNLOAD_FILE")
        if (boReLoad && file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
            }
        }
        // 运行协程
        var size: Long = 0
        mainScope.launch {
            // 获得下载文件总大小
            IApkUpgradeImpl.download(url, 0)
                    .flowOn(Dispatchers.IO)
                    .onEach {
                        if (it.isSuccessful) {
                            size = it.body()?.contentLength() ?: 0
                        }
                    }.collect()
            // 初始化已下载文件长度
            var dnLen = 0L
            if (file.exists()) {
                dnLen = file.length()
            }
            // 判断下载进度
            if (size == 0L || dnLen >= size) {
                return@launch
            }
            IApkUpgradeImpl.download(url, dnLen).flowOn(Dispatchers.IO)
                    .transform {
                        val ins: InputStream? = it.body()?.byteStream()
                        if (ins == null) {
                            emit(STATE_FAILED)
                            return@transform
                        }
                        var saveFile: RandomAccessFile? = null
                        saveFile = RandomAccessFile(file, "rw")
                        saveFile.seek(dnLen) // 跳过已下载的字节
                        val tempBtArr = ByteArray(1024)
                        var tempLen: Int
                        var tempSum = 0
                        while (ins.read(tempBtArr).also { tempLen = it } != -1) {
                            when {
                                boPaused -> {
                                    emit(STATE_PAUSED)
                                    return@transform
                                }
                                boCanceled -> {
                                    emit(STATE_CANCELED)
                                    return@transform
                                }
                                else -> {
                                    tempSum += tempLen
                                    saveFile.write(tempBtArr, 0, tempLen)
                                    inProgress = ((tempSum + dnLen) * 100 / size).toInt()
                                    emit(STATE_PROGRESS)
                                }
                            }
                        }
                        emit(STATE_SUCCESS)
                    }.onEach {
                        when (it) {
                            STATE_PROGRESS -> mDownloadListener.onProgress(inProgress)
                            STATE_PAUSED -> mDownloadListener.onPaused()
                            STATE_SUCCESS -> {
                                mainScope.cancel() // 销毁协程
                                mDownloadListener.onSuccess()
                            }
                            STATE_FAILED -> {
                                mainScope.cancel() // 销毁协程
                                mDownloadListener.onFailed()
                            }
                            STATE_CANCELED -> {
                                mainScope.cancel() // 销毁协程
                                mDownloadListener.onCanceled()
                            }
                        }
                    }.catch {

                    }
                    .collect()
        }
    }

    fun pause() {
        boPaused = true
    }

    fun cancel() {
        boCanceled = true
    }

}