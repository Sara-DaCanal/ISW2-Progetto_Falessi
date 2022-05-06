import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiffList {
    private List<DiffEntry> diffEntries;
    private List<List<String>> path;

    public DiffList(List<RevCommit> commit, Git git, List<Version> versions) throws IOException {
        this.path=new ArrayList<>();
        int i=-1;
        for(int j=0; j<versions.size();j++) {
            this.path.add(new ArrayList<>());
            if(j!=0) this.path.get(j).addAll(this.path.get(j-1));
            RevCommit oldCommit=null;
            if(i!=-1) oldCommit = commit.get(i);
            RevCommit newCommit = commit.get(i+1);
            do {
                try (ObjectReader reader = git.getRepository().newObjectReader()) {
                    AbstractTreeIterator oldTreeIterator = new EmptyTreeIterator();
                    if(i!=-1) oldTreeIterator = new CanonicalTreeParser(null, reader, oldCommit.getTree().getId());
                    AbstractTreeIterator newTreeIterator = new CanonicalTreeParser(null, reader, newCommit.getTree().getId());
                    try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                        diffFormatter.setRepository(git.getRepository());
                        this.diffEntries = diffFormatter.scan(oldTreeIterator, newTreeIterator);
                        for (int k = 0; k < this.diffEntries.size(); k++) {
                            DiffEntry entry = this.diffEntries.get(k);
                            if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
                                if(!this.path.contains(entry.getNewPath()) && entry.getNewPath().endsWith(".java"))
                                this.path.get(j).add(entry.getNewPath());
                            }
                            if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                                this.path.get(j).remove(entry.getOldPath());
                            }
                            if (entry.getChangeType() == DiffEntry.ChangeType.RENAME || entry.getChangeType() == DiffEntry.ChangeType.MODIFY || entry.getChangeType() == DiffEntry.ChangeType.COPY) {
                                this.path.get(j).remove(entry.getOldPath());
                                if(!this.path.contains(entry.getNewPath()) && entry.getNewPath().endsWith(".java"))
                                this.path.get(j).add(entry.getNewPath());
                            }
                        }
                    }
                }
                i++;
                oldCommit = commit.get(i);
                newCommit = commit.get(i + 1);
            }while((i< commit.size()-1) && (new Date((newCommit.getCommitTime()*1000L)).before(versions.get(j).getReleaseDate())));
        }
    }

    public List<List<String>> getPath(){
        return this.path;
    }
}


