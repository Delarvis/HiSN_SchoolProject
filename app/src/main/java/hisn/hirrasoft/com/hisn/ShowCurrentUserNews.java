package hisn.hirrasoft.com.hisn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowCurrentUserNews extends AppCompatActivity {

    private String name, current_userid, profile_like_avatar, profile_like_username;
    private FirebaseAuth mAuth;
    private RecyclerView feed;
    private DatabaseReference PostsRef, LikesRef, userNotyLikeRef, userRef;
    Boolean LikeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_current_user_news);

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        current_userid = mAuth.getCurrentUser().getUid().toString();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userid).child("Posts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        feed = (RecyclerView) findViewById(R.id.users_profile_posts);
        feed.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        feed.setLayoutManager(linearLayoutManager);

        DisplayFeedPosts();

    }


    private void DisplayFeedPosts(){
        FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, MainActivity.PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.alk_post_layout,
                                MainActivity.PostsViewHolder.class,
                                PostsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(MainActivity.PostsViewHolder viewHolder, Posts model, int position) {
                        final String postKey = getRef(position).getKey();
                        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                        PostsRef.child(postKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String user_id = dataSnapshot.child("uid").getValue().toString();
                                userNotyLikeRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).child("Notification").child("Likes");

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        viewHolder.setDate(model.getDate());
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setPostImage(model.getPostImage());
                        viewHolder.setProfile_image(model.getProfile_image());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setText(model.getText());

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent edit = new Intent(ShowCurrentUserNews.this, CommentsActivity.class);
                                edit.putExtra("post_key", postKey);
                                startActivity(edit);
                            }
                        });

                        viewHolder.setLikeButtonStatus(postKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent edit = new Intent(ShowCurrentUserNews.this, ChosePostActivity.class);
                                edit.putExtra("post_key", postKey);
                                startActivity(edit);
                            }
                        });

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(LikeChecker.equals(true)){
                                    LikeChecker = false;


                                    userRef.child(current_userid)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    profile_like_username = dataSnapshot.child("username").getValue().toString();
                                                    profile_like_avatar = dataSnapshot.child("profile_avatar").getValue().toString();
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                    if(profile_like_avatar != null&& profile_like_username != null && current_userid != null){
                                        final HashMap like = new HashMap();
                                        like.put("username", profile_like_username);
                                        like.put("profile_avatar", profile_like_avatar);
                                        like.put("uid", current_userid);
                                        userNotyLikeRef.child(postKey+current_userid).updateChildren(like);
                                    }


                                    LikesRef.child(postKey).child(current_userid).setValue(true);

                                }else{

                                    LikeChecker = true;

                                    LikesRef.child(postKey).child(current_userid).removeValue();
                                }
                            }
                        });

                    }
                };
        feed.setAdapter(firebaseRecyclerAdapter);
    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;

        int countLikes;
        String current_uid;
        DatabaseReference LikeRef;

        View mView;

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comments_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.liked_text);

            LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            current_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setFullname(String fullname){
            TextView fullname_show = (TextView) mView.findViewById(R.id.post_author_user_name);
            fullname_show.setText(fullname);
        }

        public void setProfile_image(String profile_image){
            CircleImageView avatar_show = (CircleImageView) mView.findViewById(R.id.post_avatar);
            Picasso.get().load(profile_image).into(avatar_show);
        }
        public void setTime(String time){
            TextView time_data = (TextView) mView.findViewById(R.id.post_time);
            time_data.setText(time);
        }

        public void setDate(String date){
            TextView date_data = (TextView) mView.findViewById(R.id.published_post_date);
            date_data.setText(date);
        }

        public void setText(String text){
            TextView post_text = (TextView) mView.findViewById(R.id.post_text);
            post_text.setText(text);
        }

        public void setPostImage(String postImage){
            if(postImage != null){
                ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
                Picasso.get().load(postImage).into(post_image);
            }else{
                LinearLayout post_image = (LinearLayout) mView.findViewById(R.id.post_image_container);
                post_image.setVisibility(View.GONE);
            }
        }

        public void setLikeButtonStatus(final String postKey) {
            LikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(current_uid)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.baseline_favorite_black_18dp);
                        DisplayNoOfLikes.setText("Этот пост собрал " + Integer.toString(countLikes) + " лайков");
                    }else{
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.baseline_favorite_border_black_18dp);
                        DisplayNoOfLikes.setText("Этот пост собрал " + Integer.toString(countLikes) + " лайков");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void closeBigAvatar(View view) {
        Intent close = new Intent(ShowCurrentUserNews.this, ProfileActivity.class);
        close.putExtra("visited_uid", current_userid);
        startActivity(close);
    }
}

