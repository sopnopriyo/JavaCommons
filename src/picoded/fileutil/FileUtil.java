package picoded.fileutil;

//java incldues
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//apache includes
import org.apache.commons.lang3.StringUtils;

///
/// Small extension of apache FileUtil, for some additional features that we needed.
/// Additionally several FilenameUtils is made avaliable here
///
/// @See https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtil.html
///
public class FileUtil extends org.apache.commons.io.FileUtils {
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// JavaCommons extensions
	//
	//------------------------------------------------------------------------------------------------------------------
	
	///
	/// List only the folders inside a folder
	///
	/// @param folder to scan
	///
	/// @return Collection of folders within the current folder
	///
	public static Collection<File> listDirs(File inFile) {
		List<File> ret = new ArrayList<File>();
		if(inFile== null){
			return ret;
		}
		for (File f : inFile.listFiles()) {
			if (f.isDirectory()) {
				ret.add(f);
			}
		}
		
		return ret;
	}
	
	/// Overwrites null encoding with US-ASCII
	public static String readFileToString(File inFile) throws IOException {
		return picoded.fileutil.FileUtil.readFileToString(inFile, (String) null);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static String readFileToString(File inFile, String encoding) throws IOException {
		if (encoding == null) {
			encoding = "US-ASCII";
		}
		return org.apache.commons.io.FileUtils.readFileToString(inFile, encoding);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static void writeStringToFile(File inFile, String data) throws IOException {
		picoded.fileutil.FileUtil.writeStringToFile(inFile, data, (String) null);
	}
	
	/// Overwrites null encoding with US-ASCII
	public static void writeStringToFile(File inFile, String data, String encoding) throws IOException {
		if (encoding == null) {
			encoding = "US-ASCII";
		}
		org.apache.commons.io.FileUtils.writeStringToFile(inFile, data, encoding);
	}
	
	///
	/// Extends the readFileToString to include a "fallback" default value,
	/// which is used if the file does not exists / is not readable / is not a
	/// file
	///
	/// @param file to read
	/// @param fallback return value if file is invalid
	/// @param encoding mode
	///
	/// @returns the file value if possible, else returns the fallback value
	///
	public static String readFileToString_withFallback(File inFile, String fallback) {
		return picoded.fileutil.FileUtil.readFileToString_withFallback(inFile, fallback, null);
	}
	
	///
	/// Extends the readFileToString to include a "fallback" default value,
	/// which is used if the file does not exists / is not readable / is not a
	/// file
	///
	/// @param file to read
	/// @param fallback return value if file is invalid
	/// @param encoding mode
	///
	/// @returns the file value if possible, else returns the fallback value
	///
	public static String readFileToString_withFallback(File inFile, String fallback, String encoding) {
		if (inFile == null || !inFile.exists() || !inFile.isFile() || !inFile.canRead()) {
			return fallback;
		}
		
		try {
			return picoded.fileutil.FileUtil.readFileToString(inFile, encoding);
		} catch (IOException e) {
			return fallback;
		}
	}
	
	///
	/// Write to file only if it differs
	///
	/// @param file to write
	/// @param value to write
	/// @param encoding mode
	///
	/// @returns the boolean indicating true if file was written to
	///
	public static boolean writeStringToFile_ifDifferant(File inFile, String data, String encoding) throws IOException {
		String original = readFileToString_withFallback(inFile, "", encoding);
		if (original.equals(data)) {
			return false;
		}
		
		writeStringToFile(inFile, data, encoding);
		return true;
	}
	
	///
	/// Write to file only if it differs
	///
	/// @param file to write
	/// @param value to write
	///
	/// @returns the boolean indicating true if file was written to
	///
	public static boolean writeStringToFile_ifDifferant(File inFile, String data) throws IOException {
		return picoded.fileutil.FileUtil.writeStringToFile_ifDifferant(inFile, data, null);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param folder to scan and copy from
	///
	public static void copyDirectory_ifDifferent(File inDir, File outDir) throws IOException {
		copyDirectory_ifDifferent(inDir, outDir, true);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param folder to scan and copy from
	///
	public static void copyDirectory_ifDifferent(File inDir, File outDir, boolean preserveFileDate) throws IOException {
		copyDirectory_ifDifferent(inDir, outDir, preserveFileDate, false); //default symlink is false : This is considered advance behaviour
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param folder to scan and copy from
	///
	public static void copyDirectory_ifDifferent(File inDir, File outDir, boolean preserveFileDate,
		boolean tryToUseSymLink) throws IOException{
		if (inDir == null || outDir == null) {
			new IOException("Invalid directory");
		}
		File[] dir_inDir = inDir.listFiles();
		for (int i = 0; i < dir_inDir.length; i++) {
			File infile = dir_inDir[i];
			if (infile.isFile()) {
				File outfile = new File(outDir, infile.getName());
				copyFile_ifDifferent(infile, outfile, preserveFileDate, tryToUseSymLink);
				
			} else if (infile.isDirectory()) {
				File newOutDir = new File(outDir.getAbsolutePath() + File.separator + infile.getName());
				newOutDir.mkdir();
				copyDirectory_ifDifferent(infile, newOutDir, preserveFileDate, tryToUseSymLink);
			}
		}
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param file to scan and copy from
	///
	public static void copyFile_ifDifferent(File inFile, File outFile) throws IOException {
		copyFile_ifDifferent(inFile, outFile, true);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param file to scan and copy from
	///
	public static void copyFile_ifDifferent(File inFile, File outFile, boolean preserveFileDate) throws IOException {
		 //default symlink is false :This is considered advance behaviour
		copyFile_ifDifferent(inFile, outFile, preserveFileDate, false);
	}
	
	///
	/// Recursively copy all directories, and files only if the file content is different
	///
	/// @param file to scan and copy from
	///
	public static void copyFile_ifDifferent(File inFile, File outFile, boolean preserveFileDate, boolean tryToUseSymLink)
		throws IOException {
			// Checks if the output file is already a symbolic link
			// And if its valid. And since both is pratically the same 
			// final file when linked, the file is considered "not different"
			//------------------------------------------------------------
			if (Files.isSymbolicLink(outFile.toPath())) {
				// Gets the symbolic link source file path, and checks if it points to source file.
				// See: http://stackoverflow.com/questions/29368308/java-nio-how-is-path-issamefile-different-from-path-equals
				// for why is `Files.isSameFile()` used
				if (Files.isSameFile(Files.readSymbolicLink(outFile.toPath()), inFile.toPath())) {
					// If it points to the same file, the symbolic link is valid
					// No copy operations is required.
					return;
				}
			}
			
			// Tries to build symlink if possible, hopefully
			if (tryToUseSymLink) {
				// NOTE: You do not test source file for symbolic link
				// Only the detination file should be a symbolic link.
				//------------------------------------------------------------
				//if(!Files.isSymbolicLink(inFile.toPath())){
				//	Files.createSymbolicLink(Paths.get(inFile.getAbsolutePath()), Paths.get(outFile.getAbsolutePath()), new FileAttribute<?>[0]);
				//}
				
				// Assumes output file is either NOT a symbolic link
				// or has the wrong symbolic link reference.
				//
				// Creates a symbolic link of the outfile, 
				// relative to the in file (if possible)
				//------------------------------------------------------------
				Files.createSymbolicLink(outFile.toPath().toAbsolutePath(), inFile.toPath().toAbsolutePath());
			}
			// Silence the error 
			// Uses fallback behaviour of copying the file if it occurs
		
		// Checks if file has not been modified, and has same data length, for skipping?
		//---------------------------------------------------------------------------------
		if (inFile.lastModified() == outFile.lastModified() && inFile.length() == outFile.length()) {
			// returns and skip for optimization
			return;
		}
		
		// Final fallback behaviour, copies file if content differs.
		//---------------------------------------------------------------------------------
		if (!FileUtil.contentEqualsIgnoreEOL(inFile, outFile, null)) {
			copyFile(inFile, outFile, preserveFileDate);
		}
	}
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// JavaCommons time utility functions
	//
	//------------------------------------------------------------------------------------------------------------------
	
	///
	/// Recursively scan for the newest file
	///
	/// @param folder to scan
	/// @param List of names to skip
	///
	/// @return The newest timestamp found, else 0 if failed
	///
	public static long newestFileTimestamp(File inFile, List<String> excludeNames) {
		if(inFile.isDirectory()) {
			long retTimestamp = 0L;
			
			for (File f : inFile.listFiles()) {
				
				if(excludeNames != null) {
					String baseName = f.getName();
					if( excludeNames.contains(baseName) ) {
						continue;
					}
				}
				
				long tmpTimestamp = 0L;
				if (f.isDirectory()) {
					tmpTimestamp = newestFileTimestamp(f, excludeNames);
				} else if(f.isFile()) {
					tmpTimestamp = f.lastModified();
				}
				
				if( tmpTimestamp > retTimestamp ) {
					retTimestamp = tmpTimestamp;
				}
			}
			
			return retTimestamp;
		} else {
			return inFile.lastModified();
		}
	}
	
	///
	/// Recursively scan for the newest file
	///
	/// @param folder to scan
	/// @param List of names to skip
	///
	/// @return The newest timestamp found, else 0 if failed
	///
	public static long newestFileTimestamp(File inFile) {
		return newestFileTimestamp(inFile, null);
	}
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// FilenameUtils functions
	//
	//------------------------------------------------------------------------------------------------------------------
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getBaseName(java.lang.String)
	/// @param raw file name/path
	/// @return filename only without the the type extension
	public static String getBaseName(String filename) {
		return org.apache.commons.io.FilenameUtils.getBaseName(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getExtension(java.lang.String)
	/// @param raw file name/path
	/// @return filename type extension
	public static String getExtension(String filename) {
		return org.apache.commons.io.FilenameUtils.getExtension(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getFullPath(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String getFullPath(String filename) {
		return org.apache.commons.io.FilenameUtils.getFullPath(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getFullPathNoEndSeparator(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path without ending / for directories
	public static String getFullPathNoEndSeparator(String filename) {
		return org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getName(java.lang.String)
	/// @param raw file name/path
	/// @return filename without the path
	public static String getName(String filename) {
		return org.apache.commons.io.FilenameUtils.getName(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getPath(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String getPath(String filename) {
		return org.apache.commons.io.FilenameUtils.getPath(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#getPathNoEndSeparator(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path without ending / for directories
	public static String getPathNoEndSeparator(String filename) {
		return org.apache.commons.io.FilenameUtils.getPathNoEndSeparator(filename);
	}
	
	/// @see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#normalize(java.lang.String)
	/// @param raw file name/path
	/// @return full resolved path with ending / for directories
	public static String normalize(String filename) {
		return org.apache.commons.io.FilenameUtils.normalize(filename);
	}
	
	//------------------------------------------------------------------------------------------------------------------
	//
	// Items below here, requires cleanup : not considered stable
	//
	//------------------------------------------------------------------------------------------------------------------
	
	///
	/// Get List only the files path
	///
	/// @param folder to scan
	/// @param raw File inFile
	///
	/// @return Collection of files Path within the current folder
	///
	public static Collection<String> getFilePaths(File inFile) {
		return getFilePaths(inFile, null, null);
	}
	
	///
	/// Get List only the files path
	///
	/// @param folder to scan
	/// @param raw File inFile
	/// @param raw String separator
	///
	/// @return Collection of files Path within the current folder
	///
	public static Collection<String> getFilePaths(File inFile, String separator) {
		return getFilePaths(inFile, separator, null);
	}
	
	///
	/// Get List only the files path
	///
	/// @param folder to scan
	/// @param raw File inFile
	/// @param raw String separator
	/// @param raw String folderPrefix
	///
	/// @return Collection of files Path within the current folder
	///
	public static Collection<String> getFilePaths(File inFile, String separator, String folderPrefix) {
		List<String> keyList = new ArrayList<String>();
		//check folder Prefix is not empt
		if (StringUtils.isEmpty(folderPrefix)) {
			folderPrefix = "";
		}
		
		if (StringUtils.isEmpty(separator)) {
			separator = "/";
		}
		
		if (inFile.isDirectory()) {
			File[] innerFiles = inFile.listFiles();
			for (File innerFile : innerFiles) {
				if (innerFile.isDirectory()) {
					String parentFolderName = innerFile.getName();
					if (!folderPrefix.isEmpty()) {
						parentFolderName = folderPrefix + separator + parentFolderName;
					}
					keyList.addAll(getFilePaths(innerFile, parentFolderName, separator));
				} else {
					keyList.addAll(getFilePaths(innerFile, folderPrefix, separator));
				}
			}
		} else {
			String fileName = inFile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			String prefix = "";
			if (!folderPrefix.isEmpty()) {
				prefix += folderPrefix + separator;
			}
			
			keyList.add(prefix + fileName);
		}
		
		return keyList;
	}
}
