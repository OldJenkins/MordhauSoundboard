package com.example.mordhausoundboard;

import java.util.List;

class ParentDataModel {

    private String name;
    private List<ChildDataModel> path_list;

    ParentDataModel(String name, List<ChildDataModel> path_list){
        this.name = name;
        this.path_list = path_list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChildDataModel> getPath_list() {
        return path_list;
    }

    public void setPath_list(List<ChildDataModel> path_list) {
        this.path_list = path_list;
    }
}
