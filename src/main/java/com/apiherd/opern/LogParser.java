package com.apiherd.opern;

import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogParser {

    private LogParser() {

    }

    public static void writeFile(String line, String name) {
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            File file = new File(name);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(line + "\r\n");
        } catch (IOException e) {

        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
            }
        }
    }

    public static void parseLogs(ArrayList<APILog> logs) {
        parseLogs(logs, "./");
    }

    public static void parseLogs(ArrayList<APILog> logs, String dir) {
        if (0 == logs.size())
            return;

        int errorCount = 0;
        int executionCount = 0;
        long executionTime = 0;
        String timeBegin = logs.get(0).getDateTime();
        StringBuilder sbCSV = new StringBuilder();
        for (APILog log:logs) {
            sbCSV.append("\r\n");
            int posBegin = sbCSV.length() - 1;
            sbCSV.append(log.getDateTime());
            sbCSV.append(",").append(log.getApiName());
            sbCSV.append(",").append(log.getRequestId());
            sbCSV.append(",").append(log.getMRTinMS());
            sbCSV.append(",").append(log.getStatusCode());

            executionCount ++;
            if (log.getStatusCode().equals("false"))
                errorCount ++;
            executionTime += log.getMRTinMS();
            writeFile("Datetime:"+timeBegin+"\r\nRequestId:"+log.getRequestId(), dir+"opernlog.txt");
            writeFile(log.getRequest(), dir+"opernlog.txt");
            writeFile(log.getResponse()+"\r\n", dir+"opernlog.txt");
            System.out.println(sbCSV.substring(posBegin));
        }
        String strCal = "{\"Datetime\":\""+timeBegin+"\",\"TotalRun\":"+executionCount+
                ",\"TimeElapse\":"+executionTime+",\"ErrorRun\":"+errorCount+"}";
        writeFile(strCal, dir+"operngt.txt");
        System.out.println(strCal);
        writeFile(sbCSV.toString(), dir+"operncsv.csv");
    }
}
