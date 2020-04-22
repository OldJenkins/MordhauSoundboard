package com.example.mordhausoundboard;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    Repository repository;
    private ArrayList<ParentDataModel> parentDataModelArrayList;
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.content_main, container, false);

        repository = new Repository((Application) getActivity().getApplicationContext());
        prefs = getActivity().getSharedPreferences(Constants.PREFS,0);

        RecyclerView rv = v.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        parentDataModelArrayList = new ArrayList<>();
        initParentData();

        RVAdapter adapter = new RVAdapter(parentDataModelArrayList,getActivity());
        rv.setAdapter(adapter);

        return v;
    }

    void initParentData(){
        boolean isAlreadyInserted = prefs.getBoolean(Constants.PARENTINITIALLIZED,false);
        if(isAlreadyInserted) {
            List<ParentDataModel> list = repository.getAllParents();
            if (!list.isEmpty()) {
                parentDataModelArrayList.addAll(repository.getAllParents());

            } else {
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Bernard)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Barbarian)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Curelknight)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Englishman)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Knight)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Raziel)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Reginald)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Scot)));
                parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Young)));


                for(int i =0;i<parentDataModelArrayList.size();i++){

                    repository.insertParent(parentDataModelArrayList.get(i));
                }
                prefs.edit().putBoolean(Constants.PARENTINITIALLIZED,true).apply();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Bernard)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Barbarian)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Curelknight)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Englishman)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Knight)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Raziel)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Reginald)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Scot)));
            parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Young)));


            for(int i =0;i<parentDataModelArrayList.size();i++){

                repository.insertParent(parentDataModelArrayList.get(i));
            }
            prefs.edit().putBoolean(Constants.PARENTINITIALLIZED,true).apply();
        }
    }

}
