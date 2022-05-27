package weka_analysis;

import com.opencsv.CSVReader;
import weka.core.converters.ArffSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CSV2Arff {

    private String fileName;

    public CSV2Arff(String fileName){
        this.fileName=fileName;
    }

    public String converter() throws IOException {


        ArffSaver saver = new ArffSaver();
        String arffPath = fileName.substring(0, fileName.lastIndexOf("."))+".arff";
        saver.setFile(new File(arffPath));
        BufferedWriter writer = saver.getWriter();
        String[] attributes = {
                "@relation " + fileName.substring(2, fileName.lastIndexOf(".")),
                "@attribute size numeric",
                "@attribute commit_number numeric",
                "@attribute loc_touched numeric",
                "@attribute loc_added numeric",
                "@attribute max_loc_added numeric",
                "@attribute avg_loc_added numeric",
                "@attribute churn numeric",
                "@attribute max_churn numeric",
                "@attribute avg_churn numeric",
                "@attribute auth_number numeric",
                "@attribute class {true, false}"
        };
        for(String s:attributes){
            writer.write(s);
            writer.newLine();
        }
        writer.newLine();
        writer.write("@data");
        writer.newLine();

        try(CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            String[] line;
            csvReader.readNext();
            while ((line = csvReader.readNext()) != null) {
                StringBuilder bld = new StringBuilder();
                bld.append(line[2]);
                for (int i = 3; i < line.length; i++) bld.append("," + line[i]);
                writer.write(bld.toString());
                writer.newLine();
            }
        }
            writer.flush();
            writer.close();
        // load CSV
        return arffPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
