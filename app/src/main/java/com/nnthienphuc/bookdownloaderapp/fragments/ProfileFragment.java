package com.nnthienphuc.bookdownloaderapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nnthienphuc.bookdownloaderapp.R;
import com.nnthienphuc.bookdownloaderapp.UploadBookActivity;
import com.nnthienphuc.bookdownloaderapp.adapters.BookAdapter;
import com.nnthienphuc.bookdownloaderapp.models.Book;
import com.nnthienphuc.bookdownloaderapp.users.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends BaseAuthenticatedFragment {
    private RecyclerView uploadedBooksRecyclerView;
    private BookAdapter adapter;
    private List<Book> uploadedBooks = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

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
            startActivity(new Intent(getContext(), UploadBookActivity.class));
        });

        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        uploadedBooksRecyclerView = view.findViewById(R.id.uploadedBooksRecyclerView);
        uploadedBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookAdapter(getContext(), uploadedBooks, "delete");
        uploadedBooksRecyclerView.setAdapter(adapter);
        loadUploadedBooks();

        return view;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadUploadedBooks() {
        db.collection("books")
                .whereEqualTo("uploaderUid", user.getUid())
                .whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    uploadedBooks.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        uploadedBooks.add(doc.toObject(Book.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
