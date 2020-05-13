package com.example.mordhausoundboard;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> implements BottomSheet.BottomSheetListener {

    private ArrayList<ParentDataModel> parents;
    private Activity activity;
    private BottomSheet sheet;
    private Repository repository;
    private SharedPreferences prefs;
    private int TEMPNumberOfFiles = 0;
    private ArrayList<ChildDataModel> TEMPChildList;
    FragmentManager fragmentManager;

    RVAdapter(ArrayList<ParentDataModel> parents, Activity activity, FragmentManager fragmentManager){
        this.parents = parents;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        repository = new Repository(activity.getApplication());
        prefs = activity.getSharedPreferences(Constants.PREFS,0);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_parent, parent, false);
        return new PersonViewHolder(v);
    }

    ParentDataModel getParentWith(String name){
        for(int i = 0;i<parents.size();i++) {
            if (parents.get(i).getName().equals(name)) {
                return parents.get(i);
            }
        }
        return null;
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
        if(!parents.get(position).isAllItemsDownloaded()){
            holder.isDownloaded.setVisibility(View.INVISIBLE);
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


                sheet = new BottomSheet(position);
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

    ParentDataModel getItem(int po){
        return parents.get(po);
    }

    @Override
    public void onButtonClicked(int text) {
        switch (text) {

            case 1:
                new getAllContentAsync().execute(parents.get(sheet.getPosition()).getName());
                break;

            case 2:
                deleteParent(parents.get(sheet.getPosition()));
                break;

            case 3:
                break;
        }
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
        Toast.makeText(activity, "deleted Content of "+ parent.getName() , Toast.LENGTH_SHORT).show();
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
                                if(oldList.get(j).isFavourite()){
                                    Toast.makeText(activity, oldList.get(j).getName()+ " gude", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    }


                    repository.insertAll(newList);

                    TEMPChildList = new ArrayList<>(newList);
                    TEMPNumberOfFiles = TEMPChildList.size();

                    new DownloadAllContentAsync().execute(name);
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

    public class DownloadAllContentAsync extends AsyncTask<String,Void,String> {
        ProgressDialog dialog;
        int position;
        String name;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(activity);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //TODO STRINGS!!!!!!!!!!!!!
            dialog.setCancelable(true);
            dialog.setMessage("Downloading the Soundata to the local Storage, to minimize Data traffic");
            dialog.setTitle("Downloading...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            name = params[0];

            dialog.setMax(TEMPChildList.size());

            for(int i = 0;i<TEMPChildList.size();i++){
                ChildDataModel model = TEMPChildList.get(i);
                if(!model.isDownloaded()) {
                    downloadFile(model);
                    dialog.incrementProgressBy(1);
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
            if(isAllDownloaded()){
                ParentDataModel parent = getParentWith(name);
                Toast.makeText(activity, "All items Downloaded", Toast.LENGTH_SHORT).show();
                parent.setAllItemsDownloaded(true);
                repository.updateParent(parent);
                TEMPChildList.clear();
                TEMPNumberOfFiles = 0;
                setParentWith(parent);
                notifyDataSetChanged();
            }
            else Toast.makeText(activity, "Could not download all items", Toast.LENGTH_SHORT).show();
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
        //request1.setDestinationInExternalFilesDir(getApplicationContext(), "/File", child.getRawname());
        //request1.setDestinationInExternalFilesDir(VoiceTypeActivity.this, "/Hurensohn/", child.getRawname());


        /*get the path to internal storage*/
        File path = Environment.getExternalStorageDirectory();

        request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/MordhauSoundboard/"+child.getRawname());

        /*code to edit the file*/



        DownloadManager manager1 = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            System.out.println("success downloading: "+DownloadUrl);


            child.setUrl(Environment.getExternalStorageDirectory()+"/Download/MordhauSoundboard/"+child.getRawname());
            child.setDownloaded(true);
            repository.update(child);
        }
    }

    boolean isAllDownloaded(){
        for(int i = 0;i<TEMPChildList.size();i++){
            if (!TEMPChildList.get(i).isDownloaded()) return false;
        }
        return true;
    }
}
