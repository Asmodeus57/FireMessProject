package com.firebase.firemess;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button AddToFriends;
    private Button DeclineRequest;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private ImageView ProfileImage;

    private DatabaseReference UserRef;
    private DatabaseReference FriendsRequestRef;
    private DatabaseReference FriendsRef;
    private FirebaseAuth mAuth;
    private DatabaseReference NotificationRef;

    private String CURRENT_STATE;



    String sender_user_id;
    String receiver_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("Friends_Requests");
        FriendsRequestRef.keepSynced(true);

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);

        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        sender_user_id = mAuth.getCurrentUser().getUid();
        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();


        AddToFriends = findViewById(R.id.visit_addfriend_button);
        DeclineRequest = findViewById(R.id.visit_removefriend_button);
        ProfileName = findViewById(R.id.visit_profile_username);
        ProfileStatus = findViewById(R.id.visit_profile_userstatus);
        ProfileImage = findViewById(R.id.default_visit_profile_pic);

        CURRENT_STATE = "not_friends";


        UserRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("user_name").getValue().toString();
                final String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile).into(ProfileImage);

                FriendsRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(receiver_user_id)){

                                        String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                        if(req_type.equals("sent")){

                                            CURRENT_STATE = "request_sent";
                                            AddToFriends.setText("Cancel Friend Request");

                                            DeclineRequest.setVisibility(View.INVISIBLE);
                                            DeclineRequest.setEnabled(false);

                                        }
                                        else if(req_type.equals("received"))
                                        {

                                            CURRENT_STATE = "request_received";
                                            AddToFriends.setText("Accept request");

                                            DeclineRequest.setVisibility(View.VISIBLE);
                                            DeclineRequest.setEnabled(true);

                                            DeclineRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    DeclineFriendRequest();
                                                }
                                            });
                                        }
                                    }

                                else {
                                        FriendsRef.child(sender_user_id)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        if (dataSnapshot.hasChild(receiver_user_id)) {
                                                            CURRENT_STATE = "friends";
                                                            AddToFriends.setText("Delete from friends");

                                                            DeclineRequest.setVisibility(View.INVISIBLE);
                                                            DeclineRequest.setEnabled(false);
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }

                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DeclineRequest.setVisibility(View.INVISIBLE);
        DeclineRequest.setEnabled(false);

       if(!sender_user_id.equals(receiver_user_id)){

           AddToFriends.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   AddToFriends.setEnabled(false);


                   if(CURRENT_STATE.equals("not_friends")){

                       AddNotFriendToFriends();
                   }

                   if(CURRENT_STATE.equals("request_sent"))
                   {

                       CancelFriendRequest();

                   }
                   if (CURRENT_STATE.equals("request_received"))
                   {
                       AcceptFriendRequest();
                   }
                   if(CURRENT_STATE.equals("friends")){

                       DeleteFromFriends();
                   }
               }
           });
       }
       else
       {
           DeclineRequest.setVisibility(View.INVISIBLE);
           AddToFriends.setVisibility(View.INVISIBLE);
       }



    }

    private void DeclineFriendRequest() {

            FriendsRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                FriendsRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    AddToFriends.setEnabled(true);
                                                    CURRENT_STATE = "not_friends";
                                                    AddToFriends.setText("Send Friend Request");

                                                    DeclineRequest.setVisibility(View.INVISIBLE);
                                                    DeclineRequest.setEnabled(false);
                                                }

                                            }
                                        });
                            }

                        }
                    });
        }

    private void DeleteFromFriends() {

        FriendsRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FriendsRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            AddToFriends.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            AddToFriends.setText("Send Friend Request");

                                            DeclineRequest.setVisibility(View.INVISIBLE);
                                            DeclineRequest.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void AcceptFriendRequest() {

        Calendar calFordATE = Calendar.getInstance();
        SimpleDateFormat currentDate = (SimpleDateFormat) DateFormat.getDateInstance();
        final String saveCurrentDate = currentDate.format(calFordATE.getTime());


        FriendsRef.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FriendsRef.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        FriendsRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){
                                                            FriendsRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                AddToFriends.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                AddToFriends.setText("Delete from friends");

                                                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                                                AddToFriends.setVisibility(View.INVISIBLE);
                                                                            }

                                                                        }
                                                                    });
                                                        }

                                                    }
                                                });


                                    }
                                });

                    }
                });
    }

    private void CancelFriendRequest() {

        FriendsRequestRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            FriendsRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                AddToFriends.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                AddToFriends.setText("Send Friend Request");

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void AddNotFriendToFriends() {

        FriendsRequestRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful())
                        {

                            FriendsRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                notificationsData.put("from", sender_user_id);
                                                notificationsData.put("type", "request");

                                                NotificationRef.child(receiver_user_id).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){

                                                                    AddToFriends.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    AddToFriends.setText("Cancel Friend Request");

                                                                    DeclineRequest.setVisibility(View.INVISIBLE);
                                                                    DeclineRequest.setEnabled(false);
                                                                }

                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }


                    }
                });



/*
        FriendsRequestRef = FirebaseDatabase.getInstance().getReference().child("Friends_Requests");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("visit_user_id");

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();


        AddToFriends = findViewById(R.id.visit_addfriend_button);
        RemoveFromFriends = findViewById(R.id.visit_removefriend_button);
        ProfileName = findViewById(R.id.visit_profile_username);
        ProfileStatus = findViewById(R.id.visit_profile_userstatus);
        ProfileImage = findViewById(R.id.default_visit_profile_pic);


        CURRENT_STATE = "not_friends";


        UserRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 name = dataSnapshot.child("user_name").getValue().toString();
                 status = dataSnapshot.child("user_status").getValue().toString();
                 image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_profile).into(ProfileImage);

                FriendsRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(dataSnapshot.hasChild(receiver_user_id)){

                                    String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                    if(req_type.equals("sent")){

                                        CURRENT_STATE = "request_sent";
                                        AddToFriends.setText("Cancel Friend Request");

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        AddToFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddToFriends.setEnabled(false);


                if(CURRENT_STATE.equals("not_friends")){

                    AddToNotFriends();
                }
            }
        });



    }

    private void AddToNotFriends() {

        FriendsRequestRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful())
                        {

                            FriendsRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("receiver")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                AddToFriends.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                AddToFriends.setText("Cancel Friend Request");

                                            }

                                        }
                                    });
                        }


                    }
                });

                */
    }


}
