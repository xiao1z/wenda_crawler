package crawler.crawler;

public class JsonObject {
	private Object originObject;
	private Class<?> jsonEntityClass;
	
	public JsonObject(Object originObject,Class<?> jsonEntityClass)
	{
		this.originObject = originObject;
		this.jsonEntityClass = jsonEntityClass;
	}
	
	
	public Object getoriginObject() {
		return originObject;
	}
	
	public Class<?> getJsonEntityClass() {
		return jsonEntityClass;
	}
	
}
