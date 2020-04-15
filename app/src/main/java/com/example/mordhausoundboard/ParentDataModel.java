package com.example.mordhausoundboard;

class ParentDataModel {

    private String name;

    private boolean isDownloaded;

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

}
