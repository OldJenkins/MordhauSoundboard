package com.example.mordhausoundboard;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<ParentDataModel> parentDataModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rv = findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        parentDataModelArrayList = new ArrayList<>();
        insertMockdata();



        RVAdapter adapter = new RVAdapter(parentDataModelArrayList,this);
        rv.setAdapter(adapter);




        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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


    void insertMockdata(){

        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Curelknight)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Englishman)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Knight)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Raziel)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Reginald)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Scot)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Young)));

    }
}
