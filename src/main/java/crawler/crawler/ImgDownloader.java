package crawler.crawler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;

public class ImgDownloader {
	
	
	public static String download(String imgurl,String rootFilePath){ 
		File rootDir = new File(rootFilePath);
		if(!rootDir.exists())
			rootDir.mkdirs();
	    String suffix = FilenameUtils.getExtension(imgurl);  
	    String uuid = UUID.randomUUID().toString();  
	    StringBuilder filename = new StringBuilder();
	    filename.append('/').append(uuid).append('.').append(suffix);
	    String filePath = rootFilePath + filename.toString();
	    try {
			createImage(imgurl, filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return filename.toString();  
	}  
	
	
	private static void createImage(String imgurl, String filePath) throws Exception {  
		  
	    URL url = new URL(imgurl);  
	  
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
	    InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据  
	    byte[] getData = readInputStream(inputStream); // 获得图片的二进制数据  
	  
	    File imageFile = new File(filePath);  
	    FileOutputStream fos = new FileOutputStream(imageFile);  
	    fos.write(getData);  
	    fos.close();  
	}  
	  
	private static byte[] readInputStream(InputStream inputStream) throws IOException {  
	    byte[] buffer = new byte[1024];  
	    int len = 0;  
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();  
	    while ((len = inputStream.read(buffer)) != -1) {  
	        bos.write(buffer, 0, len);  
	    }  
	    bos.close();  
	    return bos.toByteArray();  
	}
}
