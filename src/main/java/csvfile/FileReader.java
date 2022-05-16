package csvfile;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import java.io.*;
import java.text.ParseException;
import java.util.List;



public class FileReader {



    public static void main(String[] arg) throws GitAPIException, IOException, JSONException, ParseException {
        File table;
        FileWriter tableWriter;
        CSVWriter writer;
        ParseJSON myJson = new ParseJSON();
        myJson.setProjectName("BOOKKEEPER");
        CommitRetriever commitRetriever = new CommitRetriever("https://github.com/Sara-DaCanal/bookkeeper.git", myJson);
        DiffList diffList = new DiffList(commitRetriever.getCommit(),commitRetriever.getGit(),myJson);
        List<CSVList> map = diffList.getPath();
        table = new File("table_"+myJson.getProjectName().toLowerCase()+".csv");
        tableWriter = new FileWriter(table);
        writer = new CSVWriter(tableWriter);
        String[] header = {"CSVFile.Version", "File", "Size", "Commit number", "Loc touched", "Loc added", "Max loc added", "Avg loc added",
                "Churn", "Max churn", "Avg churn", "Authors numbers"};
        writer.writeNext(header);
        for(int m=0; m<map.size(); m++) {
            for (int p = 0; p < map.get(m).size(); p++) {
                CSVLine line = map.get(m).get(p);
                if (!line.getVersion().equals(map.get(m).getVersion().getName()))
                    line.setVersion(map.get(m).getVersion().getName());
                String[] data = line.toStringArray();
                writer.writeNext(data);
            }
        }
        writer.close();
    }

}
