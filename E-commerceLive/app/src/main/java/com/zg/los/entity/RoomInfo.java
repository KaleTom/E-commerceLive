package com.zg.los.entity;

import java.util.ArrayList;


public class RoomInfo extends Msg {
    public String roomName;
    public String roomId;
    public String masterId;
    public boolean isMuted;
    public boolean isLogout;
    public ArrayList<GoodsEntity> goodsEntities;


    public RoomInfo(int msgType, String fromUserId, String roomId, String roomName,
                    String masterId, ArrayList<GoodsEntity> goodsEntities,
                    boolean isMuted, boolean isLogout) {
        super(msgType, fromUserId);
        this.roomId = roomId;
        this.roomName = roomName;
        this.masterId = masterId;
        this.goodsEntities = goodsEntities;
        this.isMuted = isMuted;
        this.isLogout = isLogout;

    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }


}
