package com.example.mordhausoundboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Home extends Fragment {

    private ArrayList<ParentDataModel> parentDataModelArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.content_main, container, false);

        RecyclerView rv = v.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        parentDataModelArrayList = new ArrayList<>();
        insertMockdata();

        RVAdapter adapter = new RVAdapter(parentDataModelArrayList,getActivity());
        rv.setAdapter(adapter);

        return v;
    }

    void insertMockdata(){
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Bernard)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Curelknight)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Englishman)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Knight)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Raziel)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Reginald)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Scot)));
        parentDataModelArrayList.add(new ParentDataModel(getResources().getString(R.string.Young)));
    }

}
