package com.zg.los.entity;

/**
 * 连麦消息
 */
public class SpeechState extends Msg {
    public boolean isMuted;

    public SpeechState(int msgType, String fromUserId, boolean isMuted) {
        super(msgType, fromUserId);
        this.isMuted = isMuted;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }
}
