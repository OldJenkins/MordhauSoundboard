package com.norman.mordhausoundboard;

/*
This Model is used to represent a Collection of Sound files
It is also a Model inside of the Room Database
 */

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Parent_Table")
public class ParentDataModel {

    @PrimaryKey
    @NonNull
    private String name;
    private boolean isTextDownloaded;



    /* if you open a parent for the first time, the app is making a request to the server,
        and will retrieve all the NAMES of the sound files , to store them into the database.
        This is build like that to make the App a bit more flexible when using it for another Theme.
        So you can control the cards names inside of your ftp server.
        And also you can simply add new files to your Server , and the User will get the files
        without having to release a new App update */
    private boolean isDownloaded;

    // used for checking if you can set the "checked" arrow to the parent, only if ALL SOUNDFILES are downloaded and stored on the device
    private boolean isAllItemsDownloaded;

    // used for asking the user if he wants to download all files
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
