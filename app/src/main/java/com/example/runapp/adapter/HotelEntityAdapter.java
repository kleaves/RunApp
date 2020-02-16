package com.example.runapp.adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.runapp.R;
import com.example.runapp.entity.HotelEntity;
import com.example.runapp.utils.HotelUtils;

import java.util.List;

/**
 * Created by yushuangping on 2018/8/23.
 */

public class HotelEntityAdapter extends SectionedRecyclerViewAdapter<HeaderHolder, DescHolder, RecyclerView.ViewHolder> {


    public List<HotelEntity.TagsEntity> allTagList;
    private Context mContext;
    private LayoutInflater mInflater;

    private SparseBooleanArray mBooleanMap;//记录下哪个section是被打开的

    public HotelEntityAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mBooleanMap = new SparseBooleanArray();
    }

    public void setData(List<HotelEntity.TagsEntity> allTagList) {
        this.allTagList = allTagList;
        mBooleanMap.put(0, true);
        notifyDataSetChanged();
    }

    /**
     * 一共有多少个section需要展示， 返回值是我们最外称list的大小，在我们的示例图中，
     * 对应的为热门品牌—商业区—热门景点 等，对应的数据是我们的allTagList
     *
     * @Override
     */

    protected int getSectionCount() {
        return HotelUtils.isEmpty(allTagList) ? 0 : allTagList.size();
    }

    /**
     * 来展示content内容区域，返回值是我们需要展示多少内容，在本例中，我们超过8条数据只展示8条，
     * 点击展开后就会展示全部数据，逻辑就在这里控制。 对应数据结构为tagInfoList
     *
     * @param section
     * @return
     */
    @Override
    protected int getItemCountForSection(int section) {
        int  count = allTagList.get(section).tagInfoList.size();
        if (count >= 1 && !mBooleanMap.get(section)) {
            count = 0;
        }
        if (section==0&&mBooleanMap.get(section)){
            count = allTagList.get(section).tagInfoList.size();
        }
        return HotelUtils.isEmpty(allTagList.get(section).tagInfoList) ? 0 : count;
    }

    //是否有footer布局

    /**
     * 判断是否需要底部footer布局，在该例中，我们并不需要显示footer，所以默认返回false就可以，
     * 如果你对应的section需要展示footer布局，那么就在对应的section返回true就行了
     *
     * @param section
     * @return
     */
    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(mInflater.inflate(R.layout.hotel_title_item, parent, false));
    }


    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected DescHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new DescHolder(mInflater.inflate(R.layout.hotel_desc_item, parent, false));
    }


    @Override
    protected void onBindSectionHeaderViewHolder(final HeaderHolder holder, final int section) {
        holder.openView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpen = mBooleanMap.get(section);
//                String text = isOpen ? "展开" : "关闭";
                mBooleanMap.put(section, !isOpen);
//                holder.openView.setText(text);
                notifyDataSetChanged();
            }
        });
        holder.titleView.setText(allTagList.get(section).year);
//        holder.openView.setText(mBooleanMap.get(section) ? "关闭" : "展开");//true显示关闭字段，false显示展开字段
        holder.imageView.setBackgroundResource(mBooleanMap.get(section) ? R.drawable.arrow_up : R.drawable.arrow_down);

    }


    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {

    }

    /**
     * 这里有一个section和position ，有些人可能会弄混
     * section是区域，也就是我们最外层的index，position是每个section对应的内容数据的position
     *
     * @param holder
     * @param section
     * @param position
     */
    @Override
    protected void onBindItemViewHolder(final DescHolder holder, final int section, final int position) {
        holder.descView.setText(allTagList.get(section).tagInfoList.get(position).speed);
        holder.km.setText(allTagList.get(section).tagInfoList.get(position).distance);
        holder.time.setText(allTagList.get(section).tagInfoList.get(position).time);
        holder.date.setText(allTagList.get(section).tagInfoList.get(position).month);
        Log.e("=====AAA===", "section=" + section + ",position=" + position);
        holder.desc_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(section, position,holder);

            }
        });

    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClick(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int section, int position,DescHolder holder);
    }
}
