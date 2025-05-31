package com.nnthienphuc.bookdownloaderapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends BaseAuthenticatedFragment  {

    private RecyclerView downloadsRecyclerView;
    private BookAdapter adapter;
    private final List<Book> downloadedBooks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);
        downloadsRecyclerView = view.findViewById(R.id.downloadsRecyclerView);
        downloadsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookAdapter(getContext(), downloadedBooks, "read");
        downloadsRecyclerView.setAdapter(adapter);

        loadDownloadedBooks();
        return view;
    }

    private void loadDownloadedBooks() {
        File dir = requireContext().getFilesDir();
        File[] pdfs = dir.listFiles((d, name) -> name.endsWith(".pdf"));
        downloadedBooks.clear();
        if (pdfs != null) {
            for (File file : pdfs) {
                String title = file.getName().replace(".pdf", "");
                Book book = new Book();
                book.setId(title);
                book.setTitle(title);
                book.setSize(file.length());
                downloadedBooks.add(book);
            }
        }
        adapter.notifyDataSetChanged();
    }
}

