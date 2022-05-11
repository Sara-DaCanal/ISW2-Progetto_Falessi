import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommitRetriever {
    private Git git;
    private File repo;
    private Iterable<RevCommit> itCommit;
    private List<RevCommit> commit;

    public CommitRetriever(String url, ParseJSON myJSON) throws IOException, GitAPIException {
        repo = new File("./"+myJSON.getProjectName().toLowerCase());
        try {
            this.git = Git.cloneRepository().setURI(url).setDirectory(this.repo).setCloneAllBranches(true).call();
        } catch (JGitInternalException e){
            this.git = Git.open(this.repo);
            this.git.pull().call();
        }
        this.itCommit = this.git.log().call();
        invert(this.itCommit);
    }

    public Git getGit(){
        return this.git;
    }

    public List<RevCommit> getCommit(){
        return this.commit;
    }

    private void invert(Iterable<RevCommit> com){
        this.commit = new ArrayList<>();
        for (RevCommit c:com) {
            this.commit.add(c);
        }
        int len = this.commit.size();
        int j=len-1;
        for (int i=0; i < len/2; i++){
            RevCommit app = this.commit.get(i);
            this.commit.set(i, this.commit.get(j));
            this.commit.set(j, app);
            j--;
        }
    }
}
