package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Stand-alone tool for taking all statistics files found in a directory and
 * aggregate them into one big tab separated file.
 */
public class StatisticsAggregator {

    private static final Logger log = Logger
        .getLogger(StatisticsAggregator.class);

    /**
     * FileChooser used for selecting input and output directories
     */
    protected JFileChooser fc = new JFileChooser();

    public static void main(String[] args) {

        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);

        new StatisticsAggregator().run(args);
    }

    public void run(String... args) {

        fc.setDialogTitle("Please select the root folder where"
            + " the session-data files can be found");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File rootFolder = fc.getSelectedFile();

        // Set of all column headers
        Set<String> headers = new HashSet<String>();

        headers.add("filename");

        log.info("Building file list in directory " + rootFolder);

        Collection<File> files = listFiles(rootFolder, "txt");

        if (files.size() == 0) {
            MessageDialog.openError(null, "No statistics files found",
                "The folder\n" + rootFolder
                    + "\n does not contain any txt files.");
            return;
        }

        // 1st pass: Build headers
        Collection<File> secondPassFiles = new ArrayList<File>(files.size());
        int failures = 0;

        for (File file : files) {

            log.info("Processing " + file);

            Properties data = new Properties();
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    data.load(fileInputStream);
                } finally {
                    fileInputStream.close();
                }
            } catch (Exception e) {
                log.error("Failure to parse statistics: " + file, e);
                failures++;
                continue;
            }

            for (Object key : data.keySet()) {
                headers.add(key.toString());
            }

            secondPassFiles.add(file);
        }

        StringBuilder sb = new StringBuilder();

        List<String> headersSorted = new ArrayList<String>(headers);
        Collections.sort(headersSorted);

        for (String header : headersSorted) {
            sb.append(header.replaceAll("\\s", " ")).append('\t');
        }
        sb.append("\n");

        // 2nd pass: Output data in correct order
        for (File file : secondPassFiles) {

            Properties data = new Properties();
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    data.load(fileInputStream);
                } finally {
                    fileInputStream.close();
                }
            } catch (Exception e) {
                log.error("Failure to parse statistics: " + file, e);
                failures++;
                continue;
            }

            data.put("filename", file.getAbsolutePath());

            for (String header : headersSorted) {

                if (data.containsKey(header)) {
                    sb.append(data.get(header).toString()
                        .replaceAll("\\s", " "));
                }
                sb.append('\t');
            }
            sb.append("\n");
        }

        if (failures > 0) {
            MessageDialog.openWarning(null, "Errors reading statistics files",
                "There were " + failures
                    + " files which could not be parsed.\n"
                    + "Check the command line output.");
        }

        fc.setDialogTitle("Please select the log file to save the results to");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        returnVal = fc.showSaveDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            log.info("Saving statistics aggregate to disk canceled by user");
            return;
        }

        File output = fc.getSelectedFile();
        try {
            FileUtils.writeStringToFile(output, sb.toString());
        } catch (IOException e) {
            log.error("Could not write statistics aggregation to " + output, e);
        }
        log.info("Output written to " + output.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    public Collection<File> listFiles(File rootFolder, String... extensions) {
        return FileUtils.listFiles(rootFolder, extensions, true);
    }
}
