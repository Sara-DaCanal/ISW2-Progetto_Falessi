import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;

public class GitLogReader {

    public static void main(String[] arg) throws GitAPIException, IOException {
        File log = new File("./log");
        Git git;
        try {
            git = Git.cloneRepository().setURI("https://github.com/Sara-DaCanal/prova_maven.git").setDirectory(log).setCloneAllBranches(true).call();
        } catch (JGitInternalException e){
            git = Git.open(log);
            git.pull().call();
        }
        Iterable<RevCommit> commit;
        commit = git.log().call();
        RevCommit x;
        while ((x = commit.iterator().next())!=null) {
            System.out.println(x.getCommitterIdent().getName());
            System.out.println(x.getCommitterIdent().getWhen().toString());
            System.out.println(x.getId().toString());
            System.out.println(x.getShortMessage());
            System.out.println("-----");
        }
    }
}
