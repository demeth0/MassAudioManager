package com.demeth.massaudioplayer.ui;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;


import com.demeth.massaudioplayer.backend.models.objects.Audio;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SelectionManager {

    public interface SelectionListener{
        void onSelectionEnabled();
        void onSelectionDisabled();
        void onItemSelected();
        void onItemDeselected();
    }

    private SelectionListener listener=new SelectionListener() {
        @Override
        public void onSelectionEnabled() {

        }

        @Override
        public void onSelectionDisabled() {

        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemDeselected() {

        }
    };

    public SelectionManager(){

    }

    private final Set<Audio> selected_values = new HashSet<>();

    @SuppressLint("NotifyDataSetChanged")
    public void select(Audio entry, RecyclerView.Adapter<?> list_adapter){
        if(!selected_values.contains(entry)){
            selected_values.add(entry);
            listener.onItemSelected();
            if(selected_values.size()==1){
                list_adapter.notifyDataSetChanged();
                listener.onSelectionEnabled();
            }
        }
    }

    public Collection<Audio> getSelected() {
        return selected_values;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void select(Collection<Audio> entry, RecyclerView.Adapter<?> list_adapter){
        selected_values.addAll(entry);
        listener.onItemSelected();
        if(selected_values.size()==entry.size()) {
            list_adapter.notifyDataSetChanged(); //enable display
            listener.onSelectionEnabled();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void unselect(Audio entry, RecyclerView.Adapter<?> list_adapter){
        if(selected_values.contains(entry)){
            selected_values.remove(entry);
            listener.onItemDeselected();
            if(selected_values.size()<=0) {
                list_adapter.notifyDataSetChanged();
                listener.onSelectionDisabled();
            }
        }
    }

    public void setListener(SelectionListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear(RecyclerView.Adapter<?> list_adapter){
        selected_values.clear();
        listener.onItemDeselected();
        listener.onSelectionDisabled();
        list_adapter.notifyDataSetChanged();
    }

    public boolean contains(Audio u){
        return selected_values.contains(u);
    }

    public int size(){
        return selected_values.size();
    }
}
