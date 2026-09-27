package com.samiksha.bloomcare;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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

public class WorkoutActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private DatabaseReference workoutRef;
    private DatabaseReference workoutHistoryRef;

    private TextView btnBack;
    private TextView tvWorkoutName;
    private TextView tvWorkoutDetails;

    private MaterialCardView cardStretching;
    private MaterialCardView cardWalking;
    private MaterialCardView cardBreathing;

    private MaterialButton btnCompleteWorkout;

    private String selectedWorkout =
            "Gentle Stretching";

    private String selectedDetails =
            "15 min • Low Intensity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workout);

        connectViews();

        if (!setupFirebase()) {
            return;
        }

        setupWorkoutOptions();
        setupCompleteButton();
        setupBackButton();

        loadWorkoutFromFirebase();
    }

    private void connectViews() {

        btnBack =
                findViewById(R.id.btnBack);

        tvWorkoutName =
                findViewById(R.id.tvWorkoutName);

        tvWorkoutDetails =
                findViewById(R.id.tvWorkoutDetails);

        cardStretching =
                findViewById(R.id.cardStretching);

        cardWalking =
                findViewById(R.id.cardWalking);

        cardBreathing =
                findViewById(R.id.cardBreathing);

        btnCompleteWorkout =
                findViewById(R.id.btnCompleteWorkout);
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

        String userId =
                currentUser.getUid();

        userRef = FirebaseDatabase
                .getInstance(
                        getString(R.string.firebase_database_url)
                )
                .getReference("users")
                .child(userId);

        workoutRef =
                userRef.child("workout");

        workoutHistoryRef =
                userRef
                        .child("history")
                        .child("workout");

        return true;
    }

    private void setupWorkoutOptions() {

        cardStretching.setOnClickListener(v ->

                selectWorkout(
                        "Gentle Stretching",
                        "15 min • Low Intensity"
                )
        );

        cardWalking.setOnClickListener(v ->

                selectWorkout(
                        "Light Walking",
                        "20 min • Low Intensity"
                )
        );

        cardBreathing.setOnClickListener(v ->

                selectWorkout(
                        "Breathing Exercise",
                        "10 min • Relaxation"
                )
        );
    }

    private void selectWorkout(
            String name,
            String details
    ) {

        selectedWorkout = name;
        selectedDetails = details;

        tvWorkoutName.setText(name);
        tvWorkoutDetails.setText(details);
    }

    private void setupCompleteButton() {

        btnCompleteWorkout.setOnClickListener(v ->

                saveWorkout()
        );
    }

    private void saveWorkout() {

        String historyId =
                workoutHistoryRef.push().getKey();

        if (historyId == null) {
            return;
        }

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "workout/selectedWorkout",
                selectedWorkout
        );

        updates.put(
                "workout/selectedDetails",
                selectedDetails
        );

        updates.put(
                "workout/completed",
                true
        );

        updates.put(
                "history/workout/"
                        + historyId
                        + "/selectedWorkout",
                selectedWorkout
        );

        updates.put(
                "history/workout/"
                        + historyId
                        + "/selectedDetails",
                selectedDetails
        );

        updates.put(
                "history/workout/"
                        + historyId
                        + "/completed",
                true
        );

        updates.put(
                "history/workout/"
                        + historyId
                        + "/timestamp",
                ServerValue.TIMESTAMP
        );

        userRef.updateChildren(updates)

                .addOnSuccessListener(unused ->

                        Toast.makeText(
                                this,
                                selectedWorkout
                                        + " marked complete ♡",
                                Toast.LENGTH_SHORT
                        ).show()
                )

                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Could not save workout.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void loadWorkoutFromFirebase() {

        workoutRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        String savedWorkout =
                                snapshot
                                        .child("selectedWorkout")
                                        .getValue(String.class);

                        String savedDetails =
                                snapshot
                                        .child("selectedDetails")
                                        .getValue(String.class);

                        if (savedWorkout != null) {

                            selectedWorkout =
                                    savedWorkout;

                            tvWorkoutName.setText(
                                    savedWorkout
                            );
                        }

                        if (savedDetails != null) {

                            selectedDetails =
                                    savedDetails;

                            tvWorkoutDetails.setText(
                                    savedDetails
                            );
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                WorkoutActivity.this,
                                "Could not load workout.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setupBackButton() {

        btnBack.setOnClickListener(v -> finish());
    }
}