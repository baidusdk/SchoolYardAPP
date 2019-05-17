package module;

import android.util.Log;

/**
 * 景点具体信息类
 */
public class SpotInformation {
    private String spotID;
    private String spotName;
    private Integer[] spotImageID;
    private String spotInformation;
    public SpotInformation(String id,String name,Integer [] arrry,String sIn)
    {
        this.spotID = id;
        this.spotName = name;
        int i = 0;
        int k = arrry.length;
        spotImageID = new Integer[k];
        for( i=0;i<k;i++)
        {
            this.spotImageID[i]=arrry[i];
            String s = String.valueOf(arrry[i]);
            Log.d("tttttt", s);
        }
        this.spotInformation = sIn;
    }
    public Integer[] getSpotImageID()
    {
        Integer [] result = new Integer[this.spotImageID.length];
        int i = 0;
        for(i=0;i<this.spotImageID.length;i++)
        {
            result[i]=this.spotImageID[i];
        }
        return result;
    }
    public String getSpotID()
    {
        return this.spotID;
    }
    public String getSpotName()
    {
        return this.spotName;
    }
    public String getSpotInformation()
    {
        return this.spotInformation;
    }
    public void setSpotID(String i)
    {
        this.spotID = i;
    }
    public void setSpotName(String n){
        this.spotName = n;
    }
    public void setSpotInformation(String in)
    {
        this.spotInformation = in;
    }
    public void setSpotImageID(Integer [] array)
    {
        int i = 0;
        for(i=0;i<array.length;i++)
        {
            this.spotImageID[i]=array[i];
        }
    }

}
