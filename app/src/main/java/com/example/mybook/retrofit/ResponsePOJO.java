package com.example.mybook.retrofit;

public class ResponsePOJO
{
    private String code;

    private String payload;

    private String message;

    private String token;

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getPayload ()
    {
        return payload;
    }

    public void setPayload (String payload)
    {
        this.payload = payload;
    }

    public String getMessage ()
    {
        return message;
    }

    public void setMessage (String message)
    {
        this.message = message;
    }

    public String getToken ()
    {
        return token;
    }

    public void setToken (String token)
    {
        this.token = token;
    }

    @Override
    public String toString()
    {
        return "res [code = "+code+", payload = "+payload+", message = "+message+", token = "+token+"]";
    }
}