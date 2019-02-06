package hisn.hirrasoft.com.hisn;

public class Posts {

    public Posts(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return post_image;
    }

    public void setPostImage(String post_image) {
        this.post_image = post_image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Posts(String uid, String time, String date, String postImage, String text, String profile_image, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.post_image = post_image;
        this.text = text;
        this.profile_image = profile_image;
        this.fullname = fullname;

    }

    public String uid, time, date, post_image, text, profile_image, fullname;
}
