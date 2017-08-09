package crawler.crawler;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import dao.MybatisSqlSessionFactory;


public class MybatisSqlUtil {
	
	private static SimpleDateFormat sf; 
	
	static
	{
		sf = new SimpleDateFormat();
		sf.setTimeZone(TimeZone.getTimeZone("UTC"));
		sf.applyPattern("yyyy-MM-dd hh:mm:ss");
	}
	
	public static void setDataFormat(SimpleDateFormat simpleDateFormat)
	{
		sf = simpleDateFormat;
	}
	
	public static SqlObject getSQL(String mapperId,Object paramObject,Class<?> paramClazz) throws IllegalArgumentException, IllegalAccessException{
		String unhandleSql = MybatisSqlSessionFactory.getSqlSessionFactory().getConfiguration().getMappedStatement(mapperId).getBoundSql(paramObject).getSql();
		StringBuilder sql = new StringBuilder();
		String[] splitSql = unhandleSql.split("[?]");
		sql.append(splitSql[0]);
		if(paramClazz.isInstance(paramObject))
		{
			Object obj = paramClazz.cast(paramObject);
			Field[] fields = paramClazz.getDeclaredFields();
			String param = null;
			for(int i = 1;i<splitSql.length;i++)
			{
				fields[i].setAccessible(true);
				Object field = fields[i].get(obj);
				if(field==null);
				else
				{
					if(field instanceof Date){
						param = sf.format((Date)field);
					}
					else
					{
						param = String.valueOf(field);
					}
				}
				if(field==null)
					sql.append("''");
				else{
					sql.append('\'');
					sql.append(param);
					sql.append('\'');
				}
				sql.append(splitSql[i]);
				
			}
			fields[0].setAccessible(true);
			sql.append("#").append(fields[0].get(obj));
			if(sql.charAt(sql.length()-1)!=';')
				sql.append(";");
			sql.append("\r\n");
		}
		return new SqlObject(sql.toString(),paramClazz);
	}
	
}
