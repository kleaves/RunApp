package com.example.runapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.runapp.R;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class HeaderHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    public RelativeLayout openView;
    public ImageView imageView;
    public HeaderHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        titleView = itemView.findViewById(R.id.tv_title);
        openView = itemView.findViewById(R.id.tv_open);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
