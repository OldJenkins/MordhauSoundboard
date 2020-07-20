package com.norman.mordhausoundboard;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
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

public class VoiceTypeActivity extends AppCompatActivity implements BottomSheet.BottomSheetListener {

    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    String name;
    boolean isRefresh;
    SharedPreferences prefs;
    boolean isListAlreadyDownloaded;
    private Repository repository;
    boolean isFileDownloaded;
    GridAdapter adapter;
    ParentDataModel parent;
    private DownloadManager mManager;
    private boolean downloading = true;
    private boolean globalDownload = true;
    private ArrayList<Long> idList;
    private int idCounter = 0;
    private ArrayList<ChildDataModel> TEMPChildList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicetype);
        Toolbar toolbar = findViewById(R.id.toolbar);


        name = getIntent().getStringExtra(Constants.ITEMNAME);
        toolbar.setTitle(name);
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TitleFont);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        idList = new ArrayList<>();

        prefs = getApplicationContext().getSharedPreferences(Constants.PREFS,0);
        repository = new Repository((Application) getApplicationContext());
        parent = repository.getParent(name);


        // return TRUE if the Parent name list, is inside of the Saved List
        isListAlreadyDownloaded = prefs.getString(Constants.DOWNLOADLIST,"").contains(name);

        mLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        //mRecyclerView.setAdapter(new GridAdapter(SoundItemList,getApplicationContext()));

        //if The List was already downloaded before, the List wil be catched from the Database
        if(!isListAlreadyDownloaded) {
            new getAllContentAsync().execute(name);

        }else{
            List<ChildDataModel> list = repository.getmAllChildsbyName(name);
            ArrayList<ChildDataModel> spiele = new ArrayList<>(list);
            if(spiele.size()>0) {
                adapter = new GridAdapter(removeDataSuffix(spiele), getApplicationContext(), 0,this, getSupportFragmentManager());
                mRecyclerView.setAdapter(adapter);

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_voicetype, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            if(!parent.isAllItemsDownloaded()) {
                int howManyItems = 0;
                if (adapter != null) {
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (!adapter.getItem(i).isDownloaded()) {
                            howManyItems++;
                        }
                    }
                    handleDownloadByNMobileData(howManyItems);
                }
            }else Toasty.info(this, getResources().getString(R.string.items_already_downloaded), Toast.LENGTH_SHORT).show();
            //TODO IMPLEMENT DIALOG TO SAY FAVOURITES ARE DELETED
            return true;
        }
        if (id == R.id.action_delete) {
            final Dialog dialog = new Dialog(this);
            dialog.setTitle(getResources().getString(R.string.clear_storage));
            dialog.setContentView(R.layout.dialog_delete);
            Button delete = dialog.findViewById(R.id.btn_delete);
            Button cancel = dialog.findViewById(R.id.btn_cancel);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteParent(parent);
                    dialog.dismiss();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(VoiceTypeActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onButtonClicked(int which) {
        switch(which){
            case 0:
                adapter.clickDownload();
                break;
            case 1:
                adapter.clickShare();
                break;
            case 2:
                adapter.clickDelete();
                break;
            case 3:
                adapter.clickCancel();
                break;
        }

    }

    public class getAllContentAsync extends AsyncTask<String,Void,String> {
        String name;
        String json_url;
        String JSON_STRING;

        @Override
        protected void onPreExecute() {
            json_url = getResources().getString(R.string.rootPath)+"getDirectorycontent.php";

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
                    ArrayList<ChildDataModel> newArrayList = new ArrayList<>(newList);

                    List<ChildDataModel> oldList = repository.getmAllChildsbyName(name);

                    // Checks if the new item list is equal to the existing one to get new added Files
                    for(int i = 0;i < newList.size();i++){
                        for(int j  = 0;j<oldList.size();j++){
                            if(newList.get(i).AlmostEquals(oldList.get(j))){
                                newList.get(i).setChild(oldList.get(i));
                            }
                        }
                    }

                    repository.insertAll(newList);

                    prefs.edit().putString(Constants.DOWNLOADLIST,prefs.getString(Constants.DOWNLOADLIST,"")+"%"+name).apply();
                    adapter = new GridAdapter(removeDataSuffix(newArrayList),getApplicationContext(),0,VoiceTypeActivity.this,getSupportFragmentManager());
                    mRecyclerView.setAdapter(adapter);

                }

            }else{
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.coord_VoiceType), R.string.fetching_error, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            isRefresh = false;
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
            dialog = new ProgressDialog(VoiceTypeActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            //TODO STRINGS!!!!!!!!!!!!!
            dialog.setMessage(getResources().getString(R.string.downloading_message));
            dialog.setTitle("Downloading...");
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    int i = 0;
                    dialog.dismiss();//dismiss dialog
                    downloading = false;
                    globalDownload = false;
                    while(i<idList.size()){
                        //removing Elements out of the downloading queue

                        mManager.remove(idList.get(i));

                        i++;
                    }
                    Toasty.info(VoiceTypeActivity.this, getResources().getString(R.string.download_canceled), Toast.LENGTH_SHORT, true).show();
                    adapter.notifyDataSetChanged();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.background), new DialogInterface.OnClickListener() {
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
                mManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                while(i<TEMPChildList.size() && globalDownload) {
                    System.out.println("Initializiing the download while downloading: "+downloading +" at pos: "+i);

                    ChildDataModel model = TEMPChildList.get(i);
                    System.out.println("Downloading +"+model.getName());
                    if (!model.isDownloaded()) {

                        if(!downloadFile(model,mManager)) {
                            failCounter++;
                            System.out.println("Downloading +"+model.getName() + " failed | faliures: "+failCounter);
                        }else{
                            System.out.println("Successfully downloaded "+model.getName());
                            model.setDownloaded(true);
                            repository.update(model);
                        }
                        dialog.incrementProgressBy(1);
                    }else{
                        System.out.println("skipped "+model.getName()+" is already downloaded");
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
            adapter.notifyDataSetChanged();
        }

    }

    boolean downloadFile(ChildDataModel child,DownloadManager mManager){
        boolean flag = true;
        downloading =true;
        try{
            String DownloadUrl = getResources().getString(R.string.downloadPath)+child.getParent()+"/"+child.getRawname();


            final DownloadManager.Request mRqRequest = new DownloadManager.Request(
                    Uri.parse(DownloadUrl));
            mRqRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/MordhauSoundboard/"+child.getRawname());
            //mRqRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            long idDownLoad=mManager.enqueue(mRqRequest);

            //write download id into the queue
            idList.add(idDownLoad);
            idCounter++;
            System.out.println("Saved "+ idDownLoad + " to download queue at " +idCounter);

            DownloadManager.Query query;
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
                        updateChildInAdapter(child);
                        repository.update(child);
                        break;
                    }
                    if (status==DownloadManager.STATUS_FAILED) {
                        System.out.println("Status is critically failed");
                        downloading = false;
                        flag=false;
                        break;
                    }
                    if(status==DownloadManager.STATUS_PENDING) {
                        try {
                            Thread.sleep(10);
                            counter++;
                            if(counter== 1000){
                                System.out.println("Download Timed out");
                                return false ;

                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }

            return flag;
        }catch (Exception e) {
            System.out.println("Download has to be catched");
            System.out.println(e);
            flag = false;
            return flag;
        }
    }


    ArrayList<ChildDataModel> removeDataSuffix(ArrayList<ChildDataModel> that){
        for (int i =  0; i<that.size();i++){
            String str = that.get(i).getName();
            if (null != str && str.length() > 0 )
            {
                int endIndex = str.lastIndexOf(".");
                if (endIndex != -1)
                {
                    str = str.replace("_"," ");
                    that.get(i).setName(str.substring(0, endIndex));
                }
            }
        }
        return that;
    }

    private boolean checkWifiStatePermission()
    {
        String permission = Manifest.permission.ACCESS_WIFI_STATE;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    boolean isAllDownloaded(){
        for(int i = 0;i<adapter.getAllItems().size();i++){
            if (!adapter.getItem(i).isDownloaded()) return false;
        }
        return true;
    }

    void handleDownloadByNMobileData(final int howManyItems){
        //if(checkWifiStatePermission()) {
            //Toast.makeText(this, "permission is granted", Toast.LENGTH_SHORT).show();
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


            assert mWifi != null;
            if (mWifi.isConnected()) {


                final Dialog dialog = new Dialog(VoiceTypeActivity.this);
                dialog.setContentView(R.layout.dialog_download_check);

                Button yes = dialog.findViewById(R.id.yes);
                Button no = dialog.findViewById(R.id.no);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        new DownloadAllContentAsync().execute(parent);
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                // Do whatever

            } else {

                final Dialog dialog = new Dialog(VoiceTypeActivity.this);
                dialog.setContentView(R.layout.dialog_internet_check);
                Button yes = dialog.findViewById(R.id.yes);
                Button no = dialog.findViewById(R.id.no);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        new DownloadAllContentAsync().execute(parent);
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
       //}else Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
    }

    void deleteParent(ParentDataModel parent){

        List<ChildDataModel> childList = repository.getmAllChildsbyName(parent.getName());

        for(int i = 0;i<childList.size();i++){
            ChildDataModel child = childList.get(i);
            if(child.isDownloaded()) {
                File file = new File(child.getUrl());
                file.delete();

                child.setUrl(getResources().getString(R.string.downloadPath)+child.getParent()+"/"+child.getRawname());
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
        Toasty.success(VoiceTypeActivity.this, getResources().getString(R.string.deleted_content)+" "+ parent.getName() , Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
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

    void updateChildInAdapter(ChildDataModel newChild){
        for(int i = 0;i<adapter.getItemCount();i++){
            if(adapter.getItem(i).getRawname().equals(newChild.getRawname())){
                adapter.getItem(i).setChild(newChild);
            }
        }
    }
}
