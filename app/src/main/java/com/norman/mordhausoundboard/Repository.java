package com.norman.mordhausoundboard;

// The Database Repository for both Parent- and ChildModel
// here you can make your async Database Queries , since its a bad behaviour running database calls on the main Thread

import android.app.Application;
import android.os.AsyncTask;
import java.util.List;

class Repository {

    private final DaoChildData daoChildData;
    private final DaoParentData daoParentData;
    private List<ChildDataModel> mAllChildsByName;
    private List<ChildDataModel> mAllFavourites;
    private List<ParentDataModel> mAllParents;
    private String name;

    Repository(Application application) {
        Database db = Database.getDatabase(application);
        daoChildData = db.personDao();
        daoParentData = db.parentDao();
    }

    List<ChildDataModel> getmAllChildsbyName(String name) {
        this.name = name;
        mAllChildsByName = daoChildData.getAllChilds(name);
        return mAllChildsByName;
    }

    List<ChildDataModel> getAllFavourites() {
        mAllFavourites = daoChildData.getAllFavourites();
        return mAllFavourites;
    }

    List<ParentDataModel> getAllParents() {
        mAllParents = daoParentData.getAllParents();
        return mAllParents;
    }

    int getParentCount(){
        return daoParentData.getCount();
    }

    ParentDataModel getParent(String name){
        return daoParentData.getParent(name);
    }

    void insert (ChildDataModel child) {
        new insertAsyncTask(daoChildData).execute(child);
    }

    void insertParent (ParentDataModel parent) {
        new insertParentAsyncTask(daoParentData).execute(parent);
    }

    void update (ChildDataModel child) {
        new UpdateAsyncTask(daoChildData).execute(child);
    }

    void updateParent (ParentDataModel parent) {
        new UpdateParentAsyncTask(daoParentData).execute(parent);
    }

    void delete (ChildDataModel child) {
        new deleteAsyncTask(daoChildData).execute(child);
    }

    void deleteAll () {
        new deleteAllAsyncTask(daoChildData).execute();
    }

    void deleteAllChildsWith (String parent) {
        new deleteAllChildsWithAsyncTask(daoChildData).execute(parent);
    }

    void insertAll (List<ChildDataModel> childList) {new InsertAllAsyncTask(daoChildData).execute(childList);}

    private static class insertAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private final DaoChildData mAsyncTaskDao;

        insertAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class insertParentAsyncTask extends AsyncTask<ParentDataModel, Void, Void> {

        private final DaoParentData mAsyncTaskDao;

        insertParentAsyncTask(DaoParentData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ParentDataModel... params) {
            mAsyncTaskDao.insertParent(params[0]);
            return null;
        }
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<ChildDataModel>, Void, Void> {

        private final DaoChildData mAsyncTaskDao;

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

        private final DaoChildData mAsyncTaskDao;

        UpdateAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateParentAsyncTask extends AsyncTask<ParentDataModel, Void, Void> {

        private final DaoParentData mAsyncTaskDao;

        UpdateParentAsyncTask(DaoParentData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ParentDataModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private final DaoChildData mAsyncTaskDao;

        deleteAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<ChildDataModel, Void, Void> {

        private final DaoChildData mAsyncTaskDao;

        deleteAllAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ChildDataModel... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private static class deleteAllChildsWithAsyncTask extends AsyncTask<String, Void, Void> {

        private final DaoChildData mAsyncTaskDao;

        deleteAllChildsWithAsyncTask(DaoChildData dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteAllChildsWith(params[0]);
            return null;
        }
    }
}