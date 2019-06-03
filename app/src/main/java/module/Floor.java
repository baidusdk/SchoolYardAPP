package module;

public class Floor {
    private Integer floorImgId;
    private String floor;
    public Floor(Integer id,String fl)
    {
        this.floorImgId = id;
        this.floor = fl;
    }
    public void setFloorImgId(Integer id)
    {
        floorImgId = id;
    }
    public void setFloor(String fl)
    {
        this.floor = fl;
    }
    public Integer getFloorImgId()
    {
        return floorImgId;
    }
    public String getFloor()
    {
        return floor;
    }
}
