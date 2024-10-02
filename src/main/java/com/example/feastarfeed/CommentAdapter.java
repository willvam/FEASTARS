package com.example.feastarfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context context;
    ArrayList<Comment> arrayList;
    OnItemClickListener onItemClickListener;

    public CommentAdapter(Context context, ArrayList<Comment> arrayList, ArrayList<Comment> userList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.comment.setText(arrayList.get(position).getContent());

        holder.user.setText(arrayList.get(position).getUser());

    }

    @Override
    public int getItemCount() {
        return arrayList == null ?0 : arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView comment, user;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.list_item_title);
            user = itemView.findViewById(R.id.list_item_User);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(Comment comment);
    }
}

