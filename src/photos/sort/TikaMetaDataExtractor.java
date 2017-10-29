package photos.sort;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.image.TiffParser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.tika.sax.BodyContentHandler;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileMetadataDirectory;

public class TikaMetaDataExtractor implements IMetadataExtractor{

	@Override
	public Date extractDate(File inFile) throws MetadataExtractException {
		String ext = getFileExtension(inFile);
		
		Date result = null;
		
		// try do decode using drewnoakes
		try {
			FileType fileType = FileTypeDetector.detectFileType(new BufferedInputStream(new FileInputStream(inFile)));
			
			if(fileType == FileType.Jpeg ) {
				com.drew.metadata.Metadata metadata  = ImageMetadataReader.readMetadata(inFile);
				// obtain the Exif directory
				ExifSubIFDDirectory directory
				    = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

				// query the tag's value
				if(directory != null) {
					result = directory.getDateDigitized(TimeZone.getDefault());
				} else {
					// use file modif instead
					FileMetadataDirectory fileDirectory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
					result = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE,TimeZone.getDefault());
				}
			} else if(fileType == FileType.Riff ||fileType == FileType.Png ){
				com.drew.metadata.Metadata metadata  = ImageMetadataReader.readMetadata(inFile);
				FileMetadataDirectory directory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
				result = directory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE,TimeZone.getDefault());
			} else if (fileType == FileType.Nef || fileType == FileType.Tiff) {
				com.drew.metadata.Metadata metadata  = TiffMetadataReader.readMetadata(inFile);
				// obtain the Exif directory
				ExifIFD0Directory directory
				    = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

				// query the tag's value
				result = directory.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
			}
		} catch (IOException e) {
		} catch (ImageProcessingException e) {
		} 
		
		if(result == null) {
			// try using tika
			Parser parser = null;
			
			if(ext.equalsIgnoreCase("jpg") ) {
				parser = new JpegParser();
			} if(ext.equalsIgnoreCase("png")) {
				parser = new JpegParser();
			} else if(ext.equalsIgnoreCase("nef")) {
				parser = new TiffParser();
			} else {
				parser = new AutoDetectParser();
			}
			
		    BodyContentHandler handler = new BodyContentHandler();
		    Metadata metadata = new Metadata();
		    ParseContext context = new ParseContext();
		    
		    try {
				parser.parse(new FileInputStream(inFile), handler, metadata,context);
				String strDate = metadata.get("Creation-Date");
				
				if(strDate != null && strDate.length() > 0 && strDate.charAt(strDate.length()-1) != 'Z') {
					strDate += 'Z';
				}
				result =  Date.from( Instant.parse( strDate ));
			} catch (Exception e) {
				throw new MetadataExtractException(inFile.getAbsolutePath()+"...KO");
			}
		}
		
		return result;
	}
	
	private String getFileExtension(File file) {
	    String name = file.getName();
	    try {
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}
}
