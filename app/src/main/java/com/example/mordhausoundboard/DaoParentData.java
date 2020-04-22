package com.example.mordhausoundboard;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoParentData {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParent(ParentDataModel parent);

    @Query("SELECT * from Parent_Table ORDER BY name DESC")
    List<ParentDataModel> getAllParents();

    @Query("SELECT * from Parent_Table WHERE name = :name LIMIT 1")
    ParentDataModel getParent(String name);

    @Update
    void update(ParentDataModel... parentDataModels);

}