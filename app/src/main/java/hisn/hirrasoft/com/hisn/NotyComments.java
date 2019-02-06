package hisn.hirrasoft.com.hisn;

public class NotyComments {
    public String uid, text_body, profile_avatar, username;

    public NotyComments(){

    }

    public NotyComments(String uid, String text_body, String profile_avatar, String username) {
        this.uid = uid;
        this.text_body = text_body;
        this.profile_avatar = profile_avatar;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText_body() {
        return text_body;
    }

    public void setText_body(String text_body) {
        this.text_body = text_body;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
