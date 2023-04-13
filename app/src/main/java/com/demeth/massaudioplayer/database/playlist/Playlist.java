package com.demeth.massaudioplayer.database.playlist;

import com.demeth.massaudioplayer.database.DataType;
import com.demeth.massaudioplayer.database.IdProvider;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.ArrayList;
import java.util.Collection;

public class Playlist extends IdentifiedEntry {
    @FunctionalInterface
    public interface OnUpdateListener{
        void onUpdate(Playlist p);
    }
    private transient OnUpdateListener onUpdate = null;
    private final transient PlaylistManager manager;
    private final ArrayList<IdentifiedEntry> content;

    Playlist(String name,PlaylistManager manager){
        super(name, IdProvider.providePlaylistId());
        content=new ArrayList<>();
        this.manager = manager;
    }

    Playlist(String name,PlaylistManager manager, Collection<IdentifiedEntry> datas){
        this(name,manager);
        content.addAll(datas);
    }

    public void setOnUpdateListener(OnUpdateListener onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void add(IdentifiedEntry e){
        content.add(e);
        manager.update(this);
        if(onUpdate!=null) onUpdate.onUpdate(this);
    }

    public void remove(IdentifiedEntry e){
        content.remove(e);
        manager.update(this);
        if(onUpdate!=null) onUpdate.onUpdate(this);
    }

    public boolean contains(IdentifiedEntry e){
        return content.contains(e);
    }

    public Collection<IdentifiedEntry> get(){
        return content;
    }

    @Override
    public DataType getType() {
        return DataType.PLAYLIST;
    }
}
