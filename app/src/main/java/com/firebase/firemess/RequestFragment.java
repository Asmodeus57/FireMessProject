package com.firebase.firemess;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView mRequestsList;
    private LinearLayoutManager manager;

    private View mMainView;


    private DatabaseReference FriendsRequestsRef;
    private DatabaseReference UserRef;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter<Requests, RequestViewHolder> mRequestAdapter;

    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsReqDatabaseRef;

    String online_user_id;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestsList = mMainView.findViewById(R.id.request_list);

        manager = new LinearLayoutManager(getContext());

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();


        FriendsRequestsRef = FirebaseDatabase.getInstance().getReference().child("Friends_Requests").child(online_user_id);
        FriendsRequestsRef.keepSynced(true);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        FriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends_Requests");


        mRequestsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRequestsList.setLayoutManager(linearLayoutManager);

        Query FriendsRequestsQuery = FriendsRequestsRef.orderByKey();

        FirebaseRecyclerOptions<Requests> FriendsRequestsOptions =
                new FirebaseRecyclerOptions.Builder<Requests>().setQuery(FriendsRequestsQuery, Requests.class).build();

        mRequestAdapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(FriendsRequestsOptions) {

            @Override
            protected void onBindViewHolder(final RequestViewHolder viewHolder, int position, Requests model) {

                final String list_users_id = getRef(position).getKey();


                DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String request_type = dataSnapshot.getValue().toString();

                            if(request_type.equals("received")){

                                final Button req_acc_btn = viewHolder.mView.findViewById(R.id.request_accept_button);
                                final Button req_dec_btn = viewHolder.mView.findViewById(R.id.request_decline_button);

                                UserRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        String userStatus = dataSnapshot.child("user_status").getValue().toString();


                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserStatus(userStatus);
                                        viewHolder.setThumb_user_image(thumbImage, getContext());

                                        req_acc_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                Calendar calFordATE = Calendar.getInstance();
                                                SimpleDateFormat currentDate = (SimpleDateFormat) DateFormat.getDateInstance();
                                                final String saveCurrentDate = currentDate.format(calFordATE.getTime());


                                                FriendsDatabaseRef.child(online_user_id).child(list_users_id).child("date").setValue(saveCurrentDate)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                FriendsDatabaseRef.child(list_users_id).child(online_user_id).child("date").setValue(saveCurrentDate)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                FriendsReqDatabaseRef.child(online_user_id).child(list_users_id).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()) {
                                                                                                    FriendsReqDatabaseRef.child(list_users_id).child(online_user_id).removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        Toast.makeText(getContext(), "Friend request was accepted",
                                                                                                                                Toast.LENGTH_SHORT).show();
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
                                        });

                                        req_dec_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                {

                                                    FriendsReqDatabaseRef.child(online_user_id).child(list_users_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        FriendsReqDatabaseRef.child(list_users_id).child(online_user_id).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()) {
                                                                                            Toast.makeText(getContext(), "Friend request was declined",
                                                                                                    Toast.LENGTH_SHORT).show();
                                                                                        }

                                                                                    }
                                                                                });
                                                                    }

                                                                }
                                                            });
                                                }
                                            }
                                        });





                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else if (request_type.equals("sent")){

                                final Button req_sent_btn = viewHolder.mView.findViewById(R.id.request_accept_button);
                                req_sent_btn.setText("Request sent");

                                viewHolder.mView.findViewById(R.id.request_decline_button).setVisibility(View.INVISIBLE);

                                UserRef.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        String userStatus = dataSnapshot.child("user_status").getValue().toString();


                                        viewHolder.setUserName(userName);
                                        viewHolder.setUserStatus(userStatus);
                                        viewHolder.setThumb_user_image(thumbImage, getContext());

                                        req_sent_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                FriendsReqDatabaseRef.child(online_user_id).child(list_users_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    FriendsReqDatabaseRef.child(list_users_id).child(online_user_id).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getContext(), "Friend request was canceled",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }


                                        });
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friends_request_all_user_layout, parent, false);

                return new RequestViewHolder (view);
            }


        };

        mRequestsList.setAdapter(mRequestAdapter);
        mRequestsList.setLayoutManager(manager);

        // Inflate the layout for this fragment
        return mMainView;
    }







    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setUserName(String userName) {

            TextView userNameDisplay = mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(userName);


        }

        public void setUserStatus(String userStatus) {
            TextView userStatusDisplay = mView.findViewById(R.id.request_profile_status);
            userStatusDisplay.setText(userStatus);
        }

        public void setThumb_user_image(final String thumbImage, final Context ctx) {
            final CircleImageView thumb_image = mView.findViewById(R.id.request_profile_image);

            // Picasso.with(ctx).load(setUser_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
            Picasso.with(ctx).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.default_profile).into(thumb_image);
                        }
                    });

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRequestAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        mRequestAdapter.stopListening();
    }

}
