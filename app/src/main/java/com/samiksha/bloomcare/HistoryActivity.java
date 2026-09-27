package com.samiksha.bloomcare;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private TextView btnBack;
    private TextView tvLatestRecovery;
    private TextView tvLatestHydration;
    private TextView tvLatestWeight;
    private TextView tvLatestWorkout;

    private LinearLayout layoutRecoveryHistory;
    private LinearLayout layoutWeightHistory;
    private LinearLayout layoutWorkoutHistory;
    private LinearLayout layoutHydrationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        connectViews();

        if (!setupFirebase()) {
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        loadLatestSummary();
        loadRecoveryHistory();
        loadWeightHistory();
        loadWorkoutHistory();
        loadHydrationHistory();
    }

    private void connectViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLatestRecovery = findViewById(R.id.tvLatestRecovery);
        tvLatestHydration = findViewById(R.id.tvLatestHydration);
        tvLatestWeight = findViewById(R.id.tvLatestWeight);
        tvLatestWorkout = findViewById(R.id.tvLatestWorkout);

        layoutRecoveryHistory = findViewById(R.id.layoutRecoveryHistory);
        layoutWeightHistory = findViewById(R.id.layoutWeightHistory);
        layoutWorkoutHistory = findViewById(R.id.layoutWorkoutHistory);
        layoutHydrationHistory = findViewById(R.id.layoutHydrationHistory);
    }

    private boolean setupFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        userRef = FirebaseDatabase
                .getInstance(getString(R.string.firebase_database_url))
                .getReference("users")
                .child(user.getUid());

        return true;
    }

    private void loadLatestSummary() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long glasses = snapshot.child("hydration/currentGlasses").getValue(Long.class);
                Double weight = snapshot.child("weight/currentWeight").getValue(Double.class);
                String workout = snapshot.child("workout/selectedWorkout").getValue(String.class);
                Long recovery = snapshot.child("recovery/recoveryScore").getValue(Long.class);

                tvLatestHydration.setText(
                        glasses == null ? "No hydration entry yet" : glasses + " / 8 glasses"
                );

                tvLatestWeight.setText(
                        weight == null
                                ? "No weight entry yet"
                                : String.format(Locale.US, "%.1f kg", weight)
                );

                tvLatestWorkout.setText(
                        workout == null ? "No workout entry yet" : workout
                );

                tvLatestRecovery.setText(
                        recovery == null
                                ? "No recovery check-in yet"
                                : recovery + "/100 • " + RecoveryScoreCalculator.status(recovery.intValue())
                );
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        HistoryActivity.this,
                        "Could not load latest summary.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadRecoveryHistory() {
        userRef.child("history/recovery")
                .orderByChild("timestamp")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutRecoveryHistory.removeAllViews();
                        List<DataSnapshot> rows = snapshotToList(snapshot);

                        if (rows.isEmpty()) {
                            addEmptyMessage(layoutRecoveryHistory);
                            return;
                        }

                        for (int i = rows.size() - 1; i >= 0; i--) {
                            DataSnapshot item = rows.get(i);

                            Double sleep = item.child("sleepHours").getValue(Double.class);
                            Long energy = item.child("energyLevel").getValue(Long.class);
                            Long score = item.child("recoveryScore").getValue(Long.class);
                            Long timestamp = item.child("timestamp").getValue(Long.class);

                            String title = score == null
                                    ? "Recovery check-in"
                                    : "Recovery " + score + "/100";

                            String detail =
                                    "Sleep: " + (sleep == null ? "--" : String.format(Locale.US, "%.1f hrs", sleep))
                                            + " • Energy: " + (energy == null ? "--" : energy + "/10")
                                            + "\n" + formatTimestamp(timestamp);

                            addHistoryCard(layoutRecoveryHistory, title, detail);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        addEmptyMessage(layoutRecoveryHistory);
                    }
                });
    }

    private void loadWeightHistory() {
        userRef.child("history/weight")
                .orderByChild("timestamp")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutWeightHistory.removeAllViews();
                        List<DataSnapshot> rows = snapshotToList(snapshot);

                        if (rows.isEmpty()) {
                            addEmptyMessage(layoutWeightHistory);
                            return;
                        }

                        for (int i = rows.size() - 1; i >= 0; i--) {
                            DataSnapshot item = rows.get(i);

                            Double current = item.child("currentWeight").getValue(Double.class);
                            Double previous = item.child("previousWeight").getValue(Double.class);
                            Long timestamp = item.child("timestamp").getValue(Long.class);

                            String title = current == null
                                    ? "Weight entry"
                                    : String.format(Locale.US, "%.1f kg", current);

                            String detail =
                                    "Previous: "
                                            + (previous == null ? "--" : String.format(Locale.US, "%.1f kg", previous))
                                            + "\n" + formatTimestamp(timestamp);

                            addHistoryCard(layoutWeightHistory, title, detail);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        addEmptyMessage(layoutWeightHistory);
                    }
                });
    }

    private void loadWorkoutHistory() {
        userRef.child("history/workout")
                .orderByChild("timestamp")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutWorkoutHistory.removeAllViews();
                        List<DataSnapshot> rows = snapshotToList(snapshot);

                        if (rows.isEmpty()) {
                            addEmptyMessage(layoutWorkoutHistory);
                            return;
                        }

                        for (int i = rows.size() - 1; i >= 0; i--) {
                            DataSnapshot item = rows.get(i);

                            String workout = item.child("selectedWorkout").getValue(String.class);
                            String details = item.child("selectedDetails").getValue(String.class);
                            Boolean completed = item.child("completed").getValue(Boolean.class);
                            Long timestamp = item.child("timestamp").getValue(Long.class);

                            String title = workout == null ? "Workout" : workout;
                            if (Boolean.TRUE.equals(completed)) {
                                title += " ✓";
                            }

                            String detail =
                                    (details == null ? "" : details + "\n")
                                            + formatTimestamp(timestamp);

                            addHistoryCard(layoutWorkoutHistory, title, detail);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        addEmptyMessage(layoutWorkoutHistory);
                    }
                });
    }

    private void loadHydrationHistory() {
        userRef.child("history/hydration")
                .orderByChild("timestamp")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutHydrationHistory.removeAllViews();
                        List<DataSnapshot> rows = snapshotToList(snapshot);

                        if (rows.isEmpty()) {
                            addEmptyMessage(layoutHydrationHistory);
                            return;
                        }

                        for (int i = rows.size() - 1; i >= 0; i--) {
                            DataSnapshot item = rows.get(i);

                            Long glasses = item.child("currentGlasses").getValue(Long.class);
                            Long timestamp = item.child("timestamp").getValue(Long.class);

                            String title = glasses == null
                                    ? "Hydration entry"
                                    : glasses + " / 8 glasses";

                            addHistoryCard(
                                    layoutHydrationHistory,
                                    title,
                                    formatTimestamp(timestamp)
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        addEmptyMessage(layoutHydrationHistory);
                    }
                });
    }

    private List<DataSnapshot> snapshotToList(DataSnapshot snapshot) {
        List<DataSnapshot> result = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            result.add(child);
        }
        return result;
    }

    private void addHistoryCard(
            LinearLayout parent,
            String title,
            String detail
    ) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(1));
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.rose_light));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        titleView.setTextSize(15);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);

        TextView detailView = new TextView(this);
        detailView.setText(detail);
        detailView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        detailView.setTextSize(13);
        detailView.setPadding(0, dpToPx(5), 0, 0);

        content.addView(titleView);
        content.addView(detailView);
        card.addView(content);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, dpToPx(10));
        parent.addView(card, params);
    }

    private void addEmptyMessage(LinearLayout parent) {
        TextView text = new TextView(this);
        text.setText("No entries yet");
        text.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        text.setTextSize(13);
        text.setPadding(0, dpToPx(6), 0, dpToPx(12));
        parent.addView(text);
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) {
            return "Time unavailable";
        }

        return new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date(timestamp));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
