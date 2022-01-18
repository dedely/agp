package persistence.edb;

import persistence.edb.operator.Operator;
import persistence.lucene.LuceneIndexer;

public abstract class EDB_API {
	
	private String tableName; 
	private String key;
	private String userDirectoryPath;
	
	public EDB_API(String tableName, String key, String userDirectoryPath) {
		initDataParameters(tableName, key, userDirectoryPath);
	}
	
	public void initDataParameters(String tableName, String key, String userDirectoryPath) {
		this.tableName = tableName;
		this.key = key;
		this.userDirectoryPath = userDirectoryPath;
	}
	
	public void createFileDescription() {
		
	}
	
	public void createTextualIndex() {
		try {
			LuceneIndexer.getInstance(userDirectoryPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Operator executeSQLQuery(String query) {
		return null;	
	}
	
	public abstract Operator executeMixedQuery(String query);

}
