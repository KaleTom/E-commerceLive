package com.zg.los.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zg.los.R;
import com.zg.los.entity.GoodsEntity;
import com.zg.los.entity.PreviewItem;
import com.zg.los.entity.UserInfo;
import com.zg.los.utils.ShowUtils;
import com.zg.los.utils.StatusBarTextColorUtils;
import com.zg.los.utils.Utils;
import com.zg.los.zego.MyServer;

import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends BaseServerEventActivity implements View.OnClickListener, ActivityResultCallback {
    private static final String TAG = "MainActivity";
    private EditText mRoomIDEt;
    private EditText mRoomNameEt;
    private Button mCreateRoomBtn;
    private Button mJoinRoomBtn;
    private String mRoomName;
    private String mRoomId;
    private boolean isCreate;
    private boolean needClearGoods = true;
    private MyServer mMyserver;
    private ArrayList<GoodsEntity> mGoods;
    ActivityResultLauncher<Intent> mAddGoodslauncher;
    private UserInfo curUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setStatusTextColor(true);

        mRoomIDEt = findViewById(R.id.roomIdEt);
        mRoomNameEt = findViewById(R.id.roomNameEt);
        mCreateRoomBtn = findViewById(R.id.createRoomBtn);
        mJoinRoomBtn = findViewById(R.id.joinRoomBtn);
        ((TextView) findViewById(R.id.addText)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        findViewById(R.id.addGoodsBtn).setOnClickListener(this);
        mCreateRoomBtn.setOnClickListener(this);
        mJoinRoomBtn.setOnClickListener(this);

        mMyserver = MyServer.getInstance();

        mAddGoodslauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
    }

    private void initGoodsList() {
        String[] names = {
                "???????????????????????? ???????????????????????????????????????30ml??????",
                "??????????????????128ml??????????????????",
                "???????????????????????? ???????????????????????????????????????30ml??????",
                "???????????????????????? ???????????????????????????????????????30ml??????"
        };
        float[] prices = {969, 158, 299, 99};
        int[] imgResId = {R.mipmap.goods_1,
                R.mipmap.goods_2, R.mipmap.goods_3, R.mipmap.goods_4};
        mGoods = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            GoodsEntity goods = new GoodsEntity("g_" + Utils.randInt(0, 10000), names[i], prices[i], imgResId[i]);
            mGoods.add(goods);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        initGoodsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //??????????????????????????????????????????????????????????????????????????????
        curUserInfo = UserInfo.random();
        mMyserver.setServerListener(this);
        Log.e(TAG, "onResume");
        if (needClearGoods) {
            initGoodsList();
        }
        needClearGoods = true;

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.addGoodsBtn) {
            goAddGoods();
//            Intent intent = new Intent(this, AddGoodsActivity.class);
//            startActivity(intent);
            return;
        }
        mRoomName = mRoomNameEt.getText().toString();
        mRoomId = mRoomIDEt.getText().toString();
        if (mRoomId.length() <= 0) {
            toast("???????????????ID");
            return;
        }
        if (view == mCreateRoomBtn) {
            if (mRoomName.length() <= 0) {
                toast("?????????????????????");
                return;
            }
            if (mRoomName.length() > 8) {
                toast("??????8?????????");
                return;

            }
            isCreate = true;
        } else {
            isCreate = false;
        }

        showLoading("????????????...");
        mMyserver.reqRoomUserCount(this, mRoomId);

    }

    @Override
    public void onActivityResult(Object result) {
        ActivityResult rst = (ActivityResult) result;
        Intent intent = rst.getData();
        Log.e("Main.", "" + rst.getData() + "," + rst.getResultCode());
        //??????????????????result????????????
        if (intent != null && rst.getResultCode() == MY_ADD_GOODS_RESULT_CODE) {

            GoodsEntity goods = (GoodsEntity) intent.getParcelableExtra("goods");

            mGoods.add(0, goods);
            needClearGoods = false;

        } else {
            toast("???????????????????????????");
        }
    }


    private void goAddGoods() {

        Intent intent = new Intent(this, AddGoodsActivity.class);
        mAddGoodslauncher.launch(intent);
    }

    @Override
    protected void onGrantedAllPermission() {
        toRoomActivity();
    }

    private void toRoomActivity() {
        if (isCreate && !checkPermission()) {
            requestPermission();
            return;
        }
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("roomId", mRoomId);
        intent.putExtra("roomName", mRoomName);
        intent.putExtra("isMaster", isCreate);
        intent.putExtra("userName", curUserInfo.name);
        intent.putExtra("userId", curUserInfo.uid);
        if (!isCreate)
            intent.putExtra("goods", new ArrayList<GoodsEntity>());
        else
            intent.putExtra("goods", mGoods);

        startActivity(intent);
    }


    @Override
    public void onRoomUserCount(int userCount) {
        if (isCreate) {
            if (userCount <= 0)
                toRoomActivity();
            else
                toast("??????ID?????????");
        } else {
            if (userCount <= 0)
                toast("??????ID?????????");
            else
                toRoomActivity();
        }
    }


}