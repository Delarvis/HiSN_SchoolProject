package hisn.hirrasoft.com.hisn;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private EditText message_input;
    private ImageButton send_image_message, send_message;
    private RecyclerView messagesUserList;

    private TextView receiverUsername, receiverOnlineStatus, chatWriteStatus, receiverFullName;

    private CircleImageView receiverAvatar;

    private String messageReceiverID, messageReceiverUsername, messageSenderUserID, saveCurrentData, saveCurrentTime, postRandomName;

    private FirebaseAuth mAuth;

    private DatabaseReference rootReference;

    private Toolbar chatToolbar;

    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("getter_uid").toString();

        messageReceiverUsername = getIntent().getExtras().get("username").toString();



        Initialize();

        DisplayReceiverData();

        FetchMessages();

    }




    private void FetchMessages() {
        rootReference.child("Messages").child(messageSenderUserID).child(messageReceiverID)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.exists()){
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


    }

    private void DisplayReceiverData() {
        receiverUsername.setText(messageReceiverUsername);

        rootReference.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String first_name = dataSnapshot.child("name").getValue().toString();
                String last_name = dataSnapshot.child("last_name").getValue().toString();
                String fullname = first_name + " " + last_name;

                receiverFullName.setText(fullname);

                String avatar = dataSnapshot.child("profile_avatar").getValue().toString();

                Picasso.get().load(avatar).into(receiverAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootReference.child("Users").child(messageReceiverID).child("UserState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date_last_seen = dataSnapshot.child("date").getValue().toString();
                String time_last_seen = dataSnapshot.child("time").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();

                if(type.equals("Offline")){
                    receiverOnlineStatus.setText("Был (-а) в сети: " + date_last_seen + " в " + time_last_seen);
                }else{
                    receiverOnlineStatus.setText("Online!");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void Initialize() {

        send_image_message = (ImageButton) findViewById(R.id.send_message_image);
        send_message = (ImageButton) findViewById(R.id.send_message);
        message_input = (EditText) findViewById(R.id.input_message);




        receiverAvatar = (CircleImageView) findViewById(R.id.chat_receiver_avatar);

        mAuth = FirebaseAuth.getInstance();

        messageSenderUserID = mAuth.getCurrentUser().getUid();

        receiverFullName = (TextView) findViewById(R.id.fullname_chat);
        chatWriteStatus = (TextView) findViewById(R.id.writing_status);
        receiverOnlineStatus = (TextView) findViewById(R.id.online_status);
        receiverUsername = (TextView) findViewById(R.id.username_chat);

        rootReference = FirebaseDatabase.getInstance().getReference();

        messagesUserList = (RecyclerView) findViewById(R.id.messages_list_users);
        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesUserList.setHasFixedSize(true);
        messagesUserList.setLayoutManager(linearLayoutManager);
        messagesUserList.setAdapter(messageAdapter);

        rootReference.child("Users").child(messageReceiverID).child("Friends").child(messageSenderUserID).child("black_list")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null){
                            final String ban_check = dataSnapshot.getValue().toString();
                            if(ban_check.equals("false")){
                                send_message.setVisibility(View.GONE);
                                message_input.setText("Вы в черном списке!");
                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
    }

    private void SendMessage() {
        final String message_text_body = message_input.getText().toString();



        if(message_text_body.isEmpty()){
            Toast.makeText(this, "Введите текст сообщения!", Toast.LENGTH_SHORT);
        }else{
            final String message_sender_query = "Messages/" + messageSenderUserID + "/" + messageReceiverID;
            final String message_receiver_query = "Messages/" + messageReceiverID + "/" + messageSenderUserID;

            DatabaseReference messagmee_user_key = rootReference.child("Messages").child(messageSenderUserID)
                    .child(messageReceiverID).push();
            final String message_push_id = messagmee_user_key.getKey();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
            saveCurrentData = currentDate.format(calendar.getTime());

            Calendar time = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(time.getTime());

            Log.d("Loggg ", message_text_body + currentDate + currentTime + messageSenderUserID );

            Map messageReceiver = new HashMap();
            messageReceiver.put("message", message_text_body);
            messageReceiver.put("time", saveCurrentTime);
            messageReceiver.put("date", saveCurrentData);
            messageReceiver.put("type", "text");
            messageReceiver.put("from", messageSenderUserID);

            Map messageBodyDesc = new HashMap();
            messageBodyDesc.put(message_sender_query + "/" + message_push_id, messageReceiver);
            messageBodyDesc.put(message_receiver_query + "/" + message_push_id, messageReceiver);


            rootReference.updateChildren(messageBodyDesc).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        message_input.setText("");
                    }else{
                        String mess = task.getException().getLocalizedMessage();
                        Toast.makeText(ChatActivity.this, mess, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void back_from_chat(View view) {
        Intent goBack = new Intent(ChatActivity.this, MainActivity.class);
        startActivity(goBack);
    }
}
