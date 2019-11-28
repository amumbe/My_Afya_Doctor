package com.example.myafyadoctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DailyData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_data);

        final ConstraintLayout root = findViewById(R.id.root);
        final EditText weight = findViewById(R.id.weight);
        final EditText sleep = findViewById(R.id.sleep);
        final EditText workout = findViewById(R.id.workout);
        final EditText steps = findViewById(R.id.steps);
        final EditText medicine = findViewById(R.id.medicine);
        Button submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(weight.getText().toString()) || TextUtils.isEmpty(sleep.getText().toString())) {
                    Snackbar.make(root, R.string.fields_required, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();

                Map<String, Object> doc = new HashMap<>();
                doc.put("weight", weight.getText().toString());
                doc.put("sleep", sleep.getText().toString());
                doc.put("workout", workout.getText().toString());
                doc.put("steps", steps.getText().toString());
                doc.put("medicine", medicine.getText().toString());
                doc.put("timestamp", ts);
                db.collection("patients").document(currentUser.getUid()).collection("data").document(ts).set(doc, SetOptions.merge());
                Snackbar.make(root, "Done!", Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
