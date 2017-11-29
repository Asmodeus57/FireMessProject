package com.firebase.firemess;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private EditText RegisterUserName;
    private EditText RegisterUserEmail;
    private EditText RegisterUserPass;
    private Button CreateAccountButton;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.registration_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sing Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RegisterUserName = findViewById(R.id.register_name);
        RegisterUserEmail = findViewById(R.id.register_email);
        RegisterUserPass = findViewById(R.id.register_pass);
        CreateAccountButton = findViewById(R.id.create_account_button);
        loadingBar = new ProgressDialog(this);


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = RegisterUserName.getText().toString();
                String email = RegisterUserEmail.getText().toString();
                String password = RegisterUserPass.getText().toString();

                RegisterAccount(name,email,password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {

        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegistrationActivity.this, "Name.",
                            Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegistrationActivity.this, "Email.",
                    Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegistrationActivity.this, "Password.",
                    Toast.LENGTH_LONG).show();
        }

        else
        {

            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                                    String current_user_Id = mAuth.getCurrentUser().getUid();
                                    storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_Id);

                                    storeUserDefaultDataReference.child("user_name").setValue(name);
                                    storeUserDefaultDataReference.child("user_status").setValue("Status...");
                                    storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                                    storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                                    storeUserDefaultDataReference.child("user_thumb_image").setValue("default_pic")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){

                                                        Intent mainIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        Log.d(TAG, "createUserWithEmail:success");
                                                        FirebaseUser user = mAuth.getCurrentUser();

                                                        startActivity(mainIntent);

                                                        finish();

                                                    }
                                                }
                                            });
                                    /*Intent mainIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    startActivity(mainIntent);

                                    finish();*/
                                }
                                else
                                {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegistrationActivity.this, "Error. Try again.",
                                            Toast.LENGTH_LONG).show();
                                }

                                loadingBar.dismiss();
                            }
                        });
        }
    }



}
