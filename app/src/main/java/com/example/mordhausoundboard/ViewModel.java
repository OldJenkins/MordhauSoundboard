package com.example.mordhausoundboard;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

public class ViewModel extends AndroidViewModel {
    private Repository mRepository;
    private List<ChildDataModel> mAllChildsByName;

    public ViewModel(Application application) {
        super(application);
        mRepository = new Repository(application);
        mAllChildsByName = mRepository.getmAllChildsbyName();
    }

    List<ChildDataModel> getmAllChildsByName() { return mAllChildsByName; }

    public void insert(ChildDataModel child) { mRepository.insert(child); }

    public void delete(ChildDataModel child) {mRepository.delete(child);}

}
