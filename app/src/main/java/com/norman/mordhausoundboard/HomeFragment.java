package com.norman.mordhausoundboard;

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

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment{

    private Repository repository;
    private ArrayList<ParentDataModel> parentDataModelArrayList;
    private SharedPreferences prefs;
    private RVAdapter adapter;

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

        adapter = new RVAdapter(parentDataModelArrayList,getActivity(),getParentFragmentManager());
        rv.setAdapter(adapter);
        ((MainActivity)getActivity()).passRVAdapter(adapter);


        return v;
    }

    void initParentData(){
        boolean isAlreadyInserted = prefs.getBoolean(Constants.PARENTINITIALLIZED,false);
        if(isAlreadyInserted) {
            List<ParentDataModel> list = repository.getAllParents();
            if (!list.isEmpty()) {
                parentDataModelArrayList.addAll(list);

            } else {
                fillParentList();
                insertParentListToDb();
                Toasty.warning(getContext(), getContext().getResources().getString(R.string.somethind_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }else{
            fillParentList();
            insertParentListToDb();
        }
    }

    void fillParentList(){
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Plain)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Barbarian)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Cruel)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Commoner)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Knight)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Eager)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Foppish)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Raider)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Young)));
    }

    void insertParentListToDb(){
        for(int i =0;i<parentDataModelArrayList.size();i++){
            repository.insertParent(parentDataModelArrayList.get(i));
        }
        prefs.edit().putBoolean(Constants.PARENTINITIALLIZED,true).apply();
    }



}
