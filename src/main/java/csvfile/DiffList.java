package csvfile;

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        this.versions=myJSON.getVersionArray();
        this.bugList=myJSON.getBugList();
        this.map=new ArrayList<>();
        int i=-1;
        int j=0;
        RevCommit oldCommit=null;
        Version myV = versions.get(0);
        CSVList path = new CSVList();
        path.setVersion(myV);
        while( i< commit.size()-1 && j < versions.size()) {
            if(i!=-1) oldCommit = commit.get(i);
            RevCommit newCommit = commit.get(i+1);
            if(new Date((newCommit.getCommitTime()*1000L)).after(myV.getReleaseDate()) || (i+1)==commit.size()-1) {
                this.map.add(CSVList.copyOf(path));
                newCSVFile(map, j);
                j++;
                if(j < versions.size()){ myV=versions.get(j);
                path.setVersion(myV);}
                else return;
            }
            String authName = newCommit.getAuthorIdent().getName();
            Bug bug = searchBug(newCommit);
            try (ObjectReader reader = this.git.getRepository().newObjectReader()) {
                AbstractTreeIterator oldTreeIterator = new EmptyTreeIterator();
                if(i!=-1) oldTreeIterator = new CanonicalTreeParser(null, reader, oldCommit.getTree().getId());
                AbstractTreeIterator newTreeIterator = new CanonicalTreeParser(null, reader, newCommit.getTree().getId());
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    diffFormatter.setRepository(git.getRepository());
                    this.diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
                    for (DiffEntry entry : this.diffEntries) {
                        CSVLine l = path.pathContains(entry.getNewPath());
                        LOC locCounter=locCounterGenerator(l, entry, myV, diffFormatter);
                        long size = locCounter.getSize();
                        long locTouched = locCounter.getLOCTouched();
                        long locAdded = locCounter.getLOCAdded();
                        CSVLine newLine = new CSVLine(myV.getName(), entry.getNewPath(), size, locTouched, locAdded);
                        CSVLine oldLine = new CSVLine(myV.getName(), entry.getOldPath(), size, locTouched, locAdded);
                        newLine.addAuthNames(authName);
                        changeType(entry,newLine,oldLine,path);
                        labeling(bug, entry);
                    }

            }
            i++;
        }

    }

    public LOC locCounterGenerator(CSVLine l, DiffEntry entry, Version myV, DiffFormatter diffFormatter) throws IOException {
        LOC locCounter;
        if(l!=null && !l.getVersion().equals(myV.getName()))locCounter = new LOC(diffFormatter.toFileHeader(entry).toEditList(), l.getSize());
        else locCounter = new LOC(diffFormatter.toFileHeader(entry).toEditList(), 0);
        return locCounter;
    }

    private void newCSVFile(List<CSVList> map, int j) throws IOException {
        if(j>=versions.size()/2-1) return;
        String path = "./training_"+this.json.getProjectName().toLowerCase(Locale.ROOT)+"_"+(j+1)+".csv";
        File newFile = new File(path);
        FileWriter fileWriter = new FileWriter(newFile);
        CSVWriter writer = new CSVWriter(fileWriter);
        String[] header = {"CSVFile.Version", "File", "Size", "Commit number", "Loc touched", "Loc added", "Max loc added", "Avg loc added",
                "Churn", "Max churn", "Avg churn", "Authors numbers", "buggy"};
        writer.writeNext(header);
        for(int i=0; i<map.size(); i++) {
            for (int k = 0; k < map.get(i).size(); k++) {
                CSVLine line = map.get(i).get(k);
                if (!line.getVersion().equals(map.get(i).getVersion().getName()))
                    line.setVersion(map.get(i).getVersion().getName());
                writer.writeNext(line.toStringArray());
            }
        }writer.close();
    }

    public void labeling(Bug bug, DiffEntry entry){
        if(bug!=null){
            boolean in = false;
            for(int i=0; i<this.map.size(); i++){
                CSVLine l = map.get(i).pathContains(entry.getOldPath());
                if(map.get(i).getVersion().equals(bug.getAffectedVersion())) in = true;
                if(map.get(i).getVersion().equals(bug.getFixedVersion()) || map.get(i).getVersion().getReleaseDate().after(bug.getFixedVersion().getReleaseDate())) in=false;
                if(in && l!=null){
                    l.setBuggy(true);
                }
            }
        }
    }

    private void changeType(DiffEntry entry, CSVLine newLine, CSVLine oldLine, CSVList path){
        CSVLine l = path.pathContains(entry.getNewPath());
        if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
            if(l==null && entry.getNewPath().endsWith(".java") && !entry.getNewPath().contains("/test/"))
                path.add(newLine);
            else if(l!=null) {
                changeLine(l,newLine);
            }
        }
        if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
            path.remove(oldLine);
        }
        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY && l!=null) {
            changeLine(l,newLine);
            if(!l.getVersion().equals(newLine.getVersion()))l.setCommitNumber(1);
            else l.increaseCommit();
        }
    }

    private void changeLine(CSVLine l, CSVLine newLine){
        l.addSize(newLine.getSize());
        if(!l.getVersion().equals(newLine.getVersion())){
            l.setVersion(newLine.getVersion());
            l.setLocAdded(newLine.getLocAdded());
            l.setLocTouch(l.getLocTouch());
        }
        else {
            l.addLocTouched(newLine.getLocTouch());
            l.addLoc(newLine.getLocAdded());
        }
        l.addAuthNames(newLine.getAuthNames().get(0));
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


