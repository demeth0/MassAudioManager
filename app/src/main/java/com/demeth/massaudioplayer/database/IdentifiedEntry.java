package com.demeth.massaudioplayer.database;

import android.net.Uri;

public abstract class IdentifiedEntry implements Typed,Comparable<IdentifiedEntry>{
    private final int id;
    private final String name;
    private Uri albumCover;

    public IdentifiedEntry(String name, int id){
        this.name=name;
        this.albumCover=null;
        this.id=id;
    }

    public IdentifiedEntry(String name,Uri album, int id){
        this(name,id);
        this.albumCover=album;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void setAlbumCover(Uri albumCover) {
        this.albumCover = albumCover;
    }

    public Uri getAlbumCover(){
        return albumCover;
    }

    @Override
    public int compareTo(IdentifiedEntry identifiedEntry) {
        return name.compareTo(identifiedEntry.name);
    }
}
