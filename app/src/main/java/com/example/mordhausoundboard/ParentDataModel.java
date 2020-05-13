package com.example.mordhausoundboard;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Parent_Table")
public class ParentDataModel {

    @PrimaryKey
    @NonNull
    private String name;
    private boolean isTextDownloaded;
    private boolean isDownloaded;
    private boolean isAllItemsDownloaded;
    private boolean askedForDownload;


    ParentDataModel(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isTextDownloaded() {
        return isTextDownloaded;
    }

    public void setTextDownloaded(boolean textDownloaded) {
        isTextDownloaded = textDownloaded;
    }

    public boolean isAllItemsDownloaded() {
        return isAllItemsDownloaded;
    }

    public void setAllItemsDownloaded(boolean allItemsDownloaded) {
        isAllItemsDownloaded = allItemsDownloaded;
    }

    public boolean isAskedForDownload() {
        return askedForDownload;
    }

    public void setAskedForDownload(boolean askedForDownload) {
        this.askedForDownload = askedForDownload;
    }

    void clearDownloads(){
        isAllItemsDownloaded = false;
        isDownloaded = false;
        isTextDownloaded = false;
    }
}
