package cn.mutils.app.proguard.diff;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 2) {
            System.out.println("Usage: oldVersion newVersion");
            return;
        }
        String oldVersion = args[0];
        String newVersion = args[1];
        File proguardConfig = new File("proguard.json");
        if (!proguardConfig.exists()) {
            if (!proguardConfig.exists()) {
                System.out.println("Error: " + proguardConfig.getAbsolutePath() + " is not exists.");
                return;
            }
        }
        String proguardJson = FileUtil.getString(proguardConfig);
        ProguardConfig config = new Gson().fromJson(proguardJson, ProguardConfig.class);
        MappingFiles mappingFiles = new MappingFiles(config.mapping);
        MappingFiles.MappingFile oldMapping = mappingFiles.getMapping(oldVersion);
        System.out.println("Debug: mapping file for old version -> " + (oldMapping != null ? oldMapping.file.getAbsolutePath() : "null"));
        MappingFiles.MappingFile newMapping = mappingFiles.getMapping(newVersion);
        if (newMapping == null) {
            System.out.println("Error: mapping file for new version is not exists!");
            return;
        }
        System.out.println("Debug: mapping file for new version -> " + (newMapping != null ? newMapping.file.getAbsolutePath() : "null"));
        GitRepositories gitRepositories = new GitRepositories(config);
        if (oldMapping != null) {
            oldMapping.unchangedClasses = ProguardUtil.transformUnchangedInfo(ProguardUtil.pump(oldMapping.file));
            gitRepositories.updateGitInfo(oldMapping.unchangedClasses);
        }
        if (newMapping != null) {
            newMapping.unchangedClasses = ProguardUtil.transformUnchangedInfo(ProguardUtil.pump(newMapping.file));
            gitRepositories.updateGitInfo(newMapping.unchangedClasses);
        }
        if (oldMapping != null) {
            printProguardList(oldMapping.unchangedClasses, new File("mapping_log_" + oldVersion + ".txt"));
        }
        printProguardList(newMapping.unchangedClasses, new File("mapping_log_" + newVersion + ".txt"));
        DiffData diffData = new DiffData(oldVersion, newVersion, oldMapping, newMapping);
        printProguardGroup(newMapping, config, new File("mapping_module_log.txt"));
        new DiffJob(diffData).execute(config);
    }

    private static void printProguardList(List<ProguardClass> list, File out) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            for (int i = 0, size = list.size(); i < size; i++) {
                ProguardClass proguardClass = list.get(i);
                writer.write((i + 1) + ". ");
                writer.write(proguardClass.className);
                if (proguardClass.gitInfo != null) {
                    writer.write(" -> [");
                    writer.write(proguardClass.gitInfo.committer.getName());
                    writer.write("<");
                    writer.write(proguardClass.gitInfo.committer.getEmailAddress());
                    writer.write(">] : ");
                    writer.write(proguardClass.gitInfo.gitDir);
                    writer.write("/");
                    writer.write(proguardClass.gitInfo.gitPath);
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(writer);
        }
    }

    private static void printProguardGroup(MappingFiles.MappingFile mappingFile, ProguardConfig config, File out) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            for (int i = 0, size = config.sources.size(); i < size; i++) {
                ProguardConfig.GitSource gitSource = config.sources.get(i);
                writer.write((i + 1) + ". ");
                writer.write(gitSource.git_root);
                writer.write("  \n");
                MappingFiles.MappingGroup newGroup = mappingFile.groupMap.get(gitSource.git_root);
                if (newGroup == null) {
                    continue;
                }
                List<ProguardClass> groupClasses = newGroup.unchangedClasses;
                if (newGroup.diffAddUnchangedClasses.size() != 0) {
                    groupClasses = new LinkedList<ProguardClass>(groupClasses);
                    groupClasses.removeAll(newGroup.diffAddUnchangedClasses);
                    groupClasses.addAll(0, newGroup.diffAddUnchangedClasses);
                }
                for (ProguardClass proguardClass : groupClasses) {
                    writer.write("  * ");
                    writer.write(proguardClass.className);
                    writer.write(" [");
                    writer.write(proguardClass.gitInfo.committer.getName());
                    writer.write("<");
                    writer.write(proguardClass.gitInfo.committer.getEmailAddress());
                    writer.write(">]");
                    writer.write("  \n");
                    writer.write("    -> ");
                    writer.write(proguardClass.gitInfo.gitDir);
                    writer.write("/");
                    writer.write(proguardClass.gitInfo.gitPath);
                    writer.write("  \n\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(writer);
        }
    }

}
