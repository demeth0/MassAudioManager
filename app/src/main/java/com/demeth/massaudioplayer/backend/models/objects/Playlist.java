package com.demeth.massaudioplayer.backend.models.objects;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The list of audio played by the audio manager
 */
public class Playlist {

    private List<Audio> audios;
    private List<Integer> order;
    private int current=-1;
    private boolean random = false;

    public Playlist(List<Audio> audios){
        this.audios = new ArrayList<>(audios);
        order = new ArrayList<>();
        for(int i=0;i<audios.size();i++){
            order.add(i);
        }
    }

    public Playlist(Audio ...audios){
        this(Arrays.asList(audios));
    }

    @Nullable
    public Audio next(){
        current=(current+1)%order.size();
        if(current==0 && random){
            shuffle_list();
        }
        return get();
    }

    @Nullable
    public Audio prev(){
        if(current==-1) current = order.size()-1;
        else            current = (current-1+order.size())%order.size();
        return get();
    }

    public void set(Audio audio){
        current = audios.indexOf(audio);
    }

    public void set(int index){
        if(index<audios.size())
            current = index;
    }

    public boolean is_last_audio(){
        return current == order.size()-1;
    }

    public static boolean is_last_audio(Playlist p){
        if(p==null) return true;
        return p.is_last_audio();
    }

    @Nullable
    public Audio get(){
        if(current>order.size()) return null;
        return audios.get(order.get(current));
    }

    public int index(){
        return current;
    }

    public void random(boolean mode){
        this.random=mode;
        if(mode){
            int cur_audio = 0;
            if(current!=-1) cur_audio = order.remove(current);
            shuffle_list();
            if(current!=-1) order.add(0,cur_audio);
        }else{
            order_list();
        }
    }

    private void shuffle_list(){
        Collections.shuffle(order);
    }
    private void order_list(){
        Collections.sort(order);
    }

    public boolean is_random(){
        return random;
    }

    public void extend(List<Audio> extension){
        for(int i=0;i<extension.size();i++){ //resize audio order list to match audio
            order.add(i+audios.size());
        }
        audios.addAll(extension);
    }

    public List<Audio> view(){
        List<Audio> audio_list = new ArrayList<>(audios.size());
        order.forEach(i -> audio_list.add(audios.get(i)));
        return audio_list;
    }
}
