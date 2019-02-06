package hisn.hirrasoft.com.hisn;

import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton send_comment;
    private RecyclerView comment_list;
    private EditText comment_body;

    private DatabaseReference usersRef, postDBRef, postProfileImageDBRef, postRef;
    private FirebaseAuth mAuth;

    private String post_key, current_uid, profile_image, profile_verifed, postmaker_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_key = getIntent().getExtras().get("post_key").toString();

        mAuth = FirebaseAuth.getInstance();
        current_uid = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postDBRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_key).child("Comments");
        postProfileImageDBRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        postRef.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postmaker_id = dataSnapshot.child("uid").getValue().toString();

            }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        comment_list = (RecyclerView) findViewById(R.id.comments_list);
        comment_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        comment_list.setLayoutManager(linearLayoutManager);

        comment_body = (EditText) findViewById(R.id.send_comment_body);
        send_comment = (ImageButton) findViewById(R.id.send_comment_button);

        send_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(current_uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String username = dataSnapshot.child("username").getValue().toString();

                            ValidationComment(username);


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });



    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setBody(String body) {
            TextView c_body = (TextView) mView.findViewById(R.id.comment_body_show);
            c_body.setText(body);
        }

        public void setProfile_image(String profile_image) {
            CircleImageView c_body = (CircleImageView) mView.findViewById(R.id.profile_avatar_comment);

            Picasso.get().load(profile_image).into(c_body);
        }

        public void setDate_and_time(String date_and_time) {
            TextView c_date_and_time = (TextView) mView.findViewById(R.id.comment_data_and_time);
            c_date_and_time.setText("Комментарий был опубликован: " + date_and_time);
        }

        public void setUsername(String username) {
            TextView c_username = (TextView) mView.findViewById(R.id.comment_get_username);

            c_username.setText(username);
        }

        public void setVerifivation_status(String verifivation_status) {
            ImageView c_verify = (ImageView) mView.findViewById(R.id.comment_verifed_accont);

            if (verifivation_status == "0") {
                c_verify.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                        Comments.class,
                        R.layout.comment_fragment,
                        CommentsViewHolder.class,
                        postDBRef

                ) {
                    @Override
                    protected void populateViewHolder(final CommentsViewHolder viewHolder, Comments model, final int position) {
                        final String commentsKey =  getRef(position).getKey();

                        postDBRef.child(commentsKey).child("uid").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String user_id = dataSnapshot.getValue().toString();

                                usersRef.child(user_id).child("profile_avatar").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String avatar = dataSnapshot.getValue().toString();
                                        viewHolder.setProfile_image(avatar);

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
                        viewHolder.setBody(model.getBody());
                        viewHolder.setDate_and_time(model.getDate_and_time());
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setVerifivation_status(model.getVerifivation_status());

                    }
                };

        comment_list.setAdapter(firebaseRecyclerAdapter);


    }

    private void ValidationComment(final String username) {
        final String commnet_body = comment_body.getText().toString();

        postProfileImageDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                   profile_image = dataSnapshot.child("profile_avatar").getValue().toString();
                   profile_verifed = dataSnapshot.child("ver_status").getValue().toString();

                if(TextUtils.isEmpty(commnet_body)){
                    Toast.makeText(CommentsActivity.this, "Не стоит пытаться создать пустой комментарий :)", Toast.LENGTH_SHORT);
                }else{

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
                    final String saveCurrentData = currentDate.format(calendar.getTime());
                    int min = 0;
                    int max = 999999999;

                    Random rnd = new Random(System.currentTimeMillis());
                    // Получаем случайное число в диапазоне от min до max (включительно)
                    int number = min + rnd.nextInt(max - min + 1);

                    final String randomCode = Integer.toString(number);

                    Calendar time = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                    final String saveCurrentTime = currentTime.format(time.getTime());

                    final String randomCommentName = saveCurrentData + saveCurrentData + current_uid;

                    HashMap commentAddToServerMap = new HashMap();
                    commentAddToServerMap.put("uid", current_uid);
                    commentAddToServerMap.put("date_and_time", saveCurrentData + " " + saveCurrentTime);
                    commentAddToServerMap.put("username", username);
                    commentAddToServerMap.put("body", commnet_body);
                    commentAddToServerMap.put("profile_image", profile_image);
                    commentAddToServerMap.put("verifivation_status", profile_verifed);



                    final HashMap notyComments = new HashMap();
                    notyComments.put("username", username);
                    notyComments.put("text_body", commnet_body);
                    notyComments.put("profile_avatar", profile_image);


                    //usersRef.child(postmaker_id).child("Notification").child("Comments").child(postmaker_id+current_uid+post_key).setValue(notyComments);
                    postDBRef.child(randomCode+randomCommentName).updateChildren(commentAddToServerMap);

                }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    private void RusskiyKostil(){

    }
}
