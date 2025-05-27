package com.example.newEcom.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.newEcom.R;
import com.example.newEcom.model.ProductModel;
import com.example.newEcom.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import cn.pedant.SweetAlert.SweetAlertDialog;
public class AddProductActivity extends AppCompatActivity {
    TextInputEditText idEditText, nameEditText, descEditText, specEditText, stockEditText, priceEditText, discountEditText;
    Button imageBtn, addProductBtn;
    ImageView backBtn, productImageView;
    TextView removeImageBtn;
    AutoCompleteTextView categoryDropDown;
    ArrayAdapter<String> arrayAdapter;
    String[] categories;
    String category, productImage, shareLink;
    String productName;
    int productId;
    Context context = this;
    boolean imageUploaded = false;
    //    ProgressDialog dialog;
    SweetAlertDialog dialog;
    // Thêm 1 TAG chung cho log
    private static final String TAG = "AddProductActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        idEditText = findViewById(R.id.idEditText);
        nameEditText = findViewById(R.id.nameEditText);
        categoryDropDown = findViewById(R.id.categoryDropDown);
        descEditText = findViewById(R.id.descriptionEditText);
        specEditText = findViewById(R.id.specificationEditText);
        stockEditText = findViewById(R.id.stockEditText);
        priceEditText = findViewById(R.id.priceEditText);
        discountEditText = findViewById(R.id.discountEditText);
        productImageView = findViewById(R.id.productImageView);

        imageBtn = findViewById(R.id.imageBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        backBtn = findViewById(R.id.backBtn);
        removeImageBtn = findViewById(R.id.removeImageBtn);

        // ... phần khởi tạo các view ...

        FirebaseUtil.getDetails().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                productId = Integer.parseInt(task.getResult().get("lastProductId").toString()) + 1;
                                idEditText.setText(productId + "");
                                Log.d(TAG, "Loaded lastProductId: " + productId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing lastProductId", e);
                            }
                        } else {
                            Log.e(TAG, "Failed to get lastProductId", task.getException());
                        }
                    }
                });


        imageBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        addProductBtn.setOnClickListener(v -> {
            generateDynamicLink();
        });

        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        removeImageBtn.setOnClickListener(v -> {
            removeImage();
        });

//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Uploading image...");
//        dialog.setCancelable(false);
        dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Uploading image...");
        dialog.setCancelable(false);
    }
    private void getCategories(MyCallback myCallback) {
        FirebaseUtil.getCategories().orderBy("name")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> categoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = (String) document.getData().get("name");
                            categoryList.add(name);
                        }
                        categories = categoryList.toArray(new String[0]);
                        Log.d(TAG, "Loaded categories: " + Arrays.toString(categories));
                        myCallback.onCallback(categories);
                    } else {
                        Log.e(TAG, "Failed to load categories", task.getException());
                        Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addToFirebase() {
        productName = nameEditText.getText().toString();
        List<String> sk = Arrays.asList(productName.trim().toLowerCase().split(" "));
        String desc = descEditText.getText().toString();
        String spec = specEditText.getText().toString();

        int price = 0, discount = 0, stock = 0;
        try {
            price = Integer.parseInt(priceEditText.getText().toString());
            discount = Integer.parseInt(discountEditText.getText().toString());
            stock = Integer.parseInt(stockEditText.getText().toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format in price, discount or stock", e);
            Toast.makeText(this, "Please enter valid numbers for price, discount and stock", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductModel model = new ProductModel(productName, sk, productImage, category, desc, spec, price, discount, price - discount, productId, stock, shareLink, 0f, 0);

        FirebaseUtil.getProducts().add(model)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Product added with ID: " + documentReference.getId());
                    FirebaseUtil.getDetails().update("lastProductId", productId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Updated lastProductId to " + productId);
                                    Toast.makeText(AddProductActivity.this, "Product has been added successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.e(TAG, "Failed to update lastProductId", task.getException());
                                }
                            });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add product", e));
    }
    private void generateDynamicLink() {
        if (!validate())
            return;

        Log.d(TAG, "Generating dynamic link for productId: " + productId);

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/?product_id=" + productId))
                .setDomainUriPrefix("https://shopeasea.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.example.newEcom").build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(productName)
                        .setImageUrl(Uri.parse(productImage))
                        .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Uri shortLink = task.getResult().getShortLink();
                        shareLink = shortLink.toString();
                        Log.d(TAG, "Dynamic link generated: " + shareLink);
                        addToFirebase();
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Failed to generate dynamic link", exception);
                    }
                });
    }

    private boolean validate() {
        boolean isValid = true;
        if (idEditText.getText().toString().trim().length() == 0) {
            idEditText.setError("Id is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Id is required");
        }
        if (nameEditText.getText().toString().trim().length() == 0) {
            nameEditText.setError("Name is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Name is required");
        }
        if (categoryDropDown.getText().toString().trim().length() == 0) {
            categoryDropDown.setError("Category is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Category is required");
        }
        if (descEditText.getText().toString().trim().length() == 0) {
            descEditText.setError("Description is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Description is required");
        }
        if (stockEditText.getText().toString().trim().length() == 0) {
            stockEditText.setError("Stock is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Stock is required");
        }
        if (priceEditText.getText().toString().trim().length() == 0) {
            priceEditText.setError("Price is required");
            isValid = false;
            Log.e(TAG, "Validation failed: Price is required");
        }
        if (!imageUploaded) {
            Toast.makeText(context, "Image is not selected", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Image is not selected");
            isValid = false;
        }
        return isValid;
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
                        productImageView.setImageDrawable(null);
                        productImageView.setVisibility(View.GONE);
                        removeImageBtn.setVisibility(View.GONE);


                        alertDialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            if (idEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please fill the ID first", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Image upload failed: ID is empty");
                return;
            }

            try {
                productId = Integer.parseInt(idEditText.getText().toString());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid product ID format", e);
                Toast.makeText(this, "Invalid product ID format", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.setTitleText("Uploading image...");
            dialog.show();

            MediaManager.get().upload(imageUri)
                    .unsigned("ImageUpload") // Thay bằng preset Cloudinary của bạn
                    .option("folder", "products")     // Tùy chọn folder
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Image upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            Log.d(TAG, "Upload progress: " + bytes + "/" + totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            dialog.dismiss();
                            imageUploaded = true;
                            productImage = resultData.get("secure_url").toString();

                            Log.d(TAG, "Image uploaded successfully: " + productImage);

                            Picasso.get().load(productImage).into(productImageView);
                            productImageView.setVisibility(View.VISIBLE);
                            removeImageBtn.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            dialog.dismiss();
                            Log.e(TAG, "Image upload error: " + error.getDescription());
                            Toast.makeText(context, "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Log.d(TAG, "Image upload rescheduled: " + error.getDescription());
                        }

                    })
                    .dispatch();
        }
    }

    public interface MyCallback {
        void onCallback(String[] categories);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getCategories(cate -> {
            arrayAdapter = new ArrayAdapter<>(context, R.layout.dropdown_item, cate);
            categoryDropDown.setAdapter(arrayAdapter);
            categoryDropDown.setOnItemClickListener((adapterView, view, i, l) -> {
                category = adapterView.getItemAtPosition(i).toString();
            });
        });
    }

}