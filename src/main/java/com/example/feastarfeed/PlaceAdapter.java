package com.example.feastarfeed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<String> placeList;
    public List<String> filteredList;

    public PlaceAdapter(Context context, List<String> placeList) {
        this.context = context;
        this.placeList = placeList;
        this.filteredList = new ArrayList<>(placeList);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the convertView is null. If so, inflate the layout.
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.place_search_item, parent, false);
        }

        // Get the data item for this position
        String place = filteredList.get(position);

        // Lookup view for data population
        TextView placeTextView = convertView.findViewById(R.id.place_text);

        // Populate the data into the template view using the data object
        placeTextView.setText(place);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> filtered = new ArrayList<>();
                Log.d("Placeadapter","constraint = "+constraint);

                if (constraint == null || constraint.length() == 0) {

                } else {
                    String filterPattern = (String) constraint;
                    Log.d("Placeadapter","filterPattern = "+filterPattern);
                    for (String place : placeList) {
                        if (place.contains(filterPattern)) {
                            filtered.add(place);
                        }
                    }
                }

                results.values = filtered;
                results.count = filtered.size();
                Log.d("Placeadapter","results.count = "+results.count);
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.count != 0){
                    filteredList.addAll((List<String>) results.values);
                }

                notifyDataSetChanged();
            }
        };
    }

}
