package com.norman.mordhausoundboard;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Repository repository;
    private GridAdapter gridAdapter;
    private TextView tv_status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.content_voicetype, container, false);

        // init Repository for database queries
        repository = new Repository((Application) getActivity().getApplicationContext());

        mLayoutManager = new GridLayoutManager(getContext(),2);
        mRecyclerView = v.findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // is shown when no favourites were found
        tv_status = v.findViewById(R.id.tv_status);

        // fill adapter with entries from database
        RefreshAdapter();


        ((MainActivity)getActivity()).passGridAdapter(gridAdapter);

        return v;
    }

    void RefreshAdapter(){
        List<ChildDataModel> posts = repository.getAllFavourites();
        ArrayList<ChildDataModel> spiele = new ArrayList<>(posts);
        if(spiele.size()>0) {
            gridAdapter = new GridAdapter(removeDataSuffix(spiele), getActivity().getApplicationContext(), 1, getActivity(),getParentFragmentManager());
            mRecyclerView.setAdapter(gridAdapter);
        }else{
            tv_status.setText(getActivity().getResources().getString(R.string.no_favourites));
            tv_status.setVisibility(View.VISIBLE);
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
}
