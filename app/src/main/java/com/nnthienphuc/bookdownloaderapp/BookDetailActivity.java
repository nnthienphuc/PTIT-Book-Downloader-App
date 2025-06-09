package com.nnthienphuc.bookdownloaderapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final String EXTRA_MODE = "mode";

    private EditText title, author, genre, pageCount, description;
    private TextView size, uploader;
    private ImageView thumbnail;
    private Button actionBtn;
    private CheckBox deleteCheck;

    private Book book;
    private String mode;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_book_detail);

        title = findViewById(R.id.detailBookTitle);
        author = findViewById(R.id.detailBookAuthor);
        genre = findViewById(R.id.detailBookGenre);
        pageCount = findViewById(R.id.detailBookPageCount);
        size = findViewById(R.id.detailBookSize);
        uploader = findViewById(R.id.detailBookUploader);
        description = findViewById(R.id.detailBookDescription);
        thumbnail = findViewById(R.id.detailBookThumbnail);
        actionBtn = findViewById(R.id.detailBookDownloadBtn);
        deleteCheck = findViewById(R.id.detailBookDeleteCheckbox);

        book = (Book) getIntent().getSerializableExtra(EXTRA_BOOK);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        db = FirebaseFirestore.getInstance();

        if (book == null) {
            finish();
            return;
        }

        bindData();
        setupAction();
    }

    private void bindData() {
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        genre.setText(book.getGenre());
        pageCount.setText(String.valueOf(book.getPageCount()));
        size.setText("Dung lượng: " + formatSize(book.getSize()));
        uploader.setText("Người upload: " + book.getUploader());
        description.setText(book.getDescription());
//        deleteCheck.setChecked(book.isDeleted());
//        deleteCheck.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(convertGoogleDriveUrl(book.getThumbnailUrl()))
                .placeholder(R.drawable.ic_books)
                .error(R.drawable.ic_books)
                .into(thumbnail);

        boolean editable = "delete".equals(mode);
        title.setEnabled(editable);
        author.setEnabled(editable);
        genre.setEnabled(editable);
        pageCount.setEnabled(editable);
        description.setEnabled(editable);
        deleteCheck.setEnabled(editable);

//         Log.d("BookDetail", "isDeleted = " + book.isDeleted());
        deleteCheck.setChecked(book.getIsDeleted());
        deleteCheck.setVisibility(editable ? View.VISIBLE : View.GONE);
    }


    private void setupAction() {
        switch (mode) {
            case "download":
                actionBtn.setText("Tải xuống");
                actionBtn.setOnClickListener(v -> downloadBook());
                break;
            case "read":
                actionBtn.setText("Đọc offline");
                actionBtn.setOnClickListener(v -> readBook());
                break;
            case "delete":
                actionBtn.setText("Lưu chỉnh sửa");
                actionBtn.setOnClickListener(v -> editBook());
                break;
        }
    }

    private void downloadBook() {
        if (book.getFileUrl() == null || book.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Không có đường dẫn tải sách", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(convertGoogleDriveUrl(book.getFileUrl()));

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(book.getTitle());
        request.setDescription("Đang tải sách...");
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, book.getId() + ".pdf");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedOverMetered(true);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        Toast.makeText(this, "Đã bắt đầu tải sách", Toast.LENGTH_SHORT).show();
    }

    private void readBook() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getId() + ".pdf");
        if (!file.exists()) {
            Toast.makeText(this, "File chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void editBook() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title.getText().toString().trim());
        updates.put("author", author.getText().toString().trim());
        updates.put("genre", genre.getText().toString().trim());
        updates.put("pageCount", TextUtils.isEmpty(pageCount.getText()) ? 0 : Integer.parseInt(pageCount.getText().toString()));
        updates.put("description", description.getText().toString().trim());
        updates.put("isDeleted", deleteCheck.isChecked());

        db.collection("books").document(book.getId())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật sách", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật sách", Toast.LENGTH_SHORT).show());
    }

    private String formatSize(long bytes) {
        return String.format("%.2f MB", bytes / 1024f / 1024f);
    }

    private String convertGoogleDriveUrl(String originalUrl) {
        if (originalUrl == null || !originalUrl.contains("/d/")) return originalUrl;
        String fileId = originalUrl.split("/d/")[1].split("/")[0];
        return "https://drive.google.com/uc?export=download&id=" + fileId;
    }
}
