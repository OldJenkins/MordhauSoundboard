package com.norman.mordhausoundboard;

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
    String getId() {
        return id;
    }

    void setId(@NonNull String id) {
        this.id = id;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    boolean isFavourite() {
        return favourite;
    }

    void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    boolean isDownloaded() {
        return isDownloaded;
    }

    void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    String getParent() {
        return parent;
    }

    void setParent(String parent) {
        this.parent = parent;
    }

    void setChild(ChildDataModel child){
        this.name = child.name;
        this.url = child.url;
        this.favourite = child.favourite;
        this.isDownloaded = child.isDownloaded;
        this.parent = child.parent;
    }

    String getRawname() {
        return rawname;
    }

    void setRawname(String rawname) {
        this.rawname = rawname;
    }

    // Check if items are the same BUT do not compare the url & favourite & downloadstat cause it will always be different
     boolean AlmostEquals(ChildDataModel that){
        return this.getRawname().equals(that.getRawname());
     }

}
