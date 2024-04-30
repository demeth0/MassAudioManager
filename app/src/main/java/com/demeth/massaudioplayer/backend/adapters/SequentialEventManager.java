package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Event;

import java.util.LinkedHashMap;

public class SequentialEventManager implements EventManager {
    private LinkedHashMap<String,EventHandler> handlers;

    public SequentialEventManager(){
        handlers = new LinkedHashMap<>();
    }

    @Override
    public void trigger(Event event) {
        handlers.entrySet().forEach(h->h.getValue().handle(event));
    }

    @Override
    public void registerHandler(String ID, EventHandler handler) {
        handlers.put(ID,handler);
    }

    @Override
    public void removeHandler(String ID) {
        if(handlers.containsKey(ID)){
            handlers.remove(ID);
        }
    }
}
