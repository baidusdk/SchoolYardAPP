package module;

/**
 * 路径收藏夹子项
 */

public class RouteColloctionItem {
    private String mode;//路径规划方式
    private String time;//路径收藏时间
    private String beginLocation;//起点
    private String endLocation;//终点
    private int takeTime;//花费时间
    private int distance;//距离
    private String userAccount;//用户ID，作为后端读取的依据
    private double beginLatitude;//起点纬度
    private double beginLogitude;//起点经度
    private double endLatitude;//终点纬度
    private double endLogitude;//终点经度

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public double getBeginLatitude() {
        return beginLatitude;
    }

    public void setBeginLatitude(double beginLatitude) {
        this.beginLatitude = beginLatitude;
    }

    public double getBeginLogitude() {
        return beginLogitude;
    }

    public void setBeginLogitude(double beginLogitude) {
        this.beginLogitude = beginLogitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLogitude() {
        return endLogitude;
    }

    public void setEndLogitude(double endLogitude) {
        this.endLogitude = endLogitude;
    }

    public RouteColloctionItem(String uAc, String mode, String time, String beginLocation, String endLocation, int takeTime, int distance, double bla, double blo, double ela, double elo) {
        this.userAccount = uAc;
        this.mode = mode;
        this.time = time;
        this.beginLocation = beginLocation;
        this.endLocation = endLocation;
        this.takeTime = takeTime;
        this.distance = distance;
        this.beginLatitude = bla;
        this.beginLogitude = blo;
        this.endLogitude = elo;
        this.endLatitude = ela;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setBeginLocation(String beginLocation) {
        this.beginLocation = beginLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public void setTakeTime(int takeTime) {
        this.takeTime = takeTime;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getMode() {
        return mode;
    }

    public String getTime() {
        return time;
    }

    public String getBeginLocation() {
        return beginLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public int getTakeTime() {
        return takeTime;
    }

    public int getDistance() {
        return distance;
    }
}
