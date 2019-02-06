package hisn.hirrasoft.com.hisn;

public class AddedFriendsFragment {
    public String username;
    public String uid;

    public AddedFriendsFragment(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String profile_image;

    public AddedFriendsFragment() {

    }

    public AddedFriendsFragment(String username, String uid) {
        this.username = username;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}