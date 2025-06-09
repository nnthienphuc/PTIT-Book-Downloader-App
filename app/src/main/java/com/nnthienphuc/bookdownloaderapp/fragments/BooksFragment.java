package com.nnthienphuc.bookdownloaderapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private BookAdapter bookAdapter;
    private final List<Book> books = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<Intent> detailLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadBooks();
                    }
                });

        bookAdapter = new BookAdapter(getContext(), books, "download", book -> {
            Intent intent = new Intent(getContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
            intent.putExtra(BookDetailActivity.EXTRA_MODE, "download");
            detailLauncher.launch(intent);
        });

        booksRecyclerView.setAdapter(bookAdapter);
        loadBooks();
        return view;
    }

    private void loadBooks() {
        db.collection("books")
                .whereEqualTo("isDeleted", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    books.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        books.add(doc.toObject(Book.class));
                    }
                    bookAdapter.notifyDataSetChanged();
                });
    }
}