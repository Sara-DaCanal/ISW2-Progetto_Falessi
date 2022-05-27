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
                "@attribute auth_number numeric",
                "@attribute class {true, false}"
        };
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
