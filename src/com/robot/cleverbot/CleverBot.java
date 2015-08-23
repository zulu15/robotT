package com.robot.cleverbot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class CleverBot {
	public static WebDriver driver = null;

	public static String obtenerRespuestaIA(String comentario) {
		System.out.println("(CLEVERBOT) ultimo comentario: '" + comentario + "'");
		if(comentario.indexOf("@")>=0) {
			comentario = comentario.replaceAll("@","");
		}
		String respuesta = null;
		try {
			if (driver == null) {
				System.out.println("Cleverbot: Levante el driver");
				driver = getDriver();
				driver.get("http://www.cleverbot.com/");
			}

			WebElement barraTexto = esperarElemento(By.className("stimulus"), 250);
			barraTexto.sendKeys(comentario);
			barraTexto.submit();
			WebElement terminoDeContestar = esperarElemento(By.id("snipTextIcon"), 500);
			WebElement respuestaBot = esperarElemento(By.id("line1"), 250);
			respuesta = respuestaBot.getText().trim(); //Agregue trim ultima dia
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respuesta;
	}

	public static WebElement esperarElemento(final By locatorKey, long timeout) {
		Wait<WebDriver> wait = new WebDriverWait(driver, timeout);

		WebElement element = wait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				return driver.findElement(locatorKey);
			}
		});

		return element;
	}
	public CleverBot(){
		driver = CleverBot.getDriver();
		driver.get("http://www.cleverbot.com/");
	}

	public static WebDriver getDriver() {
		if (driver == null) {
			return new FirefoxDriver();
		}
		return driver;
	}
}
