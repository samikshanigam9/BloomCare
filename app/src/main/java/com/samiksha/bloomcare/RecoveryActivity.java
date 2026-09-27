package com.samiksha.bloomcare;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RecoveryActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private DatabaseReference recoveryRef;
    private DatabaseReference recoveryHistoryRef;

    private TextView btnBack;
    private TextView tvRecoveryScore;
    private TextView tvRecoveryStatus;
    private TextView tvEnergyValue;

    private TextInputEditText etSleep;

    private SeekBar seekEnergy;

    private RadioGroup groupMood;
    private RadioGroup groupPain;

    private MaterialButton btnSaveRecovery;

    private int energyLevel = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recovery);

        connectViews();

        if (!setupFirebase()) {
            return;
        }

        setupEnergySlider();
        setupSaveButton();
        setupBackButton();

        loadRecoveryFromFirebase();
    }

    private void connectViews() {

        btnBack = findViewById(R.id.btnBack);

        tvRecoveryScore = findViewById(R.id.tvRecoveryScore);
        tvRecoveryStatus = findViewById(R.id.tvRecoveryStatus);
        tvEnergyValue = findViewById(R.id.tvEnergyValue);

        etSleep = findViewById(R.id.etSleep);

        seekEnergy = findViewById(R.id.seekEnergy);

        groupMood = findViewById(R.id.groupMood);
        groupPain = findViewById(R.id.groupPain);

        btnSaveRecovery = findViewById(R.id.btnSaveRecovery);
    }

    // ---------------------------------------------------------
    // FIREBASE
    // ---------------------------------------------------------

    private boolean setupFirebase() {

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    RecoveryActivity.this,
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

        recoveryRef =
                userRef.child("recovery");

        recoveryHistoryRef =
                userRef
                        .child("history")
                        .child("recovery");

        return true;
    }

    // ---------------------------------------------------------
    // ENERGY
    // ---------------------------------------------------------

    private void setupEnergySlider() {

        seekEnergy.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        energyLevel = progress;

                        tvEnergyValue.setText(
                                energyLevel + " / 10"
                        );
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // SAVE BUTTON
    // ---------------------------------------------------------

    private void setupSaveButton() {

        btnSaveRecovery.setOnClickListener(v -> {

            String sleepInput =
                    etSleep.getText() == null
                            ? ""
                            : etSleep.getText().toString().trim();

            if (TextUtils.isEmpty(sleepInput)) {

                etSleep.setError("Enter sleep hours");
                etSleep.requestFocus();

                return;
            }

            double sleepHours;

            try {

                sleepHours =
                        Double.parseDouble(sleepInput);

            } catch (NumberFormatException e) {

                Toast.makeText(
                        RecoveryActivity.this,
                        "Please enter valid sleep hours.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (sleepHours < 0 || sleepHours > 24) {

                etSleep.setError(
                        "Sleep must be between 0 and 24 hours"
                );

                return;
            }

            int moodScore = getMoodScore();
            int painScore = getPainScore();

            int recoveryScore =
                    RecoveryScoreCalculator.calculate(
                            sleepHours,
                            energyLevel,
                            moodScore,
                            painScore
                    );

            updateRecoveryUI(recoveryScore);

            saveRecovery(
                    sleepHours,
                    energyLevel,
                    moodScore,
                    painScore,
                    recoveryScore
            );
        });
    }

    // ---------------------------------------------------------
    // MOOD
    // ---------------------------------------------------------

    private int getMoodScore() {

        int selected =
                groupMood.getCheckedRadioButtonId();

        if (selected == R.id.radioMoodGood) {
            return 10;
        }

        if (selected == R.id.radioMoodOkay) {
            return 7;
        }

        return 4;
    }

    // ---------------------------------------------------------
    // PAIN
    // ---------------------------------------------------------

    private int getPainScore() {

        int selected =
                groupPain.getCheckedRadioButtonId();

        if (selected == R.id.radioPainNone) {
            return 10;
        }

        if (selected == R.id.radioPainMild) {
            return 7;
        }

        return 4;
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    private void updateRecoveryUI(int score) {

        tvRecoveryScore.setText(
                String.valueOf(score)
        );

        tvRecoveryStatus.setText(
                RecoveryScoreCalculator.status(score)
        );
    }

    // ---------------------------------------------------------
    // SAVE CURRENT + HISTORY
    // ---------------------------------------------------------

    private void saveRecovery(
            double sleepHours,
            int energy,
            int mood,
            int pain,
            int recoveryScore
    ) {

        Map<String, Object> currentData =
                new HashMap<>();

        currentData.put(
                "sleepHours",
                sleepHours
        );

        currentData.put(
                "energyLevel",
                energy
        );

        currentData.put(
                "moodScore",
                mood
        );

        currentData.put(
                "painScore",
                pain
        );

        currentData.put(
                "recoveryScore",
                recoveryScore
        );


        Map<String, Object> historyData =
                new HashMap<>();

        historyData.put(
                "sleepHours",
                sleepHours
        );

        historyData.put(
                "energyLevel",
                energy
        );

        historyData.put(
                "moodScore",
                mood
        );

        historyData.put(
                "painScore",
                pain
        );

        historyData.put(
                "recoveryScore",
                recoveryScore
        );

        historyData.put(
                "timestamp",
                ServerValue.TIMESTAMP
        );


        String historyId =
                recoveryHistoryRef
                        .push()
                        .getKey();


        if (historyId == null) {

            Toast.makeText(
                    RecoveryActivity.this,
                    "Could not create recovery history.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Map<String, Object> updates =
                new HashMap<>();


        // Latest recovery values
        updates.put(
                "recovery/sleepHours",
                sleepHours
        );

        updates.put(
                "recovery/energyLevel",
                energy
        );

        updates.put(
                "recovery/moodScore",
                mood
        );

        updates.put(
                "recovery/painScore",
                pain
        );

        updates.put(
                "recovery/recoveryScore",
                recoveryScore
        );


        // Historical recovery entry
        updates.put(
                "history/recovery/"
                        + historyId
                        + "/sleepHours",
                sleepHours
        );

        updates.put(
                "history/recovery/"
                        + historyId
                        + "/energyLevel",
                energy
        );

        updates.put(
                "history/recovery/"
                        + historyId
                        + "/moodScore",
                mood
        );

        updates.put(
                "history/recovery/"
                        + historyId
                        + "/painScore",
                pain
        );

        updates.put(
                "history/recovery/"
                        + historyId
                        + "/recoveryScore",
                recoveryScore
        );

        updates.put(
                "history/recovery/"
                        + historyId
                        + "/timestamp",
                ServerValue.TIMESTAMP
        );


        userRef
                .updateChildren(updates)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            RecoveryActivity.this,
                            "Recovery check-in saved ♡",
                            Toast.LENGTH_SHORT
                    ).show();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            RecoveryActivity.this,
                            "Could not save recovery data.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // LOAD LATEST RECOVERY
    // ---------------------------------------------------------

    private void loadRecoveryFromFirebase() {

        recoveryRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        Double savedSleep =
                                snapshot
                                        .child("sleepHours")
                                        .getValue(Double.class);

                        Long savedEnergy =
                                snapshot
                                        .child("energyLevel")
                                        .getValue(Long.class);

                        Long savedMood =
                                snapshot
                                        .child("moodScore")
                                        .getValue(Long.class);

                        Long savedPain =
                                snapshot
                                        .child("painScore")
                                        .getValue(Long.class);

                        Long savedRecoveryScore =
                                snapshot
                                        .child("recoveryScore")
                                        .getValue(Long.class);


                        if (savedSleep != null) {

                            etSleep.setText(
                                    String.valueOf(savedSleep)
                            );
                        }


                        if (savedEnergy != null) {

                            energyLevel =
                                    savedEnergy.intValue();

                            seekEnergy.setProgress(
                                    energyLevel
                            );

                            tvEnergyValue.setText(
                                    energyLevel + " / 10"
                            );
                        }


                        if (savedMood != null) {

                            int mood =
                                    savedMood.intValue();

                            if (mood == 10) {

                                groupMood.check(
                                        R.id.radioMoodGood
                                );

                            } else if (mood == 7) {

                                groupMood.check(
                                        R.id.radioMoodOkay
                                );

                            } else {

                                groupMood.check(
                                        R.id.radioMoodLow
                                );
                            }
                        }


                        if (savedPain != null) {

                            int pain =
                                    savedPain.intValue();

                            if (pain == 10) {

                                groupPain.check(
                                        R.id.radioPainNone
                                );

                            } else if (pain == 7) {

                                groupPain.check(
                                        R.id.radioPainMild
                                );

                            } else {

                                groupPain.check(
                                        R.id.radioPainModerate
                                );
                            }
                        }


                        if (savedRecoveryScore != null) {

                            updateRecoveryUI(
                                    savedRecoveryScore.intValue()
                            );
                        }
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        Toast.makeText(
                                RecoveryActivity.this,
                                "Could not load recovery data.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
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