package cn.mutils.app.proguard.diff;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class DiffJob {

    private DiffData mData;

    public DiffJob(DiffData data) {
        mData = data;
    }

    public void execute(ProguardConfig config) throws Exception {
        DiffReportData reportData = new DiffReportData(mData,config);
        VelocityEngine ve = new VelocityEngine();
        Properties prop = new Properties();
        prop.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        prop.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        prop.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        ve.init(prop);
        File tplFile = new File("diff.tpl.htm");
        File outFile = new File("proguard.diff.htm");
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            fileReader = new FileReader(tplFile.getPath());
            fileWriter = new FileWriter(outFile.getPath());
            ve.evaluate(reportData.toContext(), fileWriter, "", fileReader);
            System.out.println("Success: " + outFile.getAbsolutePath());
        } finally {
            FileUtil.close(fileReader);
            FileUtil.close(fileWriter);
        }
    }

}
