package com.example.mordhausoundboard;

import java.util.ArrayList;

class ParentDataModel {

    private String name;


    ParentDataModel(String name){
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String convertArrayToString(ArrayList<ChildDataModel> list){
        String result ="";
        for(int i =0;i<list.size();i++){
            result += list.get(i)+"%";
        }
        return result;
    }


}
