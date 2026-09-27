package com.samiksha.bloomcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;
    private MaterialButton btnCreateAccount;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        connectViews();
        setupLoginButton();
        setupCreateAccountButton();
    }

    private void connectViews() {

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
    }

    private void setupLoginButton() {

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText() == null
                    ? ""
                    : etEmail.getText().toString().trim();

            String password = etPassword.getText() == null
                    ? ""
                    : etPassword.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        Toast.makeText(
                                LoginActivity.this,
                                "Welcome back ♡",
                                Toast.LENGTH_SHORT
                        ).show();

                        openDashboard();
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });
    }

    private void setupCreateAccountButton() {

        btnCreateAccount.setOnClickListener(v -> {

            String email = etEmail.getText() == null
                    ? ""
                    : etEmail.getText().toString().trim();

            String password = etPassword.getText() == null
                    ? ""
                    : etPassword.getText().toString().trim();

            if (!validateInput(email, password)) {
                return;
            }

            firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        Toast.makeText(
                                LoginActivity.this,
                                "BloomCare account created ♡",
                                Toast.LENGTH_SHORT
                        ).show();

                        openDashboard();
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                LoginActivity.this,
                                "Could not create account: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });
    }

    private boolean validateInput(String email, String password) {

        if (TextUtils.isEmpty(email)) {

            etEmail.setError("Enter your email");
            etEmail.requestFocus();

            return false;
        }

        if (TextUtils.isEmpty(password)) {

            etPassword.setError("Enter your password");
            etPassword.requestFocus();

            return false;
        }

        if (password.length() < 6) {

            etPassword.setError(
                    "Password must contain at least 6 characters"
            );

            etPassword.requestFocus();

            return false;
        }

        return true;
    }

    private void openDashboard() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        startActivity(intent);

        finish();
    }
}