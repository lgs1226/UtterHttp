package com.async.utter.http.download.enums;

/**
 * Created by Administrator on 2017/9/11.
 */

public enum Priority {

    low(0),

    middle(1),

    high(2);

    private int value;

    Priority(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static Priority getInstance(int value){
        for (Priority priority:Priority.values()) {
            if (priority.getValue() == value){
                return priority;
            }
        }
        return Priority.middle;
    }
}
