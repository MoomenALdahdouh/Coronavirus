package com.moomen.coronavirus.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.moomen.coronavirus.R;
import com.moomen.coronavirus.databinding.FragmentMapBinding;
import com.moomen.coronavirus.model.Case;
import com.moomen.coronavirus.network.CasesNetworkParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback, CasesNetworkParser.OnCasesFilledListener {
    private FragmentMapBinding binding;

    private MapView map;

    private GoogleMap googleMap;

    private ArrayList<Case> mCases;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setUpMapView(savedInstanceState);

        return root;
    }

    private void setUpMapView(Bundle savedInstanceState) {
        map = binding.getRoot().findViewById(R.id.map_id);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        CasesNetworkParser parser = new CasesNetworkParser(getContext());
        parser.setOnCasesFilledListener(this);
        try {
            parser.parseCountriesCasesJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(final Marker marker) {
                final Case markerCase = (Case) marker.getTag();
                View window = LayoutInflater.from(getContext()).inflate(R.layout.case_item, null);

                TextView name = window.findViewById(R.id.text_view_country_id);
                TextView newConfirmed = window.findViewById(R.id.text_view_new_confirmed_id);
                TextView newDeaths = window.findViewById(R.id.text_view_new_deaths_id);
                TextView newRecovered = window.findViewById(R.id.text_view_new_recovered_id);
                TextView confirmed = window.findViewById(R.id.text_view_count_confirmed_id);
                TextView deaths = window.findViewById(R.id.text_view_count_recovered_id);
                TextView recovered = window.findViewById(R.id.text_view_count_deaths_id);

                ImageView flag = window.findViewById(R.id.image_view_flag_id);
                Picasso.get()
                        .load("https://www.countryflags.io/" + markerCase.getCountryCode() + "/flat/64.png")
                        .into(flag, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (markerCase.canUpdateWindow()) {
                                    markerCase.setCanUpdateWindow(false);
                                    marker.showInfoWindow();
                                }
                            }

                            @Override
                            public void onError(Exception e) { }
                        });
                name.setText(markerCase.getName());
                newConfirmed.setText(Integer.toString(markerCase.getNewConfirmed()));
                newDeaths.setText(Integer.toString(markerCase.getNewDeaths()));
                newRecovered.setText(Integer.toString(markerCase.getNewRecovered()));
                confirmed.setText(Integer.toString(markerCase.getTotalConfirmed()));
                deaths.setText(Integer.toString(markerCase.getTotalDeaths()));
                recovered.setText(Integer.toString(markerCase.getTotalRecovered()));
                return window;
            };

            @Override
            public View getInfoContents(final Marker marker) {
                final Case markerCase = (Case) marker.getTag();
                View window = LayoutInflater.from(getContext()).inflate(R.layout.case_item, null);

                TextView name = window.findViewById(R.id.text_view_country_id);
                TextView newConfirmed = window.findViewById(R.id.text_view_new_confirmed_id);
                TextView newDeaths = window.findViewById(R.id.text_view_new_deaths_id);
                TextView newRecovered = window.findViewById(R.id.text_view_new_recovered_id);
                TextView confirmed = window.findViewById(R.id.text_view_count_confirmed_id);
                TextView deaths = window.findViewById(R.id.text_view_count_recovered_id);
                TextView recovered = window.findViewById(R.id.text_view_count_deaths_id);

                ImageView flag = window.findViewById(R.id.image_view_flag_id);
                Picasso.get()
                        .load("https://www.countryflags.io/" + markerCase.getCountryCode() + "/flat/64.png")
                        .into(flag, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (markerCase.canUpdateWindow()) {
                                    markerCase.setCanUpdateWindow(false);
                                    marker.showInfoWindow();
                                }
                            }

                            @Override
                            public void onError(Exception e) { }
                        });
                name.setText(markerCase.getName());
                newConfirmed.setText(Integer.toString(markerCase.getNewConfirmed()));
                newDeaths.setText(Integer.toString(markerCase.getNewDeaths()));
                newRecovered.setText(Integer.toString(markerCase.getNewRecovered()));
                confirmed.setText(Integer.toString(markerCase.getTotalConfirmed()));
                deaths.setText(Integer.toString(markerCase.getTotalDeaths()));
                recovered.setText(Integer.toString(markerCase.getTotalRecovered()));
                return window;
            }
        });
    }

    @Override
    public void startFillingCases(ArrayList<Case> cases) {
        mCases = cases;
        Case globalCase = cases.get(0); // TODO Do something with it
        for (int i = 1 ; i<cases.size() ; i++) {
            Case currentCase = cases.get(i);
            LatLng latLng = currentCase.getLatLng();
            googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(currentCase.getTotalConfirmed()/3)
                .strokeWidth(3f)
                .strokeColor(Color.RED)
                .fillColor(0x22FF0000));

            googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(getResources().getResourceName(R.drawable.map_marker2), 16, 16))))
                .setTag(currentCase);
        }
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getContext().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }
}