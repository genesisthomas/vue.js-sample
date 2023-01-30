package perfecto;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import io.appium.java_client.AppiumDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@ExtendWith(PerfectoWatcher.class)
public class TestBase {

  public static RemoteWebDriver driver;
  public static ReportiumClient reportiumClient;

  @BeforeEach
  public void setUp() throws Exception {
    String type = "local";
    if (type == "local"){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("start-maximized"); // open Browser in maximized mode
        options.addArguments("disable-infobars"); // disabling infobars
        options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--no-sandbox"); // Bypass OS security model
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
	} else{
        // Replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as maven properties: -DcloudName=<<cloud name>>
        String cloudName = "<<cloud name>>";
        // Replace <<security token>> with your perfecto security token or pass it as  maven properties: -DsecurityToken=<<SECURITY TOKEN>> More info:
        // https://developers.perfectomobile.com/display/PD/Generate+security+tokens
        String securityToken = "<<security token>>";
        DesiredCapabilities capabilities = new DesiredCapabilities("","", Platform.ANY);
        capabilities.setCapability("securityToken",Utils.fetchSecurityToken(securityToken));
        capabilities.setCapability("platformName", "Windows");
        capabilities.setCapability("platformVersion", "11");
        capabilities.setCapability("browserName", "Chrome");
        capabilities.setCapability("browserVersion", "latest");
        capabilities.setCapability("location", "US East");
        capabilities.setCapability("resolution", "1024x768");
        if (System.getProperty("tunnelId") != null) capabilities.setCapability(
          "tunnelId",
          System.getProperty("tunnelId")
        );

        try {
          driver =
            new RemoteWebDriver(
              new URL(
                "https://" +
                Utils.fetchCloudName(cloudName) +
                ".perfectomobile.com/nexperience/perfectomobile/wd/hub"
              ),
              capabilities
            );
          driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        } catch (SessionNotCreatedException e) {
          throw new RuntimeException(
            "Driver not created with capabilities: " + capabilities.toString()
          );
        }
        reportiumClient = Utils.setReportiumClient(driver, reportiumClient); // Creates reportiumClient
    }
  }

  @Test
  void connect() {
    String title = "event";
    driver.get("http://nexus-a.kaholodemo.net:8090/");
    WebDriverWait wait = new WebDriverWait(driver, 5);
    wait.until(
      ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[@placeholder='Title']")
      )
    );
    driver.findElement(By.xpath("//*[@placeholder='Title']")).sendKeys(title);
    driver
      .findElement(By.xpath("//*[@placeholder='Detail']"))
      .sendKeys("check out");
    driver
      .findElement(By.xpath("//*[@placeholder='Date']"))
      .sendKeys("1999-08-08");
    driver.findElement(By.xpath("//button[contains(.,'Submit')]")).click();

    By event = By.xpath("//h4[contains(.,'" + title + "')]");
    wait.until(ExpectedConditions.presenceOfElementLocated(event));
    WebElement data = driver.findElement(event);
    Utils.assertContainsText(data, reportiumClient, title);
  }

  @Test
  @Disabled
  void perfectoConnect() {
    String title = "event";
    reportiumClient.testStart(
      "Junit Perfecto Connect Test",
      new TestContext("tunnel", "test")
    ); // Starts the reportium test

    reportiumClient.stepStart("Add an event"); // Starts a reportium step
    driver.get("http://0.0.0.0:8090/");
    WebDriverWait wait = new WebDriverWait(driver, 5);
    wait.until(
      ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[@placeholder='Title']")
      )
    );
    driver.findElement(By.xpath("//*[@placeholder='Title']")).sendKeys(title);
    driver
      .findElement(By.xpath("//*[@placeholder='Detail']"))
      .sendKeys("check out");
    driver
      .findElement(By.xpath("//*[@placeholder='Date']"))
      .sendKeys("1999-08-08");
    driver.findElement(By.xpath("//button[contains(.,'Submit')]")).click();
    reportiumClient.stepEnd();

    reportiumClient.stepStart("Verify if the event is added");
    By event = By.xpath("//h4[contains(.,'" + title + "')]");
    wait.until(ExpectedConditions.presenceOfElementLocated(event));
    WebElement data = driver.findElement(event);
    Utils.assertContainsText(data, reportiumClient, title);
    reportiumClient.stepEnd();
  }

  @Test
  @Disabled
  void failTest() {
    // Fail test wantedly ( currently disabled )
    reportiumClient.testStart(
      "Junit Perfecto Connect Fail Test",
      new TestContext("fail", "tag3")
    );
    reportiumClient.stepStart("Verify test fails");
    driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    driver.get("http://0.0.0.0:8090/");
    driver
      .findElement(By.xpath("//*[contains(@placeholder,':test91823749283')]"))
      .isDisplayed();
    reportiumClient.stepEnd();
  }

  public RemoteWebDriver getDriver() {
    return driver;
  }

  public ReportiumClient getReportiumClient() {
    return reportiumClient;
  }
}
