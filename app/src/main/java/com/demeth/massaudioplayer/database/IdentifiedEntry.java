package com.demeth.massaudioplayer.database;

import android.net.Uri;

import java.io.Serializable;

/**
 * generic definition of an entry that can be processed by a AudioPlayer
 *
 * @see com.demeth.massaudioplayer.audio_player.AudioPlayer
 */
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

    /**
     * define an entry by it's name and a dynamic ID given at initialisation. Should use the IdProvider to prevent collisions
     * @param name the name of the entry
     * @param id the dynamic id of the entry
     */
    public IdentifiedEntry(String name, int id){
        this.name=name;
        this.albumCover=null;
        this.id=id;
    }

    /**
     * define an entry by it's name and a dynamic ID given at initialisation. Should use the IdProvider to prevent collisions
     * @param name the name of the entry
     * @param id the dynamic id of the entry
     * @param album the album cover source if known
     */
    public IdentifiedEntry(String name,Uri album, int id){
        this(name,id);
        this.albumCover=album;
    }

    /**
     * @return the ID assigned with this entry, should be unique
     */
    public int getId(){
        return id;
    }

    /**
     * @return the name of this entry may not be unique
     */
    public String getName(){
        return name;
    }

    /**
     * set the album cover for display
     * @param albumCover the cover to use
     */
    public void setAlbumCover(Uri albumCover) {
        this.albumCover = albumCover;
    }

    /**
     * @return the album cover
     */
    public Uri getAlbumCover(){
        return albumCover;
    }

    /**
     * for ordering process, compare names only
     * @param identifiedEntry the entry to compare to
     * @return comparison of strings
     */
    @Override
    public int compareTo(IdentifiedEntry identifiedEntry) {
        return name.compareTo(identifiedEntry.name);
    }
}
