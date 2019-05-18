package module;

import java.io.Serializable;


public class Picture implements Serializable {
    private static final long serialVersionUID = 1L; //经度
    private byte[] bitmapBytes = null;    //图片的byte[]形式
    private String name = null;    //图片名
    private String url;    //与后端进行传输的url


    public void setBitmapBytes(byte[] bitmapBytes) {
        this.bitmapBytes = bitmapBytes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getBitmapBytes() {
        return this.bitmapBytes;
    }

    public String getName() {
        return this.name;
    }

    public Picture(byte[] bitmapBytes, String name) {

        this.bitmapBytes = bitmapBytes;
        this.name = name;
    }


    public Picture() {

    }

}
