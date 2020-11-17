package com.dazhi.sample;

import android.widget.TextView;
import com.dazhi.libroot.root.RootSimpActivity;

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-9 下午6:36
 */
public class MainActivity extends RootSimpActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initConfig(TextView tvToolTitle) {
        tvToolTitle.setText("视图库");
    }

    @Override
    protected void initViewAndDataAndEvent() {

    }

}
