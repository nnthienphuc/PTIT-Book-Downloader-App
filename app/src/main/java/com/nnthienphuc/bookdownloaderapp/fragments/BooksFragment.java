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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends BaseAuthenticatedFragment {
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private List<Book> books = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bookAdapter = new BookAdapter(getContext(), books, "download");
        booksRecyclerView.setAdapter(bookAdapter);
        loadBooks();
        return view;
    }

    private void loadBooks() {
        db.collection("books")
                .whereEqualTo("deleted", false)
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
