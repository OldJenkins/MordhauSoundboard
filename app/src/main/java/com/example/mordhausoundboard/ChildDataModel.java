package com.example.mordhausoundboard;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Child_Table")
public class ChildDataModel {

    @PrimaryKey
    @NonNull
    private String name;
    private String url;
    private boolean favourite;
    private boolean isDownloaded;


    ChildDataModel(String name,String url){
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    void setChild(ChildDataModel child){
        this.name = child.name;
        this.url = child.url;
        this.favourite = child.favourite;
        this.isDownloaded = child.isDownloaded;
    }
}
