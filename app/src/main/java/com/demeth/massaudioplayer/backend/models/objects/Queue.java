package com.demeth.massaudioplayer.backend.models.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queue {
    private List<Audio> audios;
    public Queue(){
        audios = new ArrayList<>();
    }
    public void add(Audio audio){
        audios.add(audio);
    }
    public void set(Audio audio){
        while(!audios.get(0).equals(audio)){
            audios.remove(0);
        }
    }

    public void clear(){
        audios.clear();
    }

    public int size(){
        return audios.size();
    }

    public Audio next(){
        if(audios.size()==0)
            return null;
        return audios.remove(0);
    }

    public List<Audio> view(){
        return new ArrayList<>(audios);

    }
}
