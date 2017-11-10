package com.app.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mit.mitsutils.MitsUtils;
import com.app.demo.pojo.MarkerPojo;
import com.app.demo.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private Activity activity;

    private View llMap;
    private ImageView ivMap;
    private RecyclerView rvItems;
    private GoogleMap map;

    private MarkerRecyclerAdapter adapter;
    private HashMap<Marker, MarkerPojo> hashMap = new HashMap<>();
    private ArrayList<MarkerPojo> listMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_main);

        setupViews();

        setupData();

        if(AppUtils.isNetworkAvailable(activity))
        {
            loadMarkersAsync();
        }
        else
        {
            Toast.makeText(activity, "Np internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViews()
    {
        llMap = findViewById(R.id.llMap);
        ivMap = (ImageView) findViewById(R.id.ivMap);
        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(activity));
    }

    private void setupData()
    {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMarkers(GoogleMap googleMap)
    {
        try {
            for (int i = 0; i < listMarkers.size(); i++)
            {
                MarkerPojo markerPojo = listMarkers.get(i);

                MarkerOptions markerOptions = new MarkerOptions();
                // Setting latitude and longitude for the marker
                LatLng latLng = new LatLng(AppUtils.getValidAPIDoubleResponse(markerPojo.getDispLatitude()), AppUtils.getValidAPIDoubleResponse(markerPojo.getDispLongitude()));
                markerOptions.position(latLng);

                // Adding marker on the Google Map
                Marker marker = googleMap.addMarker(markerOptions);
                hashMap.put(marker, markerPojo);
            }

            googleMap.setOnMarkerClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickEvents()
    {
        llMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(rvItems.getVisibility() == View.VISIBLE)
                    {
                        ivMap.setImageResource(R.drawable.ic_list);
                        rvItems.setVisibility(View.GONE);
                    }
                    else
                    {
                        ivMap.setImageResource(R.drawable.ic_map);
                        rvItems.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // move the map's camera to the same location.
        try {
            map = googleMap;
            LatLng seattle = new LatLng(47.6129432, -122.4821499);
//            googleMap.addMarker(new MarkerOptions().position(seattle));

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(seattle));
            googleMap.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ) );

            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnMapLoadedCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMarkersAsync()
    {
        try
        {
            new AsyncTask<Void, Void, Void>()
            {
                private ProgressDialog progressDialog;
                @Override
                protected void onPreExecute()
                {
                    try {
                        listMarkers = new ArrayList<>();

                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setMessage("Loading...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... params)
                {
                    loadMarkers();
                    return null;
                }

                private void loadMarkers()
                {
                    try
                    {
                        final String API = "https://www.wikileaf.org/develop/masterapi/all_dispensary/";
                        HashMap<String, String> hashMap = new HashMap<String , String>();
                        hashMap.put("nelat", "47.53440275454189");
                        hashMap.put("nelng", "-122.6587424003418");
                        hashMap.put("swlat", "47.67791780444596");
                        hashMap.put("swlng", "-122.0053991996582");

                        String response = MitsUtils.readJSONServiceUsingPOST(API, hashMap);

                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int dispId = AppUtils.getValidAPIIntegerResponse(jsonObject.getString("disp_id"));
                            String dispName = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_name"));
                            String dispPhone = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_phone"));
                            String dispCity = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_city"));
                            String dispState = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_state"));
                            String dispLatitude = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_latitude"));
                            String dispLongitude = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_longitude"));
                            String dispUrl = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_url"));
                            String dispType = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_type"));
                            String dispStoreType = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_store_type"));
                            String dispImage = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_image"));
                            int dispTotalReviews = AppUtils.getValidAPIIntegerResponse(jsonObject.getString("disp_total_reviews"));;
                            String dispRating = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_rating"));
                            String dispPrice1Gm = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_price_1_gm"));
                            String dispPrice18Oz = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_price_1/8_oz"));
                            String dispPrice14Oz = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_price_1/4_oz"));
                            String dispPrice12Oz = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_price_1/2_oz"));
                            String dispPrice1Oz = AppUtils.getValidAPIStringResponse(jsonObject.getString("disp_price_1_oz"));

                            MarkerPojo markerPojo = new MarkerPojo();
                            markerPojo.setDispId(dispId);
                            markerPojo.setDispName(dispName);
                            markerPojo.setDispPhone(dispPhone);
                            markerPojo.setDispCity(dispCity);
                            markerPojo.setDispState(dispState);
                            markerPojo.setDispLatitude(dispLatitude);
                            markerPojo.setDispLongitude(dispLongitude);
                            markerPojo.setDispUrl(dispUrl);
                            markerPojo.setDispType(dispType);
                            markerPojo.setDispStoreType(dispStoreType);
                            markerPojo.setDispImage(dispImage);
                            markerPojo.setDispTotalReviews(dispTotalReviews);
                            markerPojo.setDispRating(dispRating);
                            markerPojo.setDispPrice1Gm(getValidPrice(dispPrice1Gm));
                            markerPojo.setDispPrice18Oz(getValidPrice(dispPrice18Oz));
                            markerPojo.setDispPrice14Oz(getValidPrice(dispPrice14Oz));
                            markerPojo.setDispPrice12Oz(getValidPrice(dispPrice12Oz));
                            markerPojo.setDispPrice1Oz(getValidPrice(dispPrice1Oz));

                            listMarkers.add(markerPojo);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                private String getValidPrice(String price)
                {
                    int p = AppUtils.getValidAPIIntegerResponse(price);
                    if(p == 0)
                    {
                        return "NA";
                    }

                    return price;
                }

                @Override
                protected void onPostExecute(Void result)
                {
                    super.onPostExecute(result);
                    try {
                        if(progressDialog != null)
                        {
                            progressDialog.dismiss();
                            progressDialog.cancel();
                        }

                        setupClickEvents();

                        addMarkers(map);

                        adapter = new MarkerRecyclerAdapter(listMarkers);
                        rvItems.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void openBottomSheetDialog(final MarkerPojo markerPojo)
    {
        try {
            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
            View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_dialog_marker, null);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.show();

            TextView btnViewMenu = (TextView) sheetView.findViewById(R.id.btnViewMenu);
            ImageView ivMarker = (ImageView) sheetView.findViewById(R.id.ivMarker);
            TextView txtTitle = (TextView) sheetView.findViewById(R.id.txtTitle);
            TextView txtPrice = (TextView) sheetView.findViewById(R.id.txtPrice);
            RatingBar rbRatings = (RatingBar) sheetView.findViewById(R.id.rbRatings);
            TextView txtReviewCounts = (TextView) sheetView.findViewById(R.id.txtReviewCounts);
            TextView txtLocation = (TextView) sheetView.findViewById(R.id.txtLocation);
            TextView txtDistance = (TextView) sheetView.findViewById(R.id.txtDistance);

            try {
                if(markerPojo.getDispImage().length() > 0)
                {
                    Glide.with(activity)
                            .load(markerPojo.getDispImage())
                            .centerCrop()
                            .dontAnimate()
                            .into(ivMarker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            txtTitle.setText(markerPojo.getDispName());
            if(markerPojo.getDispPrice18Oz().equals("NA"))
            {
                txtPrice.setText(markerPojo.getDispPrice18Oz());
            }
            else
            {
                txtPrice.setText("$ " + markerPojo.getDispPrice18Oz());
            }
            txtReviewCounts.setText("(" + markerPojo.getDispTotalReviews() + ")");
            txtLocation.setText(markerPojo.getDispCity() + ", " + markerPojo.getDispState());
            /*txtDistance.setText("");*/
            rbRatings.setRating(AppUtils.getValidAPIIntegerResponse(markerPojo.getDispRating()));

            btnViewMenu.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try {
                        mBottomSheetDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        try {
            MarkerPojo markerPojo = hashMap.get(marker);
            if(markerPojo != null && markerPojo.getDispName().length() > 0)
            {
                openBottomSheetDialog(markerPojo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onMapLoaded()
    {
        LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;
        System.out.println(curScreen.toString());

        //top-left corner
        double nelatitude=curScreen.northeast.latitude;
        double swlongitude=curScreen.southwest.longitude;

        //bottom-right corner
        double swlatitude=curScreen.southwest.latitude;
        double nelongitude=curScreen.northeast.longitude;

        Log.v("MAP BOUNDS", "nelat: " + nelatitude + " & swlong: " + swlongitude
                + " & swlat: " + swlatitude + " & nelong: " + nelongitude);

        LatLng southwest = new LatLng(swlatitude, swlongitude);
        LatLng northeast = new LatLng(nelatitude, nelongitude);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(southwest, northeast), 0));
        map.animateCamera( CameraUpdateFactory.zoomTo( 12.0f ) );
    }

    private class MarkerRecyclerAdapter extends RecyclerView.Adapter<MarkerRecyclerAdapter.ViewHolder> {
        ArrayList<MarkerPojo> items;

        MarkerRecyclerAdapter(ArrayList<MarkerPojo> list) {
            this.items = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_marker_details, viewGroup, false);
            return new ViewHolder(v);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivMarker;
            final TextView txtTitle, txtPrice, txtReviewCounts, txtLocation, txtDistance;
            final RatingBar rbRatings;
            final View viewline;

            ViewHolder(View convertView) {
                super(convertView);
                ivMarker = (ImageView) convertView.findViewById(R.id.ivMarker);
                txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
                txtPrice = (TextView) convertView.findViewById(R.id.txtPrice);
                rbRatings = (RatingBar) convertView.findViewById(R.id.rbRatings);
                txtReviewCounts = (TextView) convertView.findViewById(R.id.txtReviewCounts);
                txtLocation = (TextView) convertView.findViewById(R.id.txtLocation);
                txtDistance = (TextView) convertView.findViewById(R.id.txtDistance);
                viewline = convertView.findViewById(R.id.viewline);
            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final MarkerPojo markerPojo = items.get(position);

            if (position == items.size() - 1) {
                holder.viewline.setVisibility(View.INVISIBLE);
            } else {
                holder.viewline.setVisibility(View.VISIBLE);
            }

            try {
                if(markerPojo.getDispImage().length() > 0)
                {
                    Glide.with(activity)
                            .load(markerPojo.getDispImage())
                            .centerCrop()
                            .dontAnimate()
                            .into(holder.ivMarker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.txtTitle.setText(markerPojo.getDispName());

            if(markerPojo.getDispPrice18Oz().equals("NA"))
            {
                holder.txtPrice.setText(markerPojo.getDispPrice18Oz());
            }
            else
            {
                holder.txtPrice.setText("$ " + markerPojo.getDispPrice18Oz());
            }

            holder.txtReviewCounts.setText("(" + markerPojo.getDispTotalReviews() + ")");
            holder.txtLocation.setText(markerPojo.getDispCity() + ", " + markerPojo.getDispState());
            /*holder.txtDistance.setText("");*/
            holder.rbRatings.setRating(AppUtils.getValidAPIIntegerResponse(markerPojo.getDispRating()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
