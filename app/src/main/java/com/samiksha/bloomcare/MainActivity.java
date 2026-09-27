package com.samiksha.bloomcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ---------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------

    private DatabaseReference userRef;
    private DatabaseReference hydrationRef;
    private DatabaseReference weightRef;
    private DatabaseReference workoutRef;
    private DatabaseReference recoveryRef;


    // ---------------------------------------------------------
    // TEXT VIEWS
    // ---------------------------------------------------------

    private TextView tvWater;
    private TextView tvSleep;

    private TextView tvWellnessScore;
    private TextView tvEnergyHome;

    private TextView tvWorkoutHome;
    private TextView tvWeightHome;
    private TextView tvRecoveryHome;

    private TextView tvAiInsight;


    // ---------------------------------------------------------
    // PROGRESS BAR
    // ---------------------------------------------------------

    private ProgressBar progressWellness;


    // ---------------------------------------------------------
    // BUTTONS
    // ---------------------------------------------------------

    private MaterialButton btnAddWater;
    private MaterialButton btnAiInsight;


    // ---------------------------------------------------------
    // CARDS
    // ---------------------------------------------------------

    private MaterialCardView cardHydration;
    private MaterialCardView cardWorkout;
    private MaterialCardView cardWeight;
    private MaterialCardView cardRecovery;
    private MaterialCardView cardProfile;
    private MaterialCardView cardHistory;


    // ---------------------------------------------------------
    // LOCAL DATA
    // ---------------------------------------------------------

    private int waterGlasses = 0;

    private final int waterGoal = 8;


    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        connectViews();


        boolean firebaseReady =
                setupFirebase();


        if (!firebaseReady) {
            return;
        }


        setupHydrationButton();

        setupCards();

        setupAiPreview();


        loadHydration();

        loadWeight();

        loadWorkout();

        loadRecovery();

        loadAiInsight();

    }


    // ---------------------------------------------------------
    // CONNECT XML
    // ---------------------------------------------------------

    private void connectViews() {

        tvWater =
                findViewById(R.id.tvWater);

        tvSleep =
                findViewById(R.id.tvSleep);


        tvWellnessScore =
                findViewById(R.id.tvWellnessScore);

        tvEnergyHome =
                findViewById(R.id.tvEnergyHome);


        tvWorkoutHome =
                findViewById(R.id.tvWorkoutHome);

        tvWeightHome =
                findViewById(R.id.tvWeightHome);

        tvRecoveryHome =
                findViewById(R.id.tvRecoveryHome);


        tvAiInsight =
                findViewById(R.id.tvAiInsight);


        progressWellness =
                findViewById(R.id.progressWellness);


        btnAddWater =
                findViewById(R.id.btnAddWater);

        btnAiInsight =
                findViewById(R.id.btnAiInsight);


        cardHydration =
                findViewById(R.id.cardHydration);

        cardWorkout =
                findViewById(R.id.cardWorkout);

        cardWeight =
                findViewById(R.id.cardWeight);

        cardRecovery =
                findViewById(R.id.cardRecovery);

        cardProfile =
                findViewById(R.id.cardProfile);

        cardHistory =
                findViewById(R.id.cardHistory);
    }


    // ---------------------------------------------------------
    // USER-SPECIFIC FIREBASE
    // ---------------------------------------------------------

    private boolean setupFirebase() {

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();


        if (currentUser == null) {

            Toast.makeText(
                    MainActivity.this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();


            Intent intent =
                    new Intent(
                            MainActivity.this,
                            LoginActivity.class
                    );


            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );


            startActivity(intent);

            finish();

            return false;
        }


        String userId =
                currentUser.getUid();


        FirebaseDatabase database =
                FirebaseDatabase.getInstance(
                        getString(R.string.firebase_database_url)
                );


        userRef =
                database
                        .getReference("users")
                        .child(userId);


        hydrationRef =
                userRef.child("hydration");

        weightRef =
                userRef.child("weight");

        workoutRef =
                userRef.child("workout");

        recoveryRef =
                userRef.child("recovery");


        return true;
    }


    // ---------------------------------------------------------
    // HYDRATION READ
    // ---------------------------------------------------------

    private void loadHydration() {

        hydrationRef
                .child("currentGlasses")
                .addValueEventListener(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                Long savedGlasses =
                                        snapshot.getValue(Long.class);


                                if (savedGlasses != null) {

                                    waterGlasses =
                                            savedGlasses.intValue();

                                } else {

                                    waterGlasses = 0;
                                }


                                tvWater.setText(
                                        waterGlasses
                                                + " / "
                                                + waterGoal
                                );
                            }


                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Could not load hydration data.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }


    // ---------------------------------------------------------
    // DASHBOARD +1 WATER
    // ---------------------------------------------------------

    private void setupHydrationButton() {

        btnAddWater.setOnClickListener(v -> {

            int newValue = waterGlasses + 1;

            String historyId =
                    userRef
                            .child("history")
                            .child("hydration")
                            .push()
                            .getKey();

            if (historyId == null) {
                return;
            }

            Map<String, Object> updates =
                    new HashMap<>();

            updates.put(
                    "hydration/currentGlasses",
                    newValue
            );

            updates.put(
                    "history/hydration/"
                            + historyId
                            + "/currentGlasses",
                    newValue
            );

            updates.put(
                    "history/hydration/"
                            + historyId
                            + "/timestamp",
                    ServerValue.TIMESTAMP
            );

            userRef
                    .updateChildren(updates)
                    .addOnSuccessListener(unused -> {

                        if (newValue == waterGoal) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Daily hydration goal reached ♡",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else if (newValue > waterGoal) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "You're above today's hydration goal!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .addOnFailureListener(e ->

                            Toast.makeText(
                                    MainActivity.this,
                                    "Could not update hydration.",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }


    // ---------------------------------------------------------
    // WEIGHT READ
    // ---------------------------------------------------------

    private void loadWeight() {

        weightRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        Double currentWeight =
                                snapshot
                                        .child("currentWeight")
                                        .getValue(Double.class);


                        if (currentWeight != null) {

                            tvWeightHome.setText(
                                    "Current: "
                                            + formatWeight(currentWeight)
                                            + " kg"
                            );

                        } else {

                            tvWeightHome.setText(
                                    "No weight entry saved yet"
                            );
                        }
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Could not load weight data.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // WORKOUT READ
    // ---------------------------------------------------------

    private void loadWorkout() {

        workoutRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        String workoutName =
                                snapshot
                                        .child("selectedWorkout")
                                        .getValue(String.class);


                        String workoutDetails =
                                snapshot
                                        .child("selectedDetails")
                                        .getValue(String.class);


                        Boolean completed =
                                snapshot
                                        .child("completed")
                                        .getValue(Boolean.class);


                        if (workoutName != null) {

                            String text =
                                    workoutName;


                            if (workoutDetails != null) {

                                text =
                                        text
                                                + " • "
                                                + workoutDetails;
                            }


                            if (Boolean.TRUE.equals(completed)) {

                                text =
                                        text + " ✓";
                            }


                            tvWorkoutHome.setText(text);

                        } else {

                            tvWorkoutHome.setText(
                                    "No workout saved yet"
                            );
                        }
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Could not load workout data.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // RECOVERY READ
    // ---------------------------------------------------------

    private void loadRecovery() {

        recoveryRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        Double sleepHours =
                                snapshot
                                        .child("sleepHours")
                                        .getValue(Double.class);


                        Long energy =
                                snapshot
                                        .child("energyLevel")
                                        .getValue(Long.class);


                        Long recoveryScore =
                                snapshot
                                        .child("recoveryScore")
                                        .getValue(Long.class);


                        // -----------------------------
                        // SLEEP
                        // -----------------------------

                        if (sleepHours != null) {

                            tvSleep.setText(
                                    formatSleep(sleepHours)
                                            + " hrs"
                            );

                        } else {

                            tvSleep.setText(
                                    "-- hrs"
                            );
                        }


                        // -----------------------------
                        // ENERGY
                        // -----------------------------

                        if (energy != null) {

                            tvEnergyHome.setText(
                                    energy.intValue()
                                            + " / 10"
                            );

                        } else {

                            tvEnergyHome.setText(
                                    "-- / 10"
                            );
                        }


                        // -----------------------------
                        // WELLNESS / RECOVERY
                        // -----------------------------

                        if (recoveryScore != null) {

                            int score =
                                    recoveryScore.intValue();


                            tvWellnessScore.setText(
                                    String.valueOf(score)
                            );


                            progressWellness.setProgress(
                                    score
                            );


                            tvRecoveryHome.setText(
                                    score
                                            + "/100 • "
                                            + getRecoveryStatus(score)
                            );

                        } else {

                            tvWellnessScore.setText("--");

                            progressWellness.setProgress(0);

                            tvRecoveryHome.setText(
                                    "Complete a recovery check-in"
                            );
                        }
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Could not load recovery data.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // CARD NAVIGATION
    // ---------------------------------------------------------

    private void setupCards() {


        // HYDRATION

        cardHydration.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HydrationActivity.class
                    );

            startActivity(intent);
        });


        // WORKOUT

        cardWorkout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            WorkoutActivity.class
                    );

            startActivity(intent);
        });


        // WEIGHT

        cardWeight.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            WeightActivity.class
                    );

            startActivity(intent);
        });


        // RECOVERY

        cardRecovery.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            RecoveryActivity.class
                    );

            startActivity(intent);
        });


        // PROFILE

        cardProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);
        });


        // HISTORY

        cardHistory.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HistoryActivity.class
                    );

            startActivity(intent);
        });
    }


    // ---------------------------------------------------------
    // LOAD LATEST AI INSIGHT
    // ---------------------------------------------------------

    private void loadAiInsight() {

        userRef
                .child("ai")
                .child("latestInsight")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    DataSnapshot snapshot
                            ) {

                                String savedInsight =
                                        snapshot.getValue(String.class);

                                if (savedInsight != null
                                        && !savedInsight.trim().isEmpty()) {

                                    tvAiInsight.setText(
                                            savedInsight.trim()
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    DatabaseError error
                            ) {
                                // Keep the default UI text if no saved insight can be loaded.
                            }
                        }
                );
    }


    // ---------------------------------------------------------
    // AI BUTTON
    // ---------------------------------------------------------

    private void setupAiPreview() {

        btnAiInsight.setOnClickListener(v -> {

            generatePersonalizedAiInsight();

        });
    }


    // ---------------------------------------------------------
    // READ USER DATA FOR AI
    // ---------------------------------------------------------

    private void generatePersonalizedAiInsight() {

        if (userRef == null) {

            Toast.makeText(
                    MainActivity.this,
                    "User data is not available.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        btnAiInsight.setEnabled(false);


        tvAiInsight.setText(
                "Analyzing your wellness data..."
        );


        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {


                        // -------------------------------------
                        // PROFILE
                        // -------------------------------------

                        String name =
                                snapshot
                                        .child("profile")
                                        .child("name")
                                        .getValue(String.class);


                        Long age =
                                snapshot
                                        .child("profile")
                                        .child("age")
                                        .getValue(Long.class);


                        Double height =
                                snapshot
                                        .child("profile")
                                        .child("height")
                                        .getValue(Double.class);


                        // --------------------------------------
                        // HYDRATION
                        // --------------------------------------

                        Long hydration =
                                snapshot
                                      .child("hydration")
                                        .child("currentGlasses")
                                        .getValue(Long.class);


                        // -------------------------------------
                        // WEIGHT
                        // -------------------------------------

                        Double weight =
                                snapshot
                                        .child("weight")
                                        .child("currentWeight")
                                        .getValue(Double.class);


                        // -------------------------------------
                        // WORKOUT
                        // -------------------------------------

                        String workout =
                                snapshot
                                        .child("workout")
                                        .child("selectedWorkout")
                                        .getValue(String.class);


                        String workoutDetails =
                                snapshot
                                        .child("workout")
                                        .child("selectedDetails")
                                        .getValue(String.class);


                        Boolean workoutCompleted =
                                snapshot
                                        .child("workout")
                                        .child("completed")
                                        .getValue(Boolean.class);


                        // -------------------------------------
                        // RECOVERY
                        // -------------------------------------

                        Double sleep =
                                snapshot
                                        .child("recovery")
                                        .child("sleepHours")
                                        .getValue(Double.class);


                        Long energy =
                                snapshot
                                        .child("recovery")
                                        .child("energyLevel")
                                        .getValue(Long.class);


                        Long recoveryScore =
                                snapshot
                                        .child("recovery")
                                        .child("recoveryScore")
                                        .getValue(Long.class);


                        // -------------------------------------
                        // BUILD GEMINI PROMPT
                        // -------------------------------------

                        String prompt =
                                "You are BloomCare, a supportive AI wellness coach.\n\n"

                                        + "The following information belongs to the logged-in user:\n"

                                        + "Name: "
                                        + safeText(name)
                                        + "\n"

                                        + "Age: "
                                        + (age == null
                                        ? "Not provided"
                                        : age)
                                        + "\n"

                                        + "Height: "
                                        + (height == null
                                        ? "Not provided"
                                        : String.format(
                                        Locale.US,
                                        "%.1f cm",
                                        height
                                ))
                                        + "\n"

                                        + "Current weight: "
                                        + (weight == null
                                        ? "Not provided"
                                        : String.format(
                                        Locale.US,
                                        "%.1f kg",
                                        weight
                                ))
                                        + "\n"

                                        + "Hydration today: "
                                        + (hydration == null
                                        ? "Not recorded"
                                        : hydration
                                          + " of 8 glasses")
                                        + "\n"

                                        + "Sleep last night: "
                                        + (sleep == null
                                        ? "Not recorded"
                                        : String.format(
                                        Locale.US,
                                        "%.1f hours",
                                        sleep
                                ))
                                        + "\n"

                                        + "Energy level: "
                                        + (energy == null
                                        ? "Not recorded"
                                        : energy + "/10")
                                        + "\n"

                                        + "Recovery score: "
                                        + (recoveryScore == null
                                        ? "Not recorded"
                                        : recoveryScore
                                          + "/100")
                                        + "\n"

                                        + "Latest workout: "
                                        + safeText(workout)
                                        + "\n"

                                        + "Workout details: "
                                        + safeText(workoutDetails)
                                        + "\n"

                                        + "Workout completed: "
                                        + (workoutCompleted == null
                                        ? "Not recorded"
                                        : workoutCompleted)
                                        + "\n\n"

                                        + "Generate a short personalized wellness insight "
                                        + "for today using only the information provided above. "

                                        + "Give exactly 3 practical suggestions. "

                                        + "Focus on hydration, rest, recovery, sleep, "
                                        + "and gentle physical activity where relevant. "

                                        + "Use the user's actual values when useful. "

                                        + "Keep the response supportive, concise, "
                                        + "and easy to understand. "

                                        + "Do not diagnose diseases. "

                                        + "Do not prescribe medication. "

                                        + "Do not claim that the recovery score "
                                        + "is a medical or clinical measurement. "

                                        + "Do not invent information that is missing. "

                                        + "If any value appears concerning, recommend "
                                        + "seeking appropriate professional medical advice "
                                        + "rather than diagnosing the user. "

                                        + "Keep the response around 100 words.";


                        generateGeminiInsight(prompt);
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        tvAiInsight.setText(
                                "Could not read your wellness data."
                        );


                        btnAiInsight.setEnabled(true);


                        Toast.makeText(
                                MainActivity.this,
                                "Firebase error: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    // ---------------------------------------------------------
    // GEMINI AI REQUEST
    // ---------------------------------------------------------

    private void generateGeminiInsight(
            String promptText
    ) {

        GenerativeModel ai =
                FirebaseAI
                        .getInstance(
                                GenerativeBackend.googleAI()
                        )
                        .generativeModel(
                                "gemini-3.8-flash"
                        );


        GenerativeModelFutures model =
                GenerativeModelFutures.from(ai);


        Content prompt =
                new Content.Builder()
                        .addText(promptText)
                        .build();


        ListenableFuture<GenerateContentResponse> response =
                model.generateContent(prompt);


        Futures.addCallback(
                response,

                new FutureCallback<GenerateContentResponse>() {

                    @Override
                    public void onSuccess(
                            GenerateContentResponse result
                    ) {

                        String resultText =
                                result.getText();


                        if (resultText == null
                                || resultText
                                .trim()
                                .isEmpty()) {

                            tvAiInsight.setText(
                                    "No personalized insight was generated. "
                                            + "Please try again."
                            );

                        } else {

                            String cleanInsight =
                                    resultText
                                            .trim()
                                            .replace("**", "");


                            tvAiInsight.setText(
                                    cleanInsight
                            );


                            String historyId =
                                    userRef
                                            .child("history")
                                            .child("ai")
                                            .push()
                                            .getKey();


                            Map<String, Object> updates =
                                    new HashMap<>();


                            updates.put(
                                    "ai/latestInsight",
                                    cleanInsight
                            );


                            updates.put(
                                    "ai/generatedAt",
                                    ServerValue.TIMESTAMP
                            );


                            if (historyId != null) {

                                updates.put(
                                        "history/ai/"
                                                + historyId
                                                + "/insight",
                                        cleanInsight
                                );


                                updates.put(
                                        "history/ai/"
                                                + historyId
                                                + "/timestamp",
                                        ServerValue.TIMESTAMP
                                );
                            }


                            userRef.updateChildren(updates);
                        }


                        btnAiInsight.setEnabled(true);
                    }


                    @Override
                    public void onFailure(
                            Throwable t
                    ) {

                        tvAiInsight.setText(
                                "Your AI wellness insight could not "
                                        + "be generated right now."
                        );


                        btnAiInsight.setEnabled(true);


                        String errorMessage =
                                t.getMessage();


                        if (errorMessage == null
                                || errorMessage.trim().isEmpty()) {

                            errorMessage =
                                    "Unknown AI error";
                        }


                        Toast.makeText(
                                MainActivity.this,
                                "AI error: "
                                        + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },

                ContextCompat.getMainExecutor(
                        MainActivity.this
                )
        );
    }


    // ---------------------------------------------------------
    // SAFE STRING
    // ---------------------------------------------------------

    private String safeText(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not provided";
        }

        return value.trim();
    }


    // ---------------------------------------------------------
    // RECOVERY STATUS
    // ---------------------------------------------------------

    private String getRecoveryStatus(
            int score
    ) {

        return RecoveryScoreCalculator.status(score);
    }


    // ---------------------------------------------------------
    // FORMAT WEIGHT
    // ---------------------------------------------------------

    private String formatWeight(
            double weight
    ) {

        return String.format(
                Locale.US,
                "%.1f",
                weight
        );
    }


    // ---------------------------------------------------------
    // FORMAT SLEEP
    // ---------------------------------------------------------

    private String formatSleep(
            double sleep
    ) {

        return String.format(
                Locale.US,
                "%.1f",
                sleep
        );
    }
}