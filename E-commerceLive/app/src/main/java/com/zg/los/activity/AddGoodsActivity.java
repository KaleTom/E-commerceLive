package com.zg.los.activity;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.codingending.popuplayout.PopupLayout;
import com.zg.los.R;
import com.zg.los.entity.GoodsEntity;
import com.zg.los.utils.Utils;


import androidx.appcompat.widget.Toolbar;

public class AddGoodsActivity extends BaseActivity implements View.OnClickListener, TextWatcher, OnClickImgListener {
    private static final String TAG = "AddGoodsActivity";
    private static final int CROPED_H = 20;
    private static final int CROPED_W = 50;
    private ImageView mGoodsImgView;
    private int curImgResId = -1;
    private View mEmptyGoodsImg;
    private EditText mPriceEt;
    private EditText mNameEt;
    private PopupLayout mPopup;
    private int[] mResIds = {R.mipmap.goods_1, R.mipmap.goods_2,
            R.mipmap.goods_3, R.mipmap.goods_4,
            R.mipmap.goods_5, R.mipmap.goods_6,
            R.mipmap.goods_7, R.mipmap.goods_8};
    /**
     * 选择得到的图片uri
     */
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goods);
//        StatusBarUtil.setTranslucentForImageView(this, 0, findViewById(R.id.toolbar));
        setStatusTextColor(true);
        mEmptyGoodsImg = findViewById(R.id.emptyGoodsImg);
        mGoodsImgView = findViewById(R.id.goodsImg);
        mGoodsImgView.setOnClickListener(this);
        findViewById(R.id.publishBtn).setOnClickListener(this);
        findViewById(R.id.addGoodsBtn).setOnClickListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //设置Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mNameEt = findViewById(R.id.goodsNameEt);
        mPriceEt = findViewById(R.id.goodsPriceEt);
        mPriceEt.addTextChangedListener(this);
        initPopup();
    }

    private void initPopup() {

        GridView root = new GridView(this);
        root.setPadding(12, 50, 12, 12);
        root.setNumColumns(2);
        root.setHorizontalSpacing(12);
        root.setVerticalSpacing(12);
        mPopup = newPopup(root);
        root.setAdapter(new ImgsPopupAdapter(this, mResIds, this));
    }

    @Override
    public void onClickImg(int resId) {
        mPopup.hide();
        curImgResId = resId;
        mGoodsImgView.setImageResource(resId);
        mEmptyGoodsImg.setVisibility(View.GONE);
        mGoodsImgView.setVisibility(View.VISIBLE);
    }

    private void submit() {
        String name = mNameEt.getText().toString().trim();
        String price = mPriceEt.getText().toString().trim();
        price = price.replaceAll("¥", "");
        if (name.length() <= 0) {
            toast("请输入商品名称");
            return;
        }
        if (price.length() <= 0) {
            toast("请输入商品价格");
            return;
        }
        if (curImgResId <= 0) {
            toast("请选择商品图片");
            return;
        }

        float p = Float.parseFloat(price);
        GoodsEntity goods = new GoodsEntity();
        goods.goodsName = name;
        goods.goodsId = "g_" + Utils.randInt(0, 10000);
        goods.price = p;
        goods.imgId = curImgResId;
        goods.num = 10;
        Intent intent = new Intent();
        intent.putExtra("goods", goods);
        setResult(MY_ADD_GOODS_RESULT_CODE, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.publishBtn:
                submit();
                break;
            case R.id.addGoodsBtn:
            case R.id.goodsImg:
                mPopup.show();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPopup.dismiss();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
        String text = s.toString();
        boolean hasFoundDot = false;
        boolean append = false;
        boolean change = false;

        for (int idx = 0; idx < text.length(); ++idx) {
            char c = text.charAt(idx);
            if (c == '.') {
                if (hasFoundDot) {
                    text = text.substring(0, idx);
                    append = true;
                    change = true;
                } else {
                    if (append) {
                        text += c;
                    }
                }
                hasFoundDot = true;
            }
        }
        if (!text.startsWith("¥")) {
            text.replaceAll("¥", "");
            text = "¥" + text;
            change = true;
        }
        if (change) {
            mPriceEt.setText(text);
            mPriceEt.setSelection(text.length());
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}

interface OnClickImgListener {
    void onClickImg(int resId);
}

class ImgsPopupAdapter extends BaseAdapter {
    private int[] images;
    private Context ctx;
    private OnClickImgListener listener;

    public ImgsPopupAdapter(Context context, int[] images, OnClickImgListener listener) {
        this.images = images;
        this.ctx = context;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return images[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv = new ImageView(ctx);
        iv.setImageResource(images[position]);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickImg((images[position]));
            }
        });
        return iv;
    }
}