package cn.mutils.app.proguard.diff;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitRepositories {

    private ProguardConfig mConfig;
    private List<GitRepository> mGitRepositoryList = new ArrayList<GitRepository>();

    public GitRepositories(ProguardConfig config) {
        init(config);
    }

    private void init(ProguardConfig config) {
        mConfig = config;
        for (ProguardConfig.GitSource gitSource : mConfig.sources) {
            try {
                Repository repository = new RepositoryBuilder().setGitDir(new File(mConfig.work_dir +
                        "/" + gitSource.git_root + "/.git")).readEnvironment().findGitDir().build();
                String branch = repository.getBranch();
                if (branch == null || branch.isEmpty()) {
                    continue;
                }
                System.out.println("Debug: " + gitSource.git_root + " -> " + repository.getBranch());
                Git git = new Git(repository);
                GitRepository rep = new GitRepository();
                rep.git = git;
                rep.repository = repository;
                rep.gitSource = gitSource;
                mGitRepositoryList.add(rep);
            } catch (IOException e) {
                if (config.check_git) {
                    throw new RuntimeException(e);
                }
                e.printStackTrace();
            }
        }
        if (config.check_git) {
            if (mGitRepositoryList.size() != config.sources.size()) {
                throw new RuntimeException("Git repository is not ready!");
            }
        }
    }

    public void updateGitInfo(List<ProguardClass> proguardClasses) {
        for (ProguardClass proguardClass : proguardClasses) {
            String classFilePath = toClassFilePath(proguardClass.className);
            for (GitRepository repository : mGitRepositoryList) {
                for (String code_dir : repository.gitSource.code_dirs) {
                    File classFile = new File(mConfig.work_dir + "/" + repository.gitSource.git_root + "/" + code_dir + "/" + classFilePath);
                    if (!classFile.exists()) {
                        continue;
                    }
                    ProguardClass.ProguardGitInfo gitInfo = new ProguardClass.ProguardGitInfo();
                    gitInfo.filePath = classFile.getAbsolutePath();
                    gitInfo.gitPath = code_dir + "/" + classFilePath;
                    gitInfo.gitDir = repository.gitSource.git_root;
                    gitInfo.committer = GitUtil.getCommitter(repository.git, gitInfo.gitPath);
                    proguardClass.gitInfo = gitInfo;
                    System.out.println("Debug: " + proguardClass.className + " -> " + gitInfo.committer);
                    break;
                }
                if (proguardClass.gitInfo != null) {
                    break;
                }
            }
        }
    }

    private static String toClassFilePath(String className) {
        int indexOf$ = className.indexOf('$');
        String classFilePath = indexOf$ != -1 ? className.substring(0, indexOf$) : className;
        classFilePath = classFilePath.replaceAll("\\.", "/") + ".java";
        return classFilePath;
    }

    public static class GitRepository {
        Repository repository;
        Git git;
        ProguardConfig.GitSource gitSource;
    }

}
