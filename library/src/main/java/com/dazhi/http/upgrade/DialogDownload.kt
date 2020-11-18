package com.dazhi.http.upgrade

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.dazhi.http.R
import kotlinx.android.synthetic.main.dialog_download.*

/**
 * 功能：下载Apk对话框
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-14 上午10:30
 */
class DialogDownload(context: Context?, private val intProgress: Long=0, private val intApkSize: Long=0,
                     private val mCancelCallback: (() -> Unit)? = null) : AppCompatDialog(context) {
    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        setContentView(R.layout.dialog_download)
        initView()
    }


    //字节数(byte);   当前下载字节数;  apk总字节数
    @SuppressLint("CheckResult")
    private fun initView() {
        if (tvDownloadPercent != null) {
            tvDownloadPercent!!.text = getPercent(intProgress, intApkSize)
        }
        // 关闭按钮
        if(mCancelCallback==null) {
            btDownloadEsc.visibility = View.GONE
        }else {
            btDownloadEsc.visibility = View.VISIBLE
            btDownloadEsc.setOnClickListener {
                dismiss()
                mCancelCallback.invoke()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getPercent(intProgress: Long, intApkSize: Long): String {
        if (intProgress < 0 || intApkSize < 0) {
            pbDownloadProgress!!.progress = 0
            pbDownloadProgress!!.max = 0
            return "0/0 M"
        }
        //byte to MB
        val douProgress = intProgress.toDouble() / (1024 * 1024)
        val douMaxSize = intApkSize.toDouble() / (1024 * 1024)
        pbDownloadProgress!!.progress = douProgress.toInt()
        pbDownloadProgress!!.max = douMaxSize.toInt()
        return String.format("%.2f/%.2f M", douProgress, douMaxSize)
    }

    fun updateData(intProgress: Long, intApkSize: Long) {
        tvDownloadPercent!!.text = getPercent(intProgress, intApkSize)
    }

}