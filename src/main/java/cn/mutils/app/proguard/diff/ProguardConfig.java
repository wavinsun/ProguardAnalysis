package cn.mutils.app.proguard.diff;

import java.util.ArrayList;

public class ProguardConfig {

    public ArrayList<GitSource> sources;
    public ProguardMapping mapping;
    public String proguard;
    public String work_dir;
    public boolean check_git;

    public static class GitSource {
        public String git_root;
        public ArrayList<String> code_dirs;
    }

    public static class ProguardMapping {
        public String root;
        public String split;
        public String tail;
        public String key;
    }
}
