package com.example.mordhausoundboard;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
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

        @SuppressLint("WrongViewCast")
        ViewHolder(View v) {
            super(v);
            nameText = v.findViewById(R.id.name);
            cardView = v.findViewById(R.id.cardView);
            btn_favourite = v.findViewById(R.id.btn_favourite);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"Position: "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        holder.nameText.setText(mDataset.get(position).getName());
        holder.btn_favourite.setFavorite(mDataset.get(position).isFavourite());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(mDataset.get(position).getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getResources().getString(R.string.Media_not_found), Toast.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar
                            .make(v.findViewById(R.id.coordHome), R.string.Media_not_found, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });




        holder.btn_favourite.setOnFavoriteChangeListener(
                new MaterialFavoriteButton.OnFavoriteChangeListener() {
                    @Override
                    public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                        ChildDataModel child = mDataset.get(position);
                        child.setFavourite(favorite);
                        if(favorite){
                            repository.delete(child);
                            holder.btn_favourite.setAnimateUnfavorite(true);
                            Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            repository.insert(child);
                            holder.btn_favourite.setAnimateFavorite(true);
                            Toast.makeText(context, "inserted", Toast.LENGTH_SHORT).show();
                        }
                        mDataset.get(position).setChild(child);
                    }
                });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
