package com.example.feastarfeed;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;;
import java.util.Calendar;
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


public class upload extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progressIndicator;
    Uri video;
    Button uploadvideo,selectvideo,selectphoto;
    private ImageButton Back_button;
    ImageView videopreview;
    EditText titleEditText,addressEditText,priceEditText,selectedDateEditText;

    TextView foodTagsEditText,foodclass,selectedDateText,foodTagsText,titleText,priceText,addressText;
    DatePicker datePicker;
    private DatabaseReference databaseReference;
    DatabaseReference userRef,totalFoodTagRef;
    int imageSize = 224;
    String savevideotag;
    private StringBuffer selectedTagsBuffer = new StringBuffer();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add);
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");
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

        foodTagsEditText = findViewById(R.id.foodTagsEditText);
        titleEditText = findViewById(R.id.titleEditText);
        priceEditText = findViewById(R.id.priceEditText);
        addressEditText = findViewById(R.id.addressEditText);
        selectedDateEditText = findViewById(R.id.selectedDateEditText);
        Back_button = findViewById(R.id.back_button);

        foodTagsText = findViewById(R.id.foodTagsText);
        titleText = findViewById(R.id.titleText);
        priceText = findViewById(R.id.priceText);
        addressText = findViewById(R.id.addressText);
        selectedDateText = findViewById(R.id.selectedDateText);
        datePicker = findViewById(R.id.datePicker);

        selectvideo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                activityResultLauncher.launch(intent);
                //startActivityForResult(intent, 3);
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
                startActivity(MainActivityIntent);
                finish();
            }
        });
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
        //偵測照片
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedfile = result.getData().getData();
                if (selectedfile != null) {
                    String fileType = getContentResolver().getType(selectedfile);
//                    foodTagEditText.setVisibility(View.VISIBLE);
//                    foodTagsText.setVisibility(View.VISIBLE);
//                    titleEditText.setVisibility(View.VISIBLE);
//                    addressEditText.setVisibility(View.VISIBLE);
//                    priceEditText.setVisibility(View.VISIBLE);
//                    priceEditText.setVisibility(View.VISIBLE);
//                    selectedDateEditView.setVisibility(View.VISIBLE);
//                    priceEditText.setVisibility(View.VISIBLE);
//
//                    titleText.setVisibility(View.VISIBLE);
//                    addressText.setVisibility(View.VISIBLE);
//                    selectedDateTextView.setVisibility(View.VISIBLE);

                    if (fileType != null && fileType.startsWith("image/")) {
                        // 用戶選擇了照片
                        List<Uri> imageUris = new ArrayList<>();
                        imageUris.add(selectedfile);
                        //handleImageSelection(imageUris);
                    } else {
                        //選影片
                        Bitmap image = null;
                        Bitmap image2 = null;//
                        Bitmap image3 = null;//

                        uploadvideo.setEnabled(true);
                        video = selectedfile;
                        // 使用 MediaMetadataRetriever 获取视频的预览图像
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(upload.this, video);
                        Bitmap bitmap = retriever.getFrameAtTime(); // 获取视频的第一帧图像
                        videopreview.setImageBitmap(bitmap);
                        //這裡要不要讓他可以看整個影片，不要只是第一幀?

                        image = retriever.getFrameAtTime(5 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第一張
                        image2 = retriever.getFrameAtTime(10 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第二張
                        image3 = retriever.getFrameAtTime(15 * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);//第三張

                        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                        image2 = Bitmap.createScaledBitmap(image2, imageSize, imageSize, false);
                        image3 = Bitmap.createScaledBitmap(image3, imageSize, imageSize, false);

                        String[] resultdachi = new String[3];
                        classifyImage(image);//辨識
                        resultdachi[0] = (String) foodclass.getText();
                        classifyImage(image2);//
                        resultdachi[1] = (String) foodclass.getText();
                        classifyImage(image3);//
                        resultdachi[2] = (String) foodclass.getText();
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
                    }

                } else if (result.getData().getClipData() != null) {
                    // 用戶選擇了多張照片

                    ClipData clipData = result.getData().getClipData();
                    List<Uri> imageUris = new ArrayList<>();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        imageUris.add(clipData.getItemAt(i).getUri());
                    }
                    //handleImageSelection(imageUris);
                }
            } else {
                Toast.makeText(upload.this, "please select a video or image", Toast.LENGTH_SHORT).show();
            }

        }
    });
//    private void handleImageSelection(List<Uri> imageUris) {
//        // 创建VideoConverter对象
//        VideoConverter videoConverter = new VideoConverter(upload.this);
//        videoConverter.convertImagesToVideo(imageUris, 5, new VideoConverter.VideoConversionCallback() {
//            @Override
//            public void onVideoConversionSuccess(Uri videoUri) {
//                // 图像转换成功后的逻辑，例如上传视频等
//                video = videoUri;
//                uploadvideo.setEnabled(true);
//
//                // 使用 MediaMetadataRetriever 獲取視頻的預覽圖像
//                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                retriever.setDataSource(upload.this, video);
//                Bitmap bitmap = retriever.getFrameAtTime();
//                videopreview.setImageBitmap(bitmap);
//
//                try {
//                    retriever.release();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//
//            @Override
//            public void onVideoConversionFailure(String errorMessage) {
//                // 图像转换失败后的逻辑，例如显示错误信息等
//                Toast.makeText(upload.this, errorMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }




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
        String vid= UUID.randomUUID().toString();
        StorageReference reference = storageReference.child("Videos/" + vid);

        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 上傳成功後，獲取影片的下載 URL
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // 將影片 URL 存儲到 Realtime Database 中
                        DatabaseReference videoRef = databaseReference.child("Videos").child(vid);
                        DatabaseReference foodtagsRef = videoRef.child("Foodtags");
                        videoRef.child("url").setValue(downloadUri.toString());

                        // 獲取並解析食物標籤
                        //EditText foodTagEditText = findViewById(R.id.foodTagsEditText);
                        String foodTags = foodTagsEditText.getText().toString();
                        String[] tagsArray = foodTags.split("[,，]");
                        // 獲取影片資訊
                        String title = titleEditText.getText().toString();
                        String address = addressEditText.getText().toString();
                        String price = priceEditText.getText().toString();
                        String date = selectedDateEditText.getText().toString();; // 獲取日期的毫秒值

                        // 將食物標籤存儲到 Realtime Database 中
                        int count = 1;
                        for (String tag : tagsArray) {
                            String key = "Foodtag" + count;
                            foodtagsRef.child(key).setValue(tag.trim());
                            count++;
                        }
                        //videoRef.child("url").setValue(downloadUri.toString());
                        videoRef.child("title").setValue(title);
                        videoRef.child("address").setValue(address);
                        videoRef.child("price").setValue(price);
                        videoRef.child("date").setValue(date);

                        // 獲取當前使用者的 UID
                        String username = SharedPreferencesUtils.getUsername(upload.this);
                        // 將上傳者的 UID 存儲到 Realtime Database 中
                        videoRef.child("Uploader").setValue(username);

                        //上傳者的個人檔案出現上傳影片的vid
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username); // 獲取當前用戶節點的引用                        ownVideosRef.push().setValue(vid);
                        DatabaseReference ownVideosRef = userRef.child("ownVideos");
                        ownVideosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                initAvailableKeys(snapshot);
                                String nextVideoKey = availableKeys.iterator().next();
                                availableKeys.remove(nextVideoKey);
                                DatabaseReference nextVideoRef = ownVideosRef.child(nextVideoKey);
                                nextVideoRef.setValue(vid);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // 處理錯誤情況
                            }
                        });
                        Toast.makeText(upload.this,"Video uploaded successful!",Toast.LENGTH_SHORT).show();
                        SharedPreferencesUtils.clearVideotag(upload.this);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        //finish();
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


}