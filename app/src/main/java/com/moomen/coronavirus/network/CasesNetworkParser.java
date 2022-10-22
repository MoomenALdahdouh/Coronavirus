package com.moomen.coronavirus.network;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.moomen.coronavirus.utils.Utils;
import com.moomen.coronavirus.model.Case;
import com.moomen.coronavirus.model.CountryInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CasesNetworkParser {
    private Context context;
    public static final String CASES_SUMMARY =
            "https://api.covid19api.com/summary";
    public static final String CASES_COUNTRIES_INFO =
            "https://restcountries.eu/rest/v2/all";

    private HashMap<String, CountryInfo> mapCountryCodeToInfo = new HashMap<>();

    private RequestQueue queue;
    private StringRequest request;

    public interface OnCasesFilledListener {
        void startFillingCases(ArrayList<Case> cases);
    }

    private OnCasesFilledListener casesListener;

    public <T extends OnCasesFilledListener> void setOnCasesFilledListener(T casesOrMapFragment) {
        this.casesListener = casesOrMapFragment;
    }

    private static ArrayList<Case> mCases;

    public CasesNetworkParser(Context context){
        this.context = context;
    }

    public void parseCountriesCasesJson() throws JSONException {
        if(mCases != null) {
            if(casesListener != null)
                casesListener.startFillingCases(mCases);
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, CASES_SUMMARY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ArrayList<Case> listOfCases = new ArrayList<>();
                    JSONObject root = new JSONObject(response);

                    JSONObject global = root.getJSONObject("Global");
                    Case globalCase = fillCase(new Case(), global, true);
                    listOfCases.add(globalCase);

                    JSONArray countries = root.getJSONArray("Countries");
                    for(int i = 0; i<countries.length(); i++) {
                        JSONObject country = countries.getJSONObject(i);
                        Case currentCase = fillCase(new Case(), country, false);
                        listOfCases.add(currentCase);
                    }
                    listOfCases = fillCountriesInfoMap(listOfCases);
                    mCases = new ArrayList<>(listOfCases);
                    if(casesListener != null)
                        casesListener.startFillingCases(listOfCases);
                }catch (JSONException ex) { }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        try {
                            parseCountriesCasesJson();
                        }catch (Exception ex) {}
                    }
                }, 500);
            }
        });
        queue.add(request);
    }

    private Case fillCase(Case currentCase, JSONObject jsonObject, boolean isGlobal){
        try {
            if(isGlobal)
                currentCase.setGlobal(true);
            currentCase.setNewConfirmed(jsonObject.getInt("NewConfirmed"));
            currentCase.setTotalConfirmed(jsonObject.getInt("TotalConfirmed"));
            currentCase.setNewDeaths(jsonObject.getInt("NewDeaths"));
            currentCase.setTotalDeaths(jsonObject.getInt("TotalDeaths"));
            currentCase.setNewRecovered(jsonObject.getInt("NewRecovered"));
            currentCase.setTotalRecovered(jsonObject.getInt("TotalRecovered"));
            currentCase.setCountryCode(jsonObject.getString("CountryCode"));
            currentCase.setName(jsonObject.getString("Country"));
            return currentCase;
        }catch (JSONException ex) { return currentCase; }
    }

    public ArrayList<Case> fillCountriesInfoMap(ArrayList<Case> cases) throws JSONException {
        JSONArray root = new JSONArray(Utils.loadJSONFromAsset(context,"countries_info.json"));
        for (int i = 0; i <root.length() ; i++) {
            try {
                JSONObject currentCountry = root.getJSONObject(i);
                JSONArray latlng = currentCountry.getJSONArray("latlng");
                LatLng mapLatLng = new LatLng(latlng.getDouble(0), latlng.getDouble(1));
                String flag = currentCountry.getString("flag");
                if (latlng.length() == 0 || flag.equals("")) continue;
                mapCountryCodeToInfo.put(
                        currentCountry.getString("alpha2Code"), new CountryInfo(mapLatLng, flag));
            }catch (Exception ex) {} // Move on when we don't have some info of some country
        }
        return passCountriesInfoToCases(cases);
    }

    private ArrayList<Case> passCountriesInfoToCases(ArrayList<Case> cases) {
        ArrayList<Case> listOfCountriesToBeRemoved = new ArrayList<>();
        for (Case currentCase: cases) {
            try {
                CountryInfo info = mapCountryCodeToInfo.get(currentCase.getCountryCode());
                if (currentCase.isGlobal())
                    continue;
                if (info == null || info.getLatLng() == null || info.getFlagUrl() == null)
                    listOfCountriesToBeRemoved.add(currentCase);
                currentCase.setLatLng(info.getLatLng());
                currentCase.setFlagUrl(info.getFlagUrl());
            }catch (Exception ex) { listOfCountriesToBeRemoved.add(currentCase); }
        }
        cases.removeAll(listOfCountriesToBeRemoved);
        return cases;
    }
}

