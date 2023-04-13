package com.demeth.massaudioplayer.database;

import android.net.Uri;

import java.io.Serializable;

public abstract class IdentifiedEntry implements Typed,Comparable<IdentifiedEntry>, Serializable {
    public static final IdentifiedEntry EMPTY = new IdentifiedEntry("",0) {
        @Override
        public DataType getType() {
            return DataType.NONE;
        }
    };

    protected transient final int id;
    protected final String name;
    protected transient Uri albumCover;

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
