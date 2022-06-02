package com.zg.los.entity;


import com.zg.los.utils.Utils;
import com.zg.los.zego.Zego;

import java.util.Date;
import java.util.Random;

public class UserInfo extends BaseEntity {
    public String uid;
    public String name;
    public boolean isMuted;

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || uid == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return uid.equals(userInfo.uid);
    }

    @Override
    public int hashCode() {
        if (uid == null)
            return super.hashCode();
        return uid.hashCode();

    }

    public static UserInfo random() {
        UserInfo user = new UserInfo();
        Random random = new Random();
        random.nextInt();
        user.uid = new Date().getTime() + "" + random.nextInt();
        user.name = Utils.randomName(Math.random() > 0.5);
        user.isMuted = false;
        return user;

    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
