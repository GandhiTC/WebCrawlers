package com.github.GandhiTC.java.WebCrawlers.priceCheckers;



import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.apache.commons.validator.routines.CurrencyValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


/*
 * This class will check to see if the price of a product on Newegg.com
 * has met your desired price or not.
 * 
 * In the process, it will:
 * 		- Validate URL
 * 		- Run a headless instance of ChromeDriver to load the product page, then pass it on to JSoup
 * 		- Check to see if Newegg's website was able to find a product
 * 		- Check if the item is currently available for purchase
 * 		- Check if the item price is at-or-below your desired price
 * 		- If product is not currently available or product price has
 * 			not yet met your target price, it can keep re-checking.
 * 		- If target price has been met, option to automatically
 * 			open product page in default browser
 */

/*
 * This class takes 6 args/params
 * 
 * arg[0]	URL of product page.
 * 
 * arg[1] 	The price desired for the product.
 * 
 * arg[2] 	"true" or "false", open the product page in default browser if price target has been met.
 * 			(default value = "true")
 * 
 * arg[3] 	"true" or "false", periodically recheck the product price. (if price target not yet met.)
 * 			(default value = "true")
 * 
 * arg[4] 	The amount of time (in seconds) to wait between price rechecks.
 * 			(default value = "86400", or 24 hours)
 * 
 * arg[5] 	The max number of times to recheck product price, 0 = no limit.
 * 			(default value = "0")
 */

/*
 * This is an example of how to use JSoup with the help of WebDriver
 * to check the price of a product on Newegg.com.
 * 
 * (Because JSoup parses HTML only, we use WebDriver to initially
 * load a properly parsed product page, then passes it on to JSoup.)
 */



public class Newegg
{
	private static Document	doc			= null;
	private static String	productPage	= null;
	private static String	wantedPrice	= null;
	private static boolean	goToWebPage	= true;
	private static boolean	persist		= true;
	private static long		duration	= 86400;	//	in seconds, 86400 = 24 hours
	private static int		checkCount	= 1;
	private static int		maxChecks	= 0;
	
	
	public static void main(String[] args) throws IOException
	{
		//	limit info logged to console
		seLoggingtLevel(Level.SEVERE);
		
		if(args.length != 6)
		{
			terminateProgram(1, "Invalid number of args passed.");
		}
		
		while(persist && ((maxChecks <= 0) || ((maxChecks > 0) && (checkCount <= maxChecks))))
		{
			runProcesses(args);
		}
		
		terminateProgram(0, "Exiting program.");
	}
	
	
	private static void runProcesses(String[] args)
	{
		productPage	= args[0];
		wantedPrice = args[1];
		goToWebPage	= Boolean.valueOf(args[2]);
		persist		= Boolean.valueOf(args[3]);
		duration	= Long.valueOf(args[4]);
		maxChecks	= Integer.valueOf(args[5]);
		
		if(persist && (maxChecks > 0))
		{
			System.out.println("Running price check " + checkCount + " of " + maxChecks);
		}
		
		validateURL(productPage);
		
		if(persist && ((maxChecks <= 0) || ((maxChecks > 0) && (checkCount <= maxChecks))))
		{
			long				milliseconds	= TimeUnit.SECONDS.toMillis(duration);
			Date				nextDate		= Date.from(new Date().toInstant().plusMillis(milliseconds));
			SimpleDateFormat	dateFormat		= new SimpleDateFormat("EEEEE, MMMM dd, yyyy");
			SimpleDateFormat	timeFormat		= new SimpleDateFormat("hh:mm:ss aaa z");
			checkCount++;
			
			if(maxChecks == 0)
			{
				System.out.println("\r\nThe next price check will run " + dateFormat.format(nextDate) + " at " + timeFormat.format(nextDate) + "\r\n");
				try
				{
					Thread.sleep(milliseconds);
				}
				catch(InterruptedException e)
				{
				}
			}
			else
			{
				if(checkCount <= maxChecks)
				{
					System.out.println("\r\nPrice check " + checkCount + " of " + maxChecks + " will run " + dateFormat.format(nextDate) + " at " + timeFormat.format(nextDate) + "\r\n");
					try
					{
						Thread.sleep(milliseconds);
					}
					catch(InterruptedException e)
					{
					}
				}
				else
				{
					System.out.println("\r\nFinished running the last check.");
				}
			}
		}
	}
	
	
	private static void validateURL(String uRL)
	{
		System.out.println("Validating URL.");
		
		UrlValidator urlValidator = new UrlValidator();
		if(!urlValidator.isValid(uRL))
		{
			terminateProgram(1, "URL is invalid");
		}
		
		URI uri = null;

		try
		{
			uri = new URI(uRL);
		}
		catch(URISyntaxException e)
		{
			// e.printStackTrace();
			terminateProgram(1, "URL is invalid");
		}
		
	    if(!uri.getHost().equalsIgnoreCase("www.newegg.com"))
		{
			terminateProgram(1, "URL is invalid, please use Newegg's website");
		}
	    
	    setupJsoupDoc(productPage);
	}
	
	
	private static void setupJsoupDoc(String uRL)
	{
		System.out.println("Starting headless browser.");
		
		//	setup ChromeDriver to run headless and reduce its logs to console
		System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");
		
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.setHeadless(true);
		
		WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(uRL);
        
				
        System.out.println("Parsing webpage.");
		
		doc = Jsoup.parse(driver.getPageSource());
		if(doc == null)
		{
			terminateProgram(1, "There was an error in parsing the web page, please try again.");
		}
		
		
		driver.quit();
		driver = null;
		
		lookupItem();
	}
	
	
	private static void lookupItem()
	{
		System.out.println("Looking up item.");
		
		//	make sure newegg can find the product
		Elements errorMsgs = doc.getElementsByClass("errorMsgWarning");
		if(errorMsgs.size() > 0)
		{
			for(Element msg : errorMsgs)
			{
				if(msg.text().contains("can't find this Item"))
				{
					terminateProgram(1, "No item was found, please check the URL and try again.");
				}
			}
		}
		
		checkItemAvailability();
	}
	
	
	private static void checkItemAvailability()
	{
		System.out.println("Checking item availability.");
		
		Element		noticeBox	= doc.getElementById("version_promo");
		Elements	notInStock	= doc.getElementById("landingpage-stock").select("span:contains(OUT OF STOCK)");
		//	notInStock is an alternative to using xpath, to relatively finding a descendant of an element that was also relatively ID'ed
		//	ie:  //span[@id="landingpage-stock"]//span[contains(text(), "OUT OF STOCK")]
		
		//	noticeBox is always present on the webpage, just hidden away if the product is available for purchase
		if(noticeBox == null)
		{
			terminateProgram(1, "There was an error loading the page, please try again.");
		}
		
		if((!noticeBox.attr("style").equalsIgnoreCase("display:none;") && (noticeBox.getElementsContainingText("Not available.").size() > 0)) || (notInStock.size() > 0))
		{
			System.out.println("This item is not currently available for purchase.\r\n");
		}
		else
		{
			checkPrice(wantedPrice, goToWebPage, productPage);
		}
	}
	
	
	private static void checkPrice(String priceDesired, boolean goToWebPage, String uRL)
	{
		System.out.println("Checking item price.");
		
		String currentPrice = doc.select("div#landingpage-price li.price-current").first().text();
		
		if(currentPrice.isEmpty() || (currentPrice == null))
		{
			terminateProgram(1, "Unable to retrieve item current price.");
		}
		
		BigDecimalValidator	bdValidator		= CurrencyValidator.getInstance();
		BigDecimal			currentAmount	= bdValidator.validate(currentPrice, Locale.getDefault());
		BigDecimal			desiredAmount	= bdValidator.validate(priceDesired, Locale.getDefault());

		if(currentAmount.toString().isEmpty() || (currentAmount == null))
		{
			terminateProgram(1, "Unable to retrieve item's current price.");
		}
		
		if(desiredAmount.toString().isEmpty() || (desiredAmount == null))
		{
			terminateProgram(1, "There was an error in processing prices.");
		}
		
		if(currentAmount.floatValue() <= desiredAmount.floatValue())
		{
			//	exit the while loop in method main
			persist		= false;
			
			System.out.println("Current item price has met your target!");
			
			if(goToWebPage)
			{
				if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
				{
					boolean errored = false;
					
				    try
					{
				    	System.out.println("Opening product page in default browser.");
						Desktop.getDesktop().browse(new URI(uRL));
					}
					catch(URISyntaxException | IOException e)
					{
						// e.printStackTrace();
						errored = true;
					}
				    finally
				    {
				    	if(errored)
						{
							System.out.println("There was an error in opening product page.");
						}
				    }
				}
			}
		}
		else
		{
			System.out.println("Target price not yet met.");
		}
	}
	
	
	private static void terminateProgram(int iD, String msg)
	{
		doc	= null;
		
		System.out.println(msg + "\r\n\r\n\r\n");
		
		System.exit(iD);
	}


	private static void seLoggingtLevel(Level targetLevel)
	{
		java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
		
		root.setLevel(targetLevel);

		for(Handler handler : root.getHandlers())
		{
			handler.setLevel(targetLevel);
		}
	}
}