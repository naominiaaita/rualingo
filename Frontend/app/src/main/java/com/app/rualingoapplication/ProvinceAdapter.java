package com.app.rualingoapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ProvinceAdapter extends ArrayAdapter<Province> implements Filterable {
    private final List<Province> originalList;
    private List<Province> filteredList;

    public ProvinceAdapter(@NonNull Context context, @NonNull List<Province> provinces) {
        super(context, 0, provinces);
        this.originalList = new ArrayList<>(provinces);
        this.filteredList = provinces;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Nullable
    @Override
    public Province getItem(int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_province_dropdown, parent, false);
        }

        Province province = getItem(position);
        ImageView flagImg = convertView.findViewById(R.id.imgProvinceFlag);
        TextView nameTv = convertView.findViewById(R.id.tvProvinceName);

        if (province != null) {
            flagImg.setImageResource(province.getFlagResId());
            nameTv.setText(province.getName());
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.values = originalList;
                results.count = originalList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Province>) results.values;
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Province) resultValue).getName();
            }
        };
    }
}
