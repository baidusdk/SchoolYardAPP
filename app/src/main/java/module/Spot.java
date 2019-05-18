package module;

import android.graphics.Bitmap;


/**
 * 景点类
 */
public class Spot  {
    private String spotID;
    private String spotName;
    //private Bitmap spotImg;//景点图
    private double latitude;    //纬度
    private double longitude;    //经度
    private int imgID;

    public Spot(String id, String name, double la, double lo,int iID)
    {
        this.spotID = id;
        this.spotName = name;
        //this.spotImg = img;
        this.latitude = la;
        this.longitude = lo;
        this.imgID = iID;
    }

    public String getSpotID()
    {
        return spotID;
    }
    public String getSpotName()
    {
        return spotName;
    }
//    public Bitmap getSpotImg()
//    {
//        return spotImg;
//    }
    public double getLatitude()
    {
        return latitude;
    }
    public double getLongitude()
    {
        return longitude;
    }
    public int getImgID(){
        return imgID;
    }
    public void setSpotID(String id)
    {
        this.spotID = id;
    }
    public void setSpotName(String name)
    {
        this.spotName = name;
    }
    public void setImgID(int iID){
        this.imgID= iID;
    }
//    public void setSpotImg(Bitmap img)
//    {
//        this.spotImg = img;
//    }
    public void setSpotLatitude(double la)
    {
        this.latitude = la;
    }
    public void setSpotLongitude(double lo)
    {
        this.longitude = lo;
    }

}
