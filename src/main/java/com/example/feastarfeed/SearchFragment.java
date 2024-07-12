package com.example.feastarfeed;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;

import org.checkerframework.checker.units.qual.A;


public class SearchFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    GoogleMap googleMap;
    private AutocompleteSupportFragment autoCompleteFragment;

    private final LatLng TAIPEI = new LatLng(25.0330, 121.5654);
    private final LatLng TAICHUNG = new LatLng(24.1477, 120.6736);
    private final LatLng CHIAYI = new LatLng(23.4801, 120.4472);

    private Marker markerTAIPEI;
    private Marker markerTAICHUNG;
    private Marker markerCHIAYI;

    private final int FINE_PERMISSION_CODE = 1;

    FusedLocationProviderClient fusedLocationProviderClient;

    boolean bottomVideoViewVisible = false;


    public static CharSequence placeName, placeAddress;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    String address;

    public static ArrayList<String> nameArraylist;

    double latitude, longitude;
    Location currentLocation;

    FragmentManager fragmentManager;

    public SearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Locale locale = new Locale("zh", "TW");
        Locale.setDefault(locale);

        autoCompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autoCompleteFragment);

        Places.initialize(getContext().getApplicationContext(), "AIzaSyBJiZZZVX1857CpQvpsGUKpyOdmvHkJW3o",locale);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        RectangularBounds bounds = RectangularBounds.newInstance(
                // 設置搜索範圍的邊界
                new LatLng(21.892000, 119.560000),
                new LatLng(25.305000, 122.008000)
        );

        autoCompleteFragment.setLocationBias(bounds);
        getLastLocation();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        LatLng myLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,12f));

        LatLng chiayi = new LatLng(23.464056589663414, 120.44544208361198);
        googleMap.addMarker(new MarkerOptions()
                .position(chiayi)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chiayi, 12f));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);

                if (bottomVideoViewVisible) {
                    // 隐藏 Bottom_VIdeo_View
                    Fragment bottomVideoViewFragment = fragmentManager.findFragmentById(R.id.frame_layout);
                    if (bottomVideoViewFragment != null) {
                        fragmentTransaction.remove(bottomVideoViewFragment);
                        bottomVideoViewVisible = false;
                    }
                } else {
                    // 显示 Bottom_VIdeo_View
                    fragmentTransaction.replace(R.id.frame_layout, new Bottom_VIdeo_View());
                    bottomVideoViewVisible = true;
                }

                fragmentTransaction.commit();

                return false;
            }
        });

        nameArraylist = new ArrayList<>();

        PlacesClient placesClient = Places.createClient(this.getContext());
        //自動新增地點
        DatabaseReference placeRef = database.getReference("Videos");
        placeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    address = dataSnapshot.child("title").getValue(String.class);
                    AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                    FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                            .setTypeFilter(TypeFilter.ESTABLISHMENT) // 指定搜索类型为店铺或地点
                            .setSessionToken(token) // 设置会话令牌
                            .setQuery(address) // 设置搜索查询文本
                            .build();

// 发送请求并处理响应
                    placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener((response) -> {
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            String placeId = prediction.getPlaceId(); // 获取地点的 ID
                            String placeName1 = prediction.getPrimaryText(null).toString(); // 获取地点的主要文本（名称）
                            Log.i(TAG, "Place ID: " + placeId + ", Place Name: " + placeName1);
                            nameArraylist.add(placeName1);
                            Log.d("SearchFragment","nameArray : "+nameArraylist);
                            // 在这里处理您获取的地点信息

                            FetchPlaceRequest LAT_LNGrequest = FetchPlaceRequest.builder(placeId, Arrays.asList(Place.Field.LAT_LNG))
                                    .build();

                            // 发起请求
                            placesClient.fetchPlace(LAT_LNGrequest).addOnSuccessListener((fetchResponse) -> {
                                Place place = fetchResponse.getPlace();
                                // 获取地点的经纬度信息
                                LatLng latLng = place.getLatLng();
                                placeName = place.getName();
                                placeAddress = place.getAddress();
                                if (latLng != null) {
                                    latitude = latLng.latitude;
                                    longitude = latLng.longitude;
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    // 使用经纬度信息进行后续操作
                                    Log.i(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                                } else {
                                    // 未找到经纬度信息
                                }

                            }).addOnFailureListener((exception) -> {
                                // 处理请求失败情况
                            });
                        }
                    }).addOnFailureListener((exception) -> {
                        // 处理请求失败的情况
                        Log.e(TAG, "Place Autocomplete request failed: " + exception.getMessage());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        autoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                LatLng latLng = place.getLatLng();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

                placeName = place.getName();
                placeAddress = place.getAddress();
                Log.i(TAG, "Place Address: " + placeAddress);

            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

// Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

// Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this.getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLastLocation();
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.map);

                    if (mapFragment != null) {
                        mapFragment.getMapAsync(SearchFragment.this);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else {
                Toast.makeText(getContext(),"Location Permission is denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    public interface DirectionsApiService {
        @GET("directions/json")
        Call<DirectionsResponse> getDirections(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("key") String apiKey
        );
    }

    public class DirectionsResponse {
        @SerializedName("routes")
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }
    }

    public class Route {
        @SerializedName("overview_polyline")
        private Polyline polyline;

        public Polyline getPolyline() {
            return polyline;
        }
    }

    public class Polyline {
        @SerializedName("points")
        private String points;

        public String getPoints() {
            return points;
        }
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng destination) {
        String apiKey = "AIzaSyBJiZZZVX1857CpQvpsGUKpyOdmvHkJW3o";
        String originString = origin.latitude + "," + origin.longitude;
        String destinationString = destination.latitude + "," + destination.longitude;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionsApiService service = retrofit.create(DirectionsApiService.class);
        Call<DirectionsResponse> call = service.getDirections(originString, destinationString, apiKey);
        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful()) {
                    DirectionsResponse directionsResponse = response.body();
                    if (directionsResponse != null) {
                        List<Route> routes = directionsResponse.getRoutes();
                        if (routes != null && !routes.isEmpty()) {
                            Route route = routes.get(0); // 獲取第一條路徑
                            if (route != null) {
                                PolylineOptions polylineOptions = new PolylineOptions();
                                List<LatLng> points = decodePolyline(route.getPolyline().getPoints());
                                for (LatLng point : points) {
                                    polylineOptions.add(point);
                                }
                                googleMap.addPolyline(polylineOptions);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                // 處理請求失敗的情況
            }
        });
    }

    // 解碼 Directions API 返回的編碼過的經緯度點
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double latlng = lat / 1E5;
            double lnglng = lng / 1E5;
            LatLng point = new LatLng(latlng, lnglng);
            polyline.add(point);
        }
        return polyline;
    }
}