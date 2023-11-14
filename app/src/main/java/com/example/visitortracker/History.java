package com.example.visitortracker;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class History {
    private String date;
    private String time;
    private String name;
    public History() {

    }

    public History(String date, String time, String name) {
        this.date = date;
        this.name = name;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}