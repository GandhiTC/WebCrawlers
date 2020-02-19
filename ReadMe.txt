This is an example of how to use JSoup with the help of Selenium WebDriver
to check the price of a product on Newegg.com.

Because JSoup only parses HTML, we use Selenium WebDriver to initially
load a fully parsed product page, then passes it on to JSoup.

It can also keep rechecking periodically, at a predefined interval, with a max number of rechecks.



---------------------------------------------------------------------------------------------------------------------------------------
Newegg.java
---------------------------------------------------------------------------------------------------------------------------------------
This class takes 6 args:

	arg[0]	URL of product page.

	arg[1] 	The price desired for the product.

	arg[2] 	"true" or "false", open the product page in default browser if price target has been met.
			(default value = "true")

	arg[3] 	"true" or "false", periodically recheck the product price. (if price target not yet met.)
			(default value = "true")

	arg[4] 	The amount of time (in seconds) to wait between price rechecks.
			(default value = "86400", or 24 hours)

	arg[5] 	The max number of times to recheck product price, 0 = no limit.
			(default value = "0")


This class will:
	- Validate URL
	- Run a headless instance of ChromeDriver to load the product page, then pass it on to JSoup
	- Check to see if Newegg's website was able to find a product
	- Check if the item is currently available for purchase
	- Check if the item price is at-or-below your desired price
	- If product is not currently available or product price has
		not yet met your target price, it can keep re-checking.
	- If target price has been met, option to automatically
		open product page in default browser



---------------------------------------------------------------------------------------------------------------------------------------
To Test
---------------------------------------------------------------------------------------------------------------------------------------
Place a copy of chromedriver.exe in directory:  ./src/test/resources/

Right-click Newegg.java -> Run As -> Run Configuration

In left pane/menu, select Java Application -> New Launch Configuration

In right pane -> give it a name such as NewEgg
	- In Main tab
		Project    -> WebCrawlers
		Main Class -> com.github.GandhiTC.java.WebCrawlers.priceCheckers.Newegg
	- In Arguments tab
		In the "Program arguments" text box, add 6 args, 1 per line, no delimiters necessary, for example:
			"https://www.newegg.com/amd-ryzen-7-3800x/p/N82E16819113104?Item=N82E16819113104"
			"325.00"
			"true"
			"true"
			"5"
			"2"



