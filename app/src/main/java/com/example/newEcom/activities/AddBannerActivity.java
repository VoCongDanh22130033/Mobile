package com.example.newEcom.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.newEcom.R;
import com.example.newEcom.model.BannerModel;
import com.example.newEcom.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddBannerActivity extends AppCompatActivity {

    TextInputEditText idEditText, descEditText;
    Button imageBtn, addBannerBtn;
    ImageView backBtn, bannerImageView;
    TextView removeImageBtn;
    AutoCompleteTextView statusDropDown;

    String bannerImageUrl, status;
    int bannerId;
    Context context = this;
    boolean imageUploaded = false;

    SweetAlertDialog dialog;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_banner);

        idEditText = findViewById(R.id.idEditText);
        descEditText = findViewById(R.id.descEditText);
        bannerImageView = findViewById(R.id.bannerImageView);

        imageBtn = findViewById(R.id.imageBtn);
        addBannerBtn = findViewById(R.id.addBannerBtn);
        backBtn = findViewById(R.id.backBtn);
        removeImageBtn = findViewById(R.id.removeImageBtn);

        statusDropDown = findViewById(R.id.statusDropDown);

        // Lấy lastBannerId + 1
        FirebaseUtil.getDetails().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().contains("lastBannerId")) {
                            bannerId = Integer.parseInt(task.getResult().get("lastBannerId").toString()) + 1;
                        } else {
                            bannerId = 1;
                        }
                        idEditText.setText(String.valueOf(bannerId));
                    }
                });

        // Setup status dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.dropdown_item, new String[]{"Live", "Not Live"});
        statusDropDown.setAdapter(adapter);
        statusDropDown.setOnItemClickListener((parent, view, position, id) -> {
            status = parent.getItemAtPosition(position).toString();
            Toast.makeText(context, "Status: " + status, Toast.LENGTH_SHORT).show();
        });

        imageBtn.setOnClickListener(v -> selectImage());

        addBannerBtn.setOnClickListener(v -> addToFirebase());

        backBtn.setOnClickListener(v -> onBackPressed());

        removeImageBtn.setOnClickListener(v -> removeImage());

        dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Uploading image...");
        dialog.setCancelable(false);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    private void addToFirebase() {
        if (!validate()) return;
        String bannerDesc = descEditText.getText().toString();
        BannerModel banner = new BannerModel(bannerId, bannerImageUrl, bannerDesc, status);
        FirebaseUtil.getBanner().add(banner)
                .addOnSuccessListener(documentReference -> {
                    FirebaseUtil.getDetails().update("lastBannerId", bannerId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AddBannerActivity.this, "Banner added successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                });
    }
    private void removeImage() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Do you want to remove this image?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .setConfirmClickListener(sDialog -> {
                    imageUploaded = false;
                    bannerImageUrl = null;
                    bannerImageView.setImageDrawable(null);
                    bannerImageView.setVisibility(View.GONE);
                    removeImageBtn.setVisibility(View.GONE);
                    sDialog.dismiss();
                }).show();
    }

    private boolean validate() {
        boolean isValid = true;

        if (idEditText.getText().toString().trim().isEmpty()) {
            idEditText.setError("Id is required");
            isValid = false;
        }
        if (status == null || status.trim().isEmpty()) {
            statusDropDown.setError("Status is required");
            isValid = false;
        }
        if (descEditText.getText().toString().trim().isEmpty()) {
            descEditText.setError("Description is required");
            isValid = false;
        }
        if (!imageUploaded) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (idEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill the id first", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.show();

            MediaManager.get().upload(imageUri)
                    .unsigned("ImageUpload")  // Thay "ImageUpload" bằng preset unsigned của bạn
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) { }

                        @Override
                        public void onProgress(String requestId, long bytesSent, long totalBytes) { }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            bannerImageUrl = (String) resultData.get("secure_url");
                            imageUploaded = true;

                            Picasso.get().load(bannerImageUrl).into(bannerImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    dialog.dismiss();
                                    bannerImageView.setVisibility(View.VISIBLE);
                                    removeImageBtn.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(AddBannerActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            dialog.dismiss();
                            Toast.makeText(AddBannerActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) { }
                    }).dispatch();
        }
    }

    @Override
    public void onBackPressed() {
        // Không xóa ảnh cloudinary trực tiếp vì không có backend xử lý, chỉ gọi super
        super.onBackPressed();
    }
}
