package com.example.mordhausoundboard;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class VoiceTypeActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    String name;
    SwipeRefreshLayout pullToRefresh;
    boolean isRefresh;
    SharedPreferences prefs;
    boolean isListAlreadyDownloaded;
    private Repository repository;

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

        pullToRefresh = findViewById(R.id.pullToRefresh);

        prefs = getApplicationContext().getSharedPreferences(Constants.PREFS,0);
        repository = new Repository((Application) getApplicationContext());

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
            Toast.makeText(this, "taken from FTP - " + name, Toast.LENGTH_SHORT).show();

        }else{
            List<ChildDataModel> list = repository.getmAllChildsbyName(name);
            ArrayList<ChildDataModel> spiele = new ArrayList<>(list);
            if(spiele.size()>0) {
                mRecyclerView.setAdapter(new GridAdapter(removeDataSuffix(spiele), getApplicationContext(), 1));
                Toast.makeText(this, "taken from Database", Toast.LENGTH_SHORT).show();
            }
        }

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                new getAllContentAsync().execute(name);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class getAllContentAsync extends AsyncTask<String,Void,String> {
        String name;
        String json_url;
        String JSON_STRING;

        @Override
        protected void onPreExecute() {
            pullToRefresh.setRefreshing(true);
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
                    mRecyclerView.setAdapter(new GridAdapter(removeDataSuffix(spiele),getApplicationContext(),1));
                    prefs.edit().putString(Constants.DOWNLOADLIST,"%"+name).apply();

                    repository.insertAll(posts);
                }

            }else{
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.coord_VoiceType), R.string.fetching_error, Snackbar.LENGTH_LONG);
                snackbar.show();
            }



            pullToRefresh.setRefreshing(false);
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
}
