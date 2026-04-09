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
import com.example.myapplication.util.RegistrationValidator;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutPasswordConfirm;
    private TextInputEditText inputName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputPasswordConfirm;
    private MaterialCheckBox checkConsent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        layoutName = findViewById(R.id.layout_name);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        layoutPasswordConfirm = findViewById(R.id.layout_password_confirm);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputPasswordConfirm = findViewById(R.id.inputPasswordConfirm);
        checkConsent = findViewById(R.id.check_consent);
        checkConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkConsent.setError(null);
        });

        MaterialButton reg = findViewById(R.id.btnRegister);
        MaterialButton goLogin = findViewById(R.id.btnGoLogin);

        reg.setOnClickListener(v -> attemptRegister());

        goLogin.setOnClickListener(v -> finish());
    }

    private void clearFieldErrors() {
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutPasswordConfirm.setError(null);
        checkConsent.setError(null);
    }

    private void attemptRegister() {
        clearFieldErrors();

        String nameRaw = text(inputName);
        String emailRaw = text(inputEmail);
        String password = passwordText(inputPassword);
        String confirm = passwordText(inputPasswordConfirm);

        Integer e = RegistrationValidator.validateName(nameRaw);
        if (e != null) {
            layoutName.setError(getString(e));
            return;
        }

        e = RegistrationValidator.validateEmail(emailRaw);
        if (e != null) {
            layoutEmail.setError(getString(e));
            return;
        }

        e = RegistrationValidator.validatePassword(password);
        if (e != null) {
            layoutPassword.setError(getString(e));
            return;
        }

        e = RegistrationValidator.validatePasswordMatch(password, confirm);
        if (e != null) {
            layoutPasswordConfirm.setError(getString(e));
            return;
        }

        if (!checkConsent.isChecked()) {
            checkConsent.setError(getString(R.string.error_consent_required));
            return;
        }

        String name = nameRaw.trim();
        String em = emailRaw.trim();

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (db.emailExists(em)) {
            layoutEmail.setError(getString(R.string.error_email_exists));
            Toast.makeText(this, R.string.error_email_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        String hash = PasswordUtil.hash(em, password);
        long id = db.registerUser(em, hash, name);
        if (id < 0) {
            Toast.makeText(this, R.string.error_register, Toast.LENGTH_SHORT).show();
            return;
        }
        new SessionManager(this).setUserId(id);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static String text(TextInputEditText e) {
        if (e.getText() == null) return "";
        return e.getText().toString();
    }

    /** Пароль без trim — пробелы значимы для совпадения. */
    private static String passwordText(TextInputEditText e) {
        if (e.getText() == null) return "";
        return e.getText().toString();
    }
}
