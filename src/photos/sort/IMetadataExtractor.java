package photos.sort;
import java.io.File;
import java.util.Date;

public interface IMetadataExtractor {

	Date extractDate(File inFile) throws MetadataExtractException;
}
