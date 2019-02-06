package hisn.hirrasoft.com.hisn;

public class NotyFriends {
    public String uid, sender_username, profile_avatar;

    public NotyFriends(){

    }

    public NotyFriends(String uid, String sender_username, String profile_avatar) {
        this.uid = uid;
        this.sender_username = sender_username;
        this.profile_avatar = profile_avatar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }
}
