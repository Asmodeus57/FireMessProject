package com.firebase.firemess;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.PriorityQueue;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button SaveChangeStatusButton;
    private EditText StatusInput;

    private DatabaseReference changeStatusRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);


        mToolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SaveChangeStatusButton = findViewById(R.id.save_status_button);
        StatusInput = findViewById(R.id.status_input);
        loadingBar = new ProgressDialog(this);


        String old_status = getIntent().getExtras().get("user_status").toString();
        StatusInput.setText(old_status);

        SaveChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String new_status = StatusInput.getText().toString();

                ChangeProfileStatus(new_status);
            }
        });
    }

    private void ChangeProfileStatus(String new_status) {

        if(TextUtils.isEmpty(new_status)){
            Toast.makeText(this, "Please write your status.",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Change profile status");
            loadingBar.setMessage("Please wait.");
            loadingBar.show();

            changeStatusRef.child("user_status").setValue(new_status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                loadingBar.dismiss();

                                Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);


                                Toast.makeText(StatusActivity.this, "Profile status updated successfully...",
                                        Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(StatusActivity.this, "Error...",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }


                    });
        }
    }
}
