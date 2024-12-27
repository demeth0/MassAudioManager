package com.demeth.massaudioplayer.frontend.components

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.demeth.massaudioplayer.backend.models.objects.Audio
import java.util.Locale

/**
 * @param context context de l'application
 * @param resource view's layout for filter results
 */
class SearchFieldAutoCompleteArrayAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource) {
    private val customFilter: Filter
    private var list: Collection<Audio>? = null

    init {
        customFilter = object: Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val res = FilterResults()
                if(this@SearchFieldAutoCompleteArrayAdapter.list!=null){
                    val filter = charSequence.toString().lowercase(Locale.getDefault())
                    lateinit var filtered : List<String>
                    this@SearchFieldAutoCompleteArrayAdapter.list?.apply {
                        filtered = map(Audio::displayName).filter {
                            it.lowercase(Locale.getDefault()).contains(filter)
                        }.toList()
                        res.values = filtered
                        res.count = filtered.size
                    }
                }
                return res
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if(filterResults.count >0){
                    this@SearchFieldAutoCompleteArrayAdapter.apply {
                        clear()
                        addAll(filterResults.values as List<String>)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    /**
     * @return the filter created
     */
    override fun getFilter():Filter {
        return customFilter
    }

    /**
     * @param list the content from whom we need to filter
     */
    fun setContent(list: Collection<Audio>){
        this.list=list
    }
}
