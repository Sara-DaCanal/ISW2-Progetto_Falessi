import java.util.ArrayList;
import java.util.List;

public class CSVList {

    private List<CSVLine> list;

    public CSVList(){
        this.list=new ArrayList<>();
    }

    public boolean contains(CSVLine myLine){
        for (CSVLine l:this.list) {
            if(myLine.isEqual(l)){
                return true;
            }
        }
        return false;
    }

    public CSVLine pathContains(CSVLine myLine){
        for (CSVLine l:this.list) {
            if(myLine.getPath().contentEquals(l.getPath())){
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
