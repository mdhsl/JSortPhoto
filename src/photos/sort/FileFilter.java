package photos.sort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileFilter implements IFileFilter{

	private List<String> filterElements;
	private List<String> filterPatterns;
	
	public FileFilter() {
		filterElements = new ArrayList<String>();
		filterPatterns = new ArrayList<String>();
	}
	
	public void addElement(String...elements) {
		this.filterElements.addAll(Arrays.asList(elements));
	}
	

	@Override
	public void addRegexElement(String... elements) {
		this.filterPatterns.addAll(Arrays.asList(elements));
	}
	
	@Override
	public boolean match(String name) {
		boolean res = false;
		
		for(String elt : filterElements) {
			if(name.contains(elt)) {
				res = true;
				break;
			}
		}
		if(!res) {
			for(String pattern : filterPatterns) {
				if(name.matches(pattern)){
					res = true;
					break;
				}
			}
		}
		return res;
	}
}
