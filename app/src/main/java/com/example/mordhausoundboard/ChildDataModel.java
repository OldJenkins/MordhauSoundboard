package com.example.mordhausoundboard;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Child_Table")
public class ChildDataModel {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String parent;
    private String url;
    private boolean favourite;
    private boolean isDownloaded;
    private String rawname;


    ChildDataModel(String name,String url){
        this.name = name;
        this.url = url;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    void setChild(ChildDataModel child){
        this.name = child.name;
        this.url = child.url;
        this.favourite = child.favourite;
        this.isDownloaded = child.isDownloaded;
        this.parent = child.parent;
    }

    public String getRawname() {
        return rawname;
    }

    public void setRawname(String rawname) {
        this.rawname = rawname;
    }

    // Check if items are the same BUT do not compare the url & favourite & downloadstat cause it will always be different
    public boolean AlmostEquals(ChildDataModel that){
        return this.getRawname().equals(that.getRawname());
    }

}
