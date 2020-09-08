package com.example.mybook.retrofit;

public class UserModel
{
    private String user_pw;

    private String user_email;

    private String user_id;

    private String user_name;

    private String user_profile_uri;

    public String getUser_pw ()
    {
        return user_pw;
    }

    public void setUser_pw (String user_pw)
    {
        this.user_pw = user_pw;
    }

    public String getUser_email ()
    {
        return user_email;
    }

    public void setUser_email (String user_email)
    {
        this.user_email = user_email;
    }

    public String getUser_id ()
    {
        return user_id;
    }

    public void setUser_id (String user_id)
    {
        this.user_id = user_id;
    }

    public String getUser_name ()
    {
        return user_name;
    }

    public void setUser_name (String user_name)
    {
        this.user_name = user_name;
    }

    public String getUser_profile_uri ()
    {
        return user_profile_uri;
    }

    public void setUser_profile_uri (String user_profile_uri)
    {
        this.user_profile_uri = user_profile_uri;
    }

    @Override
    public String toString()
    {
        return " [user_pw = "+user_pw+", user_email = "+user_email+", user_id = "+user_id+", user_name = "+user_name+", user_profile_uri = "+user_profile_uri+"]";
    }
}