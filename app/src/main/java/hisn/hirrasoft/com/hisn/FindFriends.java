package hisn.hirrasoft.com.hisn;

public class FindFriends {
    public FindFriends(){

    }

    public FindFriends(String profile_avatar, String status, String username) {
        this.profile_avatar = profile_avatar;
        this.status = status;
        this.username = username;
    }

    public String getProfile_avatar() {
        return profile_avatar;
    }

    public void setProfile_avatar(String profile_avatar) {
        this.profile_avatar = profile_avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String profile_avatar, status, username;
}
