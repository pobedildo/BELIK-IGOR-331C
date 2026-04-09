package com.example.myapplication.ui.profile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.User;
import com.example.myapplication.util.SessionManager;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        long uid = new SessionManager(this).getUserId();
        User user = DatabaseHelper.getInstance(this).getUser(uid);
        TextView email = findViewById(R.id.text_email);
        TextView reg = findViewById(R.id.text_registered);
        if (user != null) {
            email.setText(user.getEmail());
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru"));
            reg.setText(sdf.format(new Date(user.getCreatedAt())));
        }
    }
}
