package com.norman.mordhausoundboard;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import es.dmoral.toasty.Toasty;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    private ArrayList<ParentDataModel> parents;
    private Activity activity;
    private BottomSheet sheet;
    private Repository repository;
    private SharedPreferences prefs;
    private ArrayList<ChildDataModel> TEMPChildList;
    private FragmentManager fragmentManager;
    private String tempParentName;
    private DownloadManager mManager;
    private boolean downloading = true;
    private boolean globalDownload = true;
    private ArrayList<Long> idList;
    private int idCounter = 0;

    RVAdapter(ArrayList<ParentDataModel> parents, Activity activity, FragmentManager fragmentManager){
        this.parents = parents;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        repository = new Repository(activity.getApplication());
        prefs = activity.getSharedPreferences(Constants.PREFS,0);
        idList = new ArrayList<>();
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_parent, parent, false);
        return new PersonViewHolder(v);
    }


    void setParentWith(ParentDataModel parent){
        for(int i = 0;i<parents.size();i++) {
            if (parents.get(i).getName().equals(parent.getName())) {
                parents.set(i,parent);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, final int position) {
        holder.parentName.setText(parents.get(position).getName());

        if(parents.get(position).isAllItemsDownloaded()){
            holder.isDownloaded.setVisibility(View.VISIBLE);
        }

        switch(parents.get(position).getName()){
            case"Commoner":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.commoner_icon));
                break;
            case "Cruel":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.cruel_icon));
                break;
            case"Foppish":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.foppish_icon));
                break;
            case"Knight":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.knight_icon));
                break;
            case"Eager":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.eager_icon));
                break;
            case"Plain":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.plain_icon));
                break;
            case"Raider":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.raider_icon));
                break;
            case"Young":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.young_icon));
                break;
            case"Barbarian":
                holder.personPhoto.setImageDrawable(activity.getResources().getDrawable(R.drawable.barbarian_icon));
                break;
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, VoiceTypeActivity.class);
                intent.putExtra(Constants.ITEMNAME, parents.get(position).getName());
                activity.startActivity(intent);
            }
        });

        holder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                sheet = new BottomSheet(position,parents.get(position).getName(),Constants.ID_HOME);
                sheet.show(fragmentManager,"bottomsheet");

                return true;
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
        ImageView isDownloaded;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cardView);
            parentName = itemView.findViewById(R.id.person_name);
            personPhoto = itemView.findViewById(R.id.person_photo);
            isDownloaded = itemView.findViewById(R.id.isDownloaded);
        }

    }

    void deleteParent(ParentDataModel parent){

        List<ChildDataModel> childList = repository.getmAllChildsbyName(parent.getName());

        for(int i = 0;i<childList.size();i++){
            ChildDataModel child = childList.get(i);
            if(child.isDownloaded()) {
                File file = new File(child.getUrl());
                file.delete();

                child.setUrl(activity.getResources().getString(R.string.downloadPath)+child.getParent()+"/"+child.getRawname());
                child.setDownloaded(false);
                repository.update(child);
            }
        }
        parent.clearDownloads();
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
        Toasty.info(activity, activity.getResources().getString(R.string.deleted_content) +" "+ parent.getName() , Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
    }

    public class getAllContentAsync extends AsyncTask<String,Void,String> {
        String name;
        String json_url;
        String JSON_STRING;

        @Override
        protected void onPreExecute() {
            json_url = activity.getResources().getString(R.string.rootPath)+"getDirectorycontent.php";
        }

        @Override
        protected String doInBackground(String... params) {

            name = params[0];
            String data;


            try {
                URL url = new URL(json_url);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection)url.openConnection();
                httpsURLConnection.setConnectTimeout(15000);
                httpsURLConnection.setReadTimeout(15000);
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoOutput(true);
                OutputStream OS = httpsURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS,"UTF-8"));

                data = URLEncoder.encode("name","UTF-8") + "=" +URLEncoder.encode(name,"UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                OS.close();

                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine())!=null){
                    stringBuilder.append(JSON_STRING+"\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpsURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {

            if (!TextUtils.isEmpty(result)) {

                if (!result.equals("") && !result.contains("failed") && isJSONValid(result)) {

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChildDataModel>>() {
                    }.getType();

                    List<ChildDataModel> newList = gson.fromJson(result, listType);
                    List<ChildDataModel> oldList = repository.getmAllChildsbyName(name);

                    for(int i = 0;i < newList.size();i++){
                        for(int j  = 0;j<oldList.size();j++){
                            if(newList.get(i).AlmostEquals(oldList.get(j))){
                                newList.get(i).setChild(oldList.get(j));
                            }
                        }
                    }

                    repository.insertAll(newList);

                    TEMPChildList = new ArrayList<>(newList);


                    new DownloadAllContentAsync().execute(parents.get(sheet.getPosition()));
                }

            }else{
                Toast.makeText(activity, R.string.fetching_error, Toast.LENGTH_SHORT).show();
            }
        }
        boolean isJSONValid(String test) {
            try {
                new JSONObject(test);
            } catch (JSONException ex) {
                // edited, to include @Arthur's comment
                // e.g. in case JSONArray is valid as well...
                try {
                    new JSONArray(test);
                } catch (JSONException ex1) {
                    return false;
                }
            }
            return true;
        }
    }

    public class DownloadAllContentAsync extends AsyncTask<ParentDataModel,Void,String> {
        ProgressDialog dialog;

        int failCounter = 0;
        ParentDataModel parent;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //TODO STRINGS!!!!!!!!!!!!!
            dialog.setCancelable(false);
            dialog.setMessage(activity.getResources().getString(R.string.downloading_message));
            dialog.setTitle("Downloading "+tempParentName);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    int i = 0;
                    dialog.dismiss();//dismiss dialog
                    downloading = false;
                    globalDownload = false;
                    while(i<idList.size()){
                        //removing Elements out of the downloading queue

                        //mManager.remove(idList.get(i));

                        //System.out.println("removing "+idList.get(i) + " out of queue list");
                        i++;
                    }
                    Toasty.info(activity, activity.getResources().getString(R.string.download_canceled), Toast.LENGTH_SHORT, true).show();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Background", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();//dismiss dialog
                }
            });
            dialog.show();
        }

        @Override
        protected String doInBackground(ParentDataModel... params) {

            parent = params[0];
            TEMPChildList = (ArrayList<ChildDataModel>) repository.getmAllChildsbyName(parent.getName());
            TEMPChildList = removeDownloadedItems(TEMPChildList);
            downloading = true;
            globalDownload = true;

            dialog.setMax(TEMPChildList.size());

            if(!parent.isAllItemsDownloaded()) {
                int i = 0;
                while(i<TEMPChildList.size() && globalDownload) {


                    ChildDataModel model = TEMPChildList.get(i);

                    if (!model.isDownloaded()) {
                        if(!downloadFile(model)) {
                            failCounter++;

                        }else{

                            model.setDownloaded(true);
                            repository.update(model);
                        }
                        dialog.incrementProgressBy(1);
                    }
                    i++;
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            dialog.incrementProgressBy(1);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if(isAllDownloaded() && failCounter==0){

                Toasty.success(activity, activity.getResources().getString(R.string.success_downloading), Toast.LENGTH_SHORT, true).show();
                parent.setAllItemsDownloaded(true);
                repository.updateParent(parent);
                TEMPChildList.clear();
                setParentWith(parent);
                tempParentName = "";
                idCounter = 0;
                idList = new ArrayList<>();

            }
            else if(!globalDownload){
                Toasty.info(activity, activity.getResources().getString(R.string.download_canceled), Toast.LENGTH_SHORT, true).show();
                TEMPChildList.clear();
                setParentWith(parent);
                tempParentName = "";
                idCounter = 0;
                idList = new ArrayList<>();
            }
            else Toasty.info(activity, activity.getResources().getString(R.string.fail_downloading_part1) +" "+ failCounter +" "+activity.getResources().getString(R.string.fail_downloading_part2), Toast.LENGTH_SHORT, true).show();

            notifyDataSetChanged();
        }
    }

    boolean downloadFile(ChildDataModel child){
        boolean flag = true;
        downloading =true;
        try{
            String DownloadUrl = activity.getResources().getString(R.string.downloadPath)+child.getParent()+"/"+child.getRawname();
            mManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            final DownloadManager.Request mRqRequest = new DownloadManager.Request(
                    Uri.parse(DownloadUrl));
            mRqRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/MordhauSoundboard/"+child.getRawname());
            mRqRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            long idDownLoad=mManager.enqueue(mRqRequest);

            //write download id into the queue
            idList.add(idDownLoad);
            idCounter++;


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

                    int status =c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    if (status==DownloadManager.STATUS_SUCCESSFUL) {

                        downloading = false;
                        flag=true;
                        child.setUrl(Environment.getExternalStorageDirectory()+"/Download/MordhauSoundboard/"+child.getRawname());
                        child.setDownloaded(true);
                        repository.update(child);
                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {

                        downloading = false;
                        flag=false;
                        break;
                    }
                    if(status==DownloadManager.STATUS_PENDING) {
                        try {
                            Thread.sleep(100);
                            counter++;
                            if(counter== 100){

                                return false ;

                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                    }
                }
            }

            return flag;
        }catch (Exception e) {

            System.out.println(e);
            flag = false;
            return flag;
        }
    }

    boolean isAllDownloaded(){
        for(int i = 0;i<TEMPChildList.size();i++){
            if (!TEMPChildList.get(i).isDownloaded()) return false;
        }
        return true;
    }

    void clickDownload(){

        sheet.dismiss();
        ParentDataModel parent = parents.get(sheet.getPosition());
        tempParentName = parent.getName();
        if(!parent.isAllItemsDownloaded()){

            if(parent.isTextDownloaded()){
                new DownloadAllContentAsync().execute(parent);
            }else{
                new getAllContentAsync().execute(parent.getName());
            }
        }else Toasty.warning(activity, activity.getResources().getString(R.string.already_downloaded), Toast.LENGTH_SHORT, true).show();

    }

    void clickDelete(){
        deleteParent(parents.get(sheet.getPosition()));
    }

    void clickCancel(){
        sheet.dismiss();
    }

    ArrayList<ChildDataModel> removeDownloadedItems(ArrayList<ChildDataModel> list){
        ArrayList<ChildDataModel> resultList = new ArrayList<>();
        for(int i = 0;i<list.size();i++){
            if(!list.get(i).isDownloaded()){
                resultList.add(list.get(i));
            }
        }
        return resultList;
    }

}
