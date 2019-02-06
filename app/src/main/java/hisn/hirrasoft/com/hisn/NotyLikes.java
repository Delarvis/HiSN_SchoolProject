package hisn.hirrasoft.com.hisn;

public class NotyLikes {
    public String uid, username, profile_avatar;

    public NotyLikes(){

    }

    public NotyLikes(String uid, String username, String profile_avatar) {
        this.uid = uid;
        this.username = username;
        this.profile_avatar = profile_avatar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }
}
