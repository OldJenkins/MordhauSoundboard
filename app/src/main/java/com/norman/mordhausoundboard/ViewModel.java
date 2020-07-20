package com.norman.mordhausoundboard;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

public class ViewModel extends AndroidViewModel {
    private Repository mRepository;
    private List<ChildDataModel> mAllChildsByName;
    private String name;

    public ViewModel(Application application) {
        super(application);
        mRepository = new Repository(application);
        mAllChildsByName = mRepository.getmAllChildsbyName(name);
    }

    List<ChildDataModel> getmAllChildsByName(String name) { return mAllChildsByName; }

    public void insert(ChildDataModel child) { mRepository.insert(child); }

    public void delete(ChildDataModel child) {mRepository.delete(child);}

}
