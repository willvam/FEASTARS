package com.example.feastarfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PersonalPageAdapter extends RecyclerView.Adapter<PersonalPageAdapter.MyViewHolder> {
    ArrayList<String> arrayList;
    Context context;
    OnItemClickListener onItemClickListener;

    public PersonalPageAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.personalpage_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String imageUrl = arrayList.get(position);
        Glide.with(context).load(imageUrl).into(holder.imageView);
        holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(arrayList.get(position),position));

        //holder.imageView.setImageBitmap(arrayList.get(position).getBitmap());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewP);

        }

    }

    public void setOnItemClickListener(PersonalPageAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(String string, int position);
    }
}
