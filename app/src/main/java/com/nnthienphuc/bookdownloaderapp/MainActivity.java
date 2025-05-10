package com.nnthienphuc.bookdownloaderapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nnthienphuc.bookdownloaderapp.users.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private TextView userInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        userInfoTextView = findViewById(R.id.userInfo);
        userInfoTextView.setText("Xin chào: " + currentUser.getDisplayName() + "\nEmail: " + currentUser.getEmail());

        Button btn = findViewById(R.id.sendEventBtn);
        btn.setOnClickListener(v -> {
            Bundle clickBundle = new Bundle();
            clickBundle.putString(FirebaseAnalytics.Param.METHOD, "manual_button_click");
            mFirebaseAnalytics.logEvent("test_event", clickBundle);
            Toast.makeText(this, "Sự kiện đã được gửi", Toast.LENGTH_SHORT).show();
        });

        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
