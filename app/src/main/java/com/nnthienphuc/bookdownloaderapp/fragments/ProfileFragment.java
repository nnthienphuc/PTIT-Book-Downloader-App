package com.nnthienphuc.bookdownloaderapp.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.BookDetailActivity;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.UploadBookActivity;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;
import com.nnthienphuc.bookdownloaderapp.users.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseAuthenticatedFragment {
    private RecyclerView uploadedBooksRecyclerView;
    private SearchView bookSearchView;
    private BookAdapter adapter;
    private final List<Book> uploadedBooks = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private ActivityResultLauncher<Intent> detailLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView userInfo = view.findViewById(R.id.userInfo);
        userInfo.setText("Xin chào: " + user.getDisplayName() + "\nEmail: " + user.getEmail());

        ImageView userPhoto = view.findViewById(R.id.userAvatar);
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(userPhoto);
        }

        Button uploadBtn = view.findViewById(R.id.uploadBookBtn);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UploadBookActivity.class);
            detailLauncher.launch(intent);
        });

        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        bookSearchView = view.findViewById(R.id.bookSearchView);
        uploadedBooksRecyclerView = view.findViewById(R.id.uploadedBooksRecyclerView);
        uploadedBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        loadUploadedBooks(null);
                    }
                });

        adapter = new BookAdapter(getContext(), uploadedBooks, "delete", book -> {
            Intent intent = new Intent(getContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
            intent.putExtra(BookDetailActivity.EXTRA_MODE, "delete");
            detailLauncher.launch(intent);
        });

        uploadedBooksRecyclerView.setAdapter(adapter);
        setupSearchListener();
        loadUploadedBooks(null);

        Button themeBtn = view.findViewById(R.id.changeThemeBtn);
        themeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Chọn giao diện")
                    .setItems(new String[]{"Hệ thống", "Sáng", "Tối"}, (dialog, which) -> {
                        int mode;
                        switch (which) {
                            case 1: mode = AppCompatDelegate.MODE_NIGHT_NO; break;
                            case 2: mode = AppCompatDelegate.MODE_NIGHT_YES; break;
                            default: mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        }
                        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
                        editor.putInt("theme_mode", mode).apply();
                        AppCompatDelegate.setDefaultNightMode(mode);
                    })
                    .show();
        });

        return view;
    }

    private void setupSearchListener() {
        bookSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadUploadedBooks(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    loadUploadedBooks(null);
                }
                return false;
            }
        });
    }

    private void loadUploadedBooks(@Nullable String keyword) {
        if (user == null) return;
        db.collection("books")
                .whereEqualTo("uploaderUid", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    uploadedBooks.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Book book = doc.toObject(Book.class);
                        if (book == null) continue;
                        if (keyword == null || keyword.isEmpty()
                                || book.getTitle().toLowerCase().contains(keyword.toLowerCase())
                                || book.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                                || book.getGenre().toLowerCase().contains(keyword.toLowerCase())) {
                            uploadedBooks.add(book);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUploadedBooks(bookSearchView.getQuery().toString().trim());
    }
}