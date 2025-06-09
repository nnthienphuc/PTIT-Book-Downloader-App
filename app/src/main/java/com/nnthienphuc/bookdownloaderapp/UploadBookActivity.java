package com.nnthienphuc.bookdownloaderapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.*;
import java.util.UUID;

public class UploadBookActivity extends AppCompatActivity {
    private static final long MAX_SIZE_MB = 100;

    private EditText titleEt, authorEt, genreEt, pageCountEt, descriptionEt, pdfUrlEt, imageUrlEt;
    private ImageView previewImg;
    private Button uploadBtn;
    private ProgressBar progressBar;
    private TextView progressText;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_book);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
        }

        titleEt = findViewById(R.id.inputBookTitle);
        authorEt = findViewById(R.id.inputBookAuthor);
        genreEt = findViewById(R.id.inputBookGenre);
        pageCountEt = findViewById(R.id.inputBookPageCount);
        descriptionEt = findViewById(R.id.inputBookDescription);
        pdfUrlEt = findViewById(R.id.inputBookPdfUrl);
        imageUrlEt = findViewById(R.id.inputBookThumbnailUrl);
        uploadBtn = findViewById(R.id.uploadBookBtn);
        previewImg = findViewById(R.id.previewBookThumbnail);
        progressBar = findViewById(R.id.uploadProgressBar);
        progressText = findViewById(R.id.uploadProgressText);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        uploadBtn.setOnClickListener(v -> uploadBook());
    }

    private void uploadBook() {
        String title = titleEt.getText().toString().trim();
        String author = authorEt.getText().toString().trim();
        String genre = genreEt.getText().toString().trim();
        String pageCountStr = pageCountEt.getText().toString().trim();
        String desc = descriptionEt.getText().toString().trim();
        String pdfUrl = pdfUrlEt.getText().toString().trim();
        String imageUrl = imageUrlEt.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || pageCountStr.isEmpty() || pdfUrl.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Điền đủ thông tin và link", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        // ✅ convert Google Drive links về đúng dạng
        String convertedPdfUrl = convertGoogleDriveUrl(pdfUrl);
        String convertedImgUrl = convertGoogleDriveUrl(imageUrl);

        String bookId = UUID.randomUUID().toString();

        Book book = new Book(
                bookId,
                title,
                author,
                genre,
                desc,
                user.getDisplayName(),
                user.getUid(),
                Integer.parseInt(pageCountStr),
                0,
                convertedImgUrl,
                convertedPdfUrl,
                "",
                false
        );

        db.collection("books").document(bookId).set(book)
                .addOnSuccessListener(unused -> {
                    saveBookInfoLocally(book);
                    Toast.makeText(this, "Tải sách thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi Firestore", Toast.LENGTH_SHORT).show());
    }

    private String convertGoogleDriveUrl(String originalUrl) {
        if (originalUrl == null || !originalUrl.contains("/d/")) return originalUrl;
        try {
            String fileId = originalUrl.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?export=download&id=" + fileId;
        } catch (Exception e) {
            return originalUrl;
        }
    }

//    private void uploadBook() {
//        String title = titleEt.getText().toString().trim();
//        String author = authorEt.getText().toString().trim();
//        String genre = genreEt.getText().toString().trim();
//        String pageCountStr = pageCountEt.getText().toString().trim();
//        String desc = descriptionEt.getText().toString().trim();
//        String pdfUrl = pdfUrlEt.getText().toString().trim();
//        String imageUrl = imageUrlEt.getText().toString().trim();
//
//        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || pageCountStr.isEmpty() || pdfUrl.isEmpty() || imageUrl.isEmpty()) {
//            Toast.makeText(this, "Điền đủ thông tin và link", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseUser user = auth.getCurrentUser();
//        if (user == null) {
//            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        progressBar.setVisibility(View.VISIBLE);
//        progressText.setVisibility(View.VISIBLE);
//
//        String bookId = UUID.randomUUID().toString();
//
//        Book book = new Book(
//                bookId,
//                title,
//                author,
//                genre,
//                desc,
//                user.getDisplayName(),
//                user.getUid(),
//                Integer.parseInt(pageCountStr),
//                0,
//                imageUrl,
//                pdfUrl,
//                "",
//                false
//        );
//
//        db.collection("books").document(bookId).set(book)
//                .addOnSuccessListener(unused -> {
//                    saveBookInfoLocally(book);
//                    Toast.makeText(this, "Tải sách thành công", Toast.LENGTH_SHORT).show();
//                    finish();
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi Firestore", Toast.LENGTH_SHORT).show());
//    }

    private void saveBookInfoLocally(Book book) {
        try {
            File file = new File(getFilesDir(), book.getId() + ".bookinfo");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(book.toJson());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không lưu được file bookinfo", Toast.LENGTH_SHORT).show();
        }
    }
}
