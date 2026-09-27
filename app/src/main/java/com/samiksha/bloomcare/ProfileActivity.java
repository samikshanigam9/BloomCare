package com.samiksha.bloomcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView btnBack;
    private TextView tvProfileName;
    private TextView tvProfileEmail;

    private TextInputEditText etName;
    private TextInputEditText etAge;
    private TextInputEditText etHeight;
    private TextInputEditText etEmail;

    private MaterialButton btnSaveProfile;
    private MaterialButton btnLogout;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private DatabaseReference profileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        connectViews();

        if (!setupFirebase()) {
            return;
        }

        setupBackButton();
        setupSaveButton();
        setupLogoutButton();

        loadProfileFromFirebase();
    }

    // ---------------------------------------------------------
    // CONNECT XML
    // ---------------------------------------------------------

    private void connectViews() {

        btnBack = findViewById(R.id.btnBack);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etEmail = findViewById(R.id.etEmail);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    // ---------------------------------------------------------
    // FIREBASE SETUP
    // ---------------------------------------------------------

    private boolean setupFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    ProfileActivity.this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            openLoginScreen();

            return false;
        }

        String userId = currentUser.getUid();

        profileRef = FirebaseDatabase
                .getInstance(
                        getString(R.string.firebase_database_url)
                )
                .getReference("users")
                .child(userId)
                .child("profile");

        String email = currentUser.getEmail();

        if (email != null) {

            etEmail.setText(email);
            tvProfileEmail.setText(email);
        }

        return true;
    }

    // ---------------------------------------------------------
    // SAVE PROFILE
    // ---------------------------------------------------------

    private void setupSaveButton() {

        btnSaveProfile.setOnClickListener(v -> {

            String name =
                    etName.getText() == null
                            ? ""
                            : etName.getText().toString().trim();

            String ageText =
                    etAge.getText() == null
                            ? ""
                            : etAge.getText().toString().trim();

            String heightText =
                    etHeight.getText() == null
                            ? ""
                            : etHeight.getText().toString().trim();


            if (TextUtils.isEmpty(name)) {

                etName.setError("Enter your name");
                etName.requestFocus();

                return;
            }


            if (TextUtils.isEmpty(ageText)) {

                etAge.setError("Enter your age");
                etAge.requestFocus();

                return;
            }


            if (TextUtils.isEmpty(heightText)) {

                etHeight.setError("Enter your height");
                etHeight.requestFocus();

                return;
            }


            int age;
            double height;

            try {

                age = Integer.parseInt(ageText);
                height = Double.parseDouble(heightText);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        ProfileActivity.this,
                        "Please enter valid profile details.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (age <= 0 || age > 120) {

                etAge.setError("Enter a valid age");
                etAge.requestFocus();

                return;
            }


            if (height <= 0 || height > 300) {

                etHeight.setError("Enter a valid height in cm");
                etHeight.requestFocus();

                return;
            }


            saveProfileToFirebase(
                    name,
                    age,
                    height
            );
        });
    }

    // ---------------------------------------------------------
    // FIREBASE WRITE
    // ---------------------------------------------------------

    private void saveProfileToFirebase(
            String name,
            int age,
            double height
    ) {

        if (profileRef == null || currentUser == null) {
            return;
        }

        String email = currentUser.getEmail();

        Map<String, Object> profileData =
                new HashMap<>();

        profileData.put(
                "name",
                name
        );

        profileData.put(
                "age",
                age
        );

        profileData.put(
                "height",
                height
        );

        profileData.put(
                "email",
                email == null ? "" : email
        );


        profileRef
                .updateChildren(profileData)

                .addOnSuccessListener(unused -> {

                    tvProfileName.setText(name);

                    if (email != null) {
                        tvProfileEmail.setText(email);
                    }

                    Toast.makeText(
                            ProfileActivity.this,
                            "Profile saved successfully ♡",
                            Toast.LENGTH_SHORT
                    ).show();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Could not save profile.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // FIREBASE READ
    // ---------------------------------------------------------

    private void loadProfileFromFirebase() {

        if (profileRef == null) {
            return;
        }

        profileRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        String name =
                                snapshot
                                        .child("name")
                                        .getValue(String.class);

                        Long age =
                                snapshot
                                        .child("age")
                                        .getValue(Long.class);

                        Double height =
                                snapshot
                                        .child("height")
                                        .getValue(Double.class);

                        String savedEmail =
                                snapshot
                                        .child("email")
                                        .getValue(String.class);


                        if (name != null) {

                            etName.setText(name);
                            tvProfileName.setText(name);
                        }


                        if (age != null) {

                            etAge.setText(
                                    String.valueOf(
                                            age.intValue()
                                    )
                            );
                        }


                        if (height != null) {

                            etHeight.setText(
                                    String.valueOf(height)
                            );
                        }


                        if (savedEmail != null
                                && !savedEmail.isEmpty()) {

                            etEmail.setText(savedEmail);
                            tvProfileEmail.setText(savedEmail);
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Could not load profile.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // BACK BUTTON
    // ---------------------------------------------------------

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    private void setupLogoutButton() {

        btnLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Toast.makeText(
                    ProfileActivity.this,
                    "Logged out successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            openLoginScreen();
        });
    }

    // ---------------------------------------------------------
    // OPEN LOGIN
    // ---------------------------------------------------------

    private void openLoginScreen() {

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}