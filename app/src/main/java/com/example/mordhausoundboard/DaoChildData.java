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

    @Query("DELETE FROM Child_Table")
    void deleteAll();

    @Query("SELECT * from Child_Table ORDER BY name DESC")
    List<ChildDataModel> getAllChilds();

    @Delete
    void delete(ChildDataModel child);

    @Update
    void update(ChildDataModel... childDataModels);

}