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

/**
 * Unit test for simple App.
 */
public class AppTest {
    
	public static void createImage(String imgurl, String filePath) throws Exception {  
		  
	    URL url = new URL(imgurl);  
	  
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
	    InputStream inputStream = conn.getInputStream(); // 通过输入流获得图片数据  
	    byte[] getData = readInputStream(inputStream); // 获得图片的二进制数据  
	  
	    File imageFile = new File(filePath);  
	    FileOutputStream fos = new FileOutputStream(imageFile);  
	    fos.write(getData);  
	    fos.close();  
	}  
	  
	public static void main(String[] args) throws Exception {  
	    // String imgurl = "http://www.dabaoku.com/gif/152/gif001.gif";  
	    String imgurl = "https://pic1.zhimg.com/3ffe7842c0f5434612585e1053bbd9fc_b.jpg";  
	    String suffix = FilenameUtils.getExtension(imgurl);  
	    String uuid = UUID.randomUUID().toString();  
	    String filePath = "D:/wendaDataJson/" + uuid + "." + suffix;  
	    createImage(imgurl, filePath);  
	    System.out.println(" read picture success:");  
	}  
	  
	public static byte[] readInputStream(InputStream inputStream) throws IOException {  
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
