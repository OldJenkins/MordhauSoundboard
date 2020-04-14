package com.example.mordhausoundboard;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;

public class Favourites extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout pullToRefresh;
    private boolean isRefresh;
    private Repository repository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.content_voicetype, container, false);
        repository = new Repository((Application) getActivity().getApplicationContext());
        mLayoutManager = new GridLayoutManager(getContext(),2);

        pullToRefresh = v.findViewById(R.id.pullToRefresh);
        mRecyclerView = v.findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                RefreshAdapter();
                pullToRefresh.setRefreshing(false);
            }
        });

        RefreshAdapter();

        return v;
    }

    void RefreshAdapter(){
        List<ChildDataModel> posts = repository.getmAllChildsbyName();
        ArrayList<ChildDataModel> spiele = new ArrayList<>(posts);
        mRecyclerView.setAdapter(new GridAdapter(removeDataSuffix(spiele),getActivity().getApplicationContext(),1));
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
