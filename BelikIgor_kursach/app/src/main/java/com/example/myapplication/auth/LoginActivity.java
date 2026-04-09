package com.example.myapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.util.PasswordUtil;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            goMain();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextInputEditText email = findViewById(R.id.inputEmail);
        TextInputEditText password = findViewById(R.id.inputPassword);
        MaterialButton login = findViewById(R.id.btnLogin);
        MaterialButton goReg = findViewById(R.id.btnGoRegister);

        login.setOnClickListener(v -> {
            String em = text(email);
            String pw = text(password);
            if (em.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            String hash = PasswordUtil.hash(em, pw);
            long id = DatabaseHelper.getInstance(this).login(em, hash);
            if (id < 0) {
                Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
                return;
            }
            session.setUserId(id);
            goMain();
        });

        goReg.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private static String text(TextInputEditText e) {
        if (e.getText() == null) return "";
        return e.getText().toString().trim();
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
