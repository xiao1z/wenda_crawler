package crawler.crawler;

import com.alibaba.fastjson.JSON;

public class JsonUtil {
	
	public static JsonObject getJson(Object paramObject,Class<?> paramClazz){
		return new JsonObject(JSON.toJSONString(paramObject),paramClazz);
	}
	
}
