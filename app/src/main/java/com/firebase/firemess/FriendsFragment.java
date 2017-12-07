package com.firebase.firemess;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private LinearLayoutManager manager;
    private Context c;
    private RecyclerView myFriendsList;

    private DatabaseReference FriendsRef;
    private DatabaseReference UserRef;
    private FirebaseAuth mAuth;

    String online_user_id;

    private View myMainView;

    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> mFriendsAdapter;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);


        c = getContext();
        manager = new LinearLayoutManager(c);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsRef.keepSynced(true);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        myFriendsList = myMainView.findViewById(R.id.friends_list);
        //myFriendsList.setHasFixedSize(true);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));


        Query FriendsQuery = FriendsRef.orderByKey();

        FirebaseRecyclerOptions<Friends> FriendsOptions =
                new FirebaseRecyclerOptions.Builder<Friends>().setQuery(FriendsQuery, Friends.class).build();
        mFriendsAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(FriendsOptions) {
            @Override
            protected void onBindViewHolder(final FriendsViewHolder viewHolder, int position, Friends model) {

                viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){

                            String online_status = (String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(thumbImage, c);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{

                                    userName + "`s Profile",
                                        "Send message"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                builder.setTitle("Select options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {

                                        if(position == 0)
                                        {
                                            Intent profileIntent = new Intent(c, ProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(position == 1)
                                        {
                                           if (dataSnapshot.child("online").exists())
                                           {
                                               Intent chatIntent = new Intent(c, ChatActivity.class);
                                               chatIntent.putExtra("visit_user_id", list_user_id);
                                               chatIntent.putExtra("user_name", userName);
                                               startActivity(chatIntent);
                                           }
                                           else
                                           {
                                               UserRef.child(list_user_id).child("online")
                                                       .setValue(ServerValue.TIMESTAMP)
                                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void aVoid) {

                                                               Intent chatIntent = new Intent(c, ChatActivity.class);
                                                               chatIntent.putExtra("visit_user_id", list_user_id);
                                                               chatIntent.putExtra("user_name", userName);
                                                               startActivity(chatIntent);

                                                           }
                                                       });
                                           }
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };

        mFriendsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFriendsAdapter.getItemCount();
                int lastVisiblePosition =
                        manager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    myFriendsList.scrollToPosition(positionStart);
                }
            }


        });

        myFriendsList.setAdapter(mFriendsAdapter);
        myFriendsList.setLayoutManager(manager);

        // mFriendsAdapter.startListening();

        return myMainView;

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

         View mView;

        public FriendsViewHolder(View itemView) {

            super(itemView);

            mView = itemView;

        }

        public void setDate(String date) {

            TextView sinceFriendsData = mView.findViewById(R.id.all_users_status);
            sinceFriendsData.setText("Friends since: \n" + date);

        }

        public void setUserName(String userName) {

            TextView userNameDisplay = mView.findViewById(R.id.all_users_name);
            userNameDisplay.setText(userName);

        }

        public void setThumbImage(final String thumbImage, final Context ctx) {

            final CircleImageView thumb_image = mView.findViewById(R.id.all_users_profile_pic);

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

        public void setUserOnline(String online_status) {

            ImageView onlineStatusView = mView.findViewById(R.id.online_status);

            if(online_status.equals("true"))
            {
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFriendsAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        mFriendsAdapter.stopListening();
    }

}