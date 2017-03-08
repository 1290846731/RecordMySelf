package com.miaojun.record.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miaojun.record.OnItemClickListener;
import com.miaojun.record.R;

import java.util.List;
import java.util.Map;

/**
 * Created by miaojun on 16/11/3.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoHolder>{
    private Context context;
    private List<Map<String,String>> list;
    private OnItemClickListener onItemClickListener;
    public VideoListAdapter(Context context, List<Map<String,String>> list){
        this.list = list;
        this.context = context;
    }

    @Override
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = View.inflate(parent.getContext(), R.layout.item_video_list, null);
        return new VideoHolder(v);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, final int position) {
        holder.nameTV.setText(list.get(position).get("name"));
        holder.nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }



    class VideoHolder extends RecyclerView.ViewHolder{
        TextView nameTV;
        public VideoHolder(View itemView) {
            super(itemView);
            nameTV = (TextView)itemView.findViewById(R.id.video_name);
        }
    }


}
