package com.example.mordhausoundboard;

public class ChildDataModel {

    String name;
    String path;

    ChildDataModel(String name,String path){
        this.name = name;
        this.path = path;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
