package crawler.crawler;

public class SqlObject {
	private String sql;
	private Class<?> sqlEntityClass;
	
	SqlObject(String sql,Class<?> sqlEntityClass)
	{
		this.sql = sql;
		this.sqlEntityClass = sqlEntityClass;
	}
	
	public String getSql() {
		return sql;
	}
	public Class<?> getSqlEntityClass() {
		return sqlEntityClass;
	}
	
}
