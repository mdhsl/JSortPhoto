package photos.sort;
import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileMetadataDirectory;

public class SimpleFileVisitorExample extends SimpleFileVisitor<Path> {

	   private static Calendar calendar = Calendar.getInstance();
	   private static NumberFormat formatter = new DecimalFormat("00");  
	   private static final String logName = "log.log";
	   
	   private static File logFile = new File(logName);
	   
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
	   
	   public SimpleFileVisitorExample(File destRootDir) {
		   this.destRootDir = destRootDir;
		   
	   }
	   
		@Override
		public FileVisitResult visitFile(Path path,
				BasicFileAttributes basicFileAttributes) throws IOException {
			String error = "";
			
			try {
				if (basicFileAttributes.isRegularFile()) {
					File inFile = path.toFile();

					if(inFile.getName().startsWith("android")) {
						return CONTINUE;
					}
					
					FileType fileType = FileTypeDetector.detectFileType(new BufferedInputStream(new FileInputStream(inFile)));
					File outFile = null;
					
					if (fileType == FileType.Jpeg) {
							Metadata metadata  = ImageMetadataReader.readMetadata(inFile);
							// obtain the Exif directory
							ExifSubIFDDirectory directory
							    = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		
							// query the tag's value
							if(directory != null) {
								Date date
								    = directory.getDateDigitized(TimeZone.getDefault());
								if(date != null) {
									outFile = getOutFile(date,inFile,destRootDir);	
								} else {
									error = "File not supported or is in error: "+path.toFile();
								}
								
							} else {
								error = "File not supported or is in error: "+path.toFile();
							}
						} else if (fileType == FileType.Nef || fileType == FileType.Tiff) {
						Metadata metadata  = TiffMetadataReader.readMetadata(inFile);
						// obtain the Exif directory
						ExifIFD0Directory directory
						    = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

						// query the tag's value
						Date date
						    = directory.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
						
						outFile = getOutFile(date,inFile,destRootDir);
					} else if(fileType == FileType.Riff){
						Metadata metadata  = ImageMetadataReader.readMetadata(inFile);
						FileMetadataDirectory directory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
						Date date  = directory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE,TimeZone.getDefault());
						outFile = getOutFile(date,inFile,destRootDir);
					} else if(inFile.getName().endsWith(".mp4")){
						String strDate = read(inFile.getAbsolutePath());
						java.util.Date date = Date.from( Instant.parse( strDate ));
						outFile = getOutFile(date, inFile, destRootDir);
					} else {
						error = "Unknow type: "+fileType+" -−> "+path.toFile();
					}
					
					
					if(outFile != null) {
						outFile.mkdirs();
						Files.copy(path, Paths.get(outFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
					} 
					
				}
			} catch (ImageProcessingException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TikaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(!error.isEmpty()) {
					error += "\n";
					System.err.println(error);
					// log errors
					java.nio.file.Files.write(Paths.get(logFile.toURI()), 
							error.getBytes("utf-8"), 
							StandardOpenOption.CREATE, 
							StandardOpenOption.APPEND);

				}
			}
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path path, IOException ioException) {
			System.err.println(ioException);
			return CONTINUE;
		}
		
		public static void copyFile(File in, File out) {
			
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
				 case 1 : month = "Janvier";break;
				 case 2 : month = "Fevrier";break;
				 case 3 : month = "Mars";break;
				 case 4 : month = "Avril";break;
				 case 5 : month = "Mai";break;
				 case 6 : month = "Juin";break;
				 case 7 : month = "Juillet";break;
				 case 8 : month = "Aout";break;
				 case 9 : month = "Septembre";break;
				 case 10 : month = "Octobre";break;
				 case 11 : month = "Novembre";break;
				 case 12 : month = "Decembre";break;
			}
			
			return month;
		}
		
		public static String read(String videoFilePath) throws IOException, SAXException, TikaException {

			//detecting the file type
		      BodyContentHandler handler = new BodyContentHandler();
		      org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
		      FileInputStream inputstream = new FileInputStream(new File(videoFilePath));
		      ParseContext pcontext = new ParseContext();
		      
		      //Html parser
		      MP4Parser MP4Parser = new MP4Parser();
		      MP4Parser.parse(inputstream, handler, metadata,pcontext);
 			return metadata.get("Creation-Date");
	    }
	}


