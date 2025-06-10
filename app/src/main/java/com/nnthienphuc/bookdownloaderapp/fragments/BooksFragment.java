package com.nnthienphuc.bookdownloaderapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.BookDetailActivity;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends BaseAuthenticatedFragment {
    private RecyclerView booksRecyclerView;
    private SearchView bookSearchView;
    private BookAdapter bookAdapter;
    private final List<Book> books = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<Intent> detailLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        bookSearchView = view.findViewById(R.id.bookSearchView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadBooks(null);
                    }
                });

        bookAdapter = new BookAdapter(getContext(), books, "download", book -> {
            Intent intent = new Intent(getContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
            intent.putExtra(BookDetailActivity.EXTRA_MODE, "download");
            detailLauncher.launch(intent);
        });

        booksRecyclerView.setAdapter(bookAdapter);
        setupSearchListener();
        loadBooks(null);
        return view;
    }

    private void setupSearchListener() {
        bookSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadBooks(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    loadBooks(null);
                }
                return false;
            }
        });
    }

    private void loadBooks(@Nullable String keyword) {
        db.collection("books")
                .whereEqualTo("isDeleted", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    books.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Book book = doc.toObject(Book.class);
                        if (book == null) continue;
                        if (keyword == null || keyword.isEmpty()
                                || book.getTitle().toLowerCase().contains(keyword.toLowerCase())
                                || book.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                                || book.getGenre().toLowerCase().contains(keyword.toLowerCase())) {
                            books.add(book);
                        }
                    }
                    bookAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        String keyword = bookSearchView.getQuery().toString().trim();
        loadBooks(keyword.isEmpty() ? null : keyword);
    }
}
