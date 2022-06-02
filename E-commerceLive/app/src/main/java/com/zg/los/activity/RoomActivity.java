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
        //初始化用户信息，这些信息本应从服务器读取
        mMyserver.initCurUserInfo(mIsMaster, mRoomInfo, mZego);
        mZego.setHandler(mMyserver);

        showLoading("正在连接...");
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
        // 更新数量气泡视图
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
                String msg = mIsMaster ? "退出将解散房间，确定退出？" : "确定退出房间？";
                ShowUtils.comfirm(this, "退出房间", msg, "退出房间", new ShowUtils.OnClickOkListener() {
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
                toast("主播已解散！");
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

        Log.e(TAG, "登录房间");
        mZego.loginRoom(mUserId, mUserName, mRoomId, token);

        //针对观众，如果3秒内没有收到当前房间信息，则表示主播一直处于断网状态，要自动退出
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
                                    toast("主播已离线！");
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
            Log.e(TAG, stream.streamID + "停止推流");
        }
    }

    @Override
    public void onAddStream(ArrayList<ZegoStream> streamList) {
        super.onAddStream(streamList);
        for (ZegoStream stream : streamList) {
            Log.e(TAG, stream.streamID + "收到推流");
            // 这里只有一个推流，所以直接将获取到的推流播放即可
            // 如果有多个推流，则需要根据实际需求选择播放不同的流
            play(stream.streamID);
        }
    }

    @Override
    public void onConnected() {
        Log.e(TAG, "已连接房间...");
        if (mIsMaster) {
            play(mUserId);
        }
    }

    @Override
    public void onDisconnected() {
        Log.e(TAG, "已断开连接房间...");
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