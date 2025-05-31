package com.nnthienphuc.bookdownloaderapp;

import android.app.Activity;

import android.Manifest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.*;
import java.util.UUID;

public class UploadBookActivity extends AppCompatActivity {
    private static final int PICK_PDF = 1;
    private static final int PICK_IMAGE = 2;
    private static final long MAX_SIZE_MB = 100;

    private Uri pdfUri = null;
    private Uri imageUri = null;

    private EditText titleEt, authorEt, genreEt, pageCountEt, descriptionEt;
    private Button pickPdfBtn, pickImageBtn, uploadBtn;
    private ImageView previewImg;
    private ProgressBar progressBar;
    private TextView progressText;

    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_book);

        // 📌 Yêu cầu quyền đọc ảnh nếu Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
        }

        titleEt = findViewById(R.id.inputBookTitle);
        authorEt = findViewById(R.id.inputBookAuthor);
        genreEt = findViewById(R.id.inputBookGenre);
        pageCountEt = findViewById(R.id.inputBookPageCount);
        descriptionEt = findViewById(R.id.inputBookDescription);
        pickPdfBtn = findViewById(R.id.pickPdfBtn);
        pickImageBtn = findViewById(R.id.pickImageBtn);
        uploadBtn = findViewById(R.id.uploadBookBtn);
        previewImg = findViewById(R.id.previewBookThumbnail);
        progressBar = findViewById(R.id.uploadProgressBar);
        progressText = findViewById(R.id.uploadProgressText);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        pickPdfBtn.setOnClickListener(v -> pickFile(PICK_PDF));
        pickImageBtn.setOnClickListener(v -> pickFile(PICK_IMAGE));
        uploadBtn.setOnClickListener(v -> uploadBook());
    }

    private void pickFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(requestCode == PICK_PDF ? "application/pdf" : "image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_PDF) pdfUri = data.getData();
            else if (requestCode == PICK_IMAGE) {
                imageUri = data.getData();
                previewImg.setImageURI(imageUri);
            }
        }
    }

    private void uploadBook() {
        String title = titleEt.getText().toString().trim();
        String author = authorEt.getText().toString().trim();
        String genre = genreEt.getText().toString().trim();
        String pageCountStr = pageCountEt.getText().toString().trim();
        String desc = descriptionEt.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || pageCountStr.isEmpty() || pdfUri == null || imageUri == null) {
            Toast.makeText(this, "Điền đủ thông tin và chọn file", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        long pdfSize;
        try (AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(pdfUri, "r")) {
            pdfSize = (fd != null) ? fd.getLength() : 0L;
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi đọc file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pdfSize > MAX_SIZE_MB * 1024 * 1024) {
            Toast.makeText(this, "File vượt quá 100MB", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        String bookId = UUID.randomUUID().toString();
        StorageReference pdfRef = storage.getReference().child("books/" + bookId + ".pdf");
        StorageReference imgRef = storage.getReference().child("thumbnails/" + bookId + ".jpg");

        pdfRef.putFile(pdfUri)
                .addOnProgressListener(taskSnapshot -> {
                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress(progress);
                    progressText.setText("Upload: " + progress + "%");
                })
                .addOnSuccessListener(pdfTask -> {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 300, 400, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageData = baos.toByteArray();

                        imgRef.putBytes(imageData).addOnSuccessListener(imgTask -> {
                            pdfRef.getDownloadUrl().addOnSuccessListener(pdfUrl ->
                                    imgRef.getDownloadUrl().addOnSuccessListener(imgUrl -> {

                                        Book book = new Book(
                                                bookId,
                                                title,
                                                author,
                                                genre,
                                                desc,
                                                user.getDisplayName(),
                                                user.getUid(),
                                                Integer.parseInt(pageCountStr),
                                                pdfSize,
                                                imgUrl.toString(),
                                                pdfUrl.toString(),
                                                "",
                                                false
                                        );

                                        db.collection("books").document(bookId).set(book)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Tải sách thành công", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi Firestore", Toast.LENGTH_SHORT).show());
                                    })
                            );
                        });
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}