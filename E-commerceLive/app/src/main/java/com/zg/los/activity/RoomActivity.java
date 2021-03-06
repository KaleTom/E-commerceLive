package com.zg.los.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codingending.popuplayout.PopupLayout;
import com.zg.los.R;
import com.zg.los.entity.GoodsEntity;
import com.zg.los.entity.MsgType;
import com.zg.los.entity.PreviewItem;
import com.zg.los.entity.RoomInfo;
import com.zg.los.utils.PopupAdapter;
import com.zg.los.utils.ShowUtils;
import com.zg.los.zego.MyServer;
import com.zg.los.zego.Zego;

import java.util.ArrayList;

import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class RoomActivity extends BaseServerEventActivity implements View.OnClickListener, PopupAdapter.OnClickItemBtnListener {
    private final static String TAG = "RoomActivity";
    private String mRoomName;
    private String mRoomId;
    private String mUserId;
    private String mUserName;
    private TextureView mPreviewTV;
    private TextView mRoomNameTV;
    private MyServer mMyserver;
    private RoomInfo mRoomInfo = null;
    private PopupLayout mGoodsListPopup;
    private PopupAdapter mGoodsAdapter;
    private QBadgeView qBadgeView;
    private ImageView mMuteBtn;
    private ArrayList<GoodsEntity> mGoodsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCfg();
        setContentView(R.layout.activity_room);
        initViews();
        initBadge();
        updateGoodsList(mGoodsList);
//        updateRedPoint(mGoodsList.size());
        mMyserver = MyServer.getInstance();
        mMyserver.setServerListener(this);
        if (mIsMaster) {
            mRoomInfo = new RoomInfo(MsgType.MSG_CMD_ROOM_INFO, mUserId, mRoomId, mRoomName, mUserId, mGoodsList, false, false);

        }
        mMuteBtn = findViewById(R.id.muteBtn);
        if (mIsMaster) {
            mMuteBtn.setOnClickListener(this);
            mMuteBtn.setVisibility(View.VISIBLE);
        } else {
            mMuteBtn.setVisibility(View.GONE);
        }
        mZego = Zego.getInstance(getApplication());
        //????????????????????????????????????????????????????????????
        mMyserver.initCurUserInfo(mIsMaster, mRoomInfo, mZego);
        mZego.setHandler(mMyserver);

        showLoading("????????????...");
        mMyserver.getToken(mUserId, mRoomId);
        Log.e(TAG, "onCreate");
    }

    private void initViews() {
        mPreviewTV = findViewById(R.id.preview);
        mRoomNameTV = findViewById(R.id.roomName);
        mRoomNameTV.setText(mRoomName + "(" + mRoomId + ")");
        findViewById(R.id.goodsListBtn).setOnClickListener(this);

        mGoodsAdapter = new PopupAdapter(this, R.layout.goods_popup);

        mGoodsAdapter.setOnClickItemBtnListener(this);
        mGoodsListPopup = newPopup(mGoodsAdapter.getPopupRootView());
        findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoomActivity.this.logout(1);
            }
        });
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        mGoodsListPopup.setHeight((int) (point.y * 0.7), false);

    }

    private void initBadge() {
        qBadgeView = new QBadgeView(this);
        int color = Color.rgb(255, 245, 83);
        qBadgeView.setVisibility(View.GONE);
        qBadgeView.setBadgeBackgroundColor(color);
        qBadgeView.bindTarget(findViewById(R.id.bagGroup));

        qBadgeView.setBadgeGravity(Gravity.END | Gravity.TOP);
//        qBadgeView.setGravityOffset(-10, -10, true);
        qBadgeView.setBadgeTextColor(Color.rgb(115, 51, 00));
        qBadgeView.setBadgeTextSize(10, true);
        qBadgeView.setBadgePadding(5, true);
        qBadgeView.setOnDragStateChangedListener(new Badge.OnDragStateChangedListener() {
            @Override
            public void onDragStateChanged(int dragState, Badge badge, View targetView) {
                if (STATE_SUCCEED == dragState) {
                    badge.hide(true);
                }
            }

        });
    }


    public void updateGoodsList(ArrayList<GoodsEntity> goodsList) {

        mGoodsList = goodsList;

        mGoodsAdapter.setData(mGoodsList);
        mGoodsAdapter.notifyDataSetChanged();
        // ????????????????????????
        int count = goodsList.size();
        if (count <= 0) {
            qBadgeView.setVisibility(View.GONE);
        } else if (count > 99) {
            qBadgeView.setBadgeText("99+");
            qBadgeView.setVisibility(View.VISIBLE);
        } else {
            qBadgeView.setBadgeText(String.valueOf(count));
            qBadgeView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClickItemBtn(int position) {
//        if (position < 0 || position >= mGoodsList.size())
//            return;
    }

    private void openGoodsList() {
        mGoodsListPopup.show();
    }

    private void toggleMic() {

        mRoomInfo.isMuted = !mRoomInfo.isMuted;
        mMyserver.updateRoomInfo(mRoomInfo);
        if (mRoomInfo.isMuted) {
            mMuteBtn.setImageResource(R.mipmap.close_mic);
        } else {
            mMuteBtn.setImageResource(R.mipmap.open_mic);
        }
        mZego.setMute(mRoomInfo.isMuted);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goodsListBtn: {
                openGoodsList();
                break;
            }
            case R.id.muteBtn: {
                toggleMic();
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoodsListPopup.dismiss();
    }

    private void play(String streamId) {

        mZego.playPreview(new PreviewItem(mPreviewTV, streamId), streamId == mUserId);

    }


    private void loadCfg() {
        Intent intent = getIntent();
        mRoomName = intent.getStringExtra("roomName");
        mRoomId = intent.getStringExtra("roomId");
        mRoomName = intent.getStringExtra("roomName");
        mIsMaster = intent.getBooleanExtra("isMaster", false);
        mUserId = intent.getStringExtra("userId");
        mUserName = intent.getStringExtra("userName");
        mGoodsList = intent.getParcelableArrayListExtra("goods");

    }

    private void performLoginOut() {

        mZego.loginOut(mRoomId);
        finish();
    }

    /**
     * level: 0 :no tips, 1: comfirm , 2:force, 3: login error
     */
    public void logout(int level) {
        if (level == 0) {
            performLoginOut();
        }
        switch (level) {
            case 1: {
                String msg = mIsMaster ? "???????????????????????????????????????" : "?????????????????????";
                ShowUtils.comfirm(this, "????????????", msg, "????????????", new ShowUtils.OnClickOkListener() {
                    @Override
                    public void onOk() {
                        performLoginOut();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                break;
            }
            case 2:
            case 3: {
                toast("??????????????????");
                performLoginOut();
                break;
            }
        }
    }


    @Override
    public void onRoomInfo(RoomInfo info) {
        super.onRoomInfo(info);
        mMyserver.updateRoomInfo(info);
        if (info.isLogout) {
            logout(3);
            return;
        }
        mRoomInfo = info;
        mRoomNameTV.setText(info.roomName);
        updateGoodsList(info.goodsEntities);


    }

    @Override
    public void onToken(String token) {

        Log.e(TAG, "????????????");
        mZego.loginRoom(mUserId, mUserName, mRoomId, token);

        //?????????????????????3????????????????????????????????????????????????????????????????????????????????????????????????
        if (!mIsMaster) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3 * 1000);
                        if (mRoomInfo == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast("??????????????????");
                                    logout(0);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    @Override
    public void onDelStream(ArrayList<ZegoStream> streamList) {
        super.onDelStream(streamList);
        for (ZegoStream stream : streamList) {
            Log.e(TAG, stream.streamID + "????????????");
        }
    }

    @Override
    public void onAddStream(ArrayList<ZegoStream> streamList) {
        super.onAddStream(streamList);
        for (ZegoStream stream : streamList) {
            Log.e(TAG, stream.streamID + "????????????");
            // ????????????????????????????????????????????????????????????????????????
            // ???????????????????????????????????????????????????????????????????????????
            play(stream.streamID);
        }
    }

    @Override
    public void onConnected() {
        Log.e(TAG, "???????????????...");
        if (mIsMaster) {
            play(mUserId);
        }
    }

    @Override
    public void onDisconnected() {
        Log.e(TAG, "?????????????????????...");
    }

    @Override
    public void onAddUser(ArrayList<ZegoUser> userList) {

    }

    @Override
    public void onBackPressed() {
        logout(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}