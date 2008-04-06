package de.fu_berlin.inf.dpp.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import sun.dc.pr.PathStroker;

/**
 * This class contains method to write file into and read file out of zip
 * archive.
 * 
 * @author orieger
 * 
 */
public class FileZipper {

	private static Logger logger = Logger.getLogger(FileZipper.class);

	public static void createZipArchive() throws Exception {

		File dir = new File("/home/troll/test_archiv");
		// archive müssen rekursiv ausgelesen werden.
		File[] files = (dir.listFiles());

		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
				"/home/troll/archive.zip"), new Adler32());
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(cos));

		FileInputStream fis;
		File f;
		int i;

		// for(String path : files){
		for (File path : files) {

			logger.debug("compress file: " + path.toString());

			f = new File(path.getPath());
			if (f.exists()) {

				if (f.isDirectory()) {
					addFolder("",f, zos);
				} else {
					addFile("", f, zos);
				}

				
			} else {
				new FileNotFoundException(path.getName());
			}

		}
		zos.close();
		// checksum
		logger.debug("checksum: " + cos.getChecksum().getValue());
	}

	private static void addFile(String path, File file,
			ZipOutputStream zos) throws Exception {
		FileInputStream fis = null;
		int i;
		
		/* Pfad übergeben*/
		ZipEntry e = new ZipEntry(path + File.separator +file.getName());
		zos.putNextEntry(e);
		

		fis = new FileInputStream(file);
		while ((i = fis.read()) != -1) {
			zos.write(i);
		}
		
		zos.closeEntry();
	}

	private static void addFolder(String path, File file, ZipOutputStream zos) throws Exception{

		File[] files = file.listFiles();
		for(File f : files){
			if(f.isDirectory()){
				logger.debug("compress folder: " + file.getName());
				addFolder(path + File.separator + file.getName(), f, zos);
			}
			else{
				logger.debug("compress file : "+file.getName() + " path "+path);
				addFile(path + File.separator + file.getName(),f,zos);
			}
		}
		
	}

	public static void readZipArchive(String archive) throws Exception {
		CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
				archive), new Adler32());
		ZipInputStream zis = new ZipInputStream(cis);

		String outputFolder = "/home/troll/test_archiv_output";
		new File(outputFolder).delete();
		
		// entpacke archiv
		ZipEntry entry;
		int i = 0;
		while ((entry = zis.getNextEntry()) != null) {
			logger.debug("unzip file: " + entry.getName());
			// zu entpackende Datei im Zielverzeichnis anlegen
			  File directory = new File(entry.getName());
			  directory.mkdirs();
//			FileOutputStream fos = new FileOutputStream(new File(
//					"/home/troll/test_archiv_output", entry.getName()));
			  FileOutputStream fos = new FileOutputStream(entry.getName());
			  
			while ((i = zis.read()) != -1) {
				fos.write(i);
			}
			fos.close();
			zis.closeEntry();
		}
		logger.debug("checksum: " + cis.getChecksum().getValue());
		zis.close();
	}


	public static void createProjectZipArchive(List<IPath> files, String descPath, IProject project)
			throws Exception {
		CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
				descPath), new Adler32());
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(cos));

		File f;

		for (IPath path : files) {
			logger.debug("compress file: " + path);

			f = project.getFile(path).getLocation().toFile();
			if (f.exists()) {

				/* create project path in folder structure. */
				String[] structure = path.segments();
				String path_structure = "";
				for(int j = 0; j< structure.length-1; j++){

					String s = structure[j];
					path_structure += s + File.separator;
				}
				
				addFile(path_structure,f,zos);

			} else {
				new FileNotFoundException(path.toString());
			}

		}
		zos.close();
		// checksum
		logger.debug("checksum: " + cos.getChecksum().getValue());
//		return cos.getChecksum().getValue();
	}

	public static void readInputStreamsProjectArchive(File file) throws Exception{
		ZipFile zip = new ZipFile(file);
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			System.out.println(entry.getName());

		}
	}
	
	public static void readProjectZipArchive() throws Exception {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Projectname");
		Path p = new Path("");
		project.getFile(p);

		CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
				"./archive.zip"), new Adler32());
		ZipInputStream zis = new ZipInputStream(cis);

		// entpacke archiv
		ZipEntry entry;
		int i = 0;
		while ((entry = zis.getNextEntry()) != null) {
			
			logger.debug("unzip file: " + entry.getName());
			// zu entpackende Datei im Zielverzeichnis anlegen
			FileOutputStream fos = new FileOutputStream(new File(
					"zielverzeichns", entry.getName()));
			while ((i = zis.read()) != -1) {
				fos.write(i);
			}
			fos.close();
			zis.closeEntry();
		}
		logger.debug("checksum: " + cis.getChecksum().getValue());
		zis.close();
	}
	
	

}
