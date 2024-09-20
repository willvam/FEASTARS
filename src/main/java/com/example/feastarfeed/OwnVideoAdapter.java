package com.example.feastarfeed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class OwnVideoAdapter extends RecyclerView.Adapter<OwnVideoAdapter.MyViewHolder> {
    ArrayList<String> arrayList;
    Context context;
    OnItemClickListener onItemClickListener;

    public OwnVideoAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_own_video, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String imageUrl = arrayList.get(position);

        // 檢查 imageUrl 和 holder.imageView 是否為 null
        if (imageUrl != null && holder.imageView != null) {
            Glide.with(context).load(imageUrl).into(holder.imageView);
        } else {
            // 處理 imageUrl 或 holder.imageView 為 null 的情況
        }

        holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(arrayList.get(position), position));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

        }

    }

    public void setOnItemClickListener(OwnVideoAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(String string, int position);
    }
}
