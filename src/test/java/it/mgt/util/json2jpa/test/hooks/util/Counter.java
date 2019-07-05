package it.mgt.util.json2jpa.test.hooks.util;

public class Counter {

    private int value;

    public Counter() {
    }

    public void increase() {
        value++;
    }

    public int getValue() {
        return value;
    }
}
