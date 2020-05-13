package com.example.mordhausoundboard;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface DaoChildData {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChildDataModel child);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ChildDataModel> list);

    @Query("DELETE FROM Child_Table")
    void deleteAll();

    @Query("SELECT * from Child_Table WHERE parent = :name ORDER BY name DESC")
    List<ChildDataModel> getAllChilds(String name);

    @Query("SELECT * from Child_Table WHERE favourite = 1 ORDER BY name DESC")
    List<ChildDataModel> getAllFavourites();

    @Delete
    void delete(ChildDataModel child);

    @Query("DELETE FROM Child_Table WHERE parent = :parent")
    void deleteAllChildsWith(String parent);

    @Update
    void update(ChildDataModel... childDataModels);

}