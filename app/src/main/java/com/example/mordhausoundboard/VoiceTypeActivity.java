package com.example.mordhausoundboard;

import android.content.Context;
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
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class VoiceTypeActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context context;
    private List<ChildDataModel> SoundItemList = new ArrayList<>();
    String name;
    SwipeRefreshLayout pullToRefresh;
    boolean isRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicetype);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = getIntent().getStringExtra("NAME");
        pullToRefresh = findViewById(R.id.pullToRefresh);





        mLayoutManager = new GridLayoutManager(context,2);
        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.setAdapter(new GridAdapter(SoundItemList,getApplicationContext()));

        new getAllContentAsync().execute(name,getResources().getString(R.string.downloadPath));


        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                isRefresh = true;
                new getAllContentAsync().execute(name,getResources().getString(R.string.downloadPath));


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

    ArrayList<String> convertStringToArray(String convertable){

        String[]splitted = convertable.split("%");
        return new ArrayList( Arrays.asList( splitted ) );
    }





    public class getAllContentAsync extends AsyncTask<String,Void,String> {

        String name;
        String json_url;
        String JSON_STRING;


        @Override
        protected void onPreExecute() {


            pullToRefresh.setRefreshing(true);

        }

        @Override
        protected String doInBackground(String... params) {

            name = params[0];
            json_url = params[1];
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

        //hallo

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
                    mRecyclerView.setAdapter(new GridAdapter(spiele,getApplicationContext()));




                }

            }else{
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.coordHome), R.string.fetching_error, Snackbar.LENGTH_LONG);
                snackbar.show();
            }


            pullToRefresh.setRefreshing(false);
            isRefresh = false;

        }


        public boolean isJSONValid(String test) {
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


}
