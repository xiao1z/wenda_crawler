package crawler.crawler;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import model.Comment;
import model.EntityType;
import model.Img;
import model.Question;
import model.User;

public class JsoupCrawler {
	
	private static final String rootUrl = "https://www.zhihu.com";
	private static final String questionDetailPagePrefix = "/question/";
	private static final String seed = "https://www.zhihu.com/topic/19739699/top-answers?page=";
	
	
	private int startPage;
	private int endPage;
	private volatile boolean isDone = false; 
	private ExecutorService exec;
	
	private BlockingQueue<String> urls = new LinkedBlockingQueue<String>();
	private Set<String> handledUrl = new HashSet<String>();

	
	public JsoupCrawler(int startPage,int endPage)
	{
		this.startPage = startPage;
		this.endPage = endPage;
	}
	
	public void startCrawl()
	{
		if(urls==null)
			return;
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				int count = 0;
				// TODO Auto-generated method stub
				try
				{
					System.out.println("startPage:"+startPage+" endPage:"+endPage);
					for(int i = startPage;i<=endPage;i++)
					{
						Thread.sleep(100);
						Document doc = Jsoup.connect(seed+i).userAgent("GoogleBot").get();
						Elements newsHeadlines = doc.select("a.question_link");
						for(Element e:newsHeadlines)
						{
							String href = e.attr("href");
							if(href.startsWith(questionDetailPagePrefix)&&href.length()<questionDetailPagePrefix.length()+10)
						    {
								if(handledUrl.add(href)){
									urls.put(rootUrl+href);
									count++;
								}
						    }
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}finally{
					isDone = true;
					System.out.println("resolve count:"+count);
				}
			}
		});
		t.start();
	}
	
	public String getUrlUnresolve() throws InterruptedException
	{
		String url = null;
		while(!this.isDone||!this.urls.isEmpty()){
			url = urls.poll(60, TimeUnit.SECONDS);
			if(url!=null)
				return url;
		}
		return null;
	}
	
	public void initResovler(int threadNum)
	{
		exec = Executors.newFixedThreadPool(threadNum);
	}
	
	public void shutDownResovler()
	{
		exec.shutdown();
	}
	
	
	public void resolve(final String url)
	{
		if(exec == null)
			throw new NullPointerException("线程池未初始化，请检查是否调用了initResovler方法");
		
		exec.submit(new Runnable(){

			@Override
			public void run() {
				System.out.println("jsoup resovle:"+url);				
				
				try {
					Document doc = Jsoup.connect(url).userAgent("GoogleBot").get();
					String questionTitle = doc.select("h1.QuestionHeader-title").text();
					Elements questionContent = doc.select("div.QuestionRichText.QuestionRichText--expandable>div>span");
					if(questionContent==null)
					{
						questionContent = doc.select("div.QuestionRichText.QuestionRichText--expandable.QuestionRichText--collapsed>div>span");
					}
					Elements commentList = doc.select("div.List-item");
					Question question = new Question();
					question.setTitle(questionTitle);
					if(questionContent!=null)
						question.setContent(questionContent.first().text());
					question.setCreatedDate(new Date());
					question.setId(Question.tempId.incrementAndGet());
					question.setCommentCount(commentList.size());
					JsonManager.getJsonManager().addQuestion(question);
					for(int i = 0;i<commentList.size();i++){
						User user = new User();
						Elements userImgElement = commentList.get(i).select("img.Avatar.AuthorInfo-avatar");
						if(userImgElement!=null)
						{
							String imgUrl = userImgElement.first().attr("src");
							user.setHeadUrl(ImgDownloader.download(imgUrl, Crawler.ROOT_HEAD_URL));
						}
						Elements userNameElement = commentList.get(i).select("span.UserLink.AuthorInfo-name>div>div>a");
						if(userNameElement!=null)
						{
							user.setUsername(userNameElement.first().text());
							user.setNickname(userNameElement.first().text());
						}
						Elements userDescriptionElement = commentList.get(i).select("div.AuthorInfo-detail>div>div");
						if(userDescriptionElement!=null)
							user.setBriefIntroduction(userDescriptionElement.first().text());
						user.setId(User.tempId.incrementAndGet());
						if(user.getUsername()!=null)
							JsonManager.getJsonManager().addUser(user);
						
						Comment comment = new Comment();
						comment.setCreateDate(new Date());
						comment.setEntityId(question.getId());
						comment.setEntityType(EntityType.QUESTION);
						comment.setUserId(user.getId());
						
						Elements commentContent = commentList.get(i).select("div.RichContent-inner");
						if(commentContent!=null)
						{
							comment.setContent(commentContent.first().text());
						}
						comment.setId(Comment.tempId.incrementAndGet());
							
						Elements imgElementList = commentList.get(i).select("div.RichContent-inner>span>noscript>img");
						if(imgElementList!=null)
						{
							comment.setImgCount(imgElementList.size());
							JsonManager.getJsonManager().addComment(comment);
							int offset = 1;
							for(int j=0;j<imgElementList.size();j++)
							{
								Img img = new Img();
								img.setEntityId(comment.getId());
								img.setEntityType(EntityType.COMMENT);
								img.setId(Img.tempId.incrementAndGet());
								img.setOffset(offset++);
								String imgUrl = imgElementList.get(j).attr("src");
								img.setUrl(ImgDownloader.download(imgUrl, Crawler.ROOT_IMG_URL));
								JsonManager.getJsonManager().addImg(img);
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
			}
		});
	}
}
