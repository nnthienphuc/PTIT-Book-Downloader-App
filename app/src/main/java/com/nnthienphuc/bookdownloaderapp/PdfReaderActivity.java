package com.nnthienphuc.bookdownloaderapp;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
//import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.PDFView;


import java.io.File;

public class PdfReaderActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);

        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        File file = new File(getFilesDir(), bookId + ".pdf");

        PDFView pdfView = findViewById(R.id.pdfView);
        if (file.exists()) {
            pdfView.fromFile(file).load();
        } else {
            finish();
        }
    }
}
