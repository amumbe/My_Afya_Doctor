package com.example.myafyadoctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class Login extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.default_prefs), 0);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAuthed = pref.getBoolean("is_authenticated", false);


        if(isAuthed && currentUser != null){
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);


        final FirebaseAuth auth = FirebaseAuth.getInstance();

        final RelativeLayout contextView = findViewById(R.id.root);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        Button signup = findViewById(R.id.btnSignup);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
                    Snackbar.make(contextView, R.string.fields_required, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                auth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser firebaseUser = auth.getCurrentUser();
                            SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.default_prefs), 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("is_authenticated", true);
                            editor.commit();
                            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
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
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        } else {
                            Snackbar.make(contextView, R.string.auth_failed, Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, SignUp.class));
            }
        });
    }
}