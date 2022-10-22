package com.moomen.coronavirus.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moomen.coronavirus.utils.Utils;
import com.moomen.coronavirus.adapters.CasesAdapter;
import com.moomen.coronavirus.databinding.FragmentCasesBinding;
import com.moomen.coronavirus.model.Case;
import com.moomen.coronavirus.network.CasesNetworkParser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

public class CasesFragment extends Fragment implements CasesNetworkParser.OnCasesFilledListener {
    private FragmentCasesBinding binding;

    private final String[] sortAsValues = {"Ascending", "Descending"};

    private static ArrayList<Case> mCases = new ArrayList<>();

    private RecyclerView recyclerViewCases;
    private CasesAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private static int position = 1;

    private static int currentScrollPosition;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCasesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        showDialogWhenNoInternet(getContext());

        Spinner spinner = binding.sortSpinner;
        setUpSpinner(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                position = i;
                if (mCases != null)
                    notifyAdapterWithSortValue(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final SearchView searchView = binding.searchId;
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                notifyAdapterWithSortValue(position);
                return false;
            }
        });

        return root;
    }

    private void setUpSpinner(Spinner spinner) {
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sortAsValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
    }

    private void downloadCases() {
        CasesNetworkParser parser = new CasesNetworkParser(getContext());
        parser.setOnCasesFilledListener(this);
        try {
            parser.parseCountriesCasesJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showDialogWhenNoInternet(final Context context) {
        if(!Utils.isNetworkAvailable(context) && mCases.size()==0)  // Don't show the dialog when cases are already loaded
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Internet Connection Alert")
                    .setMessage("Please Check Your Internet Connection")
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showDialogWhenNoInternet(context);
                        }
                    }).show();
        else
            downloadCases();
    }

    @Override
    public void startFillingCases(ArrayList<Case> cases) {
        showInvincibleViews();
        binding.progressBarCasesId.setVisibility(View.GONE);
        mCases = new ArrayList<>(cases);
        Case globalCase = cases.get(0);
        startFillingGlobalCases(globalCase);

        mCases.remove(0);
        setUpRecycleViewWithSortValue();
    }

    private void setUpRecycleViewWithSortValue() {
        recyclerViewCases = binding.recyclerViewCasesId;
        adapter = new CasesAdapter(mCases, getContext());
        recyclerViewCases.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewCases.setLayoutManager(linearLayoutManager);
        recyclerViewCases.setAdapter(adapter);
        notifyAdapterWithSortValue(position);
    }

    private void notifyAdapterWithSortValue(int position) {
        if (position == 1)
            Collections.sort(mCases, Collections.<Case>reverseOrder());
        else
            Collections.sort(mCases);
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }

    private void startFillingGlobalCases(Case currentCase) {
        binding.progressBarGlobalCaseId.setVisibility(View.GONE);
        binding.textViewCountryId.setText("Global");
        binding.textViewNewConfirmedId.setText(String.valueOf(currentCase.getNewConfirmed()));
        binding.textViewNewDeathsId.setText(String.valueOf(currentCase.getNewDeaths()));
        binding.textViewNewRecoveredId.setText(String.valueOf(currentCase.getNewRecovered()));
        binding.textViewCountConfirmedId.setText(String.valueOf(currentCase.getTotalConfirmed()));
        binding.textViewCountDeathsId.setText(String.valueOf(currentCase.getTotalDeaths()));
        binding.textViewCountRecoveredId.setText(String.valueOf(currentCase.getTotalRecovered()));
    }

    private void showInvincibleViews() {
        binding.globalIconId.setVisibility(View.VISIBLE);
        binding.textViewCountryId.setVisibility(View.VISIBLE);
        binding.textViewConfirmedId.setVisibility(View.VISIBLE);
        binding.textViewRecoveredId.setVisibility(View.VISIBLE);
        binding.textViewDeathsId.setVisibility(View.VISIBLE);
        binding.icon1Id.setVisibility(View.VISIBLE);
        binding.icon2Id.setVisibility(View.VISIBLE);
        binding.icon3Id.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(linearLayoutManager!=null)
            currentScrollPosition = linearLayoutManager.findFirstVisibleItemPosition();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewCases!=null)
            recyclerViewCases.scrollToPosition(currentScrollPosition);
    }
}
