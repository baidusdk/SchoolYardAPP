/**
 * 文件名：BookmarksActivity
 * 描述：路径收藏夹的内容
 * 作者：恒达
 * 时间：2019/4/25 19：00
 */
package com.hd.app;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BookmarksActivity extends AppCompatActivity {
    private MyAdapter adapter;
    //尚未设置  等待填入
    private String URL = "";
    private int httpCode;
    private String responseData;
    private RecyclerView mRecyclerView;
    //路径ID  全部String
    private int intBookmarksNumber;
    //目的地
    private String[] destinationNameArray;
    //出发地
    private String[] departNameArray;
    //耗时
    private String[] useTimeArray;
    //收录时间
    private String[] markTimeArray;
    //交通方式
    private String[] wayOfVehicleArray;
    //距离
    private String[] distanceArray;

    List<ContactInfo> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        mRecyclerView = (RecyclerView) findViewById(R.id.card_list);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        /**
         * 初始化适配器
         */
        adapter = new MyAdapter(mList);
        mRecyclerView.setAdapter(adapter);
        //发送POST
        sendRequestWithOkHttp();
        //解析post后的东西
//        parseJSONWithJSONObject(responseData);
////实例化MyAdapter并传入mList对象
//        initInfo();
//        adapter = new MyAdapter(mList);
////为RecyclerView对象mRecyclerView设置adapter
//        mRecyclerView.setAdapter(adapter);
    }

    //创建构造函数
    public class ContactInfo {
        protected String destination;
        protected String departName;
        protected String useTime;
        protected String markTime;
        protected String wayOfVehical;
        protected String distance;

        /**
         * 构造函数，一个卡片内信息展示如下
         */

        public ContactInfo(String destination, String departName, String useTime, String markTime, String wayOfVehicle, String distance) {
            this.departName = departName;
            this.destination = destination;
            this.distance = distance;
            this.useTime = useTime;
            this.markTime = markTime;
            this.wayOfVehical = wayOfVehicle;
        }
    }


    public class MyAdapter extends RecyclerView.Adapter
            <MyAdapter.ContactViewHolder> { //MyAdapter类 开始

        //MyAdapter的成员变量contactInfoList, 这里被我们用作数据的来源
        private List<ContactInfo> contactInfoList;

        //MyAdapter的构造器
        public MyAdapter(List<ContactInfo> contactInfoList) {
            this.contactInfoList = contactInfoList;
        }

        public void addData(List<ContactInfo> contacts) {
            this.contactInfoList = contacts;
            notifyDataSetChanged();
        }

        //重写3个抽象方法
        //onCreateViewHolder()方法 返回我们自定义的 ContactViewHolder对象
        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_bookmarks_cardview, parent, false);
            return new ContactViewHolder(itemView);
        }

        /**
         * 创造衔接变量，传到xml；
         *
         * @param holder   xml需要的信息可以通过holder.xxx传送。
         * @param position
         */
        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {

            //contactInfoList中包含的都是ContactInfo类的对象
            //通过其get()方法可以获得其中的对象
            ContactInfo ci = contactInfoList.get(position);
            //方式
            holder.vVehicle.setText(ci.wayOfVehical + " - ");
            //耗时
            holder.vUseTime.setText(ci.useTime + " - ");
            //j距离
            holder.vDistance.setText(ci.distance);
            //出发地
            holder.vDepart.setText(ci.departName);
            //目的地
            holder.vDestination.setText(ci.destination);
            //时间戳
            holder.vTime.setText(ci.markTime);
            //设置删除按钮监听事件
            holder.iDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //···
                }
            });
        }

        //此方法返回列表项的数目
        @Override
        public int getItemCount() {
            return contactInfoList.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            private TextView vDepart;
            private TextView vDestination;
            private TextView vTime;
            private TextView vVehicle;
            private TextView vUseTime;
            private TextView vDistance;
            private ImageButton iDelete;

            /**
             * 将参数传给xml内
             *
             * @param itemView
             */
            public ContactViewHolder(View itemView) {
                super(itemView);
                vTime = itemView.findViewById(R.id.tv_time);
                vUseTime = itemView.findViewById(R.id.tv_use_time);
                vVehicle = itemView.findViewById(R.id.tv_vehicle);
                vDepart = itemView.findViewById(R.id.tv_depart);
                vDestination = itemView.findViewById(R.id.tv_destination);
                vDistance = itemView.findViewById(R.id.tv_distance);
                //加入删除键
                iDelete = itemView.findViewById(R.id.ib_delete);
            }
        }
    }

    private void initInfo() {
        ContactInfo[] elementArray = new ContactInfo[intBookmarksNumber];
//        测试数据
//        ContactInfo [] element =new  ContactInfo[10];  之后用数组
        /**
         * 初始化
         */
        for (int i = intBookmarksNumber - 1; i >= 0; i--) {
            elementArray[i] = new ContactInfo(destinationNameArray[i], departNameArray[i], useTimeArray[i], markTimeArray[i], wayOfVehicleArray[i], distanceArray[i]);
            mList.add(elementArray[i]);
        }
    }

    /**
     * 解析并赋值
     *
     * @param jsonData
     */
    private void parseJSONWithJSONObject(String jsonData) {

        try {
            {
                JSONObject jsonObject = new JSONObject(jsonData);
                /**
                 * 记录个数
                 */
                intBookmarksNumber = Integer.parseInt(jsonObject.getString("userNum"));
                Log.d("userNumber", Integer.toString(intBookmarksNumber));
                /**
                 * 开数组,讲json内容读入
                 */
                departNameArray = new String[intBookmarksNumber];
                destinationNameArray = new String[intBookmarksNumber];
                distanceArray = new String[intBookmarksNumber];
                markTimeArray = new String[intBookmarksNumber];
                useTimeArray = new String[intBookmarksNumber];
                wayOfVehicleArray = new String[intBookmarksNumber];

                /**
                 * 构造record数组
                 */
                String[] userArray;
                userArray = new String[intBookmarksNumber];
                for (int i = 0; i < intBookmarksNumber; i++) {
                    //user1 ,user2..
                    userArray[i] = " user " + Integer.toString(i);
                }

                Log.d("ABCD", userArray[0]);
                for (int i = 0; i < intBookmarksNumber; i++) {
                    String message;
                    message = jsonObject.getString(userArray[i]);
                    JSONObject messageRecordX = new JSONObject(message);
                    destinationNameArray[i] = messageRecordX.getString("destinationName");
                    departNameArray[i] = messageRecordX.getString("departNameArray");
                    /**
                     店名、食堂名
                     */
                    distanceArray[i] = messageRecordX.getString("distance");
                    useTimeArray[i] = messageRecordX.getString("useTime");
                    markTimeArray[i] = messageRecordX.getString("markTime");
                    wayOfVehicleArray[i] = messageRecordX.getString("vehicle");
                    //截取时间
                    //timeArray[i] = messageTimeX.getString("date").substring(0, 19);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法名：sendRequestWithOkHttp
     * 功能： post用户ID，返回需要的数据：1.交通方式。2.耗时。3.距离。4.目的地。5.出发地。6.收藏时间戳
     * 参数
     * 返回值：无
     */
    private void sendRequestWithOkHttp() {

        try {
            //读取用户ID和token
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            String user_id = pref.getString("id", "0");
            //  String token = pref.getString("token", "0");//没有token 待删。
            Log.d("userId:", user_id);
            //  Log.d("token:", token);
            User user = new User();
            user.setUser_id(user_id);
            //user.setToken(token);
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();
            String toJson = gson.toJson(user);//转换json
            Log.d("JSON", toJson);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), toJson);
            Request request = new Request.Builder()
                    .url(URL)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("onFailure", "fail");
                }

                /**
                 * 获取6个内容。
                 * @param call
                 * @param response
                 * @throws IOException
                 */
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //异步回调，获取内容
                    responseData = response.body().string();
                    httpCode = response.code();
                    parseJSONWithJSONObject(responseData);
                    //实例化MyAdapter并传入mList对象
                    initInfo();
                    runOnUiThread(() -> adapter.addData(mList));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class User {
        private String id;
//        private String token;
//
//        public String getToken() {
//
//            return token;
//        }

//        public void setToken(String token) {
//            this.token = token;
//        }

        public String getUser_id() {
            return id;
        }

        public void setUser_id(String user_id) {
            this.id = user_id;
        }
    }
}