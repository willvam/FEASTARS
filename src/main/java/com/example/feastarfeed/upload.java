package com.example.feastarfeed;

import static android.content.ContentValues.TAG;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import com.example.feastarfeed.ml.ModelUnquant;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.Manifest;



public class upload extends AppCompatActivity{

    private static final int REQUEST_VIDEO_CAPTURE =1;
    StorageReference storageReference;
    LinearProgressIndicator progressIndicator;
    Uri video;
    TextView uploadvideo,selectvideo,selectphoto,opencamera;
    ImageButton Back_button;
    ImageView videopreview;
    EditText titleEditText,addressEditText,priceEditText,descriptionEditText;
    TextView foodTagsEditText,foodclass,selectedDateEditText;
    DatePicker datePicker;
    private DatabaseReference databaseReference;
    DatabaseReference userRef,totalFoodTagRef;
    int imageSize = 224;

    String newVid,newcont;
    long longvid;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private AutocompleteSupportFragment autoCompleteFragment;

    FusedLocationProviderClient fusedLocationProviderClient;

    String placeName,placeAddress;

    private StringBuffer selectedTagsBuffer = new StringBuffer();

    private static final int CAMERA_PERMISSION_CODE = 101;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private static final long TIMEOUT_USEC = 10000; // 10毫秒


    Button Test;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add);
        databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
        totalFoodTagRef = FirebaseDatabase.getInstance().getReference("totalfoodtag");//全部的TAg
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseApp.initializeApp((this));
        storageReference = FirebaseStorage.getInstance().getReference();

        MaterialToolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressIndicator = findViewById(R.id.process);
        videopreview = findViewById(R.id.vediopreview);

        uploadvideo = findViewById(R.id.uploadvideo);
        selectvideo =findViewById(R.id.selectvideo);
        selectphoto = findViewById(R.id.selectphoto);
        foodclass = findViewById(R.id.foodclass);
        opencamera =findViewById(R.id.opencamera);
        Test = findViewById(R.id.test);
        foodTagsEditText = findViewById(R.id.foodTagsEditText);
        titleEditText = findViewById(R.id.titleEditText);
        priceEditText = findViewById(R.id.priceEditText);
        addressEditText = findViewById(R.id.addressEditText);
        selectedDateEditText = findViewById(R.id.selectedDateEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        Locale locale = new Locale("zh", "TW");
        Locale.setDefault(locale);

        autoCompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autoCompleteFragment);

        Places.initialize(this.getApplicationContext(), "AIzaSyBJiZZZVX1857CpQvpsGUKpyOdmvHkJW3o", locale);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        RectangularBounds bounds = RectangularBounds.newInstance(
                // 設置搜索範圍的邊界
                new LatLng(21.892000, 119.560000),
                new LatLng(25.305000, 122.008000)
        );

        autoCompleteFragment.setLocationBias(bounds);

        autoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                //LatLng latLng = place.getLatLng();
                placeName = place.getName();
                placeAddress = place.getAddress();
                titleEditText.setText(placeName);
                addressEditText.setText(placeAddress);
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        Back_button = findViewById(R.id.back_button);

        datePicker = findViewById(R.id.datePicker);
        Test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d("upload","點季");
                if (ActivityCompat.checkSelfPermission(upload.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 如果權限未被授予，請求相應的權限
                    ActivityCompat.requestPermissions(upload.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    return;
                }

                // 如果已經有相機權限，啟動相機錄像
                openCameraForVideoCaptureIntent();
            }
        });
        selectvideo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                activityResultLauncher.launch(intent);
            }
        });
        selectphoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 允许用户选择多个照片
                activityResultLauncher.launch(intent);
            }
        });

        uploadvideo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (isVideoValid(video)) {//如果長度小於40秒
                    uploadvideo(video);
                } else {
                    Toast.makeText(upload.this, "影片必須在 40 秒以內", Toast.LENGTH_SHORT).show();
                }
            }
        });
        selectedDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 顯示日期選擇器
                showDatePickerDialog();
            }
        });
        //食物標籤
        foodTagsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTagSearchActivity();
            }
        });
        Back_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent MainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                SharedPreferencesUtils.clearVideotag(upload.this);
                startActivity(MainActivityIntent);
                finish();
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 相機權限已獲取,啟動相機錄影
                openCameraForVideoCaptureIntent();
            } else {
                // 相機權限被拒絕
                Toast.makeText(this, "Camera Permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openCameraForVideoCaptureIntent() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        activityResultLauncher.launch(videoIntent);
    }
    private void openTagSearchActivity() {
        Intent intent = new Intent(this, TagSearchActivity.class);

        tagSearchActivityLauncher.launch(intent);

    }

    //回傳tag值
    private final ActivityResultLauncher<Intent> tagSearchActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedTagsBuffer.setLength(0);
                        //編輯tag
                        ArrayList<String> selectedTags = result.getData().getStringArrayListExtra("selectedTags");

                        for (int i = 0; i < selectedTags.size(); i++) {
                            selectedTagsBuffer.append(selectedTags.get(i));
                            if (i < selectedTags.size() - 1) {
                                selectedTagsBuffer.append(", ");
                            }
                        }
                        // 更新 foodTagsEditText 的顯示
                        // foodTagsEditText.setText(SharedPreferencesUtils.getVideotag(upload.this));
                        foodTagsEditText.setText(selectedTagsBuffer.toString());
//                        savevideotag = foodTagsEditText.getText().toString();
//                        SharedPreferencesUtils.saveVideotag(upload.this, savevideotag);

                    }
                }
            }
    );



    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {                Uri selectedUri = null;
                if (result.getResultCode() == REQUEST_VIDEO_CAPTURE) {
                    // 用戶拍攝了新影片
                    selectedUri = result.getData().getData();
                } else {
                    selectedUri = result.getData().getData();
                }

                if (selectedUri != null) {
                    String fileType = getContentResolver().getType(selectedUri);
                    if (fileType != null) {
                        if (fileType.startsWith("image/")) {
                            // 用戶選擇了照片
                            List<Uri> imageUris = new ArrayList<>();
                            imageUris.add(selectedUri);
                            handleImageSelection(imageUris);
                        } else if (fileType.startsWith("video/")) {
                            // 用戶選擇了現有影片
                            Log.d("現有影片","1");

                            Bitmap image = null;
                            Bitmap image2 = null;//
                            Bitmap image3 = null;//
                            Bitmap image4 = null;
                            Bitmap image5 = null;
                            Bitmap image6 = null;

                            uploadvideo.setEnabled(true);
                            video = selectedUri;
                            // 使用 MediaMetadataRetriever 获取视频的预览图像
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(upload.this, video);
                            Bitmap bitmap = retriever.getFrameAtTime(); // 获取视频的第一帧图像
                            videopreview.setImageBitmap(bitmap);
                            //這裡要不要讓他可以看整個影片，不要只是第一幀?

                            image = retriever.getFrameAtTime(5 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第一張在第5秒
                            image2 = retriever.getFrameAtTime(10 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第二張在第10秒
                            image3 = retriever.getFrameAtTime(15 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第三張在第15秒
                            image4 = retriever.getFrameAtTime(20 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                            image5 = retriever.getFrameAtTime(25 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                            image6 = retriever.getFrameAtTime(30 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                            image2 = Bitmap.createScaledBitmap(image2, imageSize, imageSize, false);
                            image3 = Bitmap.createScaledBitmap(image3, imageSize, imageSize, false);
                            image4 = Bitmap.createScaledBitmap(image4, imageSize, imageSize, false);
                            image5 = Bitmap.createScaledBitmap(image5, imageSize, imageSize, false);
                            image6 = Bitmap.createScaledBitmap(image6, imageSize, imageSize, false);

                            String[] resultdachi = new String[6];
                            classifyImage(image);//辨識
                            resultdachi[0] = (String) foodclass.getText();
                            classifyImage(image2);//
                            resultdachi[1] = (String) foodclass.getText();
                            classifyImage(image3);//
                            resultdachi[2] = (String) foodclass.getText();
                            classifyImage(image4);//辨識
                            resultdachi[3] = (String) foodclass.getText();
                            classifyImage(image5);//辨識
                            resultdachi[4] = (String) foodclass.getText();
                            classifyImage(image6);//辨識
                            resultdachi[5] = (String) foodclass.getText();
                            //textView.setText(resultdachi[0]+","+resultdachi[1]+","+resultdachi[2]);

                            // 使用 HashMap 來計算 resultdachi 中每個元素出現的次數
                            HashMap<String, Integer> countMap = new HashMap<>();
                            for (String foodclass : resultdachi) {
                                if (countMap.containsKey(result)) {
                                    countMap.put(foodclass, countMap.get(result) + 1);
                                } else {
                                    countMap.put(foodclass, 1);
                                }
                            }

                            // 找出出現次數最多的元素
                            String mostFrequentResult = null;
                            int maxCount = 0;
                            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                                if (entry.getValue() > maxCount) {
                                    mostFrequentResult = entry.getKey();
                                    maxCount = entry.getValue();
                                }
                            }

                            // 設置 foodclass 的內容為出現次數最多的元素
                            if (mostFrequentResult != null) {
                                foodclass.setText(mostFrequentResult);
                                foodTagsEditText.setText(mostFrequentResult);
                                SharedPreferencesUtils.saveVideotag(upload.this, mostFrequentResult);


                            } else {
                                foodclass.setText("No result found"); // 如果沒有結果，可以設置一個默認值
                            }

                            try {
                                retriever.release(); // 释放资源
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            setCurrentDate();
                        }
                    }
                }
            }
        }
    });
    private void handleVideoSelection(Uri videoUri) {
        uploadvideo.setEnabled(true);


        // 使用 MediaMetadataRetriever 获取视频的预览图像
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(upload.this, video);
        Bitmap bitmap = retriever.getFrameAtTime(); // 获取视频的第一帧图像
        videopreview.setImageBitmap(bitmap);

        // 將影片的 URL 儲存起來，這裡假設你已經有一個名為 videoUrl 的成員變量
        // 可以將其設置為影片的 URL
        video = videoUri;
    }
    private void handleImageSelection(List<Uri> imageUris) {
        int videoWidth = 720;
        int videoHeight = 1280;
        int videoBitrate = 2000000; // 2Mbps
        int frameRate = 30;
        int delayTimeMs = 5000; // 每张照片播放5秒

        try {


            // 初始化 MediaMuxer、MediaCodec 和 MediaExtractor
            MediaMuxer muxer = new MediaMuxer(getOutputMediaFile().getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaExtractor extractor = new MediaExtractor();

            // 设置 MediaFormat 并配置 MediaCodec
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoWidth, videoHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameRate);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 开始编码每张照片
            encoder.start();
            int trackIndex = muxer.addTrack(format);
            muxer.start();
            long presentationTimeUs = 0;

            for (Uri uri : imageUris) {
                Log.d("upload", "imageUris");
                MediaFormat trackFormat = MediaFormat.createVideoFormat("video/avc", videoWidth, videoHeight);
                ByteBuffer inputBuffer = getImageBytes(uri, trackFormat);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.offset = 0;
                bufferInfo.size = inputBuffer.remaining();
                bufferInfo.presentationTimeUs = presentationTimeUs;
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;

                // 编码器输入
                int inputBufferId = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufferId >= 0) {
                    Log.d("upload", "inputBufferId");
                    ByteBuffer dstBuffer = encoder.getInputBuffer(inputBufferId);
                    dstBuffer.clear();
                    dstBuffer.put(inputBuffer);
                    encoder.queueInputBuffer(inputBufferId, 0, inputBuffer.remaining(), bufferInfo.presentationTimeUs, bufferInfo.flags);
                }

                // 获取编码器输出
                MediaCodec.BufferInfo outBufferInfo = new MediaCodec.BufferInfo();
                int outputBufferId = encoder.dequeueOutputBuffer(outBufferInfo, TIMEOUT_USEC);
                while (outputBufferId >= 0) {
                    Log.d("upload", "outputBufferId");

                    ByteBuffer encodedData = encoder.getOutputBuffer(outputBufferId);
                    if (outBufferInfo.size > 0) {
                        Log.d("upload", "outBufferInfo");

                        encodedData.position(outBufferInfo.offset);
                        encodedData.limit(outBufferInfo.offset + outBufferInfo.size);
                        muxer.writeSampleData(trackIndex, encodedData, outBufferInfo);
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false);
                    outputBufferId = encoder.dequeueOutputBuffer(outBufferInfo, TIMEOUT_USEC);
                }

                presentationTimeUs += delayTimeMs * 1000; // 更新下一张照片的时间戳
            }
            Log.d("upload", "成功");

            // 释放资源
            extractor.release();
            encoder.stop();
            encoder.release();
            muxer.stop();
            muxer.release();
        } catch (IOException e) {
            Log.e("upload", "IOException: " + e.getMessage());

            e.printStackTrace();
        }
    }

    private ByteBuffer getImageBytes(Uri uri, MediaFormat trackFormat) {
        // 從 Uri 獲取圖片的 ByteBuffer
        // 您可以使用 BitmapFactory.decodeStream 等方法將圖片解碼為 Bitmap
        // 然後將 Bitmap 轉換為 ByteBuffer
        // 這裡僅提供一個示例，您需要根據實際情況實現此方法
        int maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer inputBuffer = ByteBuffer.allocate(maxBufferSize);
        // 將圖片數據填充到 inputBuffer 中
        return inputBuffer;
    }
    private int getTrackIndex(MediaExtractor extractor, String mimeType) {
        // 獲取指定 MIME 類型的軌道索引
        // 您可以參考 Android 官方文檔實現此方法
        return 0; // 僅為示例，請自行實現
    }
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "MyVideos");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("upload", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }


    private boolean isVideoValid(Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(upload.this, uri);
            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            return duration <= 40000; // 40 秒以毫秒為單位
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void setSupportActionBar(MaterialToolbar toolbar) {
    }
    private Set<String> availableKeys = new TreeSet<>();

    private void initAvailableKeys(DataSnapshot snapshot) {//這邊將上船影片的鍵值做編號
        availableKeys.clear();
        Set<Integer> usedIndexes = new HashSet<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            String key = child.getKey();
            if (key.startsWith("Video")) {
                String indexStr = key.substring(5);
                if (!indexStr.isEmpty()) {
                    int index = Integer.parseInt(indexStr);
                    usedIndexes.add(index);
                }
            }
        }

        int nextIndex = 1;
        while (usedIndexes.contains(nextIndex)) {
            nextIndex++;
        }

        availableKeys.add("Video" + nextIndex);

        for (int i = nextIndex + 1; i <= 1000; i++) {
            if (!usedIndexes.contains(i)) {
                return; // 找到第一個可用鍵值後就退出循環
            }
        }
    }
    private void uploadvideo(Uri uri){

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxId = 0;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String videoId = childSnapshot.getKey();
                    if (videoId != null && videoId.startsWith("V")) {
                        int id = Integer.parseInt(videoId.substring(1));
                        maxId = Math.max(maxId, id);
                    }
                }
                String nextVid = "V" + (maxId + 1);
                String nextVideocont = "cont"+(maxId + 1);
                // 在這裡可以使用 nextVid 做後續處理，例如將其新增到 Firebase 中
                // 或者將 nextVid 傳遞給其他方法
                newVid = nextVid;  //影片編號
                newcont = nextVideocont;
                longvid = maxId+1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤情況
            }
        });

        String randomName = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference reference = storageRef.child("Videos/").child(randomName);

        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 上傳成功後，獲取影片的下載 URL
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // 將影片 URL 存儲到 Realtime Database 中
                        DatabaseReference videoRef = databaseReference.child(newVid);
                        DatabaseReference foodtagsRef = videoRef.child("Foodtags");
                        videoRef.child("videoUrl").setValue(downloadUri.toString());

                        //弄個May
                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference mayRef = database.child("May");

                        // 獲取並解析食物標籤
                        String foodTags = foodTagsEditText.getText().toString();
                        String[] tagsArray = foodTags.split("[,，]");
                        // 獲取影片資訊
                        String title = titleEditText.getText().toString();
                        String address = addressEditText.getText().toString();
                        String price = priceEditText.getText().toString();
                        String date = selectedDateEditText.getText().toString(); // 獲取日期的毫秒值
                        String desc = descriptionEditText.getText().toString();

//                        //這裡開始
//
//                        // 解析日期字符串並獲取月份
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                        Date dateformated;
//                        try {
//                            dateformated = dateFormat.parse(date);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                            return; // 無法解析日期字符串,直接返回
//                        }
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTime(dateformated);
//                        int month = calendar.get(Calendar.MONTH) + 1; // 月份從 0 開始,所以加 1
//                        int year = calendar.get(Calendar.YEAR);
//                        String monthNodeName = year + "_" + String.format("%02d", month); // 例如: 2023_05
//
//                        //獲取月份節點的引用
//                        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//                        DatabaseReference monthRef = database.child(monthNodeName);
//
//                        //結束

                        // 將食物標籤存儲到 Realtime Database 中
                        int count = 1;
                        for (String tag : tagsArray) {
                            //增加影片tag的地方
                            String key = "Foodtag" + count;
                            foodtagsRef.child(key).setValue(tag.trim());
//
//                            //更新對應月份節點中的標籤計數
//                            monthRef.child(tag.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    int currentCount = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
//                                    monthRef.child(tag.trim()).setValue(currentCount + 1);
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//                                    // 處理錯誤
//                                }
//                            });

                            //May加tag
                            mayRef.child(tag.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int currentCount = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                                    mayRef.child(tag.trim()).setValue(currentCount + 1);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // 處理錯誤
                                }
                            });

                            count++;
                        }
                        //這邊是上傳在Video裡面的東西
                        //videoRef.child("url").setValue(downloadUri.toString());
                        videoRef.child("title").setValue(title);
                        videoRef.child("address").setValue(address);
                        videoRef.child("price").setValue("$"+price);
                        videoRef.child("date").setValue(date);
                        videoRef.child("id").setValue(longvid);
                        videoRef.child("desc").setValue(desc);
                        videoRef.child("comment");
                        videoRef.child("videoPic").setValue(""); //這個給黃建成寫


                        Bitmap bitmap = ((BitmapDrawable)videopreview.getDrawable()).getBitmap();

                        // 上傳 Bitmap 到 Firebase Storage
                        StorageReference picReference = storageReference.child("videopics/" + newVid + ".jpg");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        UploadTask uploadTask = picReference.putBytes(data);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                picReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String uriString = uri.toString();
                                        videoRef.child("videoPic").setValue(uriString);//我想把這邊的值

                                        String username = SharedPreferencesUtils.getUsername(upload.this);
                                        videoRef.child("Uploader").setValue(username);

                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username);
                                        DatabaseReference newVideoRef = userRef.child("ownVideos").child(newVid);
                                        newVideoRef.child("videoUrl").setValue(downloadUri.toString());
                                        newVideoRef.child("videoPic").setValue(uriString);
                                    }
                                });
                            }
                        });


//                        String username = SharedPreferencesUtils.getUsername(upload.this);
//                        videoRef.child("Uploader").setValue(username);
//
//                        //上傳者的個人檔案出現上傳影片的vid
//                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username);
//                        DatabaseReference newVideoRef = userRef.child("ownVideos").child(newVid);
//                        newVideoRef.child("videoUrl").setValue(downloadUri.toString());
//                        newVideoRef.child("videoPic").setValue(""); //這個給黃建成寫
                        //所有使用者增加cont
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String userId = userSnapshot.getKey();
                                    DatabaseReference userContRef = usersRef.child(userId).child("Cont").child(newcont);                                    userContRef.child("Fav").setValue(false);
                                    userContRef.child("Tag").setValue(false);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // 處理錯誤情況
                            }
                        });
                        Toast.makeText(upload.this,"Video uploaded successful!",Toast.LENGTH_SHORT).show();
                        SharedPreferencesUtils.clearVideotag(upload.this);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("RELOAD_HOME_FRAGMENT", true);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e){
                Toast.makeText(upload.this,"Failed to upload video",Toast.LENGTH_SHORT).show();

            }

        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressIndicator.setVisibility(View.VISIBLE);
                progressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                progressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
            }
        });
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            int secondPos = 0;
            float maxConfidence = 0;
            float secondMaxConfidence =0;//
            for(int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    secondMaxConfidence = maxConfidence; // 將當前最大值設置為第二大值
                    maxConfidence = confidences[i];
                    maxPos = i;
                }else if (confidences[i] > secondMaxConfidence && confidences[i] != maxConfidence) {
                    secondMaxConfidence = confidences[i]; // 更新第二大值
                    secondPos = i;
                }
            }


//            String[] classes = {"肉圓", "牛肉麵", "牛肉湯", "肉燥飯", "珍珠奶茶", "沙拉",
//                    "起司蛋糕", "咖哩", "雞翅", "三明治", "棺材板", "涼麵",
//                    "雞排", "甜甜圈", "蛋餅", "八寶冰", "砂鍋魚頭", "薯條",
//                    "法式吐司", "炒泡麵", "炒飯", "香蒜麵包", "薑母鴨", "烤玉米",
//                    "烤香腸", "漢堡", "熱狗", "酸辣湯", "冰淇淋", "蚵仔麵線",
//                    "滷味", "芒果冰", "紅油炒手", "虱目魚粥", "臭豆腐", "味噌湯",
//                    "綠豆沙牛奶", "羊肉爐", "鍋燒意麵", "夜市牛排", "蚵嗲", "蚵仔煎",
//                    "生蠔", "鬆餅", "木瓜牛奶", "豬血湯", "鳳梨酥", "披薩",
//                    "鍋貼", "皮蛋豆腐", "拉麵", "粽子", "燉飯", "鹹酥雞",
//                    "蔥油餅", "海產粥", "麻油雞", "刈包", "義大利肉醬麵", "義大利奶油麵",
//                    "麻辣鴨血", "牛排", "水煎包", "滷豬腳", "臭豆腐", "拔絲地瓜",
//                    "太陽餅", "壽司", "地瓜球", "春捲", "貢丸湯", "大腸包小腸",
//                    "章魚燒", "糖葫蘆", "湯圓", "三杯雞", "提拉米蘇", "火雞肉飯",
//                    "格子煎餅", "車輪餅"
//            };
            String[] classes = {"牛肉麵", "珍珠奶茶", "沙拉", "咖哩", "涼麵", "雞排", "甜甜圈", "蛋餅", "薯條", "炒泡麵", "炒飯",
                    "漢堡", "熱狗", "冰淇淋", "鍋燒意麵", "夜市牛排", "蚵仔煎", "披薩", "鍋貼", "拉麵", "燉飯",
                    "鹹酥雞","義大利肉醬麵", "義大利奶油麵","牛排", "臭豆腐", "壽司", "章魚燒", "火雞肉飯","小籠包"
            };

            foodclass.setText(classes[maxPos]+","+classes[secondPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }

            //confidence.setText(s);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }


    private void showDatePickerDialog() {
        // 初始化 Calendar
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // 創建 DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        // 將所選日期格式化為指定格式
                        String selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year, month + 1, day);
                        // 將格式化後的日期設置到 TextView 中
                        selectedDateEditText.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth);

        // 顯示 DatePickerDialog
        datePickerDialog.show();
    }
    private void setCurrentDate() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());

        // 将当前日期设置到 selectedDateEditText
        selectedDateEditText.setText(currentDate);
    }

    private void getNextVideoId(DatabaseReference videoRef) {

    }
}