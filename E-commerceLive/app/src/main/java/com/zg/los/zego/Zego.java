package com.zg.los.zego;

import android.app.Application;

import android.util.Log;
import android.view.TextureView;


import com.zg.los.entity.Msg;
import com.zg.los.entity.PreviewItem;
import com.zg.los.entity.UserInfo;
import com.zg.los.utils.Base64;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBroadcastMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
import im.zego.zegoexpress.constants.ZegoRoomMode;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

import static im.zego.zegoexpress.constants.ZegoViewMode.ASPECT_FILL;

public class Zego {
    private final static String TAG = "Zego";
    private static Zego mInstance = null;
    private ZegoExpressEngine mEngine = null;


    private Zego(Application app) {

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = KeyCenter.APPID;
        profile.scenario = ZegoScenario.GENERAL;  // 通用场景接入
        profile.application = app;
        mEngine = ZegoExpressEngine.createEngine(profile, null);
//        mEngine.useFrontCamera(false);
    }

    public void setHandler(IZegoEventHandler handler) {
        mEngine.setEventHandler(handler);
    }

    public void resetToken(String roomId, String token) {
        mEngine.renewToken(roomId, token);
    }

    public static Zego getInstance(Application app) {
        if (null == mInstance) {
            synchronized (Zego.class) {
                if (null == mInstance) {
                    mInstance = new Zego(app);
                }
            }
        }
        return mInstance;
    }

    public void setMute(boolean mute) {
        mEngine.muteMicrophone(mute);
    }

    public boolean loginRoom(String userId, String userName, String roomId, String token) {

        ZegoUser user = new ZegoUser(userId, userName);
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token; // 请求开发者服务端获取
        config.isUserStatusNotify = true;
        mEngine.loginRoom(roomId, user, config);
        Log.e(TAG, "登录房间：" + roomId);
        return true;
    }

    public void loginOut(String roomId) {
        mEngine.stopSoundLevelMonitor();
        mEngine.stopPublishingStream();
        mEngine.logoutRoom(roomId);
    }

    public void playPreview(PreviewItem pi, boolean isMyself) {
        ZegoCanvas canvas = new ZegoCanvas(pi.tv);
        canvas.viewMode = ASPECT_FILL;
        Log.e(TAG, "准备播放视频流" + isMyself);
        //不管有没有推流，先停止推流
        mEngine.stopPublishingStream();
        if (isMyself) {//本地预览

            mEngine.startPublishingStream(pi.streamId);
            mEngine.startPreview(canvas);
        } else {//拉取视频流
            //拉取远程视频流
            mEngine.startPlayingStream(pi.streamId, canvas);
        }
    }

    public void sendMsg(String roomId, ArrayList<ZegoUser> userList, Msg msg) {

        String msgPack = msg.toString();
        // 发送自定义信令，`toUserList` 中指定的用户才可以通过 onIMSendCustomCommandResult 收到此信令
        // 若 `toUserList` 参数传 `null` 则 SDK 将发送该信令给房间内所有用户
        mEngine.sendCustomCommand(roomId, msgPack, userList, new IZegoIMSendCustomCommandCallback() {
            /**
             * 发送用户自定义消息结果回调处理
             */
            @Override
            public void onIMSendCustomCommandResult(int errorCode) {
                //发送消息结果成功或失败的处理
                Log.e(TAG, "消息发送结束，回调：" + errorCode);
            }
        });

    }


    public void broadcastMsg(String roomId, Msg msg) {

        try {
            //限制QPS=2
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String msgPack = msg.toString();
        // 发送广播消息，每个登录房间的用户都会通过 onIMRecvBroadcastMessage 回调收到此消息【发送方不会收到该回调】
        mEngine.sendBroadcastMessage(roomId, msgPack, new IZegoIMSendBroadcastMessageCallback() {
            /** 发送广播消息结果回调处理 */
            @Override
            public void onIMSendBroadcastMessageResult(int errorCode, long messageID) { //发送消息结果成功或失败的处理
                Log.e(TAG, "广播消息发送结束，回调：" + errorCode);
                //发送消息结果成功或失败的处理
            }
        });

    }

}
