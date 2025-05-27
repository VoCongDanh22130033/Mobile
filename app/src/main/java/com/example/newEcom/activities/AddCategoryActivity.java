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
import com.example.newEcom.model.CategoryModel;
import com.example.newEcom.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.net.Uri;

import java.util.Map;

public class AddCategoryActivity extends AppCompatActivity {
    TextInputEditText idEditText, nameEditText, descEditText, colorEditText;
    Button imageBtn, addCategoryBtn;
    ImageView backBtn, categoryImageView;
    TextView removeImageBtn;

    String categoryImage;
    String productName;
    int categoryId;
    Context context = this;
    boolean imageUploaded = false;

    SweetAlertDialog dialog;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        idEditText = findViewById(R.id.idEditText);
        nameEditText = findViewById(R.id.nameEditText);
        descEditText = findViewById(R.id.descriptionEditText);
        colorEditText = findViewById(R.id.colorEditText);
        categoryImageView = findViewById(R.id.categoryImageView);

        imageBtn = findViewById(R.id.imageBtn);
        addCategoryBtn = findViewById(R.id.addCategoryBtn);
        backBtn = findViewById(R.id.backBtn);
        removeImageBtn = findViewById(R.id.removeImageBtn);

        FirebaseUtil.getDetails().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc != null && doc.contains("lastCategoryId") && doc.get("lastCategoryId") != null) {
                                categoryId = Integer.parseInt(doc.get("lastCategoryId").toString()) + 1;
                            } else {
                                categoryId = 1; // hoặc giá trị mặc định bạn muốn
                            }
                            idEditText.setText(categoryId + "");
                        }

                    }
                });

        imageBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        addCategoryBtn.setOnClickListener(v -> {
            addToFirebase();
        });

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        removeImageBtn.setOnClickListener(v -> {
            removeImage();
        });

        dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Uploading image...");
        dialog.setCancelable(false);
    }

    private void addToFirebase() {
        if (!validate())
            return;

        categoryId = Integer.parseInt(idEditText.getText().toString());
        String name = nameEditText.getText().toString();
        String desc = descEditText.getText().toString();
        String color = colorEditText.getText().toString();

        CategoryModel category = new CategoryModel(name, categoryImage, color, desc, categoryId);

        FirebaseUtil.getCategories().add(category)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        FirebaseUtil.getDetails().update("lastCategoryId", categoryId)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AddCategoryActivity.this, "Category has been added successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });
                    }
                });

    }

    private void removeImage() {
        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        alertDialog
                .setTitleText("Are you sure?")
                .setContentText("Do you want to remove this image?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        imageUploaded = false;
                        categoryImageView.setImageDrawable(null);
                        categoryImageView.setVisibility(View.GONE);
                        removeImageBtn.setVisibility(View.GONE);

                        FirebaseUtil.getCategoryImageReference(categoryId + "").delete();
                        alertDialog.dismiss();
                    }
                }).show();
    }

    private boolean validate() {
        boolean isValid = true;
        if (idEditText.getText().toString().trim().length() == 0) {
            idEditText.setError("Id is required");
            isValid = false;
        }
        if (nameEditText.getText().toString().trim().length() == 0) {
            nameEditText.setError("Name is required");
            isValid = false;
        }
        if (descEditText.getText().toString().trim().length() == 0) {
            descEditText.setError("Description is required");
            isValid = false;
        }
        if (colorEditText.getText().toString().trim().length() == 0) {
            colorEditText.setError("Color is required");
            isValid = false;
        }
        if (colorEditText.getText().toString().charAt(0) != '#') {
            colorEditText.setError("Color should be HEX value");
            isValid = false;
        }

        if (!imageUploaded) {
            Toast.makeText(context, "Image is not selected", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();

                if (idEditText.getText().toString().trim().length() == 0) {
                    Toast.makeText(this, "Please fill the id first", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.show();

                // Upload ảnh lên Cloudinary (Unsigned upload preset)
                MediaManager.get().upload(imageUri)
                        .unsigned("ImageUpload") // Thay "your_unsigned_preset_name" bằng preset unsigned bạn tạo trên Cloudinary
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                                // Upload bắt đầu
                            }

                            @Override
                            public void onProgress(String requestId, long bytesSent, long totalBytes) {
                                // Có thể update progress ở đây nếu muốn
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                // Upload thành công, lấy URL ảnh
                                categoryImage = (String) resultData.get("secure_url");
                                imageUploaded = true;

                                // Hiển thị ảnh lên ImageView
                                Picasso.get().load(categoryImage).into(categoryImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        dialog.dismiss();
                                        categoryImageView.setVisibility(View.VISIBLE);
                                        removeImageBtn.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(AddCategoryActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                dialog.dismiss();
                                Toast.makeText(AddCategoryActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                // Upload bị tạm dừng và sẽ thử lại
                            }
                        }).dispatch();
            }
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        FirebaseUtil.getCategoryImageReference(categoryId + "").delete();
    }
}