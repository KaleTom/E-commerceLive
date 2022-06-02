package com.zg.los.utils;

import android.content.Context;
import android.util.Log;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

public class ShowUtils {
    public interface OnClickOkListener {
        void onOk();

        void onCancel();
    }

    public static void alert(Context ctx, String title, String msg) {
        AlertView alert = new AlertView(title, msg, "确定",
                null, null, ctx, AlertView.Style.Alert, null);
        alert.show();
    }

    public static void comfirm(Context ctx, String title, String msg, String ok, OnClickOkListener clickListener) {
        AlertView alert = new AlertView(title, msg, "取消",
                new String[]{ok}, null, ctx, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (clickListener == null) return;
                if (position == 0) {
                    clickListener.onOk();
                } else {//position = -1
                    clickListener.onCancel();
                }
            }
        });
        alert.show();
    }
}
