package cn.mutils.app.proguard.diff;

import java.io.File;
import java.util.*;

public class MappingFiles {

    private List<MappingFile> mFileList = new ArrayList<MappingFile>();
    private Map<String, List<MappingFile>> mFileGroupMap = new HashMap<String, List<MappingFile>>();

    public MappingFiles(ProguardConfig.ProguardMapping config) {
        init(config);
        sort();
    }

    public MappingFile getMapping(String version) {
        if (!version.contains(".")) {
            List<MappingFile> group = mFileGroupMap.get(version);
            if (group == null) {
                return null;
            }
            return group.size() != 0 ? group.get(0) : null;
        }
        for (MappingFile file : mFileList) {
            if (file.version.equals(version)) {
                return file;
            }
        }
        return null;
    }

    private void sort() {
        Collections.sort(mFileList, new Comparator<MappingFile>() {
            @Override
            public int compare(MappingFile o1, MappingFile o2) {
                double diff = o1.versionCode - o2.versionCode;
                if (diff == 0) {
                    return 0;
                }
                return diff > 0 ? -1 : 1;
            }
        });
        for (MappingFile mappingFile : mFileList) {
            List<MappingFile> group = mFileGroupMap.get(mappingFile.versionGroup);
            if (group == null) {
                group = new ArrayList<MappingFile>();
                mFileGroupMap.put(mappingFile.versionGroup, group);
            }
            group.add(mappingFile);
        }
    }

    private void init(ProguardConfig.ProguardMapping config) {
        init(new File(config.root), config);
    }

    private void init(File file, ProguardConfig.ProguardMapping config) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                init(sub, config);
            }
        } else if (file.isFile()) {
            String name = file.getName();
            if (!name.endsWith(config.tail)) {
                return;
            }
            if (!name.contains(config.key)) {
                return;
            }
            String[] strArray = name.split(config.split);
            String version = null;
            for (String str : strArray) {
                if (str.isEmpty()) {
                    continue;
                }
                if (!str.contains(".")) {
                    continue;
                }
                try {
                    Integer.parseInt(str.substring(0, 1));
                    version = str;
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
            if (version == null) {
                return;
            }
            MappingFile mappingFile = new MappingFile();
            mappingFile.version = version;
            mappingFile.file = file;
            String[] verArray = version.split("\\.");
            for (int i = verArray.length - 1, j = 0; i >= 0; i--, j++) {
                if (i == verArray.length - 1) {
                    try {
                        mappingFile.versionCode += Double.parseDouble("0." + verArray[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mappingFile.versionCode += (Integer.parseInt(verArray[i]) * Math.pow(100, j));
                    mappingFile.versionGroup = verArray[i] + mappingFile.versionGroup;
                }
            }
            mFileList.add(mappingFile);
        }
    }

    public static class MappingFile {
        public String version = "";
        public File file;
        public double versionCode;
        public String versionGroup = "";
        public List<ProguardClass> unchangedClasses;
        public Map<String, MappingGroup> groupMap;
    }

    public static class MappingGroup {
        public String name;
        public Map<String, ProguardClass> unchangedClassMap = new HashMap<String, ProguardClass>();
        public List<ProguardClass> unchangedClasses = new ArrayList<ProguardClass>();
        public List<ProguardClass> diffAddUnchangedClasses = new ArrayList<ProguardClass>();
        public List<ProguardClass> diffDeleteUnchangedClasses = new ArrayList<ProguardClass>();
        public List<ProguardClass> diffChangedUnchangedClasses = new ArrayList<ProguardClass>();
    }

}
