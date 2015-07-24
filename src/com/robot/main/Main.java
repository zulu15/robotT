package com.robot.main;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.robot.cleverbot.CleverBot;
import com.robot.taringa.Robot;

public class Main {
	private static final String TEXTBOX_SEGUIDOR = "my-shout-body-wall";
	private static final String TEXTBOX_PERFIL_COMENTARIO = "body_comm";
	private static final String TARINGA_URL_HOME = "http://www.taringa.net/";
	private static final String NOTIFICACION = "notification";

	public static void main(String[] args) {

		WebDriver driver = null;
		try {
			Robot robot = new Robot();
			robot.iniciarSesion();
			driver = Robot.getDriver();
			do {
				System.out.println("Esperando por notificaciones..");
				WebElement notificaciones = robot.esperarElemento(By.className(NOTIFICACION), 1800);
				String notificacion = notificaciones.getText();
				robot.setTipoNotificacion(notificacion);
				if (!robot.getTipoNotificacion().equals("like") && !robot.getTipoNotificacion().equals("dislike")) {
					notificaciones.click();
					robot.setUsuarioNotificacion(notificacion);
					if (robot.getTipoNotificacion().equals("comentario-perfil")) {
						WebElement comentario = robot.esperarElemento(By.cssSelector("div.activity-content > p"), 100);
						String comentarioUsuario = comentario.getText();
						String respuestaInteligente = CleverBot.obtenerRespuestaIA(comentarioUsuario);
						robot.comentar(respuestaInteligente, By.id(TEXTBOX_PERFIL_COMENTARIO), By.id("comment-button-text"));
					}
					if (robot.getTipoNotificacion().equals("seguidor")) {
						try {
							robot.navegar(TARINGA_URL_HOME + robot.getUsuarioNotificacion());
							robot.comentar(robot.obtenerAgradecimientoAleatorio(), By.id(TEXTBOX_SEGUIDOR), By.cssSelector("button[class='my-shout-add btn a floatR wall']"));
						} catch (Exception e) {
							System.out.println("Error agradeciendo seguidor: " + e);
						}
					}
					if (robot.getTipoNotificacion().equals("comentario-respuesta-charla")) {
						try {

							List<WebElement> listaElementos = driver.findElements(By.className("comment-replies-container"));
							if (!listaElementos.isEmpty()) {
								String ultimoComentario = robot.getUltimoComentario(robot.getUsuarioNotificacion(), listaElementos);
								String respuestaInteligente = CleverBot.obtenerRespuestaIA(ultimoComentario);
								robot.comentar(respuestaInteligente, By.id(TEXTBOX_PERFIL_COMENTARIO), By.id("comment-button-text"));
							}
						} catch (Exception e) {
							System.out.println("Error respondiendo en charla shout mi! ((" + e + "))");
							break;
						}

					}
					if (robot.getTipoNotificacion().equals("comentario-respuesta-post")) {
						List<WebElement> listaComentarios = driver.findElements(By.className("comment-text"));
						String ultimoComentario = robot.getUltimoComentario(robot.getUsuarioNotificacion(), listaComentarios);
						String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
						List<WebElement> listaRespuestas = driver.findElements(By.cssSelector("a.hastipsy"));
						WebElement ultimoElemento = robot.getUltimaRespuestaPost(listaRespuestas, robot.getUsuarioNotificacion(), robot.getIdComentario(driver.getCurrentUrl()));
						System.out.println("El ultimo elemento seria.. " + ultimoElemento.getAttribute("onclick"));
						if (ultimoElemento != null) {
							robot.clikearJavaScript(ultimoElemento);
							StringBuilder selectorCaja = new StringBuilder();
							selectorCaja.append("textarea#body_comm_reply_").append(robot.getNumeroPost(driver.getCurrentUrl())); // Aqui
							WebElement cajaTexto = driver.findElement(By.cssSelector(selectorCaja.toString()));
							cajaTexto.sendKeys(respuesta);
							WebElement botonEnviar = driver.findElement(By.cssSelector("button.btn.g.require-login"));
							botonEnviar.click();
						}
					}
					if (!driver.getCurrentUrl().equals(TARINGA_URL_HOME)) {
						driver.get(TARINGA_URL_HOME);
					}

				}
			} while (true);
		} catch (Exception e) {
			System.out.println("Error ((MAIN)) '" + e + "'\n Me quede en: " + driver.getCurrentUrl());

		}
	}
}
