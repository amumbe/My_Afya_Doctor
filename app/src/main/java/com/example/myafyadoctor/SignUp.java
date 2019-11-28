package com.example.myafyadoctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        final RelativeLayout contextView = findViewById(R.id.root);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText phone = findViewById(R.id.phone);
        Button register = findViewById(R.id.register);
        Button login = findViewById(R.id.login);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();
                String phoneText = phone.getText().toString();

                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText) || TextUtils.isEmpty(phoneText)) {
                    Snackbar.make(contextView, R.string.fields_required, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (passwordText.length() < 8) {
                    Snackbar.make(contextView, R.string.minimum_password_message, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                auth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser firebaseUser = auth.getCurrentUser();
                            SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.default_prefs), 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("is_authenticated", true);
                            editor.commit();
                            Map<String, Object> patient = new HashMap<>();
                            assert firebaseUser != null;
                            patient.put("uid", firebaseUser.getUid());
                            patient.put("name", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
                            patient.put("phone", phone.getText().toString());
                            patient.put("image", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl() : "");
                            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore
                                    .collection("patients")
                                    .document(firebaseUser.getUid())
                                    .set(patient)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("AFYA", "DocumentSnapshot successfully written!");
                                            FirebaseInstanceId
                                                    .getInstance()
                                                    .getInstanceId()
                                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                            if (task.isSuccessful()) {
                                                                String token = task.getResult().getToken();
                                                                Map<String, Object> doc = new HashMap<>();
                                                                doc.put("token", token);
                                                                firestore.collection("patients").document(firebaseUser.getUid()).set(doc, SetOptions.merge());
                                                            }
                                                        }
                                                    });
                                            startActivity(new Intent(SignUp.this, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("AFYA ", e.getMessage());
                                            Snackbar.make(contextView, "Error adding you to the Patients' Database", Snackbar.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                        } else {
                            Snackbar.make(contextView, R.string.auth_failed, Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, Login.class));
            }
        });
    }
}
