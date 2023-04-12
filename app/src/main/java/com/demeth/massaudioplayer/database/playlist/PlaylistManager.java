package com.demeth.massaudioplayer.database.playlist;

import android.content.Context;
import android.util.Log;

import com.demeth.massaudioplayer.database.AudioLibrary;
import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class PlaylistManager {

    private final HashMap<String , Playlist> playlists = new HashMap<>();
    private File dir;
    public static final String PLAYLIST_DIRECTORY="playlists";

    private final AudioLibrary library;

    public PlaylistManager(Context context, AudioLibrary library){
        dir=context.getFilesDir();
        this.library = library;
        load();
        create("liked");
    }

    public Playlist get(String name){
        return playlists.get(name);
    }

    public Collection<Playlist> getAll(){
        return playlists.values();
    }

    public void create(String name){
        File f = new File(dir+"/"+PLAYLIST_DIRECTORY+"/"+name);
        if(!f.exists()){
            update(new Playlist(name,this));
            Log.d("PlaylistManager","created playlist "+name+" at "+f.getPath());
        }
    }

    public void load(){
        File pdir = new File(dir+"/"+PLAYLIST_DIRECTORY);

        if(!pdir.exists()) //noinspection ResultOfMethodCallIgnored
            pdir.mkdirs();

        for(File f : pdir.listFiles()){
            Log.d("PlaylistManager","loading "+f.getPath());
            try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(f.toPath()))){
                @SuppressWarnings("unchecked")
                List<IdentifiedEntry> data = (ArrayList<IdentifiedEntry>) ois.readObject();
                data = data.stream().map(d -> library.get(d.getName(),d.getType())).collect(Collectors.toList());
                Playlist p = new Playlist(f.getName(),this,data);
                Log.d("PlaylistManager","loaded "+p.getName()+" with "+p.get().size()+" entries");
                playlists.put(p.getName(),p);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(Playlist playlist){
        try(ObjectOutputStream ois = new ObjectOutputStream(Files.newOutputStream(new File(dir.getPath() + "/"+ PLAYLIST_DIRECTORY+"/" + playlist.getName()).toPath()))){
            ois.writeObject(new ArrayList<>(playlist.get()));
            playlists.put(playlist.getName(),playlist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
