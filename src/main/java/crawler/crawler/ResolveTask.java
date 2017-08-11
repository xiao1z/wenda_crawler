package crawler.crawler;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import model.Comment;
import model.EntityType;
import model.Img;
import model.Question;
import model.User;

public class ResolveTask implements Runnable{
	
	public static Set<Cookie> cookieSet;
	
	public static Random random = new Random();
	
	String url;
	public ResolveTask(String url)
	{
		this.url = url;
	}
	
	private WebElement findWebElement(WebDriver driver,String cssSelector)
	{
		try{
			return driver.findElement(By.cssSelector(cssSelector));
		}catch(Exception e)
		{
			return null;
		}
	}
	
	private WebElement findWebElement(WebElement webElement,String cssSelector)
	{
		try{
			return webElement.findElement(By.cssSelector(cssSelector));
		}catch(Exception e)
		{
			return null;
		}
	}
	
	private List<WebElement> findWebElements(WebElement webElement,String cssSelector)
	{
		try{
			return webElement.findElements(By.cssSelector(cssSelector));
		}catch(Exception e)
		{
			return null;
		}
	}
	
	
	
	@Override
	public void run() {
		Crawler crawler = Crawler.getCrawler();
		WebDriver driver = crawler.getWebDriver();
		try{
			driver.get(url);
			String questionTitle = findWebElement(driver,"h1.QuestionHeader-title").getText();
			boolean match = false;
			for(int i = 0;i<questionTitle.length();i++)
			{
				if(questionTitle.charAt(i)=='猫') 
				{
					match = true;
					break;
				}
			}
			if(match)
			{	
				WebElement moreAnswer = findWebElement(driver,"button.QuestionMainAction");
				WebElement showAll = findWebElement(driver,"button.Button.QuestionRichText-more.Button--plain");
				if(moreAnswer !=null)
				{
					if(showAll!=null)
						showAll.click();
					moreAnswer.click();
				    (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
				        public Boolean apply(WebDriver d) {
				        	if (d.findElements(new By.ByClassName("List-item")).size()>=3)
				         		return true;
				         	else 
				         		return false;
				        }
				    });
				}
				WebElement questionContent = findWebElement(driver,"div.QuestionRichText.QuestionRichText--expandable>div>span");
				if(questionContent==null)
				{
					questionContent = findWebElement(driver,"div.QuestionRichText.QuestionRichText--expandable.QuestionRichText--collapsed>div>span");
					if(questionContent==null)
					{
						crawler.releaseDriver(driver);
						crawler.cannotResolve(url);
						return;
					}
				}
				List<WebElement> commentList = driver.findElements(By.cssSelector("div.List-item"));
				Question question = new Question();
				question.setTitle(questionTitle);
				question.setContent(questionContent.getText());
				question.setCreatedDate(new Date());
				question.setId(Question.tempId.incrementAndGet());
				question.setCommentCount(commentList.size());
				JsonManager.getJsonManager().addQuestion(question);
				
				for(WebElement commentElement:commentList){
					User user = new User();
					
					WebElement userImgElement = findWebElement(commentElement,"img.Avatar.AuthorInfo-avatar");
					if(userImgElement!=null)
					{
						String imgUrl = userImgElement.getAttribute("src");
						user.setHeadUrl(ImgDownloader.download(imgUrl, Crawler.ROOT_HEAD_URL));
					}
					WebElement userNameElement = findWebElement(commentElement,"span.UserLink.AuthorInfo-name>div>div>a");
					if(userNameElement!=null)
					{
						user.setUsername(userNameElement.getText());
						user.setNickname(userNameElement.getText());
					}
					WebElement userDescriptionElement = findWebElement(commentElement,"div.AuthorInfo-detail>div>div");
					if(userDescriptionElement!=null)
						user.setBriefIntroduction(userDescriptionElement.getText());
					user.setId(User.tempId.incrementAndGet());
					if(user.getUsername()!=null)
						JsonManager.getJsonManager().addUser(user);
					
					Comment comment = new Comment();
					comment.setCreateDate(new Date());
					comment.setEntityId(question.getId());
					comment.setEntityType(EntityType.QUESTION);
					comment.setUserId(user.getId());
					
					WebElement commentContent = findWebElement(commentElement,"div.RichContent-inner");
					if(commentContent!=null)
					{
						comment.setContent(commentContent.getText());
					}
					comment.setId(Comment.tempId.incrementAndGet());
						
					List<WebElement> imgElementList = findWebElements(commentElement,"div.RichContent-inner>span>span>div");
					
					if(imgElementList!=null)
					{
						comment.setImgCount(imgElementList.size());
						JsonManager.getJsonManager().addComment(comment);
						int offset = 1;
						for(WebElement imgElement:imgElementList)
						{
							Img img = new Img();
							img.setEntityId(comment.getId());
							img.setEntityType(EntityType.COMMENT);
							img.setId(Img.tempId.incrementAndGet());
							img.setOffset(offset++);
							String imgUrl = imgElement.getAttribute("data-src");
							img.setUrl(ImgDownloader.download(imgUrl, Crawler.ROOT_IMG_URL));
							
							JsonManager.getJsonManager().addImg(img);
						}
					}
					
				}
			}
			System.out.println("selenium resolved:"+url);
			crawler.releaseDriver(driver);
		}catch(Exception e)
		{
			e.printStackTrace();
			if(crawler!=null)
				crawler.refreshDriver(driver);
		}
	}
}
