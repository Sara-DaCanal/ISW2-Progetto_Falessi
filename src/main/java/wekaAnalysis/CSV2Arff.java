package wekaAnalysis;

import csvfile.CSVList;
import weka.core.converters.ArffSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CSV2Arff {

    private String fileName;
    private List<CSVList> map;

    public CSV2Arff(String fileName, List<CSVList> map){
        this.fileName=fileName;
        this.map=map;
    }

    public String converter() throws IOException {


        ArffSaver saver = new ArffSaver();
        String arffPath = fileName.substring(0, fileName.lastIndexOf("."))+".arff";
        saver.setFile(new File(arffPath));
        BufferedWriter writer = saver.getWriter();
        writer.write("@relation " + fileName.substring(2, fileName.lastIndexOf(".")));
        writer.newLine();
        writer.write("@attribute size numeric");
        writer.newLine();
        writer.write("@attribute commit_number numeric");
        writer.newLine();
        writer.write("@attribute loc_touched numeric");
        writer.newLine();
        writer.write("@attribute loc_added numeric");
        writer.newLine();
        writer.write("@attribute max_loc_added numeric");
        writer.newLine();
        writer.write("@attribute avg_loc_added numeric");
        writer.newLine();
        writer.write("@attribute churn numeric");
        writer.newLine();
        writer.write("@attribute max_churn numeric");
        writer.newLine();
        writer.write("@attribute avg_churn numeric");
        writer.newLine();
        writer.write("@attribute auth_number numeric");
        writer.newLine();
        writer.write("@attribute class {true, false}");
        writer.newLine();
        writer.newLine();
        writer.write("@data");
        writer.newLine();

        for(CSVList l:map){
            for(int i=0; i<l.size(); i++){
                String[] a = l.get(i).toStringArray();
                String s = "";
                for(int j=2; j<a.length-1; j++) s=s+a[j]+",";
                s=s+a[a.length-1];
                writer.write(s);
                writer.newLine();
            }
        }
        writer.close();
        // load CSV
        return arffPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
