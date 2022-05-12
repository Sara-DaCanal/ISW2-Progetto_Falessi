import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
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
        String[] header = {"Version", "File", "Size", "Commit number", "Loc touched", "Loc added", "Max loc added", "Avg loc added",
                            "Churn", "Max churn", "Avg churn", "Authors numbers"};
        writer.writeNext(header);
        this.path=new CSVList();
        int i=-1, j=0;
        RevCommit oldCommit=null;
        Version myV = versions.get(0);
        while( i< commit.size() && j < versions.size()-1) {
            if(i!=-1) oldCommit = commit.get(i);
            RevCommit newCommit = commit.get(i+1);
            if(new Date((newCommit.getCommitTime()*1000L)).after(myV.getReleaseDate())) {
                for(int m=0; m<path.size(); m++) {
                    CSVLine line = path.get(m);
                    if(!line.getVersion().equals(myV.getName())) line.setVersion(myV.getName());
                    String[] data = line.toStringArray();
                    writer.writeNext(data);
                }
                myV=versions.get(++j);
            }
            String authName = newCommit.getAuthorIdent().getName();
            try (ObjectReader reader = this.git.getRepository().newObjectReader()) {
                AbstractTreeIterator oldTreeIterator = new EmptyTreeIterator();
                if(i!=-1) oldTreeIterator = new CanonicalTreeParser(null, reader, oldCommit.getTree().getId());
                AbstractTreeIterator newTreeIterator = new CanonicalTreeParser(null, reader, newCommit.getTree().getId());
                try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    diffFormatter.setRepository(git.getRepository());
                    this.diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
                    for (DiffEntry entry : this.diffEntries) {
                        LOC locCounter = new LOC(diffFormatter.toFileHeader(entry).toEditList());
                        long size = locCounter.getSize();
                        long locTouched = locCounter.getLOCTouched();
                        long locAdded = locCounter.getLOCAdded();
                        CSVLine newLine = new CSVLine(myV.getName(), entry.getNewPath(), size, locTouched, locAdded);
                        CSVLine oldLine = new CSVLine(myV.getName(), entry.getOldPath(), size, locTouched, locAdded);
                        newLine.addAuthNames(authName);
                        if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
                            CSVLine l = this.path.pathContains(newLine);
                            if(l==null && entry.getNewPath().endsWith(".java"))
                                this.path.add(newLine);
                            else if(l!=null) {
                                l.setVersion(newLine.getVersion());
                                l.addSize(newLine.getSize());
                                l.addLocTouched(newLine.getLocTouch());
                                l.addLoc(newLine.getLocAdded());
                                l.addAuthNames(authName);
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
                                l.addLocTouched(newLine.getLocTouch());
                                l.addLoc(newLine.getLocAdded());
                                l.increaseCommit();
                                l.addAuthNames(authName);
                            }
                        }
                    }
                }
            }
            i++;
        }
        writer.close();
    }

    public CSVList getPath(){
        return this.path;
    }
}


