package hisn.hirrasoft.com.hisn;

public class Comments {
    public String body, profile_image, date_and_time, username, verifivation_status;

    public Comments(){

    }

    public Comments(String body, String profile_image, String date_and_time, String username, String verifivation_status) {
        this.body = body;
        this.profile_image = profile_image;
        this.date_and_time = date_and_time;
        this.username = username;
        this.verifivation_status = verifivation_status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getDate_and_time() {
        return date_and_time;
    }

    public void setDate_and_time(String date_and_time) {
        this.date_and_time = date_and_time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVerifivation_status() {
        return verifivation_status;
    }

    public void setVerifivation_status(String verifivation_status) {
        this.verifivation_status = verifivation_status;
    }
}
