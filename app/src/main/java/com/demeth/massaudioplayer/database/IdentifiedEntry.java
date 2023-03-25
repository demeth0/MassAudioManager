package com.demeth.massaudioplayer.database;

import android.net.Uri;


public abstract class IdentifiedEntry {
    private int id;
    public IdentifiedEntry(String name, int id){
        this.name=name;
        this.albumCover=null;
        this.id=id;
    }
    private String name;
    private Uri albumCover;

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public Uri getAlbumCover(){
        return albumCover;
    }
}
