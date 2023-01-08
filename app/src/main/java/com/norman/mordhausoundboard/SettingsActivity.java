package com.norman.mordhausoundboard;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences prefs;
    Repository repository;
    Button btn_clear;
    Button btn_privacy;
    TextView tv_clear_info;
    long foldersize_mb;
    long folder_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TitleFont);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        repository = new Repository(getApplication());
        prefs = getSharedPreferences(Constants.PREFS,0);

        btn_clear = findViewById(R.id.btn_clear);
        tv_clear_info = findViewById(R.id.tv_clear_info);
        btn_privacy = findViewById(R.id.btn_privacy);

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(folder_size>0) {
                   final Dialog dialog = new Dialog(SettingsActivity.this);
                   dialog.setContentView(R.layout.dialog_clear_storage);
                   Button btn_yes = dialog.findViewById(R.id.yes);
                   Button btn_no = dialog.findViewById(R.id.no);

                   btn_yes.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {

                           deleteRecursive(new File(Environment.getExternalStorageDirectory().getPath()+"/Download/MordhauSoundboard/"));

                           List<ParentDataModel> parentDataModels = repository.getAllParents();
                           for (int i = 0; i < parentDataModels.size(); i++) {
                               parentDataModels.get(i).setAllItemsDownloaded(false);
                               updateAllChilds(parentDataModels.get(i));
                           }

                           Toasty.success(SettingsActivity.this, getResources().getString(R.string.storage_cleared), Toast.LENGTH_SHORT).show();
                           updateFolderSize();
                           dialog.dismiss();
                       }
                   });

                   btn_no.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           dialog.dismiss();
                       }
                   });

                   dialog.show();
               }else{
                   Toast.makeText(SettingsActivity.this, getResources().getString(R.string.storage_empty), Toast.LENGTH_SHORT).show();
               }
            }
        });

        btn_privacy.setOnClickListener(v -> {
            String url = getResources().getString(R.string.fileServerUrl)+"privacy_polocies.txt";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        updateFolderSize();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeFragment/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    // calculate size of the currently used storage by this app
    public static long getFolderSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                // System.out.println(file.getName() + " " + file.length());
                size += file.length();
            } else
                size += getFolderSize(file);
        }
        return size;
    }

    // update the textview which is showing the used storage
    void updateFolderSize() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/MordhauSoundboard/");
        if (file.exists()) {
            folder_size = getFolderSize(file);
            foldersize_mb = folder_size / 1000000;
        }else{
            foldersize_mb = 0;
        }
        tv_clear_info.setText(foldersize_mb + "mb");
    }

    // update the files that they are not downloaded anymore
    boolean updateAllChilds(ParentDataModel parent){

        List<ChildDataModel> childList = repository.getmAllChildsbyName(parent.getName());

        for(int i = 0;i<childList.size();i++){
            ChildDataModel child = childList.get(i);
            if(child.isDownloaded()) {
                child.setUrl(getResources().getString(R.string.fileServerUrl)+child.getParent()+"/"+child.getRawname());
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
        return true;
    }

    // delete all files from the storage
    public void deleteRecursive(File fileOrDirectory) {
        repository.deleteAll();
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

}
