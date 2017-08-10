package crawler.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Crawler {
	
	//driver的数量和线程池线程的数量
	private int driverNum = 2;
	
	//待解析url的最大缓存量
	private int maxCacheNum = Integer.MAX_VALUE;
	
	//头像图片根地址
	static String ROOT_HEAD_URL;
	
	//其他图片根地址
	static String ROOT_IMG_URL;
	
	private BlockingQueue<WebDriver> queue;
	public BlockingQueue<String> cannotResolveQueue;
	private ExecutorService exec;
	
	private JsoupCrawler jsoupCrawler;
	
	enum DriverType{
		FIREFOX,
		CHROME,
		PHANTOMJS;
	}
	
	public static Crawler getCrawler()
	{
		return CrawlerHolder.Crawler;
	}
	
	private void dispatch()
	{
		Thread unresolvedUrlDispatcher = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true)
				{
					String url = null;
					try {
						url = jsoupCrawler.getUrlUnresolve();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						url = null;
						e.printStackTrace();
					}
					if(url!=null)
					{
						ResolveTask tast = new ResolveTask(url);
						exec.submit(tast);
					}
					else break;
				}
				exec.shutdown();
				System.out.println("exec shutdown");
			}
		});
		unresolvedUrlDispatcher.start();
		Thread cannotresolveUrlDispatcher = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true)
				{
					String url = null;
					try {
						url = cannotResolveQueue.poll(60, TimeUnit.SECONDS);
						while(url==null&&!exec.isTerminated())
						{
							url = cannotResolveQueue.poll(60, TimeUnit.SECONDS);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						url = null;
					}
					if(url!=null)
						jsoupCrawler.resolve(url);
					else break;
				}
				jsoupCrawler.shutDownResovler();
				System.out.println("jsoupCrawler shutdown");
			}
		});
		cannotresolveUrlDispatcher.start();
		
	}
	
	/*
	public Set<Cookie> getCookie(String url,String username,String password) throws InterruptedException{
		this.initWebDriver(DriverType.FIREFOX);
		WebDriver driver = this.getWebDriver();
		driver.get(url);
		WebElement userPasswordElement = driver.findElement(By.cssSelector("span.signin-switch-password"));
		userPasswordElement.click();
		Thread.sleep(1000);
		new WebDriverWait(driver, 10);
		WebElement usernameElement = driver.findElement(By.cssSelector("div.account.input-wrapper>input"));
		new Actions(driver).moveToElement(usernameElement).perform();  
		usernameElement.sendKeys(username);
		Thread.sleep(1000);
		WebElement passwordElement = driver.findElement(By.cssSelector("div.verification.input-wrapper>input"));
		new Actions(driver).moveToElement(passwordElement).perform();
		passwordElement.sendKeys(password);
		Thread.sleep(1000);
		Actions action = new Actions(driver);   
		action.sendKeys(Keys.ENTER).build().perform();
		//WebElement loginElement = driver.findElement(By.cssSelector("div.button-wrapper.command>button"));
		//loginElement.click();
		new WebDriverWait(driver, 10);
		System.out.println(driver.getCurrentUrl());
		this.closeAllDriver();
		return null;
	}
	*/
	
	
	public void start(int startPage,int endPage,
			String sqlFilePath,String rootHeadUrl,String rootImgUrl){
		this.init(startPage,endPage,sqlFilePath,rootHeadUrl,rootImgUrl);
		jsoupCrawler.initResovler(driverNum);
		this.dispatch();
		while(true)
		{
			try {
				Thread.sleep(1000*60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(exec.isTerminated())
			{
				closeAllDriver();
				break;
			}
		}
	}
	
	private static class CrawlerHolder
	{
		private static Crawler Crawler = new Crawler();
	}
	
	public void cannotResolve(String url)
	{
		this.cannotResolveQueue.add(url);
	}
	
	private Crawler(){}
	
	private void initWebDriver(DriverType type)
	{
		queue = new LinkedBlockingQueue<WebDriver>(driverNum);
		try {
			switch(type)
			{
			case FIREFOX:
			{
				System.setProperty("webdriver.gecko.driver","drivers/geckodriver.exe");
				for(int i=0;i<driverNum;i++)
				{
					WebDriver d = new FirefoxDriver();				
					queue.put(d);
				}
				break;
			}
			case CHROME:
			{
				System.setProperty("webdriver.chrome.driver","drivers/chromedriver.exe");
				for(int i=0;i<driverNum;i++)
				{
					WebDriver d = new FirefoxDriver();				
					queue.put(d);
				}
				break;
			}
			case PHANTOMJS:
			{
				System.setProperty("phantomjs.binary.path","drivers/phantomjs.exe");
				for(int i=0;i<driverNum;i++)
				{
					WebDriver d = new FirefoxDriver();				
					queue.put(d);
				}
				break;
			}
			default:
				break;
			}
		}catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void init(int startPage,int endPage,
			String sqlFilePath,String rootHeadUrl,String rootImgUrl){
		ROOT_IMG_URL = rootImgUrl;
		ROOT_HEAD_URL = rootHeadUrl;
		JsonManager.initJsonManager(sqlFilePath);
		jsoupCrawler = new JsoupCrawler(startPage,endPage);		
		jsoupCrawler.startCrawl();
		cannotResolveQueue = new LinkedBlockingQueue<String>();
		this.initWebDriver(DriverType.FIREFOX);
		exec = new ThreadPoolExecutor(
				driverNum,
				driverNum, 
	            60,
	            TimeUnit.SECONDS,
	            new LinkedBlockingQueue<Runnable>(maxCacheNum),
	            new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	public WebDriver getWebDriver()
	{
		try {
			return queue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void releaseDriver(WebDriver webDriver)
	{
		try {
			queue.put(webDriver);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void closeAllDriver()
	{
		for(WebDriver webDriver:queue)
		{
			webDriver.quit();
		}
	}
	
	public static void main(String args[]) throws InterruptedException
	{
		/*
		if(args.length!=3)
			throw new IllegalArgumentException();
		int startPage = Integer.parseInt(args[0]);
		int endPage = Integer.parseInt(args[1]);
		String path = args[2];
		*/
		
	
		int startPage = 1;
		int endPage = 45;
		String path = "D:/wendaDataJson";
		String rootHeadUrl = "D:/headimg";
		String rootImgUrl = "D:/questionimg";
		Crawler.getCrawler().start(startPage,endPage,path,rootHeadUrl,rootImgUrl);
		
		
		//new Crawler().getCookie("https://www.zhihu.com/#signin", "15754311189", "z840078718");
	}
}
