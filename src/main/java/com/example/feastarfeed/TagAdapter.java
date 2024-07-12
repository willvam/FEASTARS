package com.example.feastarfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder>{

    Context context;

    ArrayList<Tag> arrayList;

    public TagAdapter(Context context, ArrayList<Tag> arrayList){
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tag.setText(arrayList.get(position).getTag());
    }

    @Override
    public int getItemCount() {
        return arrayList == null ?0 : arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tag;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag_text);
        }
    }
}
