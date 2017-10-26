package cn.mutils.app.proguard.diff;


import java.util.TreeMap;

public class DiffData {

    public static final String UNKNOWN_GIT_NAME = "UNKNOWN";

    public String oldVersion;
    public String newVersion;
    public MappingFiles.MappingFile oldMappingFile;
    public MappingFiles.MappingFile newMappingFile;

    public DiffData(String oldVersion, String newVersion, MappingFiles.MappingFile oldMappingFile, MappingFiles.MappingFile newMappingFile) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.oldMappingFile = oldMappingFile;
        this.newMappingFile = newMappingFile;
        updateDiffInfo();
    }

    private void updateDiffInfo() {
        initGroupMap(newMappingFile);
        if (oldMappingFile == null) {
            return;
        }
        initGroupMap(oldMappingFile);
        for (MappingFiles.MappingGroup newGroup : newMappingFile.groupMap.values()) {
            MappingFiles.MappingGroup oldGroup = oldMappingFile.groupMap.get(newGroup.name);
            for (ProguardClass proguardClass : newGroup.unchangedClasses) {
                if (oldGroup == null || !oldGroup.unchangedClassMap.containsKey(proguardClass.className)) {
                    newGroup.diffAddUnchangedClasses.add(proguardClass);
                    continue;
                }
            }
        }
        for (MappingFiles.MappingGroup oldGroup : oldMappingFile.groupMap.values()) {
            MappingFiles.MappingGroup newGroup = newMappingFile.groupMap.get(oldGroup.name);
            if (newGroup == null) {
                continue;
            }
            for (ProguardClass proguardClass : oldGroup.unchangedClasses) {
                if (!newGroup.unchangedClassMap.containsKey(proguardClass.className)) {
                    newGroup.diffDeleteUnchangedClasses.add(proguardClass);
                }
            }
        }
        for (MappingFiles.MappingGroup newGroup : newMappingFile.groupMap.values()) {
            newGroup.diffChangedUnchangedClasses.addAll(newGroup.diffAddUnchangedClasses);
            newGroup.diffChangedUnchangedClasses.addAll(newGroup.diffDeleteUnchangedClasses);
        }
    }

    private static void initGroupMap(MappingFiles.MappingFile mappingFile) {
        mappingFile.groupMap = new TreeMap<String, MappingFiles.MappingGroup>();
        for (ProguardClass proguardClass : mappingFile.unchangedClasses) {
            String groupName = proguardClass.gitInfo != null ? proguardClass.gitInfo.gitDir : UNKNOWN_GIT_NAME;
            MappingFiles.MappingGroup group = mappingFile.groupMap.get(groupName);
            if (group == null) {
                group = new MappingFiles.MappingGroup();
                group.name = groupName;
                mappingFile.groupMap.put(groupName, group);
            }
            group.unchangedClasses.add(proguardClass);
            group.unchangedClassMap.put(proguardClass.className, proguardClass);
        }
    }

}
