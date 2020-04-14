package com.example.mordhausoundboard;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

    private ArrayList<ParentDataModel> parents;
    private Activity activity;

    RVAdapter(ArrayList<ParentDataModel> parents, Activity activity){
        this.parents = parents;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_parent, parent, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, final int position) {
        holder.parentName.setText(parents.get(position).getName());

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, VoiceTypeActivity.class);
                intent.putExtra(Constants.ITEMNAME, parents.get(position).getName());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return parents.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView parentName;
        ImageView personPhoto;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cardView);
            parentName = itemView.findViewById(R.id.person_name);
            personPhoto = itemView.findViewById(R.id.person_photo);
        }

    }
}
