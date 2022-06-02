package com.zg.los.activity;

import com.zg.los.entity.RoomInfo;
import com.zg.los.entity.SpeechState;
import com.zg.los.zego.IMyServerListener;
import com.zg.los.zego.Zego;

import java.util.ArrayList;

import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class BaseServerEventActivity extends BaseActivity implements IMyServerListener {

    protected boolean mIsMaster = false;
    protected Zego mZego = null;

    @Override
    public void onBefore() {

        //执行所有回调之前，一定会调用这个函数，用于清理loading窗口
        hideLoading();
    }

    @Override
    public void onShowToast(String msg) {
        toast(msg);
    }

    @Override
    public void onShowLoading(String msg) {
        showLoading(msg);
    }

    @Override
    public void onCloseLoading() {
        hideLoading();
    }

    @Override
    public void onError(String msg) {
        toast(msg);
    }

    @Override
    public void onToken(String token) {

    }

    @Override
    public void onRoomUserCount(int userCount) {

    }

    @Override
    public void onRoomInfo(RoomInfo info) {

    }

    @Override
    public void onChangeSpeechState(SpeechState isSpeeching) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMySoundLevelUpdate(float level) {

    }

    @Override
    public void onRemoteSoundLevelUpdate(String steamId, float level) {

    }

    @Override
    public void onAddUser(ArrayList<ZegoUser> userList) {

    }

    @Override
    public void onDelUser(ArrayList<ZegoUser> userList) {

    }

    @Override
    public void onAddStream(ArrayList<ZegoStream> streamList) {

    }

    @Override
    public void onDelStream(ArrayList<ZegoStream> streamList) {

    }
}
