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

    public RouteColloctionItem(String mode, String time, String beginLocation, String endLocation, int takeTime, int distance) {
        this.mode = mode;
        this.time = time;
        this.beginLocation = beginLocation;
        this.endLocation = endLocation;
        this.takeTime = takeTime;
        this.distance = distance;
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
