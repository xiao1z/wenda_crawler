package crawler.crawler;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Selenium2Example  {
    public static void main(String[] args) throws IOException {
    	System.setProperty("phantomjs.binary.path","C:\\Users\\zhangxiao\\Desktop\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
    	WebDriver driver=new PhantomJSDriver();
    	
        driver.get("https://www.zhihu.com/question/30694841");
        
        System.out.println(driver.getTitle());
        WebElement title = driver.findElement(By.cssSelector("div.QuestionHeader-main"));
        System.out.println(title.getText());
        WebElement element = driver.findElement(By.cssSelector("button.Button.QuestionMainAction"));
        element.click();
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
            	if (d.findElements(new By.ByClassName("List-item")).size()>=3)
            		return true;
            	else 
            		return false;
            }
        });

        System.out.println(driver.findElements(new By.ByClassName("List-item")).size());
        driver.quit();
    }
}
