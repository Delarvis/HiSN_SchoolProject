package hisn.hirrasoft.com.hisn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersFriends extends AppCompatActivity {

    private RecyclerView friendList;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference, friendReference, blackListReference;
    private String current_uid, visited_uid;
    private LinearLayoutManager friends;

    private boolean CheckBlackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_friends);

        mAuth = FirebaseAuth.getInstance();

        visited_uid = getIntent().getExtras().get("visited_uid").toString();
        current_uid = mAuth.getCurrentUser().getUid();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendReference = FirebaseDatabase.getInstance().getReference().child("Users").child(visited_uid).child("Friends");
        blackListReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("BlackList");

        friendList = (RecyclerView) findViewById(R.id.users_friends_list);

        friends = new LinearLayoutManager(this);
        friends.setStackFromEnd(true);
        friends.setReverseLayout(true);
        friendList.setLayoutManager(friends);



        DisplayFriendsList();


    }

    private void DisplayFriendsList() {
        FirebaseRecyclerAdapter <AddedFriendsFragment, FriendsListViewHolder> firebaseUsersFriendsRecyclerAdapter
                = new FirebaseRecyclerAdapter<AddedFriendsFragment, FriendsListViewHolder>
                (
                        AddedFriendsFragment.class,
                        R.layout.all_friends_added,
                        FriendsListViewHolder.class,
                        friendReference
                )
        {
            @Override
            protected void populateViewHolder(final FriendsListViewHolder viewHolder, final AddedFriendsFragment model, int position) {
                final String friendsKey = getRef(position).getKey();
                friendReference.child(friendsKey).child("black_list").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            String black_list_result = dataSnapshot.getValue().toString();

                            if (black_list_result.equals("false")) {
                                viewHolder.message_to_friend.setVisibility(View.GONE);
                                viewHolder.remove_from_friends.setVisibility(View.GONE);
                                viewHolder.add_friend_to_black_list.setText("Убрать из черного списка");
                            } else if (black_list_result.equals(null)) {
                                viewHolder.message_to_friend.setVisibility(View.GONE);
                                viewHolder.remove_from_friends.setVisibility(View.GONE);
                                viewHolder.add_friend_to_black_list.setText("Убрать из черного списка");
                            } else {
                                viewHolder.message_to_friend.setVisibility(View.VISIBLE);
                                viewHolder.remove_from_friends.setVisibility(View.VISIBLE);
                                viewHolder.add_friend_to_black_list.setText("Заблокировать");

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setUsername(model.getUsername());

                userReference.child(friendsKey).child("profile_avatar").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String profile_avatar = dataSnapshot.getValue().toString();
                        viewHolder.setProfile_image(profile_avatar);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if(visited_uid.equals(current_uid)){
                    viewHolder.add_friend_to_black_list.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            friendReference.child(friendsKey).child("black_list").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String black_list_result = "";

                                    if(dataSnapshot.exists()){
                                        black_list_result = dataSnapshot.getValue().toString();;
                                    }


                                        if (black_list_result.equals("false")) {
                                            friendReference.child(friendsKey).child("black_list").setValue(true);
                                            viewHolder.message_to_friend.setVisibility(View.GONE);
                                            viewHolder.remove_from_friends.setVisibility(View.GONE);
                                            viewHolder.add_friend_to_black_list.setText("Убрать из черного списка");
                                        } else if (black_list_result.equals("")) {
                                            friendReference.child(friendsKey).child("black_list").setValue(true);
                                            viewHolder.message_to_friend.setVisibility(View.GONE);
                                            viewHolder.remove_from_friends.setVisibility(View.GONE);
                                            viewHolder.add_friend_to_black_list.setText("Убрать из черного списка");
                                        } else {
                                            friendReference.child(friendsKey).child("black_list").setValue(false);
                                            viewHolder.message_to_friend.setVisibility(View.VISIBLE);
                                            viewHolder.remove_from_friends.setVisibility(View.VISIBLE);
                                            viewHolder.add_friend_to_black_list.setText("Заблокировать");
                                        }
                                    }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    viewHolder.remove_from_friends.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            friendReference.child(friendsKey).removeValue();
                            userReference.child(friendsKey).child("Friends").child(current_uid);
                        }
                    });

                    viewHolder.message_to_friend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent chat = new Intent(UsersFriends.this, ChatActivity.class);
                            chat.putExtra("getter_uid", friendsKey);
                            chat.putExtra("username", model.getUsername());
                            startActivity(chat);
                        }
                    });

                }else{
                    viewHolder.message_to_friend.setVisibility(View.GONE);
                    viewHolder.remove_from_friends.setVisibility(View.GONE);
                    viewHolder.add_friend_to_black_list.setVisibility(View.GONE);
                }


            }
        };

        friendList.setAdapter(firebaseUsersFriendsRecyclerAdapter);
    }

    public void back(View view) {
        finish();

    }

    public static class FriendsListViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Button message_to_friend, remove_from_friends, add_friend_to_black_list;
        TextView username_show;
        CircleImageView avatar_friends;
        String visited_uid, current_uid;

        public FriendsListViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            message_to_friend = (Button) mView.findViewById(R.id.write_message_to_friends);
            remove_from_friends = (Button) mView.findViewById(R.id.delete_from_friends);
            add_friend_to_black_list = (Button) mView.findViewById(R.id.friends_to_black_list);

            username_show = (TextView) mView.findViewById(R.id.added_friend_username);

            avatar_friends = (CircleImageView) mView.findViewById(R.id.adf_avatar);
        }

        public void setUsername(String username) {
            username_show.setText(username);
        }

        public void setProfile_image(String profile_image) {
            Picasso.get().load(profile_image).into(avatar_friends);
        }


    }

}
