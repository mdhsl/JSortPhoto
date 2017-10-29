package photos.sort;
import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

public class FileVisitor extends SimpleFileVisitor<Path> {

	   private static Calendar calendar = Calendar.getInstance();
	   private static NumberFormat formatter = new DecimalFormat("00");  
	   private static final String logName = "log.log";
	   private static final String NOT_SUPPORTED_NAME="notSupported";
	   
	   private File notSupportedDir=null;
	   
	   private static File logFile = new File(logName);
	   
	   private IMetadataExtractor extractor;
	   
	   private static long nbPhotos=0;
	   
	   private int nbNotSupported = 0;
	   
	   static {
		   if(logFile.exists()) {
			   try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		   }
		  
	   }
	   
	   private File destRootDir;
	   private IFileFilter filter;
	   
	   public FileVisitor(File destRootDir,IFileFilter filter) {
		   this.destRootDir = destRootDir;
		   extractor = new TikaMetaDataExtractor();
		   
		   
		   notSupportedDir = new File(destRootDir+File.separator+NOT_SUPPORTED_NAME);
		   if(!notSupportedDir.exists()) {
			   notSupportedDir.mkdirs();
		   }
		   
		   this.filter = filter;
	   }
	   
	   public FileVisitor(File destRootDir) {
		   this(destRootDir,new FileFilter());
	   }
	   
		@Override
		public FileVisitResult visitFile(Path path,
				BasicFileAttributes basicFileAttributes) throws IOException {
			String log = "";
			File inFile = path.toFile();

			// skip some patterns
			if(this.filter.match(inFile.getAbsolutePath())){
				log = inFile.getAbsolutePath()+"...SKIPPING\n";
				java.nio.file.Files.write(Paths.get(logFile.toURI()), 
						log.getBytes("utf-8"), 
						StandardOpenOption.CREATE, 
						StandardOpenOption.APPEND);

				return CONTINUE;
			}
			
			if (basicFileAttributes.isRegularFile()) {
				
				Date date;
				try {
					date = extractor.extractDate(inFile);
				
					File outFile = getOutFile(date, inFile, destRootDir);
					
					if(outFile != null) {
						
						// check existing
						if(outFile.exists()) {
							long origLength = inFile.length();
							long destLength = outFile.length();
							
							if(origLength < destLength) {
								outFile.mkdirs();
								Files.copy(path, Paths.get(outFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
								log = inFile.getAbsolutePath()+"...OK";
							}  else {
								log = inFile.getAbsolutePath()+"...EXISTS";
							}
						} else {
							outFile.mkdirs();
							Files.copy(path, Paths.get(outFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
							log = inFile.getAbsolutePath()+"...OK";
						}
					}
						
				} catch (MetadataExtractException e) {
					log = inFile.getAbsolutePath()+"...KO";
					System.err.println(e);
					
					String fName = (nbNotSupported++)+"__"+inFile.getName();
					
					Files.copy(
							path, 
							Paths.get(new File(destRootDir+File.separator+NOT_SUPPORTED_NAME+File.separator+fName).toURI()), StandardCopyOption.REPLACE_EXISTING);
				}
				
				log += "\n";
				// log errors
				java.nio.file.Files.write(Paths.get(logFile.toURI()), 
						log.getBytes("utf-8"), 
						StandardOpenOption.CREATE, 
						StandardOpenOption.APPEND);
					
			}
			if(nbPhotos++ % 200 == 0) {
				System.out.println("Number of photos having been processed: "+nbPhotos);
			}
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path path, IOException ioException) {
			System.err.println(ioException);
			return CONTINUE;
		}
		
		public static synchronized File getOutFile(Date date,File inFile, File rootDir) {
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);
			int month= calendar.get(Calendar.MONTH);
			int day= calendar.get(Calendar.DAY_OF_MONTH);
			
			return new File(rootDir.getAbsolutePath()+File.separator+year+File.separator+getMonth(month)+File.separator+formatter.format(day)+"_"+inFile.getName());
		}
		
		public static String getMonth(int number) {
			String month = "Unknown";
			
			switch(number) {
				 case 0 : month = "01_Janvier";break;
				 case 1 : month = "02_Fevrier";break;
				 case 2 : month = "03_Mars";break;
				 case 3 : month = "04_Avril";break;
				 case 4 : month = "05_Mai";break;
				 case 5 : month = "06_Juin";break;
				 case 6 : month = "07_Juillet";break;
				 case 7 : month = "08_Aout";break;
				 case 8 : month = "09_Septembre";break;
				 case 9 : month = "10_Octobre";break;
				 case 10 : month = "11_Novembre";break;
				 case 11 : month = "12_Decembre";break;
			}
			
			return month;
		}
	}


