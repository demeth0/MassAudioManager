package com.demeth.massaudioplayer.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.demeth.massaudioplayer.database.IdentifiedEntry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFieldAutoCompleter extends ArrayAdapter<String> {
    private final Filter customFilter;
    private Collection<? extends IdentifiedEntry> list=null;

    /**
     * @param context context de l'application
     * @param resource layout des view pour résultat du filtre

     */
    public SearchFieldAutoCompleter(@NonNull Context context, int resource) {
        super(context, resource);
        customFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults res=null;
                if(charSequence!=null && SearchFieldAutoCompleter.this.list!=null){
                    res = new FilterResults();
                    String filter = charSequence.toString().toLowerCase();
                    List<String> filtered = SearchFieldAutoCompleter.this.list.stream().map(IdentifiedEntry::getName).filter(name -> name.toLowerCase().contains(filter)).collect(Collectors.toList());
                    res.values = filtered;
                    res.count = filtered.size();
                }
                return res;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults != null && filterResults.count >0){
                    SearchFieldAutoCompleter.this.clear();
                    SearchFieldAutoCompleter.this.addAll((List<String>)filterResults.values);
                    SearchFieldAutoCompleter.this.notifyDataSetChanged();
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
    public void setContent(Collection<? extends IdentifiedEntry> list){
        this.list=list;
    }
}
