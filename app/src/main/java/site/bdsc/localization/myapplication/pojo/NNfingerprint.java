package site.bdsc.localization.myapplication.pojo;


public class NNfingerprint {
    private int rpid;
    private int subid;
    private float Xaxis;
    private float Yaxis;
    private String WiFiFeatures;
    private String MAC;
    private String timeStamp;
    private String magReading;

    public void NNfingerprint(){

    }
    public int getRpid() {
        return rpid;
    }

    public void setRpid(int rpid) {
        this.rpid = rpid;
    }

    public int getSubid() {
        return subid;
    }

    public void setSubid(int subid) {
        this.subid = subid;
    }

    public float getXaxis() {
        return Xaxis;
    }

    public void setXaxis(float xaxis) {
        Xaxis = xaxis;
    }

    public float getYaxis() {
        return Yaxis;
    }

    public void setYaxis(float yaxis) {
        Yaxis = yaxis;
    }

    public String getWiFiFeatures() {
        return WiFiFeatures;
    }

    public void setWiFiFeatures(String wiFiFeatures) {
        WiFiFeatures = wiFiFeatures;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMagReading() {
        return magReading;
    }

    public void setMagReading(String magReading) {
        this.magReading = magReading;
    }

    @Override
    public String toString() {
        return "NNfingerprint{" +
                "rpid=" + rpid +
                ", subid=" + subid +
                ", Xaxis=" + Xaxis +
                ", Yaxis=" + Yaxis +
                ", WiFiFeatures='" + WiFiFeatures + '\'' +
                ", MAC='" + MAC + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", magReading=" + magReading +
                '}';
    }
}
