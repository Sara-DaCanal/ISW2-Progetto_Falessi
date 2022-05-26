package weka_analysis;

import com.opencsv.CSVReader;
import csvfile.CSVList;
import weka.core.converters.ArffSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

        CSVReader csvReader = new CSVReader(new FileReader(fileName));
        String[] line;
        while ((line = csvReader.readNext())!=null){
            String s = line[2];
            for(int i=3; i<line.length; i++)  s = s+","+line[i];
            writer.write(s);
            writer.newLine();
        }

        writer.close();
        // load CSV
        return arffPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
