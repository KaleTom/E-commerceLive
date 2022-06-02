package com.zg.los.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GoodsEntity extends BaseEntity implements Parcelable {
    public String goodsId;
    public String goodsName;
    public float price;
    public int num;
    public int imgId;

    public GoodsEntity() {

    }

    public GoodsEntity(String goodsId, String name, float price, int imgId) {
        this.goodsId = goodsId;
        this.goodsName = name;
        this.price = price;
        this.imgId = imgId;
    }

    private GoodsEntity(Parcel in) {
        goodsId = in.readString();
        goodsName = in.readString();
        price = in.readFloat();
        num = in.readInt();
        imgId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(goodsId);
        parcel.writeString(goodsName);
        parcel.writeFloat(price);
        parcel.writeInt(num);
        parcel.writeInt(imgId);
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<GoodsEntity> CREATOR
            = new Parcelable.Creator<GoodsEntity>() {
        public GoodsEntity createFromParcel(Parcel in) {
            return new GoodsEntity(in);
        }

        public GoodsEntity[] newArray(int size) {
            return new GoodsEntity[size];
        }
    };

}
