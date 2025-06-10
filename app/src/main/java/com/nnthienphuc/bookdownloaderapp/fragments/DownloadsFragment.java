package com.nnthienphuc.bookdownloaderapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.nnthienphuc.bookdownloaderapp.BookDetailActivity;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment {
    private RecyclerView downloadsRecyclerView;
    private SearchView bookSearchView;
    private BookAdapter adapter;
    private final List<Book> downloadedBooks = new ArrayList<>();
    private final List<Book> allBooks = new ArrayList<>();
    private ActivityResultLauncher<Intent> detailLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);

        downloadsRecyclerView = view.findViewById(R.id.downloadsRecyclerView);
        bookSearchView = view.findViewById(R.id.bookSearchView);
        downloadsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadDownloadedBooks(null);
                    }
                });

        adapter = new BookAdapter(getContext(), downloadedBooks, "read", book -> {
            Intent intent = new Intent(getContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
            intent.putExtra(BookDetailActivity.EXTRA_MODE, "read");
            detailLauncher.launch(intent);
        });

        downloadsRecyclerView.setAdapter(adapter);
        setupSearchListener();
        loadDownloadedBooks(null);
        return view;
    }

    private void setupSearchListener() {
        bookSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadDownloadedBooks(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    loadDownloadedBooks(null);
                }
                return false;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        loadDownloadedBooks(bookSearchView.getQuery().toString().trim());
    }

    private void loadDownloadedBooks(@Nullable String keyword) {
        File fileDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File infoDir = requireContext().getFilesDir();
        if (fileDir == null || !fileDir.exists()) return;

        File[] pdfs = fileDir.listFiles((d, name) -> name.endsWith(".pdf"));
        downloadedBooks.clear();
        allBooks.clear();
        Gson gson = new Gson();

        if (pdfs != null) {
            for (File file : pdfs) {
                String id = file.getName().replace(".pdf", "");
                File infoFile = new File(infoDir, id + ".bookinfo");
                Book book;
                if (infoFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
                        book = gson.fromJson(reader, Book.class);
                        book.setSize(file.length());
                    } catch (Exception e) {
                        book = new Book();
                        book.setId(id);
                        book.setTitle(id);
                        book.setSize(file.length());
                    }
                } else {
                    book = new Book();
                    book.setId(id);
                    book.setTitle(id);
                    book.setSize(file.length());
                }
                allBooks.add(book);
            }
        }

        if (keyword == null || keyword.isEmpty()) {
            downloadedBooks.addAll(allBooks);
        } else {
            for (Book b : allBooks) {
                if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                        b.getGenre().toLowerCase().contains(keyword.toLowerCase())) {
                    downloadedBooks.add(b);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
