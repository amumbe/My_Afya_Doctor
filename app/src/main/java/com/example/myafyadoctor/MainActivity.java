package com.example.myafyadoctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import net.steamcrafted.materialiconlib.MaterialIconView;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ConstraintLayout root = findViewById(R.id.root);
        final TextView weight = findViewById(R.id.weight);
        final TextView sleep = findViewById(R.id.sleep);
        final TextView workout = findViewById(R.id.workout);
        final TextView steps = findViewById(R.id.steps);
        final TextView rate = findViewById(R.id.rate);
        final TextView user = findViewById(R.id.user);
        user.setText(currentUser.getUid());
        final MaterialIconView logout = findViewById(R.id.logout);
        Button update = findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DailyData.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });

        final String user_id = currentUser.getUid();

        if (!user_id.isEmpty()) {
            DocumentReference docRef = db.collection("patients").document(user_id);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("myOnChange", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("myOnChange", "Current data: " + snapshot.getData());
                        rate.setText(snapshot.getString("heart_rate") + " BPM");
                    } else {
                        Log.d("myOnChange", "Current data: null");
                    }
                }
            });

            final CollectionReference colRef = db.collection("patients").document(user_id).collection("data");
            colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("myOnChange", "listen:error", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        db.collection("patients")
                                .document(user_id)
                                .collection("data")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                weight.setText(document.get("weight").toString() + " KGs");
                                                sleep.setText(document.get("sleep").toString() + " Hrs");
                                                workout.setText(document.get("workout").toString() + " Hrs");
                                                steps.setText(document.get("steps").toString() + " KMs");
                                            }
                                        } else {
                                            Snackbar.make(root, R.string.fetch_error, Snackbar.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    }

                }
            });
        }
    }
}
