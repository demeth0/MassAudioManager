package com.demeth.massaudioplayer.database.playlist;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdProvider;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * define a Playlist that stock a list of track saved on the local storage
 */
public class Playlist extends IdentifiedEntry {
    /**
     * define a listener called when the playlist is updated
     */
    @FunctionalInterface
    public interface OnUpdateListener{
        void onUpdate(Playlist p);
    }
    private transient OnUpdateListener onUpdate = null;
    private final transient PlaylistManager manager;
    private final ArrayList<IdentifiedEntry> content;

    /**
     * create an empty playlsit
     * @param name name of the playlist should be unique
     * @param manager the playlist manager that manage data persistence
     */
    Playlist(String name,PlaylistManager manager){
        super(name, IdProvider.providePlaylistId());
        content=new ArrayList<>();
        this.manager = manager;
    }

    /**
     * create a playlist from a set of tracks, mainly used to load a playlist from disk without triggering updates
     * @param name name of the playlist should be unique
     * @param manager the manage that manage data persistence
     * @param datas the data loaded from external source
     */
    Playlist(String name,PlaylistManager manager, Collection<IdentifiedEntry> datas){
        this(name,manager);
        content.addAll(datas);
    }

    /**
     * set the event to run when the playlist is updated
     * @param onUpdate the update runnable
     */
    public void setOnUpdateListener(OnUpdateListener onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * add en entry to the playlist and update the file
     * @param e th eentry to add
     */
    public void add(IdentifiedEntry e){
        content.add(e);
        manager.update(this);
        if(onUpdate!=null) onUpdate.onUpdate(this);
    }

    /**
     * remove an entry from the playlist and pudate the file
     * @param e the entry to add
     */
    public void remove(IdentifiedEntry e){
        content.remove(e);
        manager.update(this);
        if(onUpdate!=null) onUpdate.onUpdate(this);
    }

    /**
     *
     * @param e the entry to check
     * @return true if the entry is in the playlist
     */
    public boolean contains(IdentifiedEntry e){
        return content.contains(e);
    }

    /**
     * @return the list of entry saved in the playlist
     */
    public Collection<IdentifiedEntry> get(){
        return content;
    }

    /**
     * @return DataEntry.PLAYLIST
     */
    @Override
    public DataType getType() {
        return DataType.PLAYLIST;
    }
}
