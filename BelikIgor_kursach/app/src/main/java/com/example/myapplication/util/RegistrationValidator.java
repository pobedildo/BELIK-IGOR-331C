package com.example.myapplication.util;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.myapplication.R;

import java.util.regex.Pattern;

/**
 * Валидация полей регистрации.
 */
public final class RegistrationValidator {

    /** Email: user@domain.tld (домен с точкой, зона не короче 2 букв). */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z]{2,})+$"
    );

    private RegistrationValidator() {
    }

    /**
     * Имя: 2–30 символов (после trim), только буквы Unicode, пробел и дефис U+002D.
     *
     * @return null если ок, иначе id строки с ошибкой
     */
    @Nullable
    @StringRes
    public static Integer validateName(String raw) {
        if (raw == null) return R.string.error_name_invalid;
        String name = raw.trim();
        int len = name.length();
        if (len < 2 || len > 30) return R.string.error_name_length;
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '-') {
                return R.string.error_name_chars;
            }
        }
        return null;
    }

    @Nullable
    @StringRes
    public static Integer validateEmail(String email) {
        if (email == null) return R.string.error_email_format;
        String em = email.trim();
        if (em.isEmpty()) return R.string.error_email_format;
        if (!EMAIL_PATTERN.matcher(em).matches()) return R.string.error_email_format;
        return null;
    }

    @Nullable
    @StringRes
    public static Integer validatePassword(String password) {
        if (password == null || password.length() < 6) return R.string.error_password_min;
        return null;
    }

    @Nullable
    @StringRes
    public static Integer validatePasswordMatch(String password, String confirm) {
        if (password == null || confirm == null) return R.string.error_password_mismatch;
        if (!password.equals(confirm)) return R.string.error_password_mismatch;
        return null;
    }
}
