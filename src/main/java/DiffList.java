import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class DiffList {
    private List<DiffEntry> diffEntries;
    private CSVList path;
    private File table;
    private FileWriter tableWriter;
    private CSVWriter writer;
    private Git git;

    public DiffList(List<RevCommit> commit, Git git, List<Version> versions, ParseJSON myJSON) throws IOException {
        this.git=git;
        table = new File("table_"+myJSON.getProjectName().toLowerCase()+".csv");
        tableWriter = new FileWriter(table);
        writer = new CSVWriter(tableWriter);
        String[] header = {"Version", "File", "Size", "Commit_number"};
        writer.writeNext(header);
        this.path=new CSVList();
        int i=-1, j=0;
        RevCommit oldCommit=null;
        while( i< commit.size() && j < versions.size()) {
            Version myV = versions.get(j);
            if(i!=-1) oldCommit = commit.get(i);
            RevCommit newCommit = commit.get(i+1);
            try (ObjectReader reader = this.git.getRepository().newObjectReader()) {
                AbstractTreeIterator oldTreeIterator = new EmptyTreeIterator();
                if(i!=-1) oldTreeIterator = new CanonicalTreeParser(null, reader, oldCommit.getTree().getId());
                AbstractTreeIterator newTreeIterator = new CanonicalTreeParser(null, reader, newCommit.getTree().getId());
                try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    diffFormatter.setRepository(git.getRepository());
                    this.diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
                    for (DiffEntry entry : this.diffEntries) {
                        long size = this.getSize(diffFormatter.toFileHeader(entry).toEditList());
                        CSVLine newLine = new CSVLine(myV.getName(), entry.getNewPath(), size);
                        CSVLine oldLine = new CSVLine(myV.getName(), entry.getOldPath(), size);
                        if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
                            CSVLine l = this.path.pathContains(newLine);
                            if(l==null && entry.getNewPath().endsWith(".java"))
                                this.path.add(newLine);
                            else if(l!=null) {
                                l.setVersion(newLine.getVersion());
                                l.addSize(newLine.getSize());
                            }
                        }
                        if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                            this.path.remove(oldLine);
                        }
                        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                            CSVLine l = this.path.pathContains(newLine);
                            if(l!=null) {
                                l.setVersion(newLine.getVersion());
                                l.addSize(newLine.getSize());
                                l.increaseCommit();
                            }
                        }
                    }
                }
            }
            if(new Date((newCommit.getCommitTime()*1000L)).after(myV.getReleaseDate())) {
                for(int m=0; m<path.size(); m++) {
                    CSVLine line = path.get(m);
                    if(!line.getVersion().equals(myV.getName())) line.setVersion(myV.getName());
                    String[] data = line.toStringArray();
                    writer.writeNext(data);
                }
                j++;
            }
            i++;
        }
        writer.close();
    }


    public CSVList getPath(){
        return this.path;
    }

    private long getSize(List<Edit> editList){
        int linesDeleted = 0;
        int linesAdded = 0;
        for (Edit edit : editList) {
            linesDeleted += edit.getEndA() - edit.getBeginA();
            linesAdded += edit.getEndB() - edit.getBeginB();
        }
        return linesAdded-linesDeleted;

    }

}


