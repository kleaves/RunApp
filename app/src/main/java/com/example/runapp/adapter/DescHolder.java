package com.example.runapp.adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.runapp.R;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class DescHolder extends RecyclerView.ViewHolder {
    public TextView descView,km,time,date;
    public RelativeLayout desc_rl;

    public DescHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        descView = itemView.findViewById(R.id.tv_desc);
        km = itemView.findViewById(R.id.km);
        time = itemView.findViewById(R.id.time);
        date = itemView.findViewById(R.id.date);
        desc_rl = itemView.findViewById(R.id.desc_rl);
    }
}
