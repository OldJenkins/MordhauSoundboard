package com.example.mordhausoundboard;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

public class Repository {

    private DaoChildData daoChildData;
    private List<ChildDataModel> mAllChildsByName;

    public Repository(Application application) {
        Database db = Database.getDatabase(application);
        daoChildData = db.personDao();
        mAllChildsByName = daoChildData.getAllChilds();
    }

    List<ChildDataModel> getmAllChildsbyName() {
        return mAllChildsByName;
    }

    public void insert (ChildDataModel child) {
        new insertAsyncTask(daoChildData).execute(child);
    }

    public void update (ChildDataModel child) {
        new UpdateAsyncTask(daoChildData).execute(child);
    }

    public void delete (ChildDataModel child) {
        new deleteAsyncTask(daoChildData).execute(child);
    }

    private static class insertAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private DaoChildData mAsyncTaskDao;

        insertAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private DaoChildData mAsyncTaskDao;

        UpdateAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private DaoChildData mAsyncTaskDao;

        deleteAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

}