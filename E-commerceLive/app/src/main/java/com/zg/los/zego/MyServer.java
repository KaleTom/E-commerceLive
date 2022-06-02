package com.zg.los.zego;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.zg.los.entity.Msg;
import com.zg.los.entity.MsgType;
import com.zg.los.entity.RoomInfo;
import com.zg.los.entity.SpeechState;
import com.zg.los.entity.TokenEntity;
import com.zg.los.utils.HttpsRequest;
//import com.zg.los.utils.TokenServerAssistant;
import com.zg.los.utils.TokenUtils;
import com.zg.los.utils.Utils;


import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoStreamEvent;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

/**
 * 用于模拟服务器，这里将一些权限控制代码和Zego SDK的监听事件结合在一起
 * 实际线上模型应当将权限控制代码放入到后台运行
 */
public class MyServer extends IZegoEventHandler {
    private static final String TAG = "MyServer";
    private static final long MAX_VALID_SECOND = 60 * 60;
    private IMyServerListener mHandler = null;
    private static MyServer mInstance = null;
    private RoomInfo mRoomInfo;
    private boolean mIsMaster;
    private Zego mZego;


    private MyServer() {
    }


    public static MyServer getInstance() {
        if (null == mInstance) {
            synchronized (MyServer.class) {
                if (null == mInstance) {
                    mInstance = new MyServer();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化当前登录用户信息和房间信息，如果有个人服务器，应当自行从数据库查询
     */
    public void initCurUserInfo(boolean isMaster, RoomInfo roomInfo, Zego zego) {
        mIsMaster = isMaster;
        mRoomInfo = roomInfo;
        mZego = zego;
    }

    public void updateRoomInfo(RoomInfo roomInfo) {
        mRoomInfo = roomInfo;
    }

    public void setServerListener(IMyServerListener listener) {
        mHandler = listener;
    }


    public void getToken(String userId, String roomId) {
        TokenEntity tokenEntity = new TokenEntity(KeyCenter.APPID, userId, roomId, 60 * 60, 1, 1);

        String token = TokenUtils.generateToken04(tokenEntity);
        if (token != null) {
            cbToken(token);
            String decrypt = TokenUtils.decryptToken(token);
            Log.e(TAG, decrypt);
        } else {
            cbError("获取Token失败");
        }
    }

    @Override
    public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
        super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
        if (mHandler == null) return;
        mHandler.onCloseLoading();//不管有没有loading，先close
        if (reason == ZegoRoomStateChangedReason.LOGINING) {
            // 登录中
        } else if (reason == ZegoRoomStateChangedReason.LOGINED) {
            // 登录成功
            //只有当房间状态是登录成功或重连成功时，推流（startPublishingStream）、拉流（startPlayingStream）才能正常收发音视频
            //将自己的音视频流推送到 ZEGO 音视频云
        } else if (reason == ZegoRoomStateChangedReason.LOGIN_FAILED) {
            // 登录失败
        } else if (reason == ZegoRoomStateChangedReason.RECONNECTING) {
            mHandler.onShowLoading("正在重连...");
            return;
            // 重连中
        } else if (reason == ZegoRoomStateChangedReason.RECONNECTED) {
            mHandler.onShowToast("重连成功！");
            // 重连成功
        } else if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
            // 重连失败
        } else if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
            // 被踢出房间
        } else if (reason == ZegoRoomStateChangedReason.LOGOUT) {
            // 登出成功
        } else if (reason == ZegoRoomStateChangedReason.LOGOUT_FAILED) {
            // 登出失败
        }
    }



//    @Override
//    public void onRoomStreamUpdate(String s, ZegoUpdateType zegoUpdateType, ArrayList<ZegoStream> arrayList) {
//        super.onRoomStreamUpdate(s, zegoUpdateType, arrayList);
//        for (ZegoStream stream : arrayList) {
//
//        }
//        Log.e("onpush stream", s + "<><>" + s1);
//    }

    @Override
    public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, org.json.JSONObject extendedData) {
        super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
        Log.e(TAG, "收到登录状态回调..." + errorCode);
        if (mHandler == null) return;

        if (state == ZegoRoomState.CONNECTED) {
            //房间连接成功
            mHandler.onConnected();
            Log.e(TAG, "CONNECTED");
        } else if (state == ZegoRoomState.CONNECTING) {
            //房间连接中
            Log.e(TAG, "CONNECTING");
        } else if (state == ZegoRoomState.DISCONNECTED) {
            //未连接或房间连接断开
            mHandler.onDisconnected();
            Log.e(TAG, "DISCONNECTED");
        }
    }


    /**
     * 请求获取指定房间用户数量
     */
    public void reqRoomUserCount(Activity ctx, String roomId) {
        String url = "https://rtc-api.zego.im/?Action=DescribeUserNum&RoomId[]=" + roomId +
                "&" + getPublicArgs();
        HttpsRequest.getJSON(ctx, url, new HttpsRequest.OnJSONCallback() {
            @Override
            public void onJSON(JSONObject json) {
                int code = Utils.getInt(json, "Code", -1);
                String msg = Utils.getStr(json, "Message", "后台访问异常");
                Log.e(TAG, json.toString());
                if (code == 0) {
                    int userCount = Utils.getInt(json, "Data/UserCountList[RoomId=" + roomId + "]/UserCount", -1);
                    Log.e(TAG, "userCount=" + userCount);
                    if (userCount >= 0) {
                        cbRoomUserCount(userCount);
                    } else {
                        cbError("后台访问异常");
                    }
                } else {
                    cbError(msg);
                }
            }

            @Override
            public void onError(Exception e) {
                cbError("网络异常");
            }
        });
    }

    @Override
    public void onCapturedSoundLevelUpdate(float soundLevel) {

        if (mHandler != null) {
            mHandler.onMySoundLevelUpdate(soundLevel);
        }
    }

    @Override
    public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        if (mHandler != null) {
            for (String streamId : soundLevels.keySet()) {
                float level = soundLevels.get(streamId);
                mHandler.onRemoteSoundLevelUpdate(streamId, level);
            }
        }
    }

    private void cbError(String msg) {
        if (mHandler != null) {
            mHandler.onBefore();
            mHandler.onError(msg);
        }
    }

    private void cbToken(String token) {
        if (mHandler != null) {
            mHandler.onBefore();
            mHandler.onToken(token);
        }
    }

    private void cbRoomUserCount(int userCount) {
        if (mHandler != null) {
            mHandler.onBefore();
            mHandler.onRoomUserCount(userCount);
        }
    }

    public void broadcastRoomInfo(ArrayList<ZegoUser> userList) {
        if (!mIsMaster || mRoomInfo == null) return;
        if (userList != null)
            mZego.sendMsg(mRoomInfo.roomId, userList, mRoomInfo);
        else
            mZego.broadcastMsg(mRoomInfo.roomId, mRoomInfo);
    }

    @Override
    public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
        super.onRoomUserUpdate(roomID, updateType, userList);
        if (mHandler == null) return;
        if (updateType == ZegoUpdateType.ADD) {
            broadcastRoomInfo(userList);
            mHandler.onBefore();
            mHandler.onAddUser(userList);


        } else if (updateType == ZegoUpdateType.DELETE) {
            mHandler.onBefore();
            if (!mIsMaster && mRoomInfo != null) {
                for (ZegoUser user : userList) {
                    if (user.userID.equals(mRoomInfo.masterId)) {
                        mRoomInfo.isLogout = true;
                        mHandler.onRoomInfo(mRoomInfo);
                        break;
                    }
                }
            }
            mHandler.onDelUser(userList);

        }

    }

//    @Override
//    public void onPlayerStreamEvent(ZegoStreamEvent eventID, String streamID, String extraInfo) {
//        super.onPlayerStreamEvent(eventID, streamID, extraInfo);
//        Log.e(TAG, "onPlayerStreamEvent:" + eventID);
//        if (mHandler == null) return;
//        mHandler.onCloseLoading();
//        switch (eventID) {
//            case RETRY_PLAY_START: {
//                mHandler.onShowLoading("正在重试拉流...");
//                break;
//            }
//            case RETRY_PLAY_SUCCESS: {
//                mHandler.onShowToast("重试拉流成功！");
//                break;
//
//            }
//        }
//
//    }

//    @Override
//    public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
//        super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
//        if (mHandler == null) return;
//        mHandler.onCloseLoading();
//        switch (state) {
//            case PLAY_REQUESTING: {
//                mHandler.onShowLoading("正则连接...");
//            }
//
//        }
//
//    }

    // 房间内其他用户推流/停止推流时，我们会在这里收到相应流增减的通知
    @Override
    public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
        super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
        if (mHandler == null) return;

        //当 updateType 为 ZegoUpdateType.ADD 时，代表有音视频流新增，此时我们可以调用 startPlayingStream 接口拉取播放该音视频流
        if (updateType == ZegoUpdateType.ADD) {
            mHandler.onBefore();
            mHandler.onAddStream(streamList);
        } else if (updateType == ZegoUpdateType.DELETE) {

            mHandler.onBefore();
            mHandler.onDelStream(streamList);
        }
    }

    @Override
    public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
        // 收到其他用户发送消息的处理
        if (mHandler == null) return;
        try {
            mHandler.onBefore();
            Msg msg = Msg.parseMsg(fromUser.userID, command);
            if (msg.msgType == MsgType.MSG_CHG_SPEECH_STATE) {
                mHandler.onChangeSpeechState((SpeechState) msg);
            } else if (msg.msgType == MsgType.MSG_CMD_ROOM_INFO) {
                mHandler.onRoomInfo((RoomInfo) msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收房间广播消息通知
     */
    @Override
    public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
        // 收到其他用户发送消息的处理
        if (mHandler == null) return;
        mHandler.onBefore();
        for (ZegoBroadcastMessageInfo info : messageList) {
            Msg msg = Msg.parseMsg(info.fromUser.userID, info.message);
            if (msg.msgType == MsgType.MSG_CHG_SPEECH_STATE) {
                mHandler.onChangeSpeechState((SpeechState) msg);
            } else if (msg.msgType == MsgType.MSG_CMD_ROOM_INFO) {
                mHandler.onRoomInfo((RoomInfo) msg);
            }
        }
    }


    public static String getPublicArgs() {

        //生成16进制随机字符串(16位)
        byte[] bytes = new byte[8];
        //使用SecureRandom获取高强度安全随机数生成器
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);
        String signatureNonce = bytesToHex(bytes);
        long timestamp = System.currentTimeMillis() / 1000L;
        String signature = GenerateSignature(KeyCenter.APPID, signatureNonce, KeyCenter.SERVER_SECRET, timestamp);
        return "AppId=" + KeyCenter.APPID +
                "&SignatureNonce=" + signatureNonce +
                "&Timestamp=" + timestamp +
                "&Signature=" + signature + "&SignatureVersion=2.0&IsTest=false";
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];
            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString();
    }


    // Signature=md5(AppId + SignatureNonce + ServerSecret + Timestamp)
    public static String GenerateSignature(long appId, String signatureNonce, String serverSecret, long timestamp) {
        String str = String.valueOf(appId) + signatureNonce + serverSecret + String.valueOf(timestamp);
        String signature = "";
        try {
            //创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            //计算后获得字节数组
            byte[] bytes = md.digest(str.getBytes("utf-8"));
            //把数组每一字节换成16进制连成md5字符串
            signature = bytesToHex(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

}
