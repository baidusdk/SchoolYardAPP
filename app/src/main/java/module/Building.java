package module;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 楼层类，保存了楼层分布图
 */
public class Building {
    private String buildingName;
    private LatLng point;
    private List<Floor> floorList = new ArrayList<>();//楼层分布图id

    public Building(String buildingName,LatLng p,int underground,int overground,Integer[] floorImgList)
    {
        this.buildingName = buildingName;
        this.point = p;
        this.setFloorList(underground,overground,floorImgList);
    }


    public void setBuildingName(String name)
    {
        this.buildingName = name;
    }
    public void setPoint(LatLng p)
    {
        this.point = p;
    }

    /**
     *
     * @param underground //地下层数
     * @param overground // 地上层数
     */
    public void setFloorList(int underground,int overground,Integer[] floorImg)
    {
        int i=0,j=overground;
        int flag = 0;
        for(i=0;i<floorImg.length;i++)
        {
            if(j!=0&&flag==0) {
                String temp = "F" + String.valueOf(j--);
                Floor floor = new Floor(floorImg[i],temp);
                Log.d("floortest", floor.getFloor());
                floorList.add(floor);
            }
            else
            {
                flag = 1;
                j=1;
            }
            if(j<=underground&&flag==1)
            {
                String temp = "B"+String.valueOf(j++);
                Floor floor = new Floor(floorImg[i],temp);
                floorList.add(floor);
            }

        }
    }



    public String getBuildingName()
    {
        return this.buildingName;
    }

    public LatLng getPoint()
    {
        return  point;
    }

    public List<Floor> getFloorList()
    {
        List<Floor> result = new ArrayList<>();
        int i = 0;
        for(i=0;i<this.floorList.size();i++)
        {
            result.add(floorList.get(i));
        }
        return result;
    }

}
