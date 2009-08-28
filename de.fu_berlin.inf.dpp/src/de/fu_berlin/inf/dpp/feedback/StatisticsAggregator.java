package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Stand-alone tool for taking all statistics files found in a directory and
 * aggregate them into one big tab separated file.
 */
public class StatisticsAggregator {

    private static final Logger log = Logger
        .getLogger(StatisticsAggregator.class);

    protected JFileChooser fc = new JFileChooser();

    public static void main(String[] args) {

        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);

        new StatisticsAggregator().run(args);
    }

    List<Map<String, String>> allData = new ArrayList<Map<String, String>>();

    Set<String> headers = new HashSet<String>();

    public void run(String... args) {

        fc.setDialogTitle("Please select the root folder where"
            + " the session-data files can be found");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File rootFolder = fc.getSelectedFile();

        for (File item : listFiles(rootFolder, "txt")) {

            log.info("Processing " + item);

            Properties data = new Properties();
            try {
                data.load(new FileInputStream(item));
            } catch (Exception e) {
                log.error("Failure to parse statistics: " + item, e);
            }

            Map<String, String> values = new HashMap<String, String>();
            values.put("filename", item.getAbsolutePath());
            headers.add("Filename");
            allData.add(values);

            for (Entry<Object, Object> b : data.entrySet()) {
                String key = b.getKey().toString();
                String value = b.getValue().toString();

                values.put(key, value);
                headers.add(key);
            }
        }

        StringBuilder sb = new StringBuilder();

        List<String> headersSorted = new ArrayList<String>(headers);
        Collections.sort(headersSorted);

        for (String header : headersSorted) {
            sb.append('"').append(StringEscapeUtils.escapeJava(header)).append(
                '"').append('\t');
        }
        sb.append("\n");

        for (Map<String, String> row : allData) {
            for (String header : headersSorted) {

                if (row.containsKey(header)) {
                    sb.append('"').append(
                        StringEscapeUtils.escapeJava(row.get(header))).append(
                        '"');

                }
                sb.append('\t');
            }
            sb.append("\n");
        }

        File output = new File("log/statistics-aggregate.txt");
        try {
            FileUtils.writeStringToFile(output, sb.toString());
        } catch (IOException e) {
            log.error("Could not write statistics aggregation to " + output, e);
        }
        log.info("Output written to " + output);
    }

    @SuppressWarnings("unchecked")
    public Collection<File> listFiles(File rootFolder, String... extensions) {
        return FileUtils.listFiles(rootFolder, extensions, true);
    }
}
