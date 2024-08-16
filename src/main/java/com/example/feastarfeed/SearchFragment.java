package com.example.feastarfeed;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLngBounds;
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

    private final int FINE_PERMISSION_CODE = 1;

    FusedLocationProviderClient fusedLocationProviderClient;

    boolean bottomVideoViewVisible = false;


    public static String placeName, placeAddress;

    String foodTag,videoName;

    int videoId;

    public static ArrayList<String> foodsTitleArray;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    String address;

    double latitude, longitude;
    Location currentLocation;

    FragmentManager fragmentManager;

    TypeFilter typeFilter = TypeFilter.ESTABLISHMENT;

    public SearchView searchView;

    ListView listView;

    ArrayList<String> placeArraylist;

    PlaceAdapter placeAdapter;

    int x=0, y=0, count=0, a=0, b=0, count1=0;

    String foodsTitle;

    String foodsTitle1;

    String keyIn;

    String query = "";

    public SearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.searchView);
        listView = view.findViewById(R.id.listView);

        listView.setVisibility(View.GONE);

        Locale locale = new Locale("zh", "TW");
        Locale.setDefault(locale);

        Places.initialize(getContext().getApplicationContext(), "AIzaSyBJiZZZVX1857CpQvpsGUKpyOdmvHkJW3o", locale);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        getLastLocation();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 获取传递的查询文本

        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12f));

        LatLng chiayi = new LatLng(23.464056589663414, 120.44544208361198);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chiayi, 14f));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                placeName = (String) marker.getTag();
                placeAddress = marker.getSnippet();
                Log.d("SearcgFragment", "placeAddress : " + placeAddress);
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

                return true;
            }
        });

        PlacesClient placesClient = Places.createClient(this.getContext());
        placeArraylist = new ArrayList<>();
        ArrayList<String> foodTagArraylist;
        foodTagArraylist = new ArrayList<>();
        //自動新增地點
        DatabaseReference placeRef = database.getReference("Videos");
        placeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    address = dataSnapshot.child("title").getValue(String.class);
                    placeArraylist.add(address);
                    Iterable<DataSnapshot> dataSnapshotIterable = dataSnapshot.child("Foodtags").getChildren();
                    for (DataSnapshot dataSnapshot1 : dataSnapshotIterable) {
                        foodTag = dataSnapshot1.getValue(String.class);
                        foodTagArraylist.add(foodTag);
                    }

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

                            // 在这里处理您获取的地点信息

                            FetchPlaceRequest LAT_LNGrequest = FetchPlaceRequest.builder(placeId, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                                    .build();

                            // 发起请求
                            placesClient.fetchPlace(LAT_LNGrequest).addOnSuccessListener((fetchResponse) -> {
                                Place place = fetchResponse.getPlace();
                                // 获取地点的经纬度信息
                                LatLng latLng = place.getLatLng();
                                if (latLng != null) {
                                    latitude = latLng.latitude;
                                    longitude = latLng.longitude;
                                    Marker marker = googleMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    marker.setTag(placeName1);
                                    marker.setSnippet(place.getAddress());
                                    marker.setTitle(foodTagArraylist.toString());
                                    Log.d("SearchFragment", "getAddress : " + place.getAddress());
                                    // 使用经纬度信息进行后续操作
                                    Log.i(TAG, "Latitude: " + latitude + ", Longitude: " + longitude);
                                    Log.d("SearchFragment", "foodTags : " + marker.getTitle());
                                }  // 未找到经纬度信息


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

        foodsTitleArray = new ArrayList<>();
        placeAdapter = new PlaceAdapter(getContext(), placeArraylist);
        listView.setAdapter(placeAdapter);

        Bundle args = getArguments();
        if (args != null && args.containsKey("tag")) {
            query = args.getString("tag", "");
        } else if (args != null && args.containsKey("text")) {
            query = args.getString("text","");
            placeAdapter.getFilter().filter(query);
            listView.setVisibility(View.VISIBLE);
        }
        // 设置 SearchView 的查询文本
        if (query != null && !query.isEmpty()) {
            searchView.setQuery(query, true);
            x = 0;
            y = 0;
            count = 0;

            b = 0;

            foodsTitle1 = "";
            foodsTitleArray.clear();

            DatabaseReference Ref = database.getReference("Videos");
            Ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        Log.d("SearchFragment","x = "+x);
                        videoName = dataSnapshot.child("title").getValue(String.class);
                        videoId = dataSnapshot.child("id").getValue(int.class);

                        if (videoName != null && videoName.equals(query)){
                            y=videoId;
                            count++;
                        }
                        Iterable<DataSnapshot> dataSnapshotIterable = dataSnapshot.child("Foodtags").getChildren();
                        for (DataSnapshot dataSnapshot1 : dataSnapshotIterable) {
                            foodTag = dataSnapshot1.getValue(String.class);
                            if (foodTag != null && foodTag.equals(query)){ y = videoId; count++; }
                            Log.d("SearchFragment","count = "+count);
                            Log.d("SearchFragment","y = "+y);
                        }

                        if (y != 0){
                            DatabaseReference titleRef = Ref.child("V"+y).child("title");
                            titleRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    foodsTitle = snapshot.getValue(String.class);
                                    if (!foodsTitle1.equals(foodsTitle)){
                                        foodsTitle1 = foodsTitle;
                                        foodsTitleArray.add(foodsTitle1);
                                        Log.d("SearchFragment","foodstitle = "+foodsTitle);
                                        Log.d("SearchFragment","foodsTitleArray.size() = "+foodsTitleArray.size());
                                        if (foodsTitleArray.size() == count){
                                            Log.d("SearchFragment","foodsArray = "+foodsTitleArray);
                                            Log.d("placeAdapter","placeAdapter.getCount() = "+placeAdapter.getCount());
                                            placeAdapter.filteredList.addAll(foodsTitleArray);
                                            Log.d("placeAdapter","placeAdapter.filteredList = "+placeAdapter.filteredList);
                                            placeAdapter.notifyDataSetChanged();
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            listView.setVisibility(View.VISIBLE);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                keyIn = newText;
                x = 0;
                y = 0;
                count = 0;
                foodsTitle1 = "";
                foodsTitleArray.clear();

                DatabaseReference Ref = database.getReference("Videos");
                Ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ++x;
                            Log.d("SearchFragment","x = "+x);
                            Iterable<DataSnapshot> dataSnapshotIterable = dataSnapshot.child("Foodtags").getChildren();
                            for (DataSnapshot dataSnapshot1 : dataSnapshotIterable) {
                                foodTag = dataSnapshot1.getValue(String.class);
                                if (foodTag != null && foodTag.equals(newText)){ y = x; count++; }
                                Log.d("SearchFragment","count = "+count);
                                Log.d("SearchFragment","y = "+y);
                            }

                            if (y != 0){
                                DatabaseReference titleRef = Ref.child("V"+y).child("title");
                                titleRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        foodsTitle = snapshot.getValue(String.class);
                                        if (!foodsTitle1.equals(foodsTitle)){
                                            foodsTitle1 = foodsTitle;
                                            foodsTitleArray.add(foodsTitle1);
                                            Log.d("SearchFragment","foodstitle = "+foodsTitle);
                                            Log.d("SearchFragment","foodsTitleArray.size() = "+foodsTitleArray.size());
                                            if (foodsTitleArray.size() == count){
                                                Log.d("SearchFragment","foodsArray = "+foodsTitleArray);
                                                Log.d("placeAdapter","placeAdapter.getCount() = "+placeAdapter.getCount());
                                                placeAdapter.filteredList.addAll(foodsTitleArray);
                                                Log.d("placeAdapter","placeAdapter.filteredList = "+placeAdapter.filteredList);
                                                placeAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if (newText.isEmpty()) {
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    placeAdapter.getFilter().filter(keyIn);

                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = placeAdapter.filteredList.get(position);

                searchView.setQuery(selectedPlace, true);
                listView.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ESTABLISHMENT) // 指定搜索类型为店铺或地点
                        .setSessionToken(token) // 设置会话令牌
                        .setQuery(selectedPlace) // 设置搜索查询文本
                        .build();
// 发送请求并处理响应
                placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener((response) -> {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        String placeId = prediction.getPlaceId(); // 获取地点的 ID
                        String placeName1 = prediction.getPrimaryText(null).toString(); // 获取地点的主要文本（名称）
                        Log.i(TAG, "Place ID: " + placeId + ", Place Name: " + placeName1);

                        // 在这里处理您获取的地点信息

                        FetchPlaceRequest LAT_LNGrequest = FetchPlaceRequest.builder(placeId, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS))
                                .build();

                        // 发起请求
                        placesClient.fetchPlace(LAT_LNGrequest).addOnSuccessListener((fetchResponse) -> {
                            Place place = fetchResponse.getPlace();
                            // 获取地点的经纬度信息
                            LatLng latLng = place.getLatLng();
                            if (latLng != null) {
                                latitude = latLng.latitude;
                                longitude = latLng.longitude;

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

                            }  // 未找到经纬度信息


                        }).addOnFailureListener((exception) -> {
                            // 处理请求失败情况
                        });
                    }
                }).addOnFailureListener((exception) -> {
                    // 处理请求失败的情况
                    Log.e(TAG, "Place Autocomplete request failed: " + exception.getMessage());
                });

            }
        });

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(chiayi) // 設置一個偏好的區域中心
                .build();
        RectangularBounds locationBias = RectangularBounds.newInstance(bounds);

// Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

// Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this.getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
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
            ActivityCompat.requestPermissions(requireActivity(), new String[]{ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
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
                if (location != null) {
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(getContext(), "Location Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

}