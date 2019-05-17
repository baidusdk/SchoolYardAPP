package module;

import java.io.Serializable;

/**
 * 路径类
 */

public class Point implements Serializable {
    private Double Latitude;    //纬度
    private Double Longitude;    //经度


    public Point(Double latitude, Double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

}

