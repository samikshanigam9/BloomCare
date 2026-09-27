package com.samiksha.bloomcare;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class HydrationActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private DatabaseReference hydrationRef;
    private DatabaseReference hydrationHistoryRef;

    private TextView btnBack;
    private TextView tvWaterCount;
    private TextView tvWaterPercent;

    private ProgressBar progressWater;

    private MaterialButton btnAddWater;
    private MaterialButton btnRemoveWater;

    private int waterGlasses = 6;
    private final int waterGoal = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hydration);

        connectViews();

        if (!setupFirebase()) {
            return;
        }

        setupButtons();
        setupBackButton();

        loadHydrationFromFirebase();
    }

    private void connectViews() {

        btnBack = findViewById(R.id.btnBack);

        tvWaterCount = findViewById(R.id.tvWaterCount);
        tvWaterPercent = findViewById(R.id.tvWaterPercent);

        progressWater = findViewById(R.id.progressWater);

        btnAddWater = findViewById(R.id.btnAddWater);
        btnRemoveWater = findViewById(R.id.btnRemoveWater);
    }

    private boolean setupFirebase() {

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return false;
        }

        String userId = currentUser.getUid();

        userRef = FirebaseDatabase
                .getInstance(
                        getString(R.string.firebase_database_url)
                )
                .getReference("users")
                .child(userId);

        hydrationRef =
                userRef.child("hydration");

        hydrationHistoryRef =
                userRef
                        .child("history")
                        .child("hydration");

        return true;
    }

    private void setupButtons() {

        btnAddWater.setOnClickListener(v -> {

            waterGlasses++;

            updateHydrationUI();
            saveHydration();

            if (waterGlasses == waterGoal) {

                Toast.makeText(
                        this,
                        "Daily hydration goal reached ♡",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnRemoveWater.setOnClickListener(v -> {

            if (waterGlasses > 0) {

                waterGlasses--;

                updateHydrationUI();
                saveHydration();

            } else {

                Toast.makeText(
                        this,
                        "Water count cannot go below 0.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateHydrationUI() {

        tvWaterCount.setText(
                waterGlasses + " / " + waterGoal
        );

        int percentage =
                (waterGlasses * 100) / waterGoal;

        tvWaterPercent.setText(
                percentage + "% of daily goal"
        );

        progressWater.setProgress(
                Math.min(percentage, 100)
        );
    }

    private void saveHydration() {

        String historyId =
                hydrationHistoryRef.push().getKey();

        if (historyId == null) {
            return;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "hydration/currentGlasses",
                waterGlasses
        );

        updates.put(
                "history/hydration/"
                        + historyId
                        + "/currentGlasses",
                waterGlasses
        );

        updates.put(
                "history/hydration/"
                        + historyId
                        + "/timestamp",
                ServerValue.TIMESTAMP
        );

        userRef.updateChildren(updates)
                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Could not save hydration.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void loadHydrationFromFirebase() {

        hydrationRef
                .child("currentGlasses")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                Long value =
                                        snapshot.getValue(Long.class);

                                if (value != null) {

                                    waterGlasses =
                                            value.intValue();
                                }

                                updateHydrationUI();
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {

                                Toast.makeText(
                                        HydrationActivity.this,
                                        "Could not load hydration.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void setupBackButton() {

        btnBack.setOnClickListener(
                v -> finish()
        );
    }
}