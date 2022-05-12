import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import java.io.*;
import java.text.ParseException;
import java.util.List;



public class FileReader {

    public static void main(String[] arg) throws GitAPIException, IOException, JSONException, ParseException {
        ParseJSON myJson = new ParseJSON();
        myJson.setProjectName("BOOKKEEPER");
        CommitRetriever commitRetriever = new CommitRetriever("https://github.com/Sara-DaCanal/bookkeeper.git", myJson);
        List<Version> verList = myJson.getVersionArray();
        DiffList diffList = new DiffList(commitRetriever.getCommit(),commitRetriever.getGit(),verList,myJson);
    }

}
