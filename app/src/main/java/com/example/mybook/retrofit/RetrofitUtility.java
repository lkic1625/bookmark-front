package com.example.mybook.retrofit;

public class RetrofitUtility {

    private static String BASE_URL = "http://10.0.2.2:8002/";

    public static BookmarkServer getServer() {
        return RetrofitClient.getClient(BASE_URL).create(BookmarkServer.class);
}
}