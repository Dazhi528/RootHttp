package com.dazhi.http.temp

/**
 * 功能：下载状态监听
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-4 下午5:11
 */
interface DownloadListener {
    fun onProgress(progress: Int)
    fun onSuccess()
    fun onFailed()
    fun onPaused()
    fun onCanceled()
}