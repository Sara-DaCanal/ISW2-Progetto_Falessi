import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONException;
import java.io.*;
import java.lang.module.ModuleDescriptor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



public class FileReader {

    public static void main(String[] arg) throws GitAPIException, IOException, JSONException, ParseException {
        File table = new File("./table.csv");
        FileWriter tableWriter = new FileWriter(table);
        CSVWriter writer = new CSVWriter(tableWriter);
        ParseJSON myJson = new ParseJSON();
        myJson.setProjectName("SYNCOPE");
        CommitRetriever commitRetriever = new CommitRetriever("https://github.com/apache/syncope.git");
        List<Version> verList = myJson.getVersionArray();
        /*for(int i =0;i<verList.size();i++) {
            System.out.println(verList.get(i).getReleaseDate()+" "+verList.get(i).getFirstName()+" "+verList.get(i).getSecondName());
       }
       for(RevCommit c: commitRetriever.getCommit()){
           System.out.println(new Date(c.getCommitTime()*1000L));
       }*/
        DiffList diffList = new DiffList(commitRetriever.getCommit(),commitRetriever.getGit(),verList);
        int i =0;
        String[] header = {"Version", "File"};
        writer.writeNext(header);
        while(i < verList.size()){
            for(String s: diffList.getPath().get(i)){
                String[] data = {verList.get(i).getName(), s};
                writer.writeNext(data);
            }
            i++;
        }
        writer.close();
      /*  AbstractTreeIterator newTreeIterator = new FileTreeIterator( commitRetriever.getGit().getRepository() );
        AbstractTreeIterator oldTreeIterator = new DirCacheIterator( commitRetriever.getGit().getRepository().readDirCache() );
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(commitRetriever.getGit().getRepository());
            List<DiffEntry> diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
            System.out.println(diffEntries.isEmpty());
        }
        String[] header = {"Versione", "File"};
        tableCsv.writeNext(header);
      /*  for (ModuleDescriptor.Version ver:orderedVer
             ) {
            writeNames(ver, log.listFiles(), tableCsv);
        }

        tableCsv.close();*/

    }

    public static void writeNames(ModuleDescriptor.Version ver, File[] files, CSVWriter writer) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                writeNames(ver, file.listFiles(), writer); // Calls same method again.
            } else {
                if(file.getName().endsWith(".java") && !file.getName().contains("Test")) {
                    String[] data = {ver.toString(), file.getPath().substring(11)};
                    writer.writeNext(data);
                }
            }
        }
    }



}
