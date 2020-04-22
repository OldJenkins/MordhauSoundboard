package com.example.mordhausoundboard;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private List<ChildDataModel> mDataset;
    private Context context;
    private Repository repository;

    // Checking if the Adapter is called from voicetype or from favourites to handle different onclick events
    private int ActivityID;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView nameText;
        CardView cardView;
        MaterialFavoriteButton btn_favourite;
        ImageView img_dowonloaded;

        @SuppressLint("WrongViewCast")
        ViewHolder(View v) {
            super(v);
            nameText = v.findViewById(R.id.name);
            cardView = v.findViewById(R.id.cardView);
            btn_favourite = v.findViewById(R.id.btn_favourite);
            img_dowonloaded = v.findViewById(R.id.isDownloaded);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"Position: "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    ArrayList<ChildDataModel> getAllItems (){
        return new ArrayList<>(mDataset);
    }

    ChildDataModel getItem(int position){
        return mDataset.get(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    GridAdapter(List<ChildDataModel> myDataset, Context mCOntext,int ActivityID) {
        Log.d("TEST",myDataset.get(0).getName());
        mDataset = myDataset;
        context = mCOntext;
        repository = new Repository((Application) context.getApplicationContext());
        this.ActivityID = ActivityID;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(mDataset.get(position).getName());
        Log.d("TEST","Printing Names onBindView Holder"+mDataset.get(position));
        if (!getItem(position).isDownloaded()) {
            holder.img_dowonloaded.setVisibility(View.INVISIBLE);
        }
        holder.nameText.setText(mDataset.get(position).getName());
        holder.btn_favourite.setFavorite(mDataset.get(position).isFavourite());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Toast.makeText(context, mDataset.get(position).getUrl(), Toast.LENGTH_LONG).show();
                    MediaPlayer mediaPlayer = new MediaPlayer();

                    mediaPlayer.setDataSource(mDataset.get(position).getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getResources().getString(R.string.Media_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });



        holder.btn_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChildDataModel child = mDataset.get(position);
                if(child.isFavourite()){
                    child.setFavourite(false);
                    holder.btn_favourite.setFavorite(false);
                }else{
                    child.setFavourite(true);
                    holder.btn_favourite.setFavorite(true);
                }
                repository.update(child);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
