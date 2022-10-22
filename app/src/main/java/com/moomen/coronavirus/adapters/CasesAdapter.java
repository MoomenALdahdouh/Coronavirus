package com.moomen.coronavirus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moomen.coronavirus.R;
import com.moomen.coronavirus.model.Case;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CasesAdapter extends RecyclerView.Adapter<CasesAdapter.ViewHolder> implements Filterable {

    private ArrayList<Case> data;
    private ArrayList<Case> fullData;
    private Context context;

    public CasesAdapter(ArrayList<Case> data, Context context) {
        this.data = data;
        this.fullData = new ArrayList<>(data);
        this.context = context;
    }

    @NonNull
    @Override
    public CasesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.case_item, parent, false);
        return new CasesAdapter.ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull final CasesAdapter.ViewHolder holder, int position) {
        Case currentCase = data.get(position);
        Picasso.get().load("https://www.countryflags.io/" + currentCase.getCountryCode() + "/flat/64.png").into(holder.flag);
        holder.name.setText(currentCase.getName());
        holder.newConfirmed.setText(Integer.toString(currentCase.getNewConfirmed()));
        holder.newDeaths.setText(Integer.toString(currentCase.getNewDeaths()));
        holder.newRecovered.setText(Integer.toString(currentCase.getNewRecovered()));
        holder.confirmed.setText(Integer.toString(currentCase.getTotalConfirmed()));
        holder.deaths.setText(Integer.toString(currentCase.getTotalDeaths()));
        holder.recovered.setText(Integer.toString(currentCase.getTotalRecovered()));
    }

    @Override
    public int getItemCount() { return data.size(); }

    @Override
    public Filter getFilter() {
        return casesFilter;
    }

    private Filter casesFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Case> filteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0)
                filteredList.addAll((fullData));
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Case someCase: fullData)
                    if(someCase.getName().toLowerCase().startsWith(filterPattern))
                        filteredList.add(someCase);
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            data.clear();
            data.addAll( (List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView flag;
        TextView name;
        TextView newConfirmed;
        TextView newDeaths;
        TextView newRecovered;
        TextView confirmed;
        TextView deaths;
        TextView recovered;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.flag = itemView.findViewById(R.id.image_view_flag_id);
            this.name = itemView.findViewById(R.id.text_view_country_id);
            this.newConfirmed = itemView.findViewById(R.id.text_view_new_confirmed_id);
            this.newDeaths = itemView.findViewById(R.id.text_view_new_deaths_id);
            this.newRecovered = itemView.findViewById(R.id.text_view_new_recovered_id);
            this.confirmed = itemView.findViewById(R.id.text_view_count_confirmed_id);
            this.deaths = itemView.findViewById(R.id.text_view_count_deaths_id);
            this.recovered = itemView.findViewById(R.id.text_view_count_recovered_id);
        }
    }
}
