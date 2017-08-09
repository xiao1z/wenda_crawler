package crawler.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import model.Comment;
import model.Img;
import model.Question;
import model.User;

public class DatabaseManager {
	
	private BlockingQueue<SqlObject> sqlQueue;
	private static String sqlFilePath;
	
	//初始化DatabaseManager
	//如果DatabaseManager没有被初始化过，则初始化成功，返回true。否则，该次调用无效，返回false;
	public static boolean initDatabaseManager(String sqlFilePath){
		if(DatabaseManager.sqlFilePath!=null)
			return false;
		else
		{
			DatabaseManager.sqlFilePath = sqlFilePath;
			return true;
		}
	}

	private DatabaseManager(){
		sqlQueue = new LinkedBlockingQueue<SqlObject>();
		Thread writer = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					File path = new File(sqlFilePath);
					if(!path.exists())
						path.mkdirs();
					FileWriter imgFw = new FileWriter(sqlFilePath+"/img",true);
					FileWriter userFw = new FileWriter(sqlFilePath+"/user",true);
					FileWriter commentFw = new FileWriter(sqlFilePath+"/comment",true);
					FileWriter questionFw = new FileWriter(sqlFilePath+"/question",true);
					while(true)
					{
						SqlObject sql = sqlQueue.poll(60, TimeUnit.SECONDS);
						if(sql!=null){
							if(sql.getSqlEntityClass()==User.class){
								userFw.write(sql.getSql());
								userFw.flush();
							}else if(sql.getSqlEntityClass()==Comment.class){
								commentFw.write(sql.getSql());
								commentFw.flush();
							}else if(sql.getSqlEntityClass()==Question.class){
								questionFw.write(sql.getSql());
								questionFw.flush();
							}else{
								imgFw.write(sql.getSql());
								imgFw.flush();
							}
						}else
						{
							imgFw.close();
							userFw.close();
							commentFw.close();
							questionFw.close();
							break;
						}
					}
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		writer.start();
	}
	
	public String getSqlFilePath(){
		return DatabaseManager.sqlFilePath;
	}
	
	private static class DatabaseManagerHolder{
		private static DatabaseManager databaseManager = new DatabaseManager();
	}
	
	public static DatabaseManager getDatabaseManager(){
		
		if(DatabaseManager.sqlFilePath==null)
			throw new NullPointerException("DatabaseManager未初始化");
		
		return DatabaseManagerHolder.databaseManager;
	}
	
	public void addQuestion(Question question)
	{
		try {
			SqlObject sql = MybatisSqlUtil.getSQL("addQuestion", question, Question.class);
			sqlQueue.put(sql);
		} catch (IllegalArgumentException | IllegalAccessException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addImg(Img img)
	{
		try {
			SqlObject sql = MybatisSqlUtil.getSQL("addImg", img, Img.class);
			sqlQueue.put(sql);
		} catch (IllegalArgumentException | IllegalAccessException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addComment(Comment comment)
	{
		try {
			SqlObject sql = MybatisSqlUtil.getSQL("addComment", comment, Comment.class);
			sqlQueue.put(sql);
		} catch (IllegalArgumentException | IllegalAccessException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void addUser(User user){
		try {
			SqlObject sql = MybatisSqlUtil.getSQL("addUser", user, User.class);
			sqlQueue.put(sql);
		} catch (IllegalArgumentException | IllegalAccessException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
