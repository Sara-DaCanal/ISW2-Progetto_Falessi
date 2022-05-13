import com.opencsv.CSVWriter;
import org.eclipse.jetty.util.PatternMatcher;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffList {
    private List<DiffEntry> diffEntries;
    private List<CSVList> map;
    private Git git;
    private ParseJSON json;
    private List<Bug> bugList;
    private List<Version> versions;

    public DiffList(List<RevCommit> commit, Git git, ParseJSON myJSON) throws IOException, ParseException {
        this.git=git;
        this.json=myJSON;
        this.bugList=myJSON.getBugList();
        this.versions=myJSON.getVersionArray();
        this.map=new ArrayList<>();

        int i=-1, j=0;
        RevCommit oldCommit=null;
        Version myV = versions.get(0);
        CSVList path = new CSVList();
        path.setVersion(myV);
        while( i< commit.size() && j < versions.size()-1) {
            if(i!=-1) oldCommit = commit.get(i);
            RevCommit newCommit = commit.get(i+1);
            if(new Date((newCommit.getCommitTime()*1000L)).after(myV.getReleaseDate())) {
                this.map.add(path);
                path=new CSVList();
                myV=versions.get(++j);
                path.setVersion(myV);
            }
            String authName = newCommit.getAuthorIdent().getName();
            Bug bug = searchBug(newCommit);
            if(bug!=null) System.out.println(i+"----"+bug.getKey()+"\t"+bug.getFixedVersion().getName()+"-"+myV.getName());
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
                            CSVLine l = path.pathContains(newLine);
                            if(l==null && entry.getNewPath().endsWith(".java"))
                                path.add(newLine);
                            else if(l!=null) {
                                l.setVersion(newLine.getVersion());
                                l.addSize(newLine.getSize());
                                l.addLocTouched(newLine.getLocTouch());
                                l.addLoc(newLine.getLocAdded());
                                l.addAuthNames(authName);
                            }
                        }
                        if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                            path.remove(oldLine);
                        }
                        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                            CSVLine l = path.pathContains(newLine);
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

    }

    private Bug searchBug(RevCommit commit){
        if(!commit.getShortMessage().startsWith(json.getProjectName())) return null;
        Pattern pattern = Pattern.compile(json.getProjectName()+"-[0-9]+");
        Matcher matcher = pattern.matcher(commit.getShortMessage());
        while (matcher.find()) {
            String s = matcher.group(0);
            for(Bug b: this.bugList){
                if(b.getKey().equals(s)) return b;
            }
        }
        return null;
    }

    public List<CSVList> getPath(){
        return this.map;
    }
}


