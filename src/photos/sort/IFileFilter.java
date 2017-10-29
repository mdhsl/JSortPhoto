package photos.sort;

public interface IFileFilter {

	boolean match(String name);
	
	void addElement(String...elements);
	
	void addRegexElement(String...elements);
}
