package com.example.mordhausoundboard;

import android.app.Application;
import android.os.AsyncTask;
import java.util.List;

class Repository {

    private DaoChildData daoChildData;
    private List<ChildDataModel> mAllChildsByName;
    private String name;

    Repository(Application application) {
        Database db = Database.getDatabase(application);
        daoChildData = db.personDao();

    }

    List<ChildDataModel> getmAllChildsbyName(String name) {
        this.name = name;
        mAllChildsByName = daoChildData.getAllChilds(name);
        return mAllChildsByName;
    }

    void insert (ChildDataModel child) {
        new insertAsyncTask(daoChildData).execute(child);
    }

    void update (ChildDataModel child) {
        new UpdateAsyncTask(daoChildData).execute(child);
    }

    void delete (ChildDataModel child) {
        new deleteAsyncTask(daoChildData).execute(child);
    }

    void insertAll (List<ChildDataModel> childList) {new InsertAllAsyncTask(daoChildData).execute(childList);}

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

    private static class InsertAllAsyncTask extends AsyncTask<List<ChildDataModel>, Void, Void> {

        private DaoChildData mAsyncTaskDao;

        InsertAllAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<ChildDataModel>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
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