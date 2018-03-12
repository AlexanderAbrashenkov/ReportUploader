package pro.bigbro.services.counting;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pro.bigbro.models.City;
import pro.bigbro.models.CityDownloaded;
import pro.bigbro.models.GoogleHashCity;
import pro.bigbro.repositories.CityDownloadedRepository;
import pro.bigbro.repositories.GoogleHashCityRepository;
import pro.bigbro.selenium.Selenium;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
public class SeleniumService {

    @Autowired
    private GoogleHashCityRepository googleHashCityRepository;
    @Autowired
    private CityDownloadedRepository cityDownloadedRepository;

    @Value("${site.name}")
    private String siteName;

    private final static String SCRIPT_SCHEMA = "var destination_id = \"%s\"; \n" +
            "  var source = SpreadsheetApp.getActiveSpreadsheet();\n" +
            "  var sheet1 = source.getSheets()[1];\n" +
            "  var sheet2 = source.getSheets()[2];\n" +
            "  var sheet3 = source.getSheets()[3];\n" +
            "  var sheet4 = source.getSheets()[4];\n" +
            "  var sheet5 = source.getSheets()[5];\n" +
            "  var sheet6 = source.getSheets()[6];\n" +
            "  var sheet7 = source.getSheets()[7];\n" +
            "  var sheet8 = source.getSheets()[8];\n" +
            "  var sheet9 = source.getSheets()[9];\n" +
            "  var sheet10 = source.getSheets()[10];\n" +
            "  var destination = SpreadsheetApp.openById(destination_id);\n" +
            "  var dsheet1 = destination.getSheets()[1];\n" +
            "  var dsheet2 = destination.getSheets()[2];\n" +
            "  var dsheet3 = destination.getSheets()[3];\n" +
            "  var dsheet4 = destination.getSheets()[4];\n" +
            "  var dsheet5 = destination.getSheets()[5];\n" +
            "  var dsheet6 = destination.getSheets()[6];\n" +
            "  var dsheet7 = destination.getSheets()[7];\n" +
            "  var dsheet8 = destination.getSheets()[8];\n" +
            "  var dsheet9 = destination.getSheets()[9];\n" +
            "  var dsheet10 = destination.getSheets()[10];\n" +
            "  SpreadsheetApp.setActiveSpreadsheet(destination)\n" +
            "  destination.setActiveSheet(dsheet1);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet2);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet3);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet4);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet5);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet6);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet7);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet8);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet9);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  destination.setActiveSheet(dsheet10);\n" +
            "  destination.deleteActiveSheet();\n" +
            "  sheet1.copyTo(destination).setName('Клиенты по категориям');\n" +
            "  sheet2.copyTo(destination).setName('Конв. НК');\n" +
            "  sheet3.copyTo(destination).setName('Конв. ПК');\n" +
            "  sheet4.copyTo(destination).setName('Конв. К');\n" +
            "  sheet5.copyTo(destination).setName('Конв. К все');\n" +
            "  sheet6.copyTo(destination).setName('Свод конверсия');\n" +
            "  sheet7.copyTo(destination).setName('Частотность посещений');\n" +
            "  sheet8.copyTo(destination).setName('Распределение');\n" +
            "  sheet9.copyTo(destination).setName('Товары');\n" +
            "  sheet10.copyTo(destination).setName('Товары по мастерам');\n" +
            "  SpreadsheetApp.flush();";

    private WebDriver driver;
    private Selenium selenium;
    private Robot robot;
    private List<GoogleHashCity> googleHashCityList;

    public int uploadFiles(List<City> cityList) throws InterruptedException {

        selenium = new Selenium();
        selenium.startChromeDriver();

        driver = selenium.getDriver();
        driver.manage().window().maximize();

        robot = null;
        System.setProperty("java.awt.headless", "false");

        googleHashCityList = (List<GoogleHashCity>) googleHashCityRepository.findAll();
        List<Integer> hashCityIdList = googleHashCityList.stream()
                .map(googleHashCity -> googleHashCity.getCityId())
                .collect(Collectors.toList());

        LocalDate localDate = LocalDate.now().withDayOfMonth(1).minusDays(1);
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        List<CityDownloaded> cityDownloadedList = cityDownloadedRepository.findAllByMonthAndYear(month, year);
        List<Integer> cityDownloadedIds = cityDownloadedList.stream()
                .map(cityDownloaded -> cityDownloaded.getCityId())
                .collect(Collectors.toList());

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        for (City city : cityList) {

            File file = new File("C:\\Подработка\\Готовые\\" + city.getName() + ".xlsx");
            if (!file.exists())
                continue;
            if (cityDownloadedIds.contains(city.getId()))
                continue;

            importCityFile(file, city);

            if (hashCityIdList.contains(city.getId())) {
                performGoogleScript(city);
            } else {
                writeCityName(city);
            }

            cityDownloadedRepository.save(new CityDownloaded(city.getId(), month, year));
        }
        selenium.quitChromeDriver();

        return 1;
    }

    private void importCityFile(File file, City city) throws InterruptedException {
        driver.get(siteName);
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElement(By.id("docs-homescreen-add")).click();
        Thread.sleep(3000);
        selenium.waitForJSandJQueryToLoad();

        WebElement fileMenu = driver.findElement(By.id("docs-file-menu"));
        fileMenu.click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        Actions action = new Actions(driver);
        action.moveToElement(fileMenu, 25, 130).click().perform();
        Thread.sleep(2000);
        selenium.waitForJSandJQueryToLoad();

        WebElement iframe = driver.findElement(By.className("picker-frame"));
        System.out.println("iframe id = " + iframe.getAttribute("id"));
        driver.switchTo().frame(iframe);

        driver.findElements(By.className("An-Aq-Zb-Jr")).get(3).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElements(By.className("d-u-F")).get(1).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        StringSelection ss = new StringSelection(file.getAbsolutePath());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

        robot.delay(1000);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);

        driver.switchTo().defaultContent();

        Thread.sleep(5000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElement(By.name("import")).click();
        Thread.sleep(30000);
        selenium.waitForJSandJQueryToLoad();
    }

    private void performGoogleScript(City city) throws InterruptedException {
        WebElement toolsMenu = driver.findElement(By.id("docs-tools-menu"));
        toolsMenu.click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        Actions action = new Actions(driver);
        action.moveToElement(toolsMenu, 25, 85).click().perform();
        Thread.sleep(5000);
        selenium.waitForJSandJQueryToLoad();

        String hashForCity = googleHashCityList.stream()
                .filter(googleHashCity -> googleHashCity.getCityId() == city.getId())
                .map(googleHashCity -> googleHashCity.getGoogleHash())
                .findAny()
                .get();

        String scriptForCity = String.format(SCRIPT_SCHEMA, hashForCity);

        StringSelection ss = new StringSelection(scriptForCity);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

        robot.delay(1000);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        String[] handles = driver.getWindowHandles().stream().toArray(String[]::new);
        System.out.println(Arrays.toString(handles));
        driver.switchTo().window(handles[handles.length - 1]);

        driver.findElement(By.id("saveButton")).click();
        Thread.sleep(2000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElements(By.className("gwt-Button")).get(0).click();
        Thread.sleep(20000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElement(By.id("runButton")).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElements(By.className("gwt-Button")).get(0).click();
        Thread.sleep(2000);
        selenium.waitForJSandJQueryToLoad();

        handles = driver.getWindowHandles().stream().toArray(String[]::new);
        System.out.println(Arrays.toString(handles));
        driver.switchTo().window(handles[handles.length - 1]);

        driver.findElements(By.className("TnvOCe")).get(0).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        handles = driver.getWindowHandles().stream().toArray(String[]::new);
        System.out.println(Arrays.toString(handles));
        driver.switchTo().window(handles[handles.length - 1]);

        driver.findElements(By.className("xTI6Gf")).get(1).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        driver.findElements(By.className("xTI6Gf")).get(3).click();
        Thread.sleep(1000);
        selenium.waitForJSandJQueryToLoad();

        handles = driver.getWindowHandles().stream().toArray(String[]::new);
        System.out.println(Arrays.toString(handles));
        driver.switchTo().window(handles[handles.length - 1]);

        driver.findElements(By.className("RveJvd")).get(1).click();
        driver.switchTo().window(handles[1]);
        Thread.sleep(30000);
        selenium.waitForJSandJQueryToLoad();

        handles = driver.getWindowHandles().stream().toArray(String[]::new);
        System.out.println(Arrays.toString(handles));
        driver.switchTo().window(handles[0]);
    }

    private void writeCityName(City city) throws InterruptedException {
        //todo save hash to db
        driver.findElement(By.className("docs-title-input")).sendKeys(city.getName() + " аналитика");
        robot.delay(1000);

        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Thread.sleep(1000);
    }
}
