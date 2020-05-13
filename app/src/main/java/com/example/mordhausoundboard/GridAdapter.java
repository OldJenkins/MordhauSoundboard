package com.example.mordhausoundboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> implements BottomSheet.BottomSheetListener {
    private List<ChildDataModel> mDataset;
    private Context context;
    private Repository repository;
    private SharedPreferences prefs;
    private Activity activity;
    private boolean deleted;
    private BottomSheet sheet;
    FragmentManager fragmentManager;

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
    GridAdapter(List<ChildDataModel> myDataset, Context mCOntext, int ActivityID, Activity activity,FragmentManager fragmentManager) {
        Log.d("TEST",myDataset.get(0).getName());
        mDataset = myDataset;
        context = mCOntext;
        repository = new Repository((Application) context.getApplicationContext());
        this.ActivityID = ActivityID;
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS,0);
        this.activity = activity;
        this.fragmentManager = fragmentManager;
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
        if(ActivityID == 1) {
            holder.nameText.setText(mDataset.get(position).getName().replace(mDataset.get(position).getParent(), ""));
        }else if(ActivityID == 2){
            holder.nameText.setText(mDataset.get(position).getName());
        }
        holder.btn_favourite.setFavorite(mDataset.get(position).isFavourite());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    Toast.makeText(context, mDataset.get(position).getUrl(), Toast.LENGTH_SHORT).show();
                    mediaPlayer.setDataSource(mDataset.get(position).getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();

                    try {
                        mediaPlayer.setDataSource(activity.getResources().getString(R.string.downloadPath) + mDataset.get(position).getRawname());
                        Toast.makeText(context, activity.getResources().getString(R.string.downloadPath) + mDataset.get(position).getRawname(), Toast.LENGTH_SHORT).show();
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }catch (IOException a){
                        Toast.makeText(context, context.getResources().getString(R.string.Media_not_found), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                sheet = new BottomSheet(position);
                sheet.show(fragmentManager,"bottomsheet");

                return true;
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

    @Override
    public void onButtonClicked(int text) {
        /*
        int pos = sheet.getPosition();
        switch (text) {
            case 0:
                Toast.makeText(context, "Downloading "+mDataset.get(pos).getRawname(), Toast.LENGTH_SHORT).show();
                new DownloadAllContentAsync().execute(mDataset.get(pos));
                break;

            case 1:
                deleteFile(mDataset.get(pos));
                if(deleted){ Toast.makeText(context, mDataset.get(pos).getName() + " was deleted from Storage", Toast.LENGTH_SHORT).show(); deleted = false;}
                else Toast.makeText(context, "Could not Delete from Storage", Toast.LENGTH_SHORT).show();
                break;

            case 2:
                if(mDataset.get(sheet.getPosition()).isDownloaded()) {
                    String sharePath = mDataset.get(pos).getUrl();
                    Uri uri = Uri.parse(sharePath);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/*");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    activity.startActivity(Intent.createChooser(share, "Share Sound File"));
                }else Toast.makeText(context, "file must be downloaded", Toast.LENGTH_SHORT).show();
                break;
        }

         */
    }


    public class DownloadAllContentAsync extends AsyncTask<ChildDataModel,Void,String> {
        ProgressDialog dialog;

        ChildDataModel child;
        boolean isAlready;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //TODO STRINGS!!!!!!!!!!!!!
            dialog.setCancelable(true);
            dialog.setMessage("Downloading the Soundata to the local Storage");
            dialog.setTitle("Downloading...");
            dialog.setMax(1);
            dialog.show();

        }

        @Override
        protected String doInBackground(ChildDataModel... params) {


            child = params[0];
            if(!child.isDownloaded()){

                downloadFile(child);
                dialog.incrementProgressBy(1);

                if(isAllDownloaded() && ActivityID == 1){
                    ParentDataModel parent = repository.getParent(child.getParent());
                    isAlready = false;
                    parent.setAllItemsDownloaded(true);
                    repository.updateParent(parent);
                }
            }else{
                isAlready = true;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if(!isAlready){
                Toast.makeText(context, child.getName() + " was Downloaded", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }else Toast.makeText(context, child.getName() + " is already downloaded", Toast.LENGTH_SHORT).show();


        }
    }

    public void downloadFile(ChildDataModel child) {
        String DownloadUrl = "https://drinkhub.eu/MordhauSoundboard/sounds/"+child.getParent()+"/"+child.getRawname();
        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
        //TODO strings!!!!!
        request1.setDescription("Downloading Mordau Sound data");   //appears the same in Notification bar while downloading
        request1.setTitle(child.getName());
        request1.setVisibleInDownloadsUi(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner();
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }

        request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/MordhauSoundboard/"+child.getRawname());

        DownloadManager manager1 = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            System.out.println("success downloading: "+DownloadUrl);


            child.setUrl(Environment.getExternalStorageDirectory()+"/Download/MordhauSoundboard/"+child.getRawname());
            child.setDownloaded(true);
            repository.update(child);
            notifyDataSetChanged();

        }
    }

    boolean isAllDownloaded(){
        for(int i = 0;i<mDataset.size();i++){
            if (!mDataset.get(i).isDownloaded()) return false;
        }
        return true;
    }

    void deleteFile(ChildDataModel child){



            if(child.isDownloaded()) {
                File file = new File(child.getUrl());
                deleted = file.delete();
                Toast.makeText(context, child.getName()+ " deleted from STORAGE", Toast.LENGTH_SHORT).show();

                child.setUrl(context.getResources().getString(R.string.downloadPath)+child.getParent()+"/"+child.getRawname());
                Toast.makeText(context, "new url: "+child.getName() + " "+child.getRawname(), Toast.LENGTH_SHORT).show();
                child.setDownloaded(false);
                repository.update(child);

                ParentDataModel parent = repository.getParent(child.getParent());
                if(parent.isAllItemsDownloaded()){
                    parent.setAllItemsDownloaded(false);
                    repository.updateParent(parent);
                    String savedList = prefs.getString(Constants.DOWNLOADLIST,"");
                    List<String> list = new ArrayList<String>(Arrays.asList(savedList.split("%")));
                    String resultList="";

                    for(int i =0; i<list.size();i++){
                        list.remove(parent.getName());
                    }
                    for(int i =0; i<list.size();i++){
                        resultList += "%"+list.get(i);
                    }
                    prefs.edit().putString(Constants.DOWNLOADLIST,resultList).apply();
                }

                notifyDataSetChanged();
            }else {

                Toast.makeText(context, child.getName()+ " ist nicht gespeichert", Toast.LENGTH_SHORT).show();
            }


    }


}
