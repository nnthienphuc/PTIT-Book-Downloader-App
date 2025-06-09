package com.nnthienphuc.bookdownloaderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.*;
import java.util.*;

import okhttp3.*;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";
    public static final String EXTRA_MODE = "mode";

    private EditText title, author, genre, pageCount, description, fileUrl, thumbnailUrl;
    private TextView size, uploader;
    private ImageView thumbnail;
    private Button actionBtn;
    private CheckBox deleteCheck;
    private ProgressBar progressBar;

    private Book book;
    private String mode;
    private FirebaseFirestore db;

    private Handler mainHandler;

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
        fileUrl = findViewById(R.id.detailBookFileUrl);
        thumbnailUrl = findViewById(R.id.detailBookThumbnailUrl);
        progressBar = findViewById(R.id.detailBookProgressBar);

        mainHandler = new Handler(Looper.getMainLooper());
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
        fileUrl.setText(book.getFileUrl());
        thumbnailUrl.setText(book.getThumbnailUrl());

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
        fileUrl.setEnabled(false);
        thumbnailUrl.setEnabled(false);

        deleteCheck.setChecked(book.getIsDeleted());
        deleteCheck.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void setupAction() {
         if ("delete".equals(mode)) {
            actionBtn.setText("Lưu chỉnh sửa");
            actionBtn.setOnClickListener(v -> editBook());
        } else if (isDownloaded()) {
            actionBtn.setText("Đọc offline");
            actionBtn.setOnClickListener(v -> readBook());
            progressBar.setVisibility(View.GONE);
        } else if (isDownloaded() || "download".equals(mode)) {
            actionBtn.setText("Tải xuống");
            actionBtn.setOnClickListener(v -> downloadBook());
        }
    }

    private boolean isDownloaded() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getId() + ".pdf");
        return file.exists();
    }

    private void downloadBook() {
        try {
            File infoFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getId() + ".bookinfo");
            FileOutputStream fos = new FileOutputStream(infoFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(book.toJson());
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu bookinfo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = convertGoogleDriveUrl(book.getFileUrl());
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getId() + ".pdf");
                InputStream input = response.body().byteStream();
                long totalBytes = response.body().contentLength();
                byte[] buffer = new byte[4096];
                int read;
                long downloadedBytes = 0;
                FileOutputStream fos = new FileOutputStream(file);

                while ((read = input.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    downloadedBytes += read;
                    int progress = (int) (100 * downloadedBytes / totalBytes);
                    mainHandler.post(() -> progressBar.setProgress(progress));
                }
                fos.flush();
                fos.close();
                input.close();

                mainHandler.post(this::readBook);
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Tải thất bại", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void readBook() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getId() + ".pdf");
        if (!file.exists()) {
            Toast.makeText(this, "File chưa tải xong", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PdfReaderActivity.class);
        intent.putExtra(PdfReaderActivity.EXTRA_BOOK_ID, book.getId());
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
//        updates.put("fileUrl", fileUrl.getText().toString().trim());
//        updates.put("thumbnailUrl", thumbnailUrl.getText().toString().trim());

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
