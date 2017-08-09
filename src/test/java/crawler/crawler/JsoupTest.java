package crawler.crawler;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTest {
	public static final String zhihu = "https://www.zhihu.com";
	public static final String questionDetailPagePrefix = "/question/";
	public static void main(String args[]) throws IOException
	{
		
		Document doc = Jsoup.connect("https://www.zhihu.com/topic/19739699/top-answers").userAgent("GoogleBot").get();
		Elements newsHeadlines = doc.select("a");
		for(Element e:newsHeadlines)
		{
			String href = e.attr("href");
			if(href.startsWith(questionDetailPagePrefix)&&href.length()<questionDetailPagePrefix.length()+10)
		    {
		    	System.out.println(zhihu+href);
		    }
		}
	}
}
