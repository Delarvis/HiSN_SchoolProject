package hisn.hirrasoft.com.hisn;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private StorageReference storageRef;

    private ImageView avatar, avatar_big;
    private ImageView verifed_show;

    private TextView username_show;
    private TextView name_show;
    private TextView profileonline;

    private String image, username, last_name, name, verifed, frinds_count, country, status;

    private RelativeLayout show_profile, show_avatar_big, show_load_profile;

    private String current_userId, visited_uid, current_username, current_avatar;

    private Button add_friend;

    private NavigationView navigationView;

    static final int Gallery_Pick = 1;

    private Menu menu;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pDialog = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        current_userId = mAuth.getCurrentUser().getUid().toString();
        visited_uid = getIntent().getExtras().get("visited_uid").toString();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("profile_image");


        avatar = (ImageView) findViewById(R.id.profile_avatar_show);
        avatar_big = (ImageView) findViewById(R.id.show_profile_avatar_big);
        show_avatar_big = (RelativeLayout) findViewById(R.id.show_big_avatar);

        show_profile = (RelativeLayout) findViewById(R.id.show_profile);
        show_load_profile = (RelativeLayout) findViewById(R.id.load_profile);
        verifed_show = (ImageView) findViewById(R.id.p_verifed_status);

        profileonline = (TextView) findViewById(R.id.profile_online);
        username_show = (TextView) findViewById(R.id.p_username_show);
        name_show = (TextView) findViewById(R.id.p_name_and_lastnam);

        add_friend = (Button) findViewById(R.id.add_friend_button);

        if (visited_uid.equals(current_userId)) {
            add_friend.setVisibility(View.GONE);
        }


        navigationView = (NavigationView) findViewById(R.id.list_profile_menu);
        navigationView.setNavigationItemSelectedListener(getProfileMenuListener());


        dbRef.child(visited_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    show_load_profile.setVisibility(View.GONE);
                    show_profile.setVisibility(View.VISIBLE);
                    if(dataSnapshot.child("UserState") != null) {
                        String date_last_seen = dataSnapshot.child("UserState").child("date").getValue().toString();
                        String time_last_seen = dataSnapshot.child("UserState").child("time").getValue().toString();
                        String type = dataSnapshot.child("UserState").child("type").getValue().toString();

                        if (type.equals("Offline")) {
                            profileonline.setText("Был (-а) в сети: " + date_last_seen + " в " + time_last_seen);
                        } else {
                            profileonline.setText("Online!");
                        }
                    }else {
                        profileonline.setVisibility(View.GONE);
                    }

                    image = dataSnapshot.child("profile_avatar").getValue().toString();
                    username = dataSnapshot.child("username").getValue().toString();
                    last_name = dataSnapshot.child("last_name").getValue().toString();
                    name = dataSnapshot.child("name").getValue().toString();
                    verifed = dataSnapshot.child("ver_status").getValue().toString();
                    country = dataSnapshot.child("country").getValue().toString();

                    if (dataSnapshot.child("status").getValue() != null) {
                        status = dataSnapshot.child("status").getValue().toString();

                        navigationView.getMenu().findItem(R.id.profile_status).setTitle(status);
                    }

                    dbRef.child(visited_uid).child("Friends").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int friends_count = (int) dataSnapshot.getChildrenCount();
                                frinds_count = Integer.toString(friends_count);
                                Log.d("Profile Friends: ", frinds_count);
                                navigationView.getMenu().findItem(R.id.profile_friends).setTitle("Количество друзей: " + frinds_count);

                            } else {
                                frinds_count = "0";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    String fullname = name + " " + last_name;

                    name_show.setText(fullname);
                    username_show.setText(username);


                    switch (verifed) {
                        case "0":
                            verifed_show.setVisibility(View.GONE);
                            break;
                        case "1":
                            verifed_show.setVisibility(View.VISIBLE);
                            break;
                    }


                    if (visited_uid.equals(current_userId)) {
                        navigationView.getMenu().findItem(R.id.profile_country).setTitle("Вы из " + country);
                    } else {
                        navigationView.getMenu().findItem(R.id.profile_country).setTitle("Этот пользователь из " + country);

                    }

                    Picasso.get().load(image).placeholder(R.drawable.trustees).into(avatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateProfileAvatar();
            }
        });

        dbRef.child(visited_uid).child("Notification").child(current_userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            add_friend.setText("Вы отправили запрос!");
                            add_friend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final AlertDialog.Builder request_delete = new AlertDialog.Builder(ProfileActivity.this);
                                    request_delete.setTitle("Вы хотите отменить запрос в друзья");
                                    request_delete.setMessage("Вы действительно хотите удалить запрос в друзья?");
                                    request_delete.setPositiveButton("Да, хочу!", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dbRef.child(visited_uid).child("Notification").child(current_userId).removeValue();
                                            Intent friend_delete = new Intent(ProfileActivity.this, ProfileActivity.class);
                                            friend_delete.putExtra("visited_uid", visited_uid);
                                            startActivity(friend_delete);
                                        }
                                    });

                                    request_delete.setCancelable(true);
                                    request_delete.show();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateProfileAvatar() {
        if (visited_uid.equals(current_userId)) {
            Intent galleru = new Intent();
            galleru.setType("image/*");
            galleru.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(galleru, Gallery_Pick);
        } else {
            OpenImage();
        }
    }

    private void OpenImage() {
        dbRef.child(visited_uid).child("profile_avatar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String download_url = dataSnapshot.getValue().toString();

                    Picasso.get().load(download_url).into(avatar_big);

                    show_avatar_big.setVisibility(View.VISIBLE);
                    show_profile.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            pDialog.setTitle("Сохранение данных");
            pDialog.setMessage("Соединение с сервером и загрузка данных на него...");
            pDialog.show();

            Uri source_image = data.getData();
            Log.d("Settings", "Hello!" + source_image);
            Uri source = data.getData();
            Uri output = Uri.fromFile(new File(getCacheDir(), "croppedAvatar"));

            Crop.of(source, output).asSquare().start(this);

            // profileAvatar.setImageURI(Crop.getOutput(data));

            final Uri result = Uri.fromFile(new File(getCacheDir(), "croppedAvatar"));
            avatar.setImageURI(result);

            Log.d("Main", "Hi: " + result);

            StorageReference filePath = storageRef.child(current_userId + ".jpg");
            filePath.putFile(result).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        dbRef.child(current_userId).child("profile_avatar").setValue(download_url)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            String avatar_view = "has image!";
                                            Toast.makeText(ProfileActivity.this, "Ваша аватарка загружена!", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            pDialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Упс! Произошла ошибка при загрузке в базу данных: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        String message = task.getException().getMessage();
                        pDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Упс! Произошла ошибка: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private NavigationView.OnNavigationItemSelectedListener getProfileMenuListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_status:
                        Log.d("Profile: ", "PRESSED PROFILE BUTTON!");
                        EditProfileStatus();
                        break;
                    case R.id.profile_posts:
                        Intent posts = new Intent(ProfileActivity.this, ShowCurrentUserNews.class);
                        posts.putExtra("visited_uid", visited_uid);
                        startActivity(posts);
                    case R.id.profile_friends:
                        Intent friends = new Intent(ProfileActivity.this, UsersFriends.class);
                        friends.putExtra("visited_uid", visited_uid);
                        startActivity(friends);
                }
                return false;
            }
        };
    }

    public void EditProfileStatus() {
        if (visited_uid.equals(current_userId)) {
            Log.d("Profile: ", "start editing profile status");
            if (status != null) {
                StatusNotNull();
            } else {
                StatusNull();
            }
        }
    }

    private void StatusNull() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Изменение статуса");

        final EditText inputField = new EditText(ProfileActivity.this);
        builder.setView(inputField);

        builder.setPositiveButton("Обновить!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbRef.child(current_userId).child("status").setValue(inputField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Статус успешно обовлен!", Toast.LENGTH_SHORT);
                                } else {
                                    String message = task.getException().getMessage().toString();
                                    Toast.makeText(ProfileActivity.this, "Упс! Ошибка: " + message, Toast.LENGTH_SHORT);
                                }
                            }
                        });

            }
        });

        builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void StatusNotNull() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Изменение статуса");

        final EditText inputField = new EditText(ProfileActivity.this);
        inputField.setText(status);
        builder.setView(inputField);

        builder.setPositiveButton("Обновить!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dbRef.child(current_userId).child("status").setValue(inputField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Статус успешно обовлен!", Toast.LENGTH_SHORT);
                                } else {
                                    String message = task.getException().getMessage().toString();
                                    Toast.makeText(ProfileActivity.this, "Упс! Ошибка: " + message, Toast.LENGTH_SHORT);
                                }
                            }
                        });

            }
        });

        builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }


    public void add_friend_in_profile(View view) {
        String type_notification = "friend_request";
        dbRef.child(current_userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    current_username = dataSnapshot.child("username").getValue().toString();
                    current_avatar = dataSnapshot.child("profile_avatar").getValue().toString();

                    final HashMap add_friend_request = new HashMap();
                    add_friend_request.put("sender_username", current_username);
                    add_friend_request.put("profile_avatar", current_avatar);
                    add_friend_request.put("sender_uid", current_userId);
                    add_friend_request.put("getter_uid", visited_uid);
                    add_friend_request.put("getter_username", username);

                    dbRef.child(visited_uid).child("Notification").child("FriendsRequest").child(current_userId).updateChildren(add_friend_request)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Вы отправили запрос в друзья!", Toast.LENGTH_SHORT);
                                        add_friend.setText("Вы отправили запрос!");
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void closeBigAvatar(View view) {
        show_avatar_big.setVisibility(View.GONE);
        show_profile.setVisibility(View.VISIBLE);
    }


    public void UpdateUserStatus(String state) {
    }
}
