package com.zg.los.zego;

import com.zg.los.entity.RoomInfo;
import com.zg.los.entity.SpeechState;

import java.util.ArrayList;

import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public interface IMyServerListener {
    void onBefore();

    void onError(String msg);


    void onToken(String token);


    void onRoomUserCount(int userCount);


    void onRoomInfo(RoomInfo info);

    void onMySoundLevelUpdate(float level);

    void onRemoteSoundLevelUpdate(String steamId, float level);

    void onAddUser(ArrayList<ZegoUser> userList);

    void onDelUser(ArrayList<ZegoUser> userList);

    void onAddStream(ArrayList<ZegoStream> streamList);

    void onDelStream(ArrayList<ZegoStream> streamList);

    void onChangeSpeechState(SpeechState isSpeeching);

    void onConnected();

    void onDisconnected();

    void onCloseLoading();

    void onShowLoading(String msg);

    void onShowToast(String msg);
}
