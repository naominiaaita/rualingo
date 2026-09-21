package com.app.rualingoapplication;

public class Province {
    private final String name;
    private final int flagResId;

    public Province(String name, int flagResId) {
        this.name = name;
        this.flagResId = flagResId;
    }

    public String getName() {
        return name;
    }

    public int getFlagResId() {
        return flagResId;
    }

    @Override
    public String toString() {
        return name;
    }
}
