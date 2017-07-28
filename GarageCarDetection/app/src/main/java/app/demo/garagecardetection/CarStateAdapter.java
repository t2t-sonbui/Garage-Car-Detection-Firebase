/*
 * Copyright (c) 2015.
 * Author: Son Bui
 */

//Danh sach cac shop chap nhan lam shipper ruot;
package app.demo.garagecardetection;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class CarStateAdapter extends
        RecyclerView.Adapter<CarStateAdapter.ViewHolder> {
    private Context mContext;
    private ListCar mDataSet;
    OnItemClickListener mItemClickListener;
    private int focusedItem = 0;
    private int colorIn, colorOut;

    public CarStateAdapter(Context context, ListCar owners) {
        mContext = context;
        mDataSet = owners;
        colorOut = mContext.getResources().getColor(android.R.color.holo_red_light);
        colorIn = mContext.getResources().getColor(android.R.color.holo_blue_dark);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_list_3line, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.iRange.setText(Integer.toString(mDataSet.getItem(position).getDistance()));
        String timeAgo = DateUtils.getRelativeTimeSpanString(mDataSet.getItem(position).getTimestamp()).toString();
        holder.iTime.setText(timeAgo);
        if (!TextUtils.isEmpty(mDataSet.getItem(position).getName()))
            holder.iName.setText(mDataSet.getItem(position).getName());
        if (mDataSet.getItem(position).isIn_range()) {
            holder.state.setBackgroundColor(colorIn);
        } else holder.state.setBackgroundColor(colorOut);

    }

    @Override
    public int getItemCount() {
        return mDataSet.getItemCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView poster, state;
        TextView iTime, iName, iRange;

        public ViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.poster);
            state = (ImageView) itemView.findViewById(R.id.type);
            iTime = (TextView) itemView.findViewById(R.id.time);
            iName = (TextView) itemView.findViewById(R.id.name);
            iRange = (TextView) itemView.findViewById(R.id.range);
//            poster.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mItemClickListener != null)
//                        mItemClickListener.onItemClick(v, getLayoutPosition());
//                }
//            });
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int index);
    }

    public void SetOnItemClickListener(
            final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


}