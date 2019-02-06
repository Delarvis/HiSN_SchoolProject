package hisn.hirrasoft.com.hisn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class OnlineServiseHiSN extends Service {
    FirebaseAuth mAuth;

    public OnlineServiseHiSN() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateUserStatus("Online");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("Offline");
    }



    public void UpdateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm, aa");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        HashMap currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        mAuth = FirebaseAuth.getInstance();

        String currentUID_state = mAuth.getCurrentUser().getUid();
        DatabaseReference userCheck = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID_state);

        userCheck.child("UserState")
                .updateChildren(currentStateMap);
    }
}
