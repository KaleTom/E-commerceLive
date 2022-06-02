package com.zg.los.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;


import com.zg.los.R;


import androidx.annotation.Nullable;

public class ImgsGroupView extends GridView {
    private int mSlot;
    private int[] mResIds = {R.mipmap.goods_1, R.mipmap.goods_2,
            R.mipmap.goods_3, R.mipmap.goods_4,
            R.mipmap.goods_5, R.mipmap.goods_6,
            R.mipmap.goods_7, R.mipmap.goods_8};

    private void init(Context ctx, int slot) {

        setBackgroundColor(Color.rgb(50,50,50));
        mSlot = Utils.dip2px(ctx, slot);
        for (int id : mResIds) {
            ImageView iv = new ImageView(ctx);
            iv.setImageResource(id);
            addView(iv);
        }
    }


    public ImgsGroupView(Context context) {
        super(context);
        init(context, 6);
    }

    public ImgsGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, 6);
    }

    public ImgsGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, 6);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int wh = w < h ? w : h;
        int iwh = (wh - mSlot * 3) / 2;
        int wms = MeasureSpec.makeMeasureSpec(iwh, MeasureSpec.EXACTLY);
        int hms = MeasureSpec.makeMeasureSpec(iwh, MeasureSpec.EXACTLY);
        measureChildren(wms, hms);

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        int wh = w < h ? w : h;
        int iwh = (wh - mSlot * 3) / 2;
//        int iw =
        int count = getChildCount();

        int ox = 0, oy = 0;
        for (int i = 0; i < count; i++) {

            View child = getChildAt(i);
            oy = i / 2 * (iwh + mSlot);
            if (i % 2 == 0) {
                ox = mSlot * 2 + iwh;
            } else {
                ox = mSlot;
            }
            child.layout(ox, oy, ox + iwh, oy + iwh);
        }
    }


}
