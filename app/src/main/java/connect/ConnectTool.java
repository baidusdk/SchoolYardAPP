package connect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import module.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectTool {


    private Gson g;
    private OkHttpClient client;
    private MediaType JSON;


     public ConnectTool()
    {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        g=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JSON= MediaType.parse("application/json; charset=utf-8");
    }


    String loginUrl;

    //登录使用的连接工具类
    public User login(User user)
    {
        try {
            String json=g.toJson(user);
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder().url(loginUrl).post(body).build();
            Response response = client.newCall(request).execute();
            String temp=response.body().string();
            User u= g.fromJson(temp,User.class);
            if(u==null) {return null;}
            if(u.getAccount()==null||u.getAccount().equals("")){return null;}
            return u;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
