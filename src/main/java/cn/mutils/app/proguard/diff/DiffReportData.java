package cn.mutils.app.proguard.diff;

import org.apache.velocity.VelocityContext;

import java.util.*;

public class DiffReportData {
    private static final int MAX_REPORT_LINES = 10; // 最大输出详情行数

    private String mOldVersion;
    private String mNewVersion;

    List<Map<String, String>> mMainTableData = new ArrayList<Map<String, String>>();
    List<Map<String, Object>> mMainDetailData = new ArrayList<Map<String, Object>>();

    public DiffReportData(DiffData data, ProguardConfig config) {
        mOldVersion = data.oldVersion;
        mNewVersion = data.newVersion;
        init(data, config);
    }

    private void init(DiffData data, ProguardConfig config) {
        int totalOld = 0, totalNew = 0, totalAdd = 0, totalDelete = 0, totalChanged = 0;
        for (int i = 0, size = config.sources.size(); i < size; i++) {
            ProguardConfig.GitSource gitSource = config.sources.get(i);
            MappingFiles.MappingGroup newGroup = data.newMappingFile.groupMap.get(gitSource.git_root);
            MappingFiles.MappingGroup oldGroup = data.oldMappingFile != null ? data.oldMappingFile.groupMap.get(gitSource.git_root) : null;
            int oldCount = oldGroup != null ? oldGroup.unchangedClasses.size() : 0;
            int newCount = newGroup != null ? newGroup.unchangedClasses.size() : 0;
            int addCount = newGroup != null ? newGroup.diffAddUnchangedClasses.size() : 0;
            int deleteCount = newGroup != null ? newGroup.diffDeleteUnchangedClasses.size() : 0;
            int changedCount = newGroup != null ? newGroup.diffChangedUnchangedClasses.size() : 0;
            totalOld += oldCount;
            totalNew += newCount;
            totalAdd += addCount;
            totalDelete += deleteCount;
            totalChanged += changedCount;
            Map<String, String> tableRow = new HashMap<String, String>();
            tableRow.put("MODULE", "<b>" + gitSource.git_root + "</b>");
            tableRow.put("OLD_VERSION", oldCount + "");
            tableRow.put("NEW_VERSION", newCount + "");
            tableRow.put("ADD", addCount + "");
            tableRow.put("DELETE", deleteCount + "");
            tableRow.put("CHANGE", changedCount + "");
            mMainTableData.add(tableRow);
        }
        Map<String, String> tableRow = new HashMap<String, String>();
        tableRow.put("MODULE", "<b>\u603B\u8BA1</b>"); //总计
        tableRow.put("OLD_VERSION", totalOld + "");
        tableRow.put("NEW_VERSION", totalNew + "");
        tableRow.put("ADD", totalAdd + "");
        tableRow.put("DELETE", totalDelete + "");
        tableRow.put("CHANGE", totalChanged + "");
        mMainTableData.add(tableRow);
        for (int i = 0, size = config.sources.size(); i < size; i++) {
            ProguardConfig.GitSource gitSource = config.sources.get(i);
            MappingFiles.MappingGroup newGroup = data.newMappingFile.groupMap.get(gitSource.git_root);
            if (newGroup == null) {
                continue;
            }
            Map<String, Object> detailRowItem = new HashMap<String, Object>();
            List<String> detailRowData = new ArrayList<String>();
            List<ProguardClass> groupClasses = newGroup.unchangedClasses;
            if (newGroup.diffAddUnchangedClasses.size() != 0) {
                groupClasses = new LinkedList<ProguardClass>(groupClasses);
                groupClasses.removeAll(newGroup.diffAddUnchangedClasses);
                groupClasses.addAll(0, newGroup.diffAddUnchangedClasses);
            }
            boolean hasExistsNotAddClass = false; //是否遇到非新增类
            boolean hasMore = false;
            int oldCount = 0;
            for (int j = 0, size4j = groupClasses.size(); j < size4j; j++) {
                ProguardClass proguardClass = groupClasses.get(j);
                boolean isInAddList = false;
                if (!hasExistsNotAddClass) {
                    isInAddList = newGroup.diffAddUnchangedClasses.contains(proguardClass);
                    if (!isInAddList) {
                        hasExistsNotAddClass = true;
                    }
                }
                if (!isInAddList) {
                    oldCount++;
                    if (oldCount > MAX_REPORT_LINES) {
                        hasMore = true;
                        break;
                    }
                }
                StringBuilder sb = new StringBuilder();
                if (isInAddList) {
                    sb.append("<font color='red'><b>[NEW] </b></font>");
                }
                sb.append("<b>");
                sb.append(proguardClass.className);
                sb.append("</b>");
                sb.append(" <input type=\"button\" class=\"__aliyun_at_block_1508935888455 __aliyun_at_block_\" title=\"");
                sb.append(proguardClass.gitInfo.committer.getEmailAddress());
                sb.append("\" value=\" @");
                sb.append(proguardClass.gitInfo.committer.getName());
                sb.append("\" style=\"border:none;cursor:pointer;margin:0 2px 0 0;background-color:transparent;color:#0284C0\">");
                sb.append("<br/> -> ");
                sb.append(gitSource.git_root);
                sb.append("/");
                sb.append(proguardClass.gitInfo.gitPath);
                detailRowData.add(sb.toString());
            }
            detailRowItem.put("NAME", "<font color='blue'>" + newGroup.name + "</font>");
            detailRowItem.put("DATA", detailRowData);
            detailRowItem.put("HAS_MORE", hasMore);
            mMainDetailData.add(detailRowItem);
        }
    }

    public VelocityContext toContext() {
        VelocityContext vc = new VelocityContext();
        vc.put("OLD_VERSION", mOldVersion);
        vc.put("NEW_VERSION", mNewVersion);
        vc.put("MAIN_TABLE_LIST", mMainTableData);
        vc.put("MAIN_DETAIL_LIST", mMainDetailData);
        return vc;
    }


}
