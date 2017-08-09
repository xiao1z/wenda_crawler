package crawler.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import model.Comment;
import model.Img;
import model.Question;
import model.User;

public class JsonManager {
	
	private BlockingQueue<JsonObject> jsonQueue;
	private static String jsonFilePath;
	
	//初始化JsonManager
	//如果JsonManager没有被初始化过，则初始化成功，返回true。否则，该次调用无效，返回false;
	public static boolean initJsonManager(String jsonFilePath){
		if(JsonManager.jsonFilePath!=null)
			return false;
		else
		{
			JsonManager.jsonFilePath = jsonFilePath;
			return true;
		}
	}

	private JsonManager(){
		jsonQueue = new LinkedBlockingQueue<JsonObject>();
		Thread writer = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					File path = new File(jsonFilePath);
					if(!path.exists())
						path.mkdirs();
					FileWriter imgFw = new FileWriter(jsonFilePath+"/img",true);
					FileWriter userFw = new FileWriter(jsonFilePath+"/user",true);
					FileWriter commentFw = new FileWriter(jsonFilePath+"/comment",true);
					FileWriter questionFw = new FileWriter(jsonFilePath+"/question",true);
					
					
					while(true)
					{
						JsonObject json = jsonQueue.poll(60, TimeUnit.SECONDS);
						if(json!=null){
							if(json.getJsonEntityClass()==User.class){
								userFw.write(JSON.toJSONString(json.getoriginObject())+"\r\n");
								userFw.flush();
							}else if(json.getJsonEntityClass()==Comment.class){
								commentFw.write(JSON.toJSONString(json.getoriginObject())+"\r\n");
								commentFw.flush();
							}else if(json.getJsonEntityClass()==Question.class){
								questionFw.write(JSON.toJSONString(json.getoriginObject())+"\r\n");
								questionFw.flush();
							}else{
								imgFw.write(JSON.toJSONString(json.getoriginObject())+"\r\n");
								imgFw.flush();
							}
						}else
						{
							break;
						}
					}
					imgFw.close();
					userFw.close();
					commentFw.close();
					questionFw.close();

				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		writer.start();
	}
	
	public String getJsonFilePath(){
		return JsonManager.jsonFilePath;
	}
	
	private static class JsonManagerHolder{
		private static JsonManager JsonManager = new JsonManager();
	}
	
	public static JsonManager getJsonManager(){
		
		if(JsonManager.jsonFilePath==null)
			throw new NullPointerException("JsonManager未初始化");
		
		return JsonManagerHolder.JsonManager;
	}
	
	public void addQuestion(Question question)
	{
		try {
			JsonObject json = JsonUtil.getJson(question, Question.class);
			jsonQueue.put(json);
		} catch (IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addImg(Img img)
	{
		try {
			JsonObject json = JsonUtil.getJson( img, Img.class);
			jsonQueue.put(json);
		} catch (IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addComment(Comment comment)
	{
		try {
			JsonObject json = JsonUtil.getJson( comment, Comment.class);
			jsonQueue.put(json);
		} catch (IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void addUser(User user){
		try {
			JsonObject json = JsonUtil.getJson( user, User.class);
			jsonQueue.put(json);
		} catch (IllegalArgumentException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
