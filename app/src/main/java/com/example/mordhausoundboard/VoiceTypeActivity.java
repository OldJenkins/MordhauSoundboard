package com.example.mordhausoundboard;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class VoiceTypeActivity extends AppCompatActivity {

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
                adapter = new GridAdapter(removeDataSuffix(spiele), getApplicationContext(), 1);
                mRecyclerView.setAdapter(adapter);

            }
        }

        if(!parent.isAllItemsDownloaded()&&!parent.isAskedForDownload()){

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
            int howManyItems = 0;
            if(adapter != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (!adapter.getItem(i).isDownloaded()) {
                        howManyItems++;
                    }
                }
                Toast.makeText(this, "all items are downloaded " + howManyItems, Toast.LENGTH_SHORT).show();
                handleDownloadByNMobileData(howManyItems);
            }

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

    public class getAllContentAsync extends AsyncTask<String,Void,String> {
        String name;
        String json_url;
        String JSON_STRING;


        @Override
        protected void onPreExecute() {
            json_url = getResources().getString(R.string.downloadPath)+"getDirectorycontent.php";


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

                    List<ChildDataModel> posts = gson.fromJson(result, listType);
                    ArrayList<ChildDataModel> spiele = new ArrayList<>(posts);
                    adapter = new GridAdapter(removeDataSuffix(spiele),getApplicationContext(),1);
                    mRecyclerView.setAdapter(adapter);

                    prefs.edit().putString(Constants.DOWNLOADLIST,prefs.getString(Constants.DOWNLOADLIST,"")+"%"+name).apply();

                    repository.insertAll(posts);

                    if(!parent.isAllItemsDownloaded()&&!parent.isAskedForDownload()){
                        handleDownloadByNMobileData(adapter.getItemCount());
                    }



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

    public class DownloadAllContentAsync extends AsyncTask<String,Void,String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(VoiceTypeActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //TODO STRINGS!!!!!!!!!!!!!
            dialog.setMessage("Downloading the Soundata to the local Storage, to minimize Data traffic");
            dialog.setTitle("Downloading...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            int amountOfFiles = Integer.parseInt(params[0]);
            dialog.setMax(amountOfFiles);

                for(int i = 0;i<adapter.getItemCount();i++){
                    ChildDataModel model = adapter.getAllItems().get(i);
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
                Toast.makeText(VoiceTypeActivity.this, "All items Downloaded", Toast.LENGTH_SHORT).show();
                parent.setAllItemsDownloaded(true);
                repository.updateParent(parent);
                adapter.notifyDataSetChanged();
            }
            else Toast.makeText(VoiceTypeActivity.this, "Could not download all items", Toast.LENGTH_SHORT).show();
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



        DownloadManager manager1 = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            System.out.println("success downloading: "+DownloadUrl);


            child.setUrl(Environment.getExternalStorageDirectory()+"/Download/MordhauSoundboard/"+child.getRawname());
            child.setDownloaded(true);
            repository.update(child);
        }
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
                        new DownloadAllContentAsync().execute(String.valueOf(adapter.getItemCount()));
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
                        new DownloadAllContentAsync().execute(String.valueOf(howManyItems));
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
}
