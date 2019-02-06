package hisn.hirrasoft.com.hisn;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> userMessageList;

    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseReference;


    public MessageAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderUID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromMessageUID = messages.getFrom();
        String fromMessageType = messages.getType();

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromMessageUID);

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message_avatar = dataSnapshot.child("profile_avatar").getValue().toString();

                Picasso.get().load(message_avatar).into(holder.receiverAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.receiverText.setVisibility(View.INVISIBLE);
            holder.senderText.setVisibility(View.INVISIBLE);

            if(fromMessageUID.equals(messageSenderUID)){
                holder.senderText.setVisibility(View.VISIBLE);

                holder.receiverText.setVisibility(View.GONE);
                holder.receiverAvatar.setVisibility(View.GONE);

                holder.senderText.setBackgroundResource(R.drawable.sender_message);
                holder.senderText.setTextColor(Color.WHITE);
                holder.senderText.setGravity(Gravity.RIGHT);
                holder.senderText.setText(messages.getMessage());

            }else{
                holder.senderText.setVisibility(View.INVISIBLE);

                holder.receiverText.setVisibility(View.VISIBLE);
                holder.receiverAvatar.setVisibility(View.VISIBLE);

                holder.receiverText.setBackgroundResource(R.drawable.receiver_message);
                holder.receiverText.setTextColor(Color.BLACK);
                holder.receiverText.setGravity(Gravity.LEFT);
                Log.d("This ", messages.getMessage());
                holder.receiverText.setText(messages.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderText, receiverText;
        public CircleImageView receiverAvatar;

        public RelativeLayout fromMess, senderMess;

        public MessageViewHolder(View itemView) {
            super(itemView);

            senderText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverText = (TextView) itemView.findViewById(R.id.receiver_message_text);

            receiverAvatar = (CircleImageView) itemView.findViewById(R.id.receiver_message_avatar);


        }
    }
}
