package com.zg.los.activity;


import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.codingending.popuplayout.PopupDialog;
import com.codingending.popuplayout.PopupLayout;
import com.gyf.immersionbar.ImmersionBar;
import com.zg.los.R;
import com.zg.los.utils.StatusBarTextColorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;


public class BaseActivity extends AppCompatActivity {
    private LoadingDailog mLoading = null;

    private static String[] permissionNeeded = {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};
    private final static int MY_PERMISSION_REQUEST_CODE = 10001;
    public final static int MY_ADD_GOODS_RESULT_CODE = 10002;

    public static final int MY_REQUEST_CODE_PICK_IMG = 10003;
    public static final int MY_REQUEST_CODE_CROP_IMG = 10004;

    private void initStatusBarAndNavBar() {
        ImmersionBar.with(this)
                .transparentStatusBar()  //状态栏
                .transparentNavigationBar()  //透明导航栏
                .init();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBarAndNavBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 必须调用该方法，防止内存泄漏
//        ImmersionBar.destroy
//        ImmersionBar.with(this).;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 如果你的app可以横竖屏切换，并且适配4.4或者emui3手机请务必在onConfigurationChanged方法里添加这句话
        initStatusBarAndNavBar();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        translucent();
        super.setContentView(layoutResID);
//        StatusBarUtil.setTransparent(this);
    }


    public void hideLoading() {
        if (mLoading != null) {
            synchronized (BaseActivity.class) {
                if (mLoading != null) {
                    mLoading.cancel();
                    mLoading = null;
                }
            }
        }
    }

    public void showLoading(String msg) {
        hideLoading();
        LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(this)
                .setMessage(msg)
                .setCancelable(true)
                .setCancelOutside(false);
        mLoading = loadBuilder.create();
        mLoading.show();
    }

    protected PopupLayout newPopup(View popupRootView) {
        PopupLayout popupLayout = PopupLayout.init(this, popupRootView);
        try {

//            Constructor cons = PopupLayout.class.getDeclaredConstructor();
//            cons.setAccessible(true);
//            popupLayout = (PopupLayout) cons.newInstance();

//            PopupDialog dialog = new PopupDialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(popupRootView);

            Field dialogField = PopupLayout.class.getDeclaredField("mPopupDialog");
            dialogField.setAccessible(true);
            PopupDialog dialog = (PopupDialog) dialogField.get(popupLayout);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

//            Method initListener = PopupLayout.class.getDeclaredMethod("initListener");
//            initListener.setAccessible(true);
//            initListener.invoke(popupLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return popupLayout;
    }

    public void toast(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        View view = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
        TextView tv_msg = (TextView) view.findViewById(R.id.toast_text);
        tv_msg.setText(msg);
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 20);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public boolean checkHasNavigationBar() {
        WindowManager windowManager = getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    protected int getNavHeight() {
        int result = 0;
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && checkHasNavigationBar()) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void translucent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 实现透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); //布局延伸
    }

    protected boolean checkPermission() {
        for (String permission : permissionNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    protected void requestPermission() {
        ActivityCompat.requestPermissions(this, permissionNeeded, MY_PERMISSION_REQUEST_CODE);

    }

    protected void onGrantedAllPermission() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                boolean isAllGranted = true;
                // 判断是否所有的权限都已经授予了
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                if (isAllGranted) {
                    onGrantedAllPermission();

                } else {
                    // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                    toast("请先允许音视频权限");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    /*
     * 设置状态栏颜色
     * */


    /**
     * 修改状态栏颜色，支持4.4以上版本
     */
    public void setStatusTextColor(boolean isDarkText) {

        StatusBarTextColorUtils.setStatusTextColorMode(getWindow(), isDarkText);

    }


    public boolean setMiuiStatusBarDarkMode(boolean darkmode) {
        Class<? extends Window> clazz = getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setMeizuStatusBarDarkIcon(boolean dark) {
        boolean result = false;

        try {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            getWindow().setAttributes(lp);
            result = true;
        } catch (Exception e) {
        }

        return result;
    }
}