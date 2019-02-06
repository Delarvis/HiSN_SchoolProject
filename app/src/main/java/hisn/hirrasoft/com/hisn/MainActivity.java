package hisn.hirrasoft.com.hisn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static hisn.hirrasoft.com.hisn.R.layout.notification_add_friends;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public RelativeLayout show_menu;
    public RelativeLayout show_feed, show_find_friends, show_notifiation, show_messages;
    private NavigationView navView;
    public TextView show_title;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, PostsRef, allUsers, LikesRef, userNotyRef, userNotyLikeRef, messageRef, updateCheck;
    private RecyclerView feed;

    String currentUserId;
    Boolean LikeChecker = false;


    //Поиск друзей
    private String name, current_userid, profile_like_avatar, profile_like_username;
    private EditText enterFindName;
    private ImageButton findButton;
    private RecyclerView friend_list;
    private TextView findUsername, findStatus;
    private CircleImageView findAvatar;

    private Object likeAdd;

    private RecyclerView notyCommentView, notyLikeView, notyFriendsView, messageView;
    private LinearLayoutManager llmCommnet, llmLike, llmFriends, messageListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        //Начало: Поиск друзей
        enterFindName = (EditText) findViewById(R.id.find_user_field);
        findButton = (ImageButton) findViewById(R.id.find_friends_button);
        friend_list = (RecyclerView) findViewById(R.id.find_friends_list);
        findUsername = (TextView) findViewById(R.id.find_friends_username);
        findStatus = (TextView) findViewById(R.id.findfriends_status);
        findAvatar = (CircleImageView) findViewById(R.id.find_user_avatar);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = enterFindName.getText().toString();

                FindFriendByUsername(username);
            }
        });

        allUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        //Конец

        //Notification
        //Коментарии
       /* notyCommentView = (RecyclerView) findViewById(R.id.notification_comments);
        notyCommentView.setHasFixedSize(true);
        llmCommnet = new LinearLayoutManager(this);
        llmCommnet.setReverseLayout(true);
        llmCommnet.setStackFromEnd(true);
        notyCommentView.setLayoutManager(llmCommnet); */

        //Лайки
        notyLikeView = (RecyclerView) findViewById(R.id.notification_likes);
        notyLikeView.setHasFixedSize(false);
        llmLike = new LinearLayoutManager(this);
        llmLike.setStackFromEnd(true);
        llmLike.setReverseLayout(true);
        notyLikeView.setLayoutManager(llmLike);

        //Друзья
        notyFriendsView = (RecyclerView) findViewById(R.id.notification_friends);
        notyFriendsView.setHasFixedSize(false);
        llmFriends = new LinearLayoutManager(this);
        llmFriends.setReverseLayout(true);
        llmFriends.setStackFromEnd(true);
        notyFriendsView.setLayoutManager(llmFriends);
        //Конец

        messageView = (RecyclerView) findViewById(R.id.message_has_view);
        messageView.setHasFixedSize(true);
        messageListView = new LinearLayoutManager(this);
        messageListView.setReverseLayout(true);
        messageListView.setStackFromEnd(true);
        messageView.setLayoutManager(messageListView);

        show_menu = (RelativeLayout) findViewById(R.id.menu_show);
        show_notifiation = (RelativeLayout) findViewById(R.id.notification_view);
        show_feed = (RelativeLayout) findViewById(R.id.feed_show);
        show_find_friends = (RelativeLayout) findViewById(R.id.find_friends_show);

        show_title = (TextView) findViewById(R.id.title);

        feed = (RecyclerView) findViewById(R.id.feed);
        feed.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        feed.setLayoutManager(linearLayoutManager);
        friend_list.setLayoutManager(linearLayoutManager1);

        show_title.setText("Лента новостей");

        mAuth = FirebaseAuth.getInstance();





        show_messages = (RelativeLayout) findViewById(R.id.message_list_show) ;
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        navView = (NavigationView) findViewById(R.id.menu);
        navView.setNavigationItemSelectedListener(getMenuListener());

        BottomNavigationView bnv = (BottomNavigationView) findViewById(R.id.bottom_nav_view);
        bnv.setOnNavigationItemSelectedListener(getBottomNavigationListener());
        //Adapters Method
        //DisplayComments();

        if(mAuth.getCurrentUser() != null) {
            current_userid = mAuth.getCurrentUser().getUid().toString();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");
            updateCheck = FirebaseDatabase.getInstance().getReference().child("Update");
            userNotyRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userid).child("Notification").child("FriendsRequest");

            messageRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(current_userid);

            updateCheck.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    String current_app_version = BuildConfig.VERSION_NAME.toString();

                    if(dataSnapshot.child("version").getValue().toString().equals(current_app_version)){
                        startService(new Intent(MainActivity.this, OnlineServiseHiSN.class));
                    }else{
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setMessage("Вышло критическое обновление до версии: " + dataSnapshot.child("version").getValue().toString());
                        alertDialog.setCancelable(true);
                        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                stopService(new Intent(MainActivity.this,OnlineServiseHiSN.class));
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        });
                        alertDialog.setPositiveButton("Обновиться!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String url = dataSnapshot.child("update_url").getValue().toString();

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(browserIntent);
                            }
                        });

                        alertDialog.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DisplayLikes();
            DisplayFriends();
            DisplayFeedPosts();
            DisplayMessageList();
            AllUsers();
        }

    }

    private void DisplayMessageList() {
        FirebaseRecyclerAdapter <MessageShow, MessageListViewHolder> firebaseMessageRecyclerAdapter
                = new FirebaseRecyclerAdapter<MessageShow, MessageListViewHolder>
                (
                        MessageShow.class,
                        R.layout.all_users_messages,
                        MessageListViewHolder.class,
                        messageRef
                )
        {
            @Override
            protected void populateViewHolder(final MessageListViewHolder viewHolder, MessageShow model, int position) {
                final String message_key = getRef(position).getKey();

                messageRef.child(message_key).child("hasAnswer").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            viewHolder.setHasAnswer(dataSnapshot.getValue().toString());
                        }else{
                            viewHolder.setHasAnswer("answer_false");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                userRef.child(message_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String profile_avatar = dataSnapshot.child("profile_avatar").getValue().toString();
                        final String username = dataSnapshot.child("username").getValue().toString();

                        viewHolder.setMessageListUsername(username);
                        viewHolder.setMessageProfileImage(profile_avatar);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chat = new Intent(MainActivity.this, ChatActivity.class);
                                chat.putExtra("getter_uid", message_key);
                                chat.putExtra("username", username);
                                chat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(chat);
                            }
                        });
                     }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }
        };

        messageView.setAdapter(firebaseMessageRecyclerAdapter);
    }

    public static class MessageListViewHolder extends RecyclerView.ViewHolder{
        View mView;

        CircleImageView messageListAvatar;
        TextView messageListUsername, messageListHasAnswer;

        public MessageListViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            messageListAvatar = (CircleImageView) mView.findViewById(R.id.message_list_avatar_view);

            messageListHasAnswer = (TextView) mView.findViewById(R.id.message_list_answer_got);
            messageListUsername = (TextView) mView.findViewById(R.id.message_list_username);
        }

        public void setHasAnswer(String hasAnswer) {
            if(hasAnswer.equals("answer_true")){
                messageListHasAnswer.setVisibility(View.VISIBLE);
                messageListHasAnswer.setText("Есть ответ от пользователя!");
            }else {
                messageListHasAnswer.setVisibility(View.GONE);
            }
        }


        public void setMessageProfileImage(String profileImage){
            Picasso.get().load(profileImage).into(messageListAvatar);
        }

        public void setMessageListUsername(String username){
            messageListUsername.setText(username);
        }
    }



    //Показать уведомления - friends
    private void DisplayFriends() {
       FirebaseRecyclerAdapter<NotyFriends, FriendsRequestViewHolder> firebaseFriendRequestAdapter
               = new FirebaseRecyclerAdapter<NotyFriends, FriendsRequestViewHolder>(
                            NotyFriends.class,
                            R.layout.notification_add_friends,
                            FriendsRequestViewHolder.class,
                            userNotyRef
                       ) {
           @Override
           protected void populateViewHolder(final FriendsRequestViewHolder viewHolder, final NotyFriends model, int position) {
                final String notyKey = getRef(position).getKey();

                Log.d(TAG, notyKey);

                final DatabaseReference userNotyReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userid).child("Notification").child("FriendsRequest").child(notyKey);

                userRef.child(current_userid).child("Notification").child("FriendsRequest").child(notyKey)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    String uid = dataSnapshot.child("sender_uid").getValue().toString();

                                    userRef.child(uid).child("profile_avatar")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String profile_avatar = dataSnapshot.getValue().toString();

                                                    viewHolder.setProfile_avatar(profile_avatar);
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
                viewHolder.setSender_username(model.getSender_username());

                viewHolder.acceptFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        userNotyRef.child(notyKey)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot != null && dataSnapshot.exists()){
                                            String getter_username, getter_uid, sender_uid, sender_username;
                                            getter_username = dataSnapshot.child("getter_username").getValue().toString();
                                            getter_uid = dataSnapshot.child("getter_uid").getValue().toString();
                                            sender_uid = dataSnapshot.child("sender_uid").getValue().toString();
                                            sender_username = dataSnapshot.child("sender_username").getValue().toString();

                                            HashMap senderUserAdd = new HashMap();
                                            senderUserAdd.put("uid", getter_uid);
                                            senderUserAdd.put("username", getter_username);
                                            senderUserAdd.put("black_list", "true");

                                            userRef.child(sender_uid).child("Friends").child(getter_uid).updateChildren(senderUserAdd);

                                            HashMap currentUserAdd = new HashMap();
                                            currentUserAdd.put("uid", sender_uid);
                                            currentUserAdd.put("username", sender_username);
                                            currentUserAdd.put("black_list", "true");

                                            userRef.child(getter_uid).child("Friends").child(sender_uid).updateChildren(currentUserAdd);

                                            userNotyRef.child(notyKey).removeValue();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                });

                viewHolder.cancelFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userRef.child(current_userid).child("Notification").child("FriendsRequest").child(notyKey).removeValue();
                    }
                });

           }
       };
       notyFriendsView.setAdapter(firebaseFriendRequestAdapter);
    }

    //Показать уведомления - likes
    private void DisplayLikes() {
        FirebaseRecyclerAdapter <NotyLikes, LikeRequestViewHolder> firebaseLikeNoty
                = new FirebaseRecyclerAdapter<NotyLikes, LikeRequestViewHolder>
                (
                        NotyLikes.class,
                        R.layout.notification_liked_post,
                        LikeRequestViewHolder.class,
                        userRef.child(current_userid).child("Notification").child("Likes")
                )
        {
            @Override
            protected void populateViewHolder(final LikeRequestViewHolder viewHolder, final NotyLikes model, int position) {
                final String notyLikeKey = getRef(position).getKey();
                userRef.child(current_userid).child("Notification").child("Likes").child(notyLikeKey)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String uid = dataSnapshot.child("uid").getValue().toString();

                                userRef.child(uid).child("profile_avatar")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String profile_image = dataSnapshot.getValue().toString();
                                                viewHolder.setProfile_avatar(profile_image);
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
                viewHolder.setUsername(model.getUsername());
            }
        };
        notyLikeView.setAdapter(firebaseLikeNoty);
    }

    //Показать уведомления - comments
    private void DisplayComments() {
        FirebaseRecyclerAdapter<NotyComments, CommentsRequestViewHolder> firebasComments
                = new FirebaseRecyclerAdapter<NotyComments, CommentsRequestViewHolder>
                (
                        NotyComments.class,
                        R.layout.notification_add_comments,
                        CommentsRequestViewHolder.class,
                        userRef.child(current_userid).child("Notification").child("Comments")
                )
        {
            @Override
            protected void populateViewHolder(CommentsRequestViewHolder viewHolder, NotyComments model, int position) {
                viewHolder.setProfile_avatar(model.getProfile_avatar());
                viewHolder.setText_body(model.getText_body());
                viewHolder.setUsername(model.getUsername());
            }
        };
        notyCommentView.setAdapter(firebasComments);
    }

    public static class CommentsRequestViewHolder extends RecyclerView.ViewHolder{
        View mView;

        TextView commentsBody, commentsUsername;
        CircleImageView profile_avatar_comments;

        public CommentsRequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            commentsBody = (TextView) mView.findViewById(R.id.n_comment_body);
            commentsUsername = (TextView) mView.findViewById(R.id.n_username_comment);
            profile_avatar_comments = (CircleImageView) mView.findViewById(R.id.n_avatar_comment);
        }

        public void setText_body(String text_body) {
            commentsBody.setText(text_body);
        }

        public void setProfile_avatar(String profile_avatar) {
            Picasso.get().load(profile_avatar).into(profile_avatar_comments);
        }

        public void setUsername(String username) {
            commentsUsername.setText(username);
        }
    }

    //Like ViewHolder
    public static class LikeRequestViewHolder extends RecyclerView.ViewHolder{
        View mView;

        TextView usernameLike;
        CircleImageView avatarLike;
        String currentUserID;

        DatabaseReference usersNotyLikeReference;
        FirebaseAuth mAuth;

        public LikeRequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            usernameLike = (TextView) mView.findViewById(R.id.n_like_username);

            avatarLike = (CircleImageView) mView.findViewById(R.id.n_avatar_like);
        }

        public void setUsername(String username) {
            usernameLike.setText(username);
        }

        public void setProfile_avatar(String profile_avatar) {
            Picasso.get().load(profile_avatar).into(avatarLike);
        }
    }

    //ViewHolders
    public static class FriendsRequestViewHolder extends RecyclerView.ViewHolder{

        View mView;

        TextView Username;
        CircleImageView FriendsAvatar;
        Button acceptFriend, cancelFriend;
        DatabaseReference usersNotyFriendsReference;
        String currentUserId, requestUserId;
        FirebaseAuth mAuth;

        public FriendsRequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            FriendsAvatar = (CircleImageView) mView.findViewById(R.id.avatar_friend_request);
            Username = (TextView) mView.findViewById(R.id.noty_friend_username);
            acceptFriend = (Button) mView.findViewById(R.id.n_friend_accept);
            cancelFriend = (Button) mView.findViewById(R.id.n_friend_cancel);

            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
            usersNotyFriendsReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId)
                    .child("Notification").child("FriendsRequest");

        }

        public void setSender_username(String sender_username) {
            Username.setText(sender_username);
        }

        public void setProfile_avatar(String profile_avatar) {
            Picasso.get().load(profile_avatar).into(FriendsAvatar );
        }


    }

    private void FindFriendByUsername(String username) {
        String findUsername_input = enterFindName.getText().toString();
        Query searchPeople = allUsers.orderByChild("username")
                .startAt(findUsername_input).endAt(findUsername_input + "\uf8ff");
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                FindFriends.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                searchPeople
        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, final int position) {
                viewHolder.setStatus(model.getStatus());
                viewHolder.setProfile_avatar(model.getProfile_avatar());
                viewHolder.setUsername(model.getUsername());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visited_uid = getRef(position).getKey();

                        Intent profileActivity = new Intent(MainActivity.this, ProfileActivity.class);
                        profileActivity.putExtra("visited_uid", visited_uid);
                        startActivity(profileActivity);
                    }
                });
            }
        };
        friend_list.setAdapter(firebaseRecyclerAdapter);

    }

    public void AllUsers(){
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder > firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                FindFriends.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                userRef
        ) {
            @Override
            protected void populateViewHolder(final FindFriendsViewHolder viewHolder, final FindFriends model, final int position) {
                final String visited_uid = getRef(position).getKey();

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String username = dataSnapshot.child(visited_uid).child("username").getValue().toString();

                            String status = "";

                            if(dataSnapshot.child(visited_uid).child("status").getValue() != null){
                                status = dataSnapshot.child(visited_uid).child("status").getValue().toString();
                            }

                            String profile_avatar = dataSnapshot.child(visited_uid).child("profile_avatar").getValue().toString();

                            viewHolder.setUsername(username);
                            viewHolder.setStatus(status);
                            viewHolder.setProfile_avatar(profile_avatar);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    Intent profileActivity = new Intent(MainActivity.this, ProfileActivity.class);
                                    profileActivity.putExtra("visited_uid", visited_uid);
                                    startActivity(profileActivity);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });

            }
        };
        friend_list.setAdapter(firebaseRecyclerAdapter);
    }

    private void DisplayFeedPosts(){
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                          Posts.class,
                                R.layout.alk_post_layout,
                                PostsViewHolder.class,
                                PostsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position) {
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
                        viewHolder.setFullname(model.getUid());
                        viewHolder.setPostImage(model.getPostImage());
                        viewHolder.setProfile_image(model.getUid());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setText(model.getText());

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent edit = new Intent(MainActivity.this, CommentsActivity.class);
                                edit.putExtra("post_key", postKey);

                                startActivity(edit);

                            }
                        });

                        viewHolder.setLikeButtonStatus(postKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent edit = new Intent(MainActivity.this, ChosePostActivity.class);
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
        DatabaseReference userRef;

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

        public void setFullname(String uid){
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");

            userRef.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String last_name = dataSnapshot.child("last_name").getValue().toString();
                    String fullname = name + " " + last_name;

                    TextView fullname_show = (TextView) mView.findViewById(R.id.post_author_user_name);
                    fullname_show.setText(fullname);                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setProfile_image(String uid){
            final CircleImageView avatar_show = (CircleImageView) mView.findViewById(R.id.post_avatar);


            userRef = FirebaseDatabase.getInstance().getReference().child("Users");

            userRef.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String avatar = dataSnapshot.child("profile_avatar").getValue().toString();

                    Picasso.get().load(avatar).into(avatar_show);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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

    public void add_new_post(View v){
        Intent add_new_post = new Intent(MainActivity.this, AddPostActivity.class);
        add_new_post.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(add_new_post);
    }

    private NavigationView.OnNavigationItemSelectedListener getMenuListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.logout:
                        mAuth.signOut();
                        SignOutFromApp();
                        break;
                    case R.id.profile:
                        Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                        profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        profile.putExtra("visited_uid", current_userid);
                        startActivity(profile);
                        break;
                    case R.id.friends:
                        Intent friends = new Intent(MainActivity.this, UsersFriends.class);
                        friends.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        friends.putExtra("visited_uid", current_userid);
                        startActivity(friends);
                        break;
                }
                return false;
            }
        };
    }
    @NonNull
    private BottomNavigationView.OnNavigationItemSelectedListener getBottomNavigationListener() {
        return new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu:
                        show_menu.setVisibility(View.VISIBLE);
                        show_title.setText("Меню");
                        show_feed.setVisibility(View.GONE);
                        show_find_friends.setVisibility(View.GONE);
                        show_messages.setVisibility(View.GONE);
                        break;

                    case R.id.home:
                        show_feed.setVisibility(View.VISIBLE);
                        show_menu.setVisibility(View.GONE);
                        show_notifiation.setVisibility(View.GONE);
                        show_find_friends.setVisibility(View.GONE);
                        show_messages.setVisibility(View.GONE);
                        show_title.setText("Лента новостей");
                        break;
                    case R.id.search:
                        show_find_friends.setVisibility(View.VISIBLE);
                        show_feed.setVisibility(View.GONE);
                        show_menu.setVisibility(View.GONE);
                        show_messages.setVisibility(View.GONE);
                        show_notifiation.setVisibility(View.GONE);
                        break;

                        case R.id.notification_friends:
                        show_notifiation.setVisibility(View.VISIBLE);
                        show_find_friends.setVisibility(View.GONE);
                        show_feed.setVisibility(View.GONE);
                        show_messages.setVisibility(View.GONE);
                        show_menu.setVisibility(View.GONE);
                        DisplayNotification();
                        break;

                    case R.id.messages:
                        show_notifiation.setVisibility(View.GONE);
                        show_find_friends.setVisibility(View.GONE);
                        show_feed.setVisibility(View.GONE);
                        show_messages.setVisibility(View.VISIBLE);
                        show_menu.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        };
    }

    private void DisplayNotification() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, OnlineServiseHiSN.class));

    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            CheckUserData();

        }
    }



    private void CheckUserData() {
        final String cur_userid = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(cur_userid)){
                    settingAppAccount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void SignOutFromApp(){
        Toast.makeText(this, "Вы успешно вышли из аккаунта!", Toast.LENGTH_SHORT).show();
        Intent loginStart = new Intent(MainActivity.this, LoginActivity.class);
        loginStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginStart);
        finish();
    }

    public void SendUserToLoginActivity(){
        Intent loginStart = new Intent(MainActivity.this, LoginActivity.class);
        loginStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginStart);
        finish();
    }

    private void settingAppAccount(){
        Intent setup = new Intent(MainActivity.this, SettingsActivity.class);
        setup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setup);
        finish();;
    }


    //Найти пользователей
    public void findUserButton(final View view) {

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindFriendsViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }

        public void setProfile_avatar(String profile_avatar) {
            CircleImageView findAvatar = (CircleImageView) mView.findViewById(R.id.find_user_avatar);
            Picasso.get().load(profile_avatar).into(findAvatar);
        }

        public void setStatus(String status) {
            TextView findStatus = (TextView) mView.findViewById(R.id.findfriends_status);
            findStatus.setText(status);
        }

        public void setUsername(String username) {
            TextView findUsername = (TextView) mView.findViewById(R.id.find_friends_username);
            findUsername.setText(username);
        }
    }
}
