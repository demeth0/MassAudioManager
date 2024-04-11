package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Event;

public interface EventManager {
    @FunctionalInterface
    interface EventHandler{
        void handle(Event event);
    }
    void trigger(Event event);

    void registerHandler(String ID, EventHandler handler);
}
