package com.demeth.massaudioplayer.ui.viewmodel;

import android.widget.Filter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.demeth.massaudioplayer.backend.models.objects.Audio;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * contain the list to display in the UI
 *
 **/
public class ListViewModel extends ViewModel {
    private final MutableLiveData<Collection<? extends Audio>> list = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Collection<? extends Audio>> list_filtered = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Collection<? extends Audio>> queue = new MutableLiveData<>(Collections.emptyList());

    private String filter_mask = "";

    private final Filter filter = new Filter() {
        @SuppressWarnings("ConstantConditions")
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults res=new FilterResults();
            if(charSequence!=null && charSequence.length()>0){
                res = new FilterResults();
                String filter = charSequence.toString().toLowerCase();
                Collection<? extends Audio> filtered = ListViewModel.this.list.getValue().stream().filter(entry -> entry.display_name.toLowerCase().contains(filter)).collect(Collectors.toList());
                res.values = filtered;
                res.count = filtered.size();
            }else{
                Collection<? extends Audio> data = ListViewModel.this.list.getValue();
                res.values = data;
                res.count = data.size();
            }
            return res;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ListViewModel.this.list_filtered.setValue((Collection<? extends Audio>) filterResults.values);
        }
    };

    public void setList(Collection<? extends Audio> list){
        this.list.setValue(list);
        this.filter.filter(filter_mask);
    }

    public void setFilterMask(String mask){
        this.filter_mask=mask;
        this.filter.filter(filter_mask);
    }


    public void setQueue(Collection<? extends Audio> list){
        this.queue.setValue(list);
    }

    public LiveData<Collection<? extends Audio>> getList(){
        return list;
    }

    public LiveData<Collection<? extends Audio>> getQueue() {
        return queue;
    }

    public LiveData<Collection<? extends Audio>> getFilteredList(){
        return list_filtered;
    }

}