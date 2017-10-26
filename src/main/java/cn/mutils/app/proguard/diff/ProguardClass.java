package cn.mutils.app.proguard.diff;

import org.eclipse.jgit.lib.PersonIdent;

import java.util.ArrayList;
import java.util.List;

public class ProguardClass {

    public String className;
    public String newClassName;
    public String simpleClassName;
    public String newSimpleClassName;
    public List<ProguardField> fields = new ArrayList<ProguardField>();
    public List<ProguardMethod> methods = new ArrayList<ProguardMethod>();
    public boolean unchangedClassName;
    public List<ProguardField> unchangedFields = new ArrayList<ProguardField>();
    public List<ProguardMethod> unchangedMethods = new ArrayList<ProguardMethod>();
    public ProguardGitInfo gitInfo;

    public static class ProguardField {
        public String fieldType;
        public String filedName;
        public String newFieldName;
    }

    public static class ProguardMethod {
        public int fistLineNumber;
        public int lastLineNumber;
        public String methodReturnType;
        public String methodName;
        public String methodArguments;
        public String newMethodName;
    }

    public static class ProguardGitInfo {
        public String gitDir;
        public String gitPath;
        public String filePath;
        public PersonIdent committer;
    }
}
