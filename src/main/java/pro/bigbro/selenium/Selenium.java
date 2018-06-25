package pro.bigbro.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Selenium {
    private WebDriver driver;
    private static String siteName;

    public WebDriver getDriver() {
        return driver;
    }

    public static String getSiteName() {
        return siteName;
    }

    public String getSession() {
        String sess = null;
        try {
            startChromeDriver();
            sess = getDriver().manage().getCookieNamed("auth").getValue();
            quitChromeDriver();
        } catch (InterruptedException e) {
            if (getDriver() != null) {
                quitChromeDriver();
            }
            e.printStackTrace();
        }
        return sess;
    }

    public void quitChromeDriver() {
        getDriver().quit();
    }

    public void launchChromeDriver() {
        System.setProperty("webdriver.chrome.driver", new File("").getAbsolutePath() + "/ChromeDriver/chromedriver.exe");

        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
//        chromePrefs.put("profile.managed_default_content_settings.images", 2);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--dns-prefetch-disable");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);

        driver = new ChromeDriver(cap);

        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    }

    public void startChromeDriver() throws InterruptedException {
        launchChromeDriver();

        FileInputStream fis;
        Properties property = new Properties();

        String login = null;
        String password = null;

        try {
            fis = new FileInputStream(getClass().getClassLoader().getResource("siteConfig.properties").getFile());
            property.load(fis);

            siteName = property.getProperty("site.name");
            login = property.getProperty("site.login");
            password = property.getProperty("site.password");

        } catch (IOException e) {
            System.err.println("ОШИБКА: Файл свойств отсуствует!");
        }

        if (null == siteName || null == login || null == password) return;

        driver.get(siteName);
        Thread.sleep(1000);
        waitForJSandJQueryToLoad();

        WebElement emailField = getDriver().findElement(By.id("identifierId"));
        emailField.sendKeys(login);

        getDriver().findElement(By.id("identifierNext")).click();
        Thread.sleep(1000);
        waitForJSandJQueryToLoad();

        WebElement passwordField = getDriver().findElement(By.name("password"));
        passwordField.sendKeys(password);

        getDriver().findElement(By.id("passwordNext")).click();
        Thread.sleep(7000);
        waitForJSandJQueryToLoad();
    }

    public boolean waitForJSandJQueryToLoad() {

        WebDriverWait wait = new WebDriverWait(driver, 30);

        getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

        // wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return ((Long) ((JavascriptExecutor) getDriver()).executeScript("return jQuery.active") == 0);
                } catch (Exception e) {
                    // no jQuery present
                    return true;
                }
            }
        };

        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                boolean canContinue = false;
                do {
                    try {
                         canContinue = ((JavascriptExecutor) getDriver()).executeScript("return document.readyState")
                                .toString().equals("complete");
                    } catch (TimeoutException e) {
                        continue;
                    }
                } while (!canContinue);
                return canContinue;
            }
        };

        return wait.until(jQueryLoad) && wait.until(jsLoad);
    }
}
