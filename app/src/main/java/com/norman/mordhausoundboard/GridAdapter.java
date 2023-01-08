package com.norman.mordhausoundboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import es.dmoral.toasty.Toasty;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private final List<ChildDataModel> mDataset;
    private final Context context;
    private final Repository repository;
    private final SharedPreferences prefs;
    private final Activity activity;
    private boolean deleted;
    private BottomSheet sheet;
    private final FragmentManager fragmentManager;
    private int currentAdapterPosition;

    // Checking if the Adapter is called from voicetype or from favourites to handle different onclick events
    private final int ActivityID;

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView tv_nameText;
        CardView cardView;
        MaterialFavoriteButton btn_favourite;
        ImageView img_dowonloaded;

        @SuppressLint("WrongViewCast")
        ViewHolder(View v) {
            super(v);
            tv_nameText = v.findViewById(R.id.name);
            cardView = v.findViewById(R.id.cardView);
            btn_favourite = v.findViewById(R.id.btn_favourite);
            img_dowonloaded = v.findViewById(R.id.isDownloaded);
        }
    }

    ChildDataModel getItem(int position){
        return mDataset.get(position);
    }

    GridAdapter(List<ChildDataModel> myDataset, Context mContext, int ActivityID, Activity activity,FragmentManager fragmentManager) {
        mDataset = myDataset;
        context = mContext;
        repository = new Repository((Application) context.getApplicationContext());
        this.ActivityID = ActivityID;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS,0);
    }

    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_grid, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (!getItem(position).isDownloaded()) {
            holder.img_dowonloaded.setVisibility(View.INVISIBLE);
        }
        if(ActivityID == Constants.ID_GRID) {
            holder.tv_nameText.setText(mDataset.get(position).getName().replace(mDataset.get(position).getParent(), ""));
        }else if(ActivityID == Constants.ID_HOME){
            holder.tv_nameText.setText(mDataset.get(position).getName());
        }

        holder.btn_favourite.setFavorite(mDataset.get(position).isFavourite());
        if(mDataset.get(position).isDownloaded()) holder.img_dowonloaded.setVisibility(View.VISIBLE);
        else holder.img_dowonloaded.setVisibility(View.GONE);

        holder.cardView.setOnClickListener(v -> {

            try {
                // We try to tyke the path to the Database first
                // if this doesn't exists, we have to take the Server url
                playSound(mDataset.get(position).getUrl());

            } catch (IOException e) {
                e.printStackTrace();

                try {
                    String url = activity.getResources().getString(R.string.fileServerUrl) + mDataset.get(position).getParent() +"/" + mDataset.get(position).getRawname();
                    playSound(url);
                }catch (IOException a){
                    Toasty.error(context, context.getResources().getString(R.string.Media_not_found), Toast.LENGTH_SHORT).show();
                }
            }

        });

        holder.cardView.setOnLongClickListener(v -> {
            sheet = new BottomSheet(position,mDataset.get(position).getName(),Constants.ID_GRID);
            sheet.show(fragmentManager,"bottomsheet");
            currentAdapterPosition = position;
            return true;
        });

        holder.btn_favourite.setOnClickListener(v -> {
            ChildDataModel child = mDataset.get(position);
            if(child.isFavourite()){
                child.setFavourite(false);
                holder.btn_favourite.setFavorite(false);
            }else{
                child.setFavourite(true);
                holder.btn_favourite.setFavorite(true);
            }
            repository.update(child);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    void playSound(String url) throws IOException {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(url);
            mp.setOnPreparedListener(this::onPrepared);
            mp.prepareAsync();
        } catch (IOException e) {
            System.out.println("could not prepare");
        }
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    void clickDownload(){
        sheet.dismiss();
        new DownloadOneFile().execute(mDataset.get(sheet.getPosition()));
    }

    void clickDelete(){
        deleteFile(mDataset.get(currentAdapterPosition));
        if(deleted){ Toasty.success(context, mDataset.get(currentAdapterPosition).getName() + activity.getResources().getString(R.string.deleted_item_suffix), Toast.LENGTH_SHORT).show(); deleted = false;}
        else Toasty.error(context, activity.getResources().getString(R.string.not_deleted_item), Toast.LENGTH_SHORT).show();
        sheet.dismiss();
    }

    void clickShare(){
        if(mDataset.get(sheet.getPosition()).isDownloaded()) {
            String sharePath = mDataset.get(currentAdapterPosition).getUrl();
            Uri uri = Uri.parse(sharePath);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(share, activity.getResources().getString(R.string.share) + mDataset.get(sheet.getPosition()).getName()));
        }else Toasty.info(context, activity.getResources().getString(R.string.share_info), Toast.LENGTH_SHORT).show();
        sheet.dismiss();
    }

    void clickCancel(){
        sheet.dismiss();
    }

    public class DownloadOneFile extends AsyncTask<ChildDataModel,Void,String> {
        ProgressDialog dialog;
        ChildDataModel child;
        boolean isAlready;
        boolean downloadSuccess = false;

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(true);
            dialog.setMessage(activity.getResources().getString(R.string.download_dialog_info));
            dialog.setTitle(activity.getResources().getString(R.string.download_dialog_title));
            dialog.setMax(1);
            dialog.show();

        }

        @Override
        protected String doInBackground(ChildDataModel... params) {

            child = params[0];
            if(!child.isDownloaded()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // download each file
                if(downloadFile(child)) {

                    dialog.incrementProgressBy(1);
                    downloadSuccess = true;
                }

                if(isCompletlyDownloaded() && ActivityID == 1){
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
            if(downloadSuccess) {
                Toasty.success(context, child.getName() + " " + activity.getResources().getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                child.setDownloaded(true);
                notifyDataSetChanged();
            }
            else if( isAlready) Toasty.info(context, child.getName() + " "+ activity.getResources().getString(R.string.downloaded_already), Toast.LENGTH_SHORT).show();
            else Toasty.error(context,activity.getResources().getString(R.string.fail_downloading_part1)  + " "+ child.getName() , Toast.LENGTH_SHORT).show();

            downloadSuccess = false;
        }
    }

    // handle a download of a single file
    boolean downloadFile(ChildDataModel child){
        boolean flag = true;
        boolean downloading =true;
        try{
            String DownloadUrl = activity.getResources().getString(R.string.fileServerUrl)+child.getParent()+"/"+child.getRawname();
            System.out.println(" DOWNLOAD URL "+DownloadUrl);
            final DownloadManager mManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            final DownloadManager.Request mRqRequest = new DownloadManager.Request(
                    Uri.parse(DownloadUrl));
            mRqRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/MordhauSoundboard/"+child.getRawname());

            long idDownLoad=mManager.enqueue(mRqRequest);
            DownloadManager.Query query = null;
            query = new DownloadManager.Query();
            Cursor c = null;
            if(query!=null) {
                query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
            } else {
                return flag;
            }

            int counter = 0;
            while (downloading) {
                c = mManager.query(query);
                if(c.moveToFirst()) {
                    Log.i ("FLAG","Downloading");
                    @SuppressLint("Range") int status =c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if (status==DownloadManager.STATUS_SUCCESSFUL) {
                        Log.i ("FLAG","done");
                        downloading = false;
                        flag=true;
                        child.setUrl(Environment.getExternalStorageDirectory()+"/Download/MordhauSoundboard/"+child.getRawname());
                        child.setDownloaded(true);
                        repository.update(child);

                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {
                        Log.i ("FLAG","Fail");
                        downloading = false;
                        flag=false;
                        break;
                    }
                    if(status==DownloadManager.STATUS_PENDING) {

                        try {
                            Thread.sleep(1000);
                            counter++;
                            Log.i ("timer","reached "+counter);
                            if(counter== 15){
                                return false ;
                            }
                        } catch (Exception e) {
                            Log.i ("timer"," timer exceeded "+counter);
                        }
                    }
                }
            }

            return flag;
        }catch (Exception e) {
            flag = false;
            return flag;
        }
    }

    boolean isCompletlyDownloaded(){
        for(int i = 0;i<mDataset.size();i++){
            if (!mDataset.get(i).isDownloaded()) return false;
        }
        return true;
    }

    void deleteFile(ChildDataModel child){

            if(child.isDownloaded()) {
                File file = new File(child.getUrl());
                deleted = file.delete();
                Toasty.info(context, child.getName()+ " "+ context.getResources().getString(R.string.delete_from_storage), Toast.LENGTH_SHORT,true).show();

                child.setUrl(context.getResources().getString(R.string.fileServerUrl)+child.getParent()+"/"+child.getRawname());

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

                Toasty.warning(context, child.getName()+" "+ context.getResources().getString(R.string.not_already_downloaded), Toast.LENGTH_SHORT,true).show();
            }
    }

}
