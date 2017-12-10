package com.firebase.firemess;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUserList;
    private DatabaseReference allDatabaseUsersReference;
    private FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> mUsersAdapter;
    //private List<AllUsers> usersList = new ArrayList<>();
    private MaterialSearchView searchView;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        mToolbar = findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView = findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {


            }

            @Override
            public void onSearchViewClosed() {



                allUserList.setAdapter(mUsersAdapter);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                Query allUsersQuery = allDatabaseUsersReference.child("Users").orderByChild("user_name").startAt(newText).endAt("~");

                FirebaseRecyclerOptions<AllUsers> allUsersOptions =
                        new FirebaseRecyclerOptions.Builder<AllUsers>().setQuery(allUsersQuery, AllUsers.class).build();
                mUsersAdapter = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(allUsersOptions) {


                    @Override
                    protected void onBindViewHolder(final AllUsersViewHolder viewHolder, final int position, AllUsers model) {

                        viewHolder.setUser_name(model.getUser_name());
                        viewHolder.setUser_status(model.getUser_status());
                        viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String visit_user_id = getRef(viewHolder.getAdapterPosition()).getKey();

                                Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);


                            }
                        });

                    }

                    @Override
                    public AllUsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_users_display_layout, parent, false);

                        return new AllUsersViewHolder(view);
                    }

                };

                allUserList.setAdapter(mUsersAdapter);

                return false;
            }
        });


        allUserList = findViewById(R.id.all_user_view);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager(new LinearLayoutManager (this));

        allDatabaseUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        allDatabaseUsersReference.keepSynced(true);

        Query allUsersQuery = allDatabaseUsersReference.orderByKey();

        FirebaseRecyclerOptions<AllUsers> allUsersOptions =
                new FirebaseRecyclerOptions.Builder<AllUsers>().setQuery(allUsersQuery, AllUsers.class).build();
        mUsersAdapter = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(allUsersOptions) {


            @Override
            protected void onBindViewHolder(final AllUsersViewHolder viewHolder, final int position, AllUsers model) {

                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id = getRef(viewHolder.getAdapterPosition()).getKey();

                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);


                    }
                });

            }

            @Override
            public AllUsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);

                return new AllUsersViewHolder(view);
            }

        };

        allUserList.setAdapter(mUsersAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;

    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{

        View mView;


        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUser_name(String user_name){
            TextView name = mView.findViewById(R.id.all_users_name);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = mView.findViewById(R.id.all_users_status);
            status.setText(user_status);
        }

        public void setUser_thumb_image(final Context ctx, final String setUser_thumb_image){
            final CircleImageView thumb_image = mView.findViewById(R.id.all_users_profile_pic);

           // Picasso.with(ctx).load(setUser_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
            Picasso.with(ctx).load(setUser_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(setUser_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
                        }
                    });
        }



    }

    @Override
    public void onStart() {
        super.onStart();
        mUsersAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        mUsersAdapter.stopListening();
    }
}
