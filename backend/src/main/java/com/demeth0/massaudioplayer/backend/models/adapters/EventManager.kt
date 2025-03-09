package com.demeth0.massaudioplayer.backend.models.adapters

import com.demeth0.massaudioplayer.backend.models.objects.Event

/**
 * This class is used to stock callbacks created for UI or system updates.
 */
fun interface EventHandler{
    /**
     * handle an event that is triggered by a provided callback.
     * @param event The event that stock event data and identifier.
     */
    fun handle(event: Event)
}

/**
 * The event manager provide player callbacks managements.
 */
interface EventManager {


    /**
     * Used internally by the system to trigger an event and run callbacks associated with it.
     * @param event The identification and data.
     */
    fun trigger(event: Event)

    /**
     * Used by the external component to react to specific event. For example UI or system updates.
     * @param id A unique identifier to assign to the callback.
     * @param handler The callback function to run.
     */
    fun registerHandler(id: String, handler: EventHandler)

    /**
     * Remove a handler using his unique identifier.
     * @param id The ID of the event handler.
     */
    fun removeHandler(id: String)
}
