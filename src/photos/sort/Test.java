package photos.sort;

public class Test {

	static void testFilter(){
		String [] tests = {
				"/x/z/f/notSupported/.2208__._IMG_0332.jpg",
				"/x/z/f/notSupported/142__DSC_0017.NEF.metacache.xml",
				"/x/z/f/04_07_2013/@eaDir/DSC_0732.JPG/SYNOPHOTO_THUMB_XL.jpg",
				"/x/z/f/@eaDir/.DS_Store@SynoResource",
				"/x/z/f/DSC_0007.NEF.xmp",
				"/x/z/f/02_05_2014/.DS_Store",
				"/x/z/f/02_05_2014/DSC_0028.JPG",
				"/x/z/f/28_07_2012/AUTPRINT.MRK"
		};
		
		IFileFilter filter = new FileFilter();
		
		filter.addElement("@eaDir");
		filter.addRegexElement(
				".*\\/+(\\..*)",
				"(?i)^.*/?.*(.xml)$",
				"(?i)^.*/?.*(.xmp)$",
				"(?i)^.*/?.*(.mrk)$"
		);
		
		for(String test : tests) {
			if(!filter.match(test)) {
				System.out.println(test);
			}
		}
	}
	
	public static void main(String[] args) {
		testFilter();
	}
}
