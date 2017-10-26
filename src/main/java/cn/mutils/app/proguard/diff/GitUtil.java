package cn.mutils.app.proguard.diff;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Iterator;

public class GitUtil {

    public static PersonIdent getCommitter(Git git, String gitPath) {
        LogCommand logCommand = git.log();
        logCommand.addPath(gitPath);
        logCommand.setMaxCount(1);
        RevCommit commit = null;
        Iterable<RevCommit> iterator = null;
        try {
            iterator = logCommand.call();
        } catch (GitAPIException e) {
            return null;
        }
        if (iterator != null) {
            Iterator<RevCommit> i = iterator.iterator();
            if (i.hasNext()) {
                commit = i.next();
            }
        }
        return commit != null ? commit.getCommitterIdent() : null;
    }

}
