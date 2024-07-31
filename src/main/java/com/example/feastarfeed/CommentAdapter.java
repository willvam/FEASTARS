package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context context;
    ArrayList<Comment> arrayList;
    ArrayList<Comment> userList;
    OnItemClickListener onItemClickListener;

    public CommentAdapter(Context context, ArrayList<Comment> arrayList, ArrayList<Comment> userList) {
        this.context = context;
        this.arrayList = arrayList;
        this.userList = userList;
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
        if (userList != null && userList.size() > position) {
            holder.user.setText(userList.get(position).getUser());
        } else {
            holder.user.setText("Unknown User");
        }
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

