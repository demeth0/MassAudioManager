package com.demeth.massaudioplayer.ui;

public enum Category {
    PLAYLISTS("PLAYLISTS"),
    PISTES("PISTES"),
    QUEUE("QUEUE");

    private String name;
    Category(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
