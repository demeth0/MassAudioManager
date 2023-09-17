package com.demeth.massaudioplayer.backend.adapters;

import com.demeth.massaudioplayer.backend.models.adapters.EventManager;
import com.demeth.massaudioplayer.backend.models.objects.Event;

import java.util.LinkedHashSet;

public class SequentialEventManager implements EventManager {
    private LinkedHashSet<EventHandler> handlers;

    public SequentialEventManager(){
        handlers = new LinkedHashSet<>();
    }

    @Override
    public void trigger(Event event) {
        handlers.forEach(h->h.handle(event));
    }

    @Override
    public void registerHandler(EventHandler handler) {
        handlers.add(handler);
    }
}
