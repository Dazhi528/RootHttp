package com.dazhi.http.upgrade

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.dazhi.http.R
import kotlinx.android.synthetic.main.dialog_download.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 功能：下载Apk对话框
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-14 上午10:30
 */
class DialogDownload(context: Context?, private val boReLoad: Boolean,
                     private val url: String, private val saveDir: String,
                     private val callback: (boOk: Boolean) -> Unit) : AppCompatDialog(context) {
    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    private val mDialogDownloadTask = DialogDownloadTask(object: DialogDownloadTask.Call {
        override suspend fun progress(progress: Long, fileSize: Long) {
            withContext(Dispatchers.Main) {
                updateData(progress, fileSize)
            }
        }
        override fun succ() {
            callback(true)
            dismiss()
        }
        override fun fail() {
            callback(false)
            dismiss()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        setContentView(R.layout.dialog_download)
        initView()
    }

    override fun show() {
        super.show()
        mDialogDownloadTask.execute(boReLoad, url, saveDir)
    }

    //字节数(byte);   当前下载字节数;  apk总字节数
    @SuppressLint("CheckResult")
    private fun initView() {
        // 关闭按钮
        btDownloadEsc.setOnClickListener {
            dismiss()
            mDialogDownloadTask.cancel()
            callback(false)
        }
    }

    private fun getPercent(progress: Long, fileSize: Long): String {
        if (progress < 0 || fileSize <= 0) {
            pbDownloadProgress!!.progress = 0
            pbDownloadProgress!!.max = 0
            return "0/0 M"
        }
        //byte to MB
        val douProgress = progress.toDouble() / (1024 * 1024)
        val douMaxSize = fileSize.toDouble() / (1024 * 1024)
        pbDownloadProgress!!.progress = (progress*100/fileSize).toInt()
        pbDownloadProgress!!.max = 100
        return String.format("%.2f/%.2f M", douProgress, douMaxSize)
    }

    private fun updateData(progress: Long, fileSize: Long) {
        tvDownloadPercent!!.text = getPercent(progress, fileSize)
    }

}