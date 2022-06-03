package weka_analysis;

import com.opencsv.CSVWriter;
import csvfile.CSVList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestingSet {
    private static int index=1;
    private String projectName;
    private static TestingSet me = null;
    private List<CSVList> map;

    private TestingSet(String name, List<CSVList> map){
        this.projectName=name;
        this.map=map;
    }

    public String testingSetCreator() throws IOException {
        String path = "./file_"+projectName+"/test_"+projectName+"_"+index+".csv";
        File newFile = new File(path);
        FileWriter fileWriter = new FileWriter(newFile);
        CSVWriter writer = new CSVWriter(fileWriter);
        String[] header = {"CSVFile.Version", "File", "Size", "Commit number", "Loc touched", "Loc added", "Max loc added", "Avg loc added",
                "Churn", "Max churn", "Avg churn", "Authors numbers", "buggy"};
        writer.writeNext(header);
        for(int i=0;i<map.get(index).size();i++){
            writer.writeNext(map.get(index).get(i).toStringArray());
        }
        writer.close();
        return path;
    }

    public static TestingSet getInstance(String name, List<CSVList> map){
        if(me==null) me = new TestingSet(name, map);
        else index++;
        return me;
    }
}
