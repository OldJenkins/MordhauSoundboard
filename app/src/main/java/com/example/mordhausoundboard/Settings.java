package com.example.mordhausoundboard;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings extends AppCompatActivity implements BottomSheet.BottomSheetListener {

    SharedPreferences prefs;
    Repository repository;
    Button btn_clear;
    Button btn_dialog;
    Button btn_privacy;
    TextView tv_clear_info;
    long foldersize_mb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TitleFont);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        repository = new Repository(getApplication());
        prefs = getSharedPreferences(Constants.PREFS,0);

        btn_clear = findViewById(R.id.btn_clear);
        tv_clear_info = findViewById(R.id.tv_clear_info);
        btn_dialog = findViewById(R.id.btn_dialog);

        btn_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheet sheet = new BottomSheet(1);
                sheet.show(getSupportFragmentManager(),"bottomsheet");

            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(foldersize_mb>0) {
                   final Dialog dialog = new Dialog(Settings.this);
                   dialog.setContentView(R.layout.dialog_clear_storage);
                   Button btn_yes = dialog.findViewById(R.id.yes);
                   Button btn_no = dialog.findViewById(R.id.no);

                   btn_yes.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           List<ParentDataModel> parentDataModels = repository.getAllParents();
                           for (int i = 0; i < parentDataModels.size(); i++) {
                               deleteParent(parentDataModels.get(i));
                           }
                           //TODO STRINGS
                           Toast.makeText(Settings.this, "Storage was cleared", Toast.LENGTH_SHORT).show();
                           initFolderSize();
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
                   Toast.makeText(Settings.this, " Storage is already empty", Toast.LENGTH_SHORT).show();
               }
            }
        });
        initFolderSize();

    }


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Settings.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void initFolderSize(){
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/Download/MordhauSoundboard/");
        long folder_size= getFolderSize(file);
        foldersize_mb = folder_size/1000000;
        tv_clear_info.setText(String.valueOf(foldersize_mb)+ "mb");
    }


    boolean deleteParent(ParentDataModel parent){

        List<ChildDataModel> childList = repository.getmAllChildsbyName(parent.getName());

        for(int i = 0;i<childList.size();i++){
            ChildDataModel child = childList.get(i);
            if(child.isDownloaded()) {
                File file = new File(child.getUrl());
                boolean isDeleted = file.delete();

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
        return true;
    }

    @Override
    public void onButtonClicked(int which) {
        Toast.makeText(this, "What the shit ?" + which, Toast.LENGTH_SHORT).show();
    }
}
