package csvfile;

import com.opencsv.CSVWriter;
import weka_analysis.WekaParameters;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;


public class FileReader {



    public static void main(String[] arg) throws Exception {
        File table;
        FileWriter tableWriter;
        CSVWriter writer;
        ParseJSON myJson = new ParseJSON();
        myJson.setProjectName(arg[0].toUpperCase(Locale.ROOT));
        String url = "https://github.com/apache/"+arg[0].toLowerCase()+".git";
        CommitRetriever commitRetriever = new CommitRetriever(url, myJson);
        DiffList diffList = new DiffList(commitRetriever.getCommit(),commitRetriever.getGit(),myJson);
        List<CSVList> map = diffList.getPath();
        table = new File("table_"+myJson.getProjectName().toLowerCase()+".csv");
        tableWriter = new FileWriter(table);
        writer = new CSVWriter(tableWriter);
        String[] header = {"CSVFile.Version", "File", "Size", "Commit number", "Loc touched", "Loc added", "Max loc added", "Avg loc added",
                "Churn", "Max churn", "Avg churn", "Authors numbers", "buggy"};
        writer.writeNext(header);

        for(int m=0; m<map.size()/2; m++) {
            for (int p = 0; p < map.get(m).size(); p++) {
                CSVLine line = map.get(m).get(p);
                if (!line.getVersion().equals(map.get(m).getVersion().getName()))
                    line.setVersion(map.get(m).getVersion().getName());
                String[] data = line.toStringArray();
                writer.writeNext(data);
            }
        }
        writer.close();

        WekaParameters myWeka = new WekaParameters(myJson.getProjectName(), map);
        myWeka.wekaAnalysis();
    }

}
