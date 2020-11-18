package com.dazhi.sample

import android.os.Environment
import android.widget.TextView
import com.dazhi.http.temp.DownloadListener
import com.dazhi.http.upgrade.DialogDownload
import com.dazhi.http.upgrade.DialogDownloadTask
import com.dazhi.libroot.root.RootSimpActivity
import com.dazhi.libroot.util.RtCmn
import com.dazhi.libroot.util.RtLog
import com.dazhi.libroot.util.viewClick
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-9 下午6:36
 */
class MainActivity : RootSimpActivity() {

    override val layoutId: Int
        protected get() = R.layout.activity_main

    override fun initConfig(tvToolTitle: TextView?) {
        tvToolTitle!!.text = "视图库"
        RtLog.setOpen()
    }

    override fun initViewAndDataAndEvent() {
        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
        if (downloadDir == null || downloadDir.isEmpty()) {
            RtCmn.toastShort("存储目录路径获取失败")
            return
        }
        val mDialogDownloadTask = DialogDownloadTask(object : DownloadListener {
            override fun onProgress(progress: Int) {
                TODO("Not yet implemented")
            }

            override fun onSuccess() {
                TODO("Not yet implemented")
            }

            override fun onFailed() {
                TODO("Not yet implemented")
            }

            override fun onPaused() {
                TODO("Not yet implemented")
            }

            override fun onCanceled() {
                TODO("Not yet implemented")
            }
        })
        val mDialogDownload = DialogDownload(this)
        viewClick(btTest) {
            mDialogDownload.show()
            mDialogDownloadTask.execute(false,
                    "http://intelink.onecod.com:8800/File/Tpe/TpeApp-3-1.0.3R.apk",
                    downloadDir)
        }
    }
}