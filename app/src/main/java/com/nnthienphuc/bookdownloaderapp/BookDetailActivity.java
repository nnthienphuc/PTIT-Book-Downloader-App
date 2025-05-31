package com.nnthienphuc.bookdownloaderapp;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.File;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final String EXTRA_MODE = "mode"; // "download" | "read" | "delete"

    private TextView title, author, genre, pageCount, size, uploader, description;
    private ImageView thumbnail;
    private Button actionBtn;

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
        author.setText("Tác giả: " + book.getAuthor());
        genre.setText("Thể loại: " + book.getGenre());
        pageCount.setText("Số trang: " + book.getPageCount());
        size.setText("Dung lượng: " + formatSize(book.getSize()));
        uploader.setText("Người upload: " + book.getUploader());
        description.setText(book.getDescription());

        Glide.with(this).load(book.getThumbnailUrl()).into(thumbnail);
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
                actionBtn.setText("Xóa sách");
                actionBtn.setOnClickListener(v -> deleteBook());
                break;
        }
    }

    private void downloadBook() {
        if (book.getFileUrl() == null || book.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Không có đường dẫn tải sách", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(book.getFileUrl());
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

    private void deleteBook() {
        db.collection("books").document(book.getId())
                .update("isDeleted", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã ẩn sách khỏi hệ thống", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa sách", Toast.LENGTH_SHORT).show());
    }

    private String formatSize(long bytes) {
        return String.format("%.2f MB", bytes / 1024f / 1024f);
    }
}
