package com.demeth.massaudioplayer.frontend.components;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.demeth.massaudioplayer.backend.models.objects.Audio;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFieldAutoCompleteArrayAdapter extends ArrayAdapter<String> {
    private final Filter customFilter;
    private Collection<? extends Audio> list=null;

    /**
     * @param context context de l'application
     * @param resource layout des view pour résultat du filtre

     */
    public SearchFieldAutoCompleteArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        customFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults res=null;
                if(charSequence!=null && SearchFieldAutoCompleteArrayAdapter.this.list!=null){
                    res = new FilterResults();
                    String filter = charSequence.toString().toLowerCase();
                    List<String> filtered = SearchFieldAutoCompleteArrayAdapter.this.list.stream().map(a -> a.display_name).filter(name -> name.toLowerCase().contains(filter)).collect(Collectors.toList());
                    res.values = filtered;
                    res.count = filtered.size();
                }
                return res;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults != null && filterResults.count >0){
                    SearchFieldAutoCompleteArrayAdapter.this.clear();
                    SearchFieldAutoCompleteArrayAdapter.this.addAll((List<String>)filterResults.values);
                    SearchFieldAutoCompleteArrayAdapter.this.notifyDataSetChanged();
                }
            }
        };
    }

    /**
     * @return the filter created
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return customFilter;
    }

    /**
     * @return the content from whom we need to filter
     */
    public void setContent(Collection<? extends Audio> list){
        this.list=list;
    }
}
