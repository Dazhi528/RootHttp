package com.dazhi.http.upgrade

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.dazhi.http.R

/**
 * 功能：Apk需要升级提示对话框
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-14 上午9:50
 */
class DialogUpgradeHint(context:Context, boForce: Boolean, describe: String = "",
                        onClickEsc:DialogInterface.OnClickListener? = null,
                        onClickEnt:DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }) {
    private val alertDialog: AlertDialog

    init {
        var strMsge=context.getString(R.string.apkupgrade_able)+describe
        val btEntText=context.getString(R.string.apkupgrade_ent)
        var btEscText=context.getString(R.string.apkupgrade_able_esc)
        if(boForce){
            strMsge=context.getString(R.string.apkupgrade_force)+describe
            btEscText=context.getString(R.string.apkupgrade_force_esc)
        }
        //
        alertDialog=AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.apkupgrade_title))
                .setMessage(strMsge)
                .setPositiveButton(btEntText, onClickEnt)
                .setNegativeButton(btEscText, onClickEsc)
                .setCancelable(false)
                .create()
    }

    fun show(){
        alertDialog.show()
    }

}