package com.demeth.massaudioplayer.backend.models.adapters;

import com.demeth.massaudioplayer.backend.models.objects.Event;

/**
 * The event manager provide player callbacks managements.
 */
public interface EventManager {
    /**
     * This class is used to stock callbacks created for UI or system updates.
     */
    @FunctionalInterface
    interface EventHandler{
        /**
         * handle an event that is triggered by a provided callback.
         * @param event The event that stock event data and identifier.
         */
        void handle(Event event);
    }

    /**
     * Used internally by the system to trigger an event and run callbacks associated with it.
     * @param event The identification and data.
     */
    void trigger(Event event);

    /**
     * Used by the external component to react to specific event. For example UI or system updates.
     * @param ID A unique identifier to assign to the callback.
     * @param handler The callback function to run.
     */
    void registerHandler(String ID, EventHandler handler);

    /**
     * Remove a handler using his unique identifier.
     * @param ID The ID of the event handler.
     */
    void removeHandler(String ID);
}
