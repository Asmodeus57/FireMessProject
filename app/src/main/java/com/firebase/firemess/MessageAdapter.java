package com.firebase.firemess;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Android on 27.11.2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;

    private FirebaseAuth mAuth;

    private DatabaseReference UserRef;




    public MessageAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout_of_users, parent, false);

        mAuth = FirebaseAuth.getInstance();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        String message_sender_id = mAuth.getCurrentUser().getUid();

        Messages messages = userMessageList.get(position);

        String fromUserId = messages.getFrom();

        //Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        final String time = df.format(messages.getTime());

        String message_type = messages.getType();

        //long message_time = messages.getTime();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("user_name").getValue().toString();
                //String time = dataSnapshot.child("time").getValue().toString();


                holder.displayName.setText(name);

                holder.displayTime.setText(time);





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        if (fromUserId != null && fromUserId.equals(message_sender_id))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_2);

            holder.messageText.setTextColor(Color.BLACK);

            holder.messageText.setGravity(Gravity.START);

        }
        else
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);

            holder.messageText.setTextColor(Color.BLACK);

            holder.messageText.setGravity(Gravity.END);
        }

        //holder.displayTime.setText(messages.getTime());

        if(message_type.equals("text")) {

            holder.messageText.setText(messages.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
            //holder.displayTime.setText((int) message_time);



        } else {

            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(holder.messageText.getContext())
                    .load(messages.getMessage())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.messageImage);
            //holder.displayTime.setText(messages.getTime());

        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public ImageView messageImage;
        public TextView displayName;
        public TextView displayTime;
        //public CircleImageView userProfileImage;



        public MessageViewHolder(View view)
        {
            super(view);

            displayName = view.findViewById(R.id.name_text_layout);
            messageText = view.findViewById(R.id.message_text);
            messageImage = view.findViewById(R.id.message_image);
            displayTime = view.findViewById(R.id.time_text_layout);





            //userProfileImage = view.findViewById(R.id.messages_profile_image);


        }


    }

}
