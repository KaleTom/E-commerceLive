package com.zg.los.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zg.los.R;
import com.zg.los.entity.GoodsEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.GoodsViewHolder> implements View.OnClickListener {
    private static final String TAG = "MyAdapter";

    private OnClickItemBtnListener onClickItemBtnListener;
    public List<GoodsEntity> mGoodsList;
    private Context context;
    private View mPopupRootView;

    public PopupAdapter(Context context, int layoutId) {
        this.context = context;
        this.mGoodsList = new ArrayList<>();
        initPopupRootView(context, layoutId);
    }

    public View getPopupRootView() {
        return mPopupRootView;
    }

    private void initPopupRootView(Context context, int layoutId) {
        mPopupRootView = LayoutInflater.from(context).inflate(layoutId, null);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        RecyclerView recyclerView = mPopupRootView.findViewById(R.id.rclView);
//        BottomPopAdapter adapter = new BottomPopAdapter(ctx, mList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(this);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);
    }

    public void setData(List<GoodsEntity> goodsList) {
        mGoodsList = goodsList;
    }

    @NonNull
    @Override
    public GoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GoodsViewHolder viewHolder;
        View inflate = LayoutInflater.from(context).inflate(R.layout.goods_item, parent, false);
        viewHolder = new GoodsViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsViewHolder holder, int position) {
        GoodsEntity goods = mGoodsList.get(position);

        holder.goodsImgIV.setTag(position);
        holder.goodsNameTV.setText(goods.goodsName);
        holder.priceTV.setText("" + goods.price);
        holder.goodsImgIV.setImageResource(goods.imgId);
        holder.idxTV.setText("" + (position + 1));
    }


    @Override
    public int getItemCount() {
        return mGoodsList.size();
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "点击按钮" + v.getTag());
        if (this.onClickItemBtnListener == null) return;
        int pos = (int) v.getTag();
        this.onClickItemBtnListener.onClickItemBtn(pos);

    }

    class GoodsViewHolder extends RecyclerView.ViewHolder {

        ImageView goodsImgIV;
        TextView goodsNameTV;
        TextView priceTV;
        TextView idxTV;

        public GoodsViewHolder(View itemView) {
            super(itemView);
            goodsImgIV = itemView.findViewById(R.id.goodsImg);
            goodsNameTV = itemView.findViewById(R.id.goodsName);
            priceTV = itemView.findViewById(R.id.goodsPrice);
            idxTV = itemView.findViewById(R.id.idxTextView);
        }
    }


    public void setOnClickItemBtnListener(PopupAdapter.OnClickItemBtnListener onClickItemBtnListener) {
        this.onClickItemBtnListener = onClickItemBtnListener;
    }

    public interface OnClickItemBtnListener {
        void onClickItemBtn(int position);
    }
}
