/**
 * 文件名：BookmarksActivity
 * 描述：路径收藏夹的内容
 * 作者：恒达
 * 时间：2019/4/25 19：00
 */
package com.hd.app;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
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
    private String URL = "http://47.102.156.224/api/favorites";
    private int httpCode;
    private String responseData;
    private RecyclerView mRecyclerView;
    //路径ID  全部String
    //个数
    private int intRecordNumber;
    //json数组
    private String record;

    private String accountId;
    //数据库主码ID
    private int[] favoriteIdArray;
    //目的地
    private String[] destinationNameArray;
    //出发地
    private String[] departNameArray;
    //耗时
    private int[] useTimeArray;
    //收录时间
    private String[] markTimeArray;
    //交通方式
    private String[] wayOfVehicleArray;
    //距离
    private int[] distanceArray;
    //出发地经纬度，注意类型
    private double[] departLaitudeArray;

    private double[] departLongitudeArray;
    //目的地经纬度
    private double[] destinationLatitudeArray;

    private double[] destinationLongitudeArray;
    private TextView mtitle;
    private String TAG = "BookmarksActivity";
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
        mtitle= findViewById(R.id.title_name);
        mtitle.setText("路径收藏夹");
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
        protected int useTime;
        protected String markTime;
        protected String wayOfVehicle;
        protected int distance;


        /**
         * 构造函数，一个卡片内信息展示如下
         */

        public ContactInfo(String destination, String departName, int useTime, String markTime, String wayOfVehicle, int distance) {
        this.departName = departName;
        this.destination = destination;
        this.distance = distance;
        this.useTime = useTime;
        this.markTime = markTime;
        this.wayOfVehicle = wayOfVehicle;
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
            holder.vVehicle.setText(ci.wayOfVehicle );
            //耗时
            holder.vUseTime.setText(ci.useTime + "分钟");
//            //出发地
           holder.vDepart.setText(ci.departName);
//            //目的地
            holder.vDestination.setText(ci.destination);
//            //时间戳
           holder.vTime.setText(ci.markTime);
           holder.vDistance.setText(ci.distance+"米");
            //设置删除按钮监听事件
            holder.iDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });
            /**
             * 增添cardView
             */
            holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
            RelativeLayout mRelativeLayout;



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
                mRelativeLayout = itemView.findViewById(R.id.path_background);
            }
        }
    }

    private void initInfo() {
        ContactInfo[] elementArray = new ContactInfo[intRecordNumber];
//        测试数据
//        ContactInfo [] element =new  ContactInfo[10];  之后用数组
        /**
         * 初始化
         */
        for (int i = intRecordNumber - 1; i >= 0; i--) {
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
                JSONObject jsonObject1 = new JSONObject(jsonObject.getString("data"));
                /**
                 * 记录个数
                 */
                accountId = jsonObject1.getString("account");
                Log.d("accountId",jsonObject.getString("data"));
                intRecordNumber = jsonObject1.getInt("recordNum");
                record = jsonObject1.getString("record");
                /**
                 * 开数组,讲json内容读入
                 */
                favoriteIdArray=new int[intRecordNumber];
                departNameArray = new String[intRecordNumber];
                destinationNameArray = new String[intRecordNumber];
                distanceArray = new int[intRecordNumber];
                markTimeArray = new String[intRecordNumber];
                useTimeArray = new int[intRecordNumber];
                wayOfVehicleArray = new String[intRecordNumber];
                departLaitudeArray = new double[intRecordNumber];
                departLongitudeArray = new  double[intRecordNumber];
                destinationLatitudeArray = new double[intRecordNumber];
                destinationLongitudeArray = new double[intRecordNumber];

                /**
                 * 定义JSON中的JSON数组
                 */
                JSONArray jsonArray = new JSONArray(record);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject messageRecordX = (JSONObject)jsonArray.get(i);
                    //赋值
                    favoriteIdArray[i]=messageRecordX.getInt("favorite_id");
                    destinationNameArray[i] = messageRecordX.getString("destinationName");
                    departNameArray[i] = messageRecordX.getString("departName");
                    distanceArray[i] = messageRecordX.getInt("distance");
                    useTimeArray[i] = messageRecordX.getInt("usingTime");
                    markTimeArray[i] = messageRecordX.getString("time");
                    wayOfVehicleArray[i] = messageRecordX.getString("vehicle");
                    departLaitudeArray[i] = messageRecordX.getDouble("departLatitude");
                    departLongitudeArray[i]= messageRecordX.getDouble("departLongitude");
                    destinationLongitudeArray[i] = messageRecordX.getDouble("destinationLongitude");
                    destinationLatitudeArray[i]=messageRecordX.getDouble("destinationLatitude");

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
           // SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
           //  String user_id = pref.getString("id", "0");
            String account = "031602313";
            //  String token = pref.getString("token", "0");//没有token 待删。
            Log.d("userId:", account);
            //  Log.d("token:", token);
            User user = new User();
            user.setUser_id(account);
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addData(mList);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class User {
        private String account;
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
            return account;
        }

        public void setUser_id(String user_id) {
            this.account = user_id;
        }
    }
}