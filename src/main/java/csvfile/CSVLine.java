package csvfile;

import java.util.ArrayList;
import java.util.List;

public class CSVLine {

    private String version;
    private String path;
    private long size;
    private int commitNumber;
    private long locTouch;
    private long locAdded;
    private long maxLocAdded;
    private long avgLocAdded;
    private long churn;
    private long maxChurn;
    private long avgChurn;
    private List<String> authNames;
    private boolean buggy;

    public CSVLine(String version, String path, long size,long locTouch, long locAdded){
        this.version=version;
        this.path=path;
        this.size=size;
        this.commitNumber=1;
        this.locTouch=locTouch;
        this.locAdded = locAdded;
        this.maxLocAdded=locAdded;
        this.avgLocAdded=locAdded;
        this.churn = size;
        this.maxChurn=size;
        this.avgChurn=size;
        this.authNames = new ArrayList<>();
        this.buggy=false;
    }

    public CSVLine(String version, String path, long size, int commitNumber, long[] loc,  List<String> authNames){
        this.version=version;
        this.path=path;
        this.size=size;
        this.commitNumber=commitNumber;
        this.locTouch=loc[0];
        this.locAdded = loc[1];
        this.maxLocAdded=loc[2];
        this.avgLocAdded=loc[3];
        this.churn = loc[4];
        this.maxChurn=loc[5];
        this.avgChurn=loc[6];
        this.authNames = new ArrayList<>();
        this.authNames.addAll(authNames);
        this.buggy=false;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCommitNumber() {
        return commitNumber;
    }

    public void setCommitNumber(int commitNumber) {
        this.commitNumber = commitNumber;
    }

    public long getLocTouch() {
        return locTouch;
    }

    public void setLocTouch(long locTouch){
        this.locTouch = locTouch;
    }

    public long getAvgLocAdded() {
        return avgLocAdded;
    }

    public long getMaxLocAdded() {
        return maxLocAdded;
    }

    public long getLocAdded() {
        return locAdded;
    }

    public void setAvgLocAdded(long avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public void setLocAdded(long locAdded) {
        this.locAdded = locAdded;
    }

    public void setMaxLocAdded(long maxLocAdded) {
        this.maxLocAdded = maxLocAdded;
    }

    public long getChurn() {
        return churn;
    }

    public long getAvgChurn() {
        return avgChurn;
    }

    public long getMaxChurn() {
        return maxChurn;
    }

    public List<String> getAuthNames() {
        return authNames;
    }

    public void addAuthNames(String name) {
        if(!this.authNames.contains(name))
            this.authNames.add(name);
    }

    public String[] toStringArray(){
        return new String[]{this.version, this.path,
                Long.toString(this.size),
                Integer.toString(this.commitNumber),
                Long.toString(this.locTouch),
                Long.toString(this.locAdded),
                Long.toString(this.maxLocAdded),
                Long.toString(this.avgLocAdded),
                Long.toString(this.churn),
                Long.toString(this.maxChurn),
                Long.toString(this.avgChurn),
                Integer.toString(this.authNames.size()),
                Boolean.toString(this.buggy)};

    }

    public boolean isPathEqual(CSVLine l){
        return this.path.contentEquals(l.path);

    }

    public void addSize(long s){
        this.size = this.size + s;
        this.churn=this.churn+Math.abs(s);
        this.avgChurn = this.churn/this.commitNumber;
        if(Math.abs(s)>this.maxChurn) this.maxChurn=Math.abs(s);
    }

    public void increaseCommit(){
        this.commitNumber++;
        this.avgLocAdded = this.locAdded/this.commitNumber;
        this.avgChurn = this.churn/this.commitNumber;
    }
    public void addLocTouched(long l){
        this.locTouch = this.locTouch + l;
    }
    public void addLoc(long l){
        this.locAdded = this.locAdded +l;
        this.avgLocAdded = this.locAdded/this.commitNumber;
        if(l>this.maxLocAdded) this.maxLocAdded=l;
    }
}
