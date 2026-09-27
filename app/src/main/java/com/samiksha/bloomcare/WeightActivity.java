package com.samiksha.bloomcare;

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
import java.util.Locale;
import java.util.Map;

public class WeightActivity extends AppCompatActivity {

    private DatabaseReference weightRef;

    private TextView btnBack;
    private TextView tvCurrentWeight;
    private TextView tvWeightChange;
    private TextView tvPreviousWeight;
    private TextView tvDifference;

    private TextInputEditText etWeight;

    private MaterialButton btnSaveWeight;

    private double currentWeight = 62.5;
    private double previousWeight = 62.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weight);

        connectViews();

        setupFirebase();

        setupSaveButton();

        setupBackButton();

        loadWeightFromFirebase();

        updateWeightUI();
    }


    // ---------------------------------------------------------
    // CONNECT XML
    // ---------------------------------------------------------

    private void connectViews() {

        btnBack = findViewById(R.id.btnBack);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvWeightChange = findViewById(R.id.tvWeightChange);
        tvPreviousWeight = findViewById(R.id.tvPreviousWeight);
        tvDifference = findViewById(R.id.tvDifference);

        etWeight = findViewById(R.id.etWeight);

        btnSaveWeight = findViewById(R.id.btnSaveWeight);
    }


    // ---------------------------------------------------------
    // FIREBASE - USER SPECIFIC
    // ---------------------------------------------------------

    private void setupFirebase() {

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    WeightActivity.this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        String userId =
                currentUser.getUid();

        weightRef = FirebaseDatabase
                .getInstance(
                        getString(R.string.firebase_database_url)
                )
                .getReference("users")
                .child(userId)
                .child("weight");
    }


    // ---------------------------------------------------------
    // SAVE BUTTON
    // ---------------------------------------------------------

    private void setupSaveButton() {

        btnSaveWeight.setOnClickListener(v -> {

            String input =
                    etWeight.getText() == null
                            ? ""
                            : etWeight.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {

                Toast.makeText(
                        WeightActivity.this,
                        "Please enter your weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            double newWeight;

            try {

                newWeight =
                        Double.parseDouble(input);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        WeightActivity.this,
                        "Please enter a valid weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (newWeight <= 0 || newWeight > 500) {

                Toast.makeText(
                        WeightActivity.this,
                        "Please enter a realistic weight.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            previousWeight =
                    currentWeight;

            currentWeight =
                    newWeight;

            updateWeightUI();

            saveWeightToFirebase();

            etWeight.setText("");
        });
    }


    // ---------------------------------------------------------
    // FIREBASE WRITE
    // ---------------------------------------------------------

    private void saveWeightToFirebase() {

        if (weightRef == null) {
            return;
        }

        Map<String, Object> weightData =
                new HashMap<>();

        weightData.put(
                "currentWeight",
                currentWeight
        );

        weightData.put(
                "previousWeight",
                previousWeight
        );


        weightRef
                .updateChildren(weightData)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            WeightActivity.this,
                            "Weight updated successfully ♡",
                            Toast.LENGTH_SHORT
                    ).show();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            WeightActivity.this,
                            "Could not save weight data.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ---------------------------------------------------------
    // FIREBASE READ
    // ---------------------------------------------------------

    private void loadWeightFromFirebase() {

        if (weightRef == null) {
            return;
        }

        weightRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        Double savedCurrent =
                                snapshot
                                        .child("currentWeight")
                                        .getValue(Double.class);

                        Double savedPrevious =
                                snapshot
                                        .child("previousWeight")
                                        .getValue(Double.class);


                        if (savedCurrent != null) {

                            currentWeight =
                                    savedCurrent;
                        }


                        if (savedPrevious != null) {

                            previousWeight =
                                    savedPrevious;
                        }


                        updateWeightUI();
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                WeightActivity.this,
                                "Could not load weight data.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // UPDATE UI
    // ---------------------------------------------------------

    private void updateWeightUI() {

        double difference =
                currentWeight - previousWeight;


        tvCurrentWeight.setText(
                formatWeight(currentWeight) + " kg"
        );


        tvPreviousWeight.setText(
                "Previous: "
                        + formatWeight(previousWeight)
                        + " kg"
        );


        tvDifference.setText(
                "Change: "
                        + String.format(
                        Locale.US,
                        "%.1f",
                        difference
                )
                        + " kg"
        );


        if (difference > 0) {

            tvWeightChange.setText(
                    "+"
                            + String.format(
                            Locale.US,
                            "%.1f",
                            difference
                    )
                            + " kg from previous entry"
            );

        } else if (difference < 0) {

            tvWeightChange.setText(
                    String.format(
                            Locale.US,
                            "%.1f",
                            difference
                    )
                            + " kg from previous entry"
            );

        } else {

            tvWeightChange.setText(
                    "No change from previous entry"
            );
        }
    }


    // ---------------------------------------------------------
    // FORMAT
    // ---------------------------------------------------------

    private String formatWeight(double weight) {

        return String.format(
                Locale.US,
                "%.1f",
                weight
        );
    }


    // ---------------------------------------------------------
    // BACK
    // ---------------------------------------------------------

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }
}