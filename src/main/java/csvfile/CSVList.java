package csvfile;

import java.util.ArrayList;
import java.util.List;

public class CSVList {

    private List<CSVLine> list;
    private Version version;

    public CSVList(){
        this.list=new ArrayList<>();
    }

    public static CSVList copyOf(CSVList list){
        CSVList copy = new CSVList();
        for(int i=0; i< list.size();i++){
            CSVLine l = list.get(i);
            long[] loc = {l.getLocTouch(),l.getLocAdded(),l.getMaxLocAdded(),l.getMaxLocAdded(),l.getChurn(), l.getMaxChurn()
                    ,l.getAvgChurn()};
            copy.add(new CSVLine(l.getVersion(),l.getPath(),l.getSize(),l.getCommitNumber(), loc, new ArrayList<String>(l.getAuthNames())));
        }
        copy.version=list.version;
        return copy;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public boolean contains(CSVLine myLine){
        for (CSVLine l:this.list) {
            if(myLine.isEqual(l)){
                return true;
            }
        }
        return false;
    }

    public CSVLine pathContains(String mypath){
        for (CSVLine l:this.list) {
            if(l.getPath().contentEquals(mypath)){
                return l;
            }
        }
        return null;
    }

    public void add(CSVLine l){
        list.add(l);
    }

    public void addAll(CSVList newList){
        this.list.addAll(newList.list);
    }


    public int getIndex(CSVLine l){
        int i=0;
        while(i<this.list.size()){
            if(this.list.get(i).isEqual(l)) return i;
            i++;
        }
        return -1;
    }

    public int getPathIndex(CSVLine l){
        int i=0;
        while(i<this.list.size()){
            if(this.list.get(i).isPathEqual(l)) return i;
            i++;
        }
        return -1;
    }

    public CSVLine get(int i){
        return this.list.get(i);
    }

    public int size(){
        return this.list.size();
    }

    public void remove(CSVLine l){
        int myIndex = this.getPathIndex(l);
        if(myIndex!=-1)
            this.list.remove(myIndex);
    }

}
