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

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Settings extends AppCompatActivity {

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
                   final Dialog dialog = new Dialog(Settings.this);
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

                           Toasty.success(Settings.this, getResources().getString(R.string.storage_cleared), Toast.LENGTH_SHORT).show();
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
                   Toast.makeText(Settings.this, getResources().getString(R.string.storage_empty), Toast.LENGTH_SHORT).show();
               }
            }
        });



        updateFolderSize();

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

    void updateFolderSize() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/MordhauSoundboard/");
        if (file.exists()) {
            folder_size = getFolderSize(file);
            foldersize_mb = folder_size / 1000000;
        }else{
            foldersize_mb = 0;
        }
        tv_clear_info.setText(String.valueOf(foldersize_mb) + "mb");
    }

    boolean updateAllChilds(ParentDataModel parent){

        List<ChildDataModel> childList = repository.getmAllChildsbyName(parent.getName());

        for(int i = 0;i<childList.size();i++){
            ChildDataModel child = childList.get(i);
            if(child.isDownloaded()) {
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

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    void downLoadTest(){


        //showDownloadDialog();

        List<ParentDataModel> parentDataModels = repository.getAllParents();
        final List<ChildDataModel> childList = repository.getmAllChildsbyName(parentDataModels.get(1).getName());
        for(int i = 0;i<childList.size();i++){
            final int finalI = i;
            String target = Environment.DIRECTORY_DOWNLOADS+"/MordhauSoundboard/";
            String filename = childList.get(i).getRawname();
            String url = getResources().getString(R.string.downloadPath)+parentDataModels.get(1).getName()+"/"+filename;


            System.out.println("TARGET --> " + target );
            System.out.println("URL --> "+ url);
            System.out.println("FILENAME --> "+filename);


            int downloadId = PRDownloader.download(url, Environment.DIRECTORY_DOWNLOADS+"/MordhauSoundboard/", childList.get(i).getRawname())
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {

                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {

                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {

                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            System.out.println(" Downloaded "+progress.currentBytes + " of "+ progress.totalBytes);
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            System.out.println(childList.get(finalI).getName() + " runtergeladen :)");
                        }

                        @Override
                        public void onError(Error error) {
                            System.out.println(childList.get(finalI).getName() + " abgekackt :( with error: "+error.isServerError());

                        }
                    });
        }
    }

    /*
    void showDownloadDialog(int size){
        testdialog = new Dialog(Settings.this);
        testdialog.setContentView(R.layout.testdowloaddialog);
        Button cancel = testdialog.findViewById(R.id.cancel);
        Button pause = testdialog.findViewById(R.id.pause);
        bar = testdialog.findViewById(R.id.progressBar);
        progressMax = testdialog.findViewById(R.id.progressMax);
        progressValue = testdialog.findViewById(R.id.progressValue);

        bar.setMax(size);
        progressMax.setText(String.valueOf(size));
        progressValue.setText(String.valueOf(0));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testdialog.dismiss();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        testdialog.show();
        System.out.println("Showing dialog");
    }

     */

/*
    void downloadZiontest(){
        List<ParentDataModel> parentDataModels = repository.getAllParents();
        final List<ChildDataModel> childList = repository.getmAllChildsbyName(parentDataModels.get(1).getName());
        //showDownloadDialog(childList.size());


        for(int i = 0;i<childList.size();i++) {
            System.out.println("Loop "+i);
            String target = Environment.DIRECTORY_DOWNLOADS + "/MordhauSoundboard/";
            String filename = childList.get(i).getRawname();
            String url = getResources().getString(R.string.downloadPath) + parentDataModels.get(1).getName() + "/" + filename;


            ZionDownloadFactory factory = new ZionDownloadFactory(this, url, childList.get(i).getRawname());
            DownloadFile downloadFile = factory.downloadFile(FILE_TYPE.MP3);
            final int finalI = i;

            downloadFile.start(new ZionDownloadListener() {
                @Override
                public void OnSuccess(String dataPath) {
                    // the file saved in your device..
                    //dataPath--> android/{your app package}/files/Download
                    System.out.println(childList.get(0).getName() + " success with path: " + dataPath);
                    progress++;
                    progressValue.setText(String.valueOf(progress));
                    bar.setProgress(progress);
                }

                @Override
                public void OnFailed(String message) {
                    System.out.println(childList.get(0).getName() + " abgekackt :( with error: " + message);
                }

                @Override
                public void OnPaused(String message) {
                    System.out.println("i am pausing "+ finalI);
                }

                @Override
                public void OnPending(String message) {
                    System.out.println("i am pending at "+ finalI);
                }

                @Override
                public void OnBusy() {
                    System.out.println("i am busy at "+ finalI);
                }
            });

        }

    }
*/

/*
    public class DownloadAllContentAsync extends AsyncTask<ParentDataModel,Void,String> {
        Dialog testdialog;

        @Override
        protected void onPreExecute() {
            testdialog = new Dialog(Settings.this);
            testdialog.setContentView(R.layout.testdowloaddialog);
            Button cancel = testdialog.findViewById(R.id.cancel);
            Button pause = testdialog.findViewById(R.id.pause);
            bar = testdialog.findViewById(R.id.progressBar);
            progressMax = testdialog.findViewById(R.id.progressMax);
            progressValue = testdialog.findViewById(R.id.progressValue);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testdialog.dismiss();
                }
            });
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            testdialog.show();

            System.out.println("Showing dialog");
        }

        @Override
        protected String doInBackground(ParentDataModel... params) {

            List<ParentDataModel> parentDataModels = repository.getAllParents();
            final List<ChildDataModel> childList = repository.getmAllChildsbyName(parentDataModels.get(1).getName());
            bar.setMax(childList.size());
            progressMax.setText(String.valueOf(childList.size()));
            progressValue.setText(String.valueOf(0));


            downloadZiontest();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Toasty.success(Settings.this, "alles runtergeladen", Toast.LENGTH_SHORT).show();
            progress = 0;
            testdialog.dismiss();
        }

    }
*/

}
