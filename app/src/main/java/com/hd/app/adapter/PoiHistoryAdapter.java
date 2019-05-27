package com.hd.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.hd.app.R;

import java.util.List;

/**
 * @author Only_ZziTai  2019/5/27
 *
 */

public class PoiHistoryAdapter extends RecyclerView.Adapter<PoiHistoryAdapter.MyViewHolder> {

    public Context context;
    OnHistoryItemClickListener listener;
    private List<PoiInfo> list;

    public PoiHistoryAdapter(Context context, List<PoiInfo> list) {
        this.context = context;
        this.list=list;
    }

    public void changeData(List<PoiInfo> list) {
        if (list == null) {
            this.list.clear();
        }else {
            this.list = list;
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_history_item, null);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) view.getTag();
                if (listener != null) {
                    //这行可能要看着加东西
                    listener.onHistoryItemClick(view, position,list.get(position));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PoiInfo poi = list.get(position);
        holder.place.setText(poi.address);
        //这个看着删 好像没用。
        holder.district.setText(poi.getName());
        holder.itemView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView place, district;

        public MyViewHolder(View view) {
            super(view);
            place = (TextView) view.findViewById(R.id.place);
            district = (TextView) view.findViewById(R.id.district);
        }
    }

    public interface OnHistoryItemClickListener {
        public void onHistoryItemClick(View v, int position,  PoiInfo info)
        ;
    }

    public void setOnClickListener(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }
}