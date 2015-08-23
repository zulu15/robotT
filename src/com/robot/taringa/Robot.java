package com.robot.taringa;

import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.robot.cleverbot.CleverBot;
import com.robot.util.Util;

public class Robot {
	private String usuarioCuenta = null;
	private String passwordCuenta = null;
	public static WebDriver driver = null;
	private String usuarioNotificacion = null;
	private String tipoNotificacion = null;
	private static final String NOTIFICACION_COMENTARIO_PERFIL = "publicó un mensaje en tu perfil";
	private static final String NOTIFICACION_FOLLOW = " te está siguiendo";
	private static final String NOTIFICACION_COMENTARIO = "respuesta";
	private static final String NOTIFICACION_CHARLA = "comentó";
	private static final String NOTIFICACION_LIKE = "gustó";
	private static final String NOTIFICACION_DISLIKE = "no le gustó tu comentario";
	private static final String TEXTBOX_SEGUIDOR = "my-shout-body-wall";
	private static final String TEXTBOX_PERFIL_COMENTARIO = "body_comm";
	private static final String TARINGA_URL_HOME = "http://www.taringa.net/";
	private static final String NOTIFICACION = "notification";
	private static final String SECCION_MI_TARINGA = "/mi/";

	public Robot(String usuario, String password) {
		this.usuarioCuenta = usuario;
		this.passwordCuenta = password;
		iniciarSesion();
		automatizarProcesos();
	}

	public Robot() {
		new CleverBot();
		iniciarSesion();
		automatizarProcesos();
	}

	public void iniciarSesion() {
		try {
			Properties archivoPropiedades = null;
			if (driver == null) {
				// Se instancia el driver para evitar un null pointer
				System.out.println("Iniciando driver...");
				driver = new FirefoxDriver();
				driver.get("https://www.taringa.net/login?redirect=%2F");
				archivoPropiedades = Util.leerConfiguracion();
			}
			if (usuarioCuenta == null || passwordCuenta == null) {
				usuarioCuenta = archivoPropiedades.getProperty("usuario");
				setUsuarioCuenta(usuarioCuenta);
				passwordCuenta = archivoPropiedades.getProperty("password");
			}

			WebElement formUsuario = driver.findElement(By.name("nick"));
			formUsuario.sendKeys(usuarioCuenta);
			// Dentro del login se ingresa el usuario
			WebElement formPassword = driver.findElement(By.name("pass"));
			formPassword.sendKeys(passwordCuenta);
			formPassword.submit();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void automatizarProcesos() {
		while (true) {
			try {
				System.out.println("(MAIN) Esperando por notificaciones..");
				WebElement notificaciones = esperarElemento(By.className(NOTIFICACION), 1800);
				String notificacion = notificaciones.getText();
				System.out.println("(MAIN) Llego una notificacion => (" + notificacion + ")");
				setTipoNotificacion(notificacion);
				if (!getTipoNotificacion().equals("dislike")) {
					notificaciones.click();
					setUsuarioNotificacion(notificacion.trim());
					if (getTipoNotificacion().equals("comentario-perfil")) {
						responderPerfil();
					}
					if (getTipoNotificacion().equals("seguidor")) {
						agradecerSeguidor();
					}
					if (getTipoNotificacion().equals("comentario-respuesta-charla")) {
						if (isShout(driver.getCurrentUrl())) {
							responderShout();
						} else {
							responderCharlaPerfil();
						}

					}
					if (getTipoNotificacion().equals("comentario-respuesta-post")) {
						reponderPost();
					}
					if (getTipoNotificacion().equals("like")) {
						seguirUsuario();
					}
				}
				// Fin validacion 'dislike && likes'
				if (!driver.getCurrentUrl().equals(TARINGA_URL_HOME)) {
					System.out.println("(MAIN) Volvemos a la home");
					driver.get(TARINGA_URL_HOME);
				}
			}// Fin try
			catch (Exception e) {
				System.out.println("Error ((MAIN)) '" + e + "'\n Me quede en: " + driver.getCurrentUrl());

			}// Fin catch
		}// Fin while

	}// Fin metodo

	private void seguirUsuario() {
		try {
			navegar(TARINGA_URL_HOME + getUsuarioNotificacion());
			WebElement btnSeguir = esperarElemento(By.cssSelector("a.btn.g.not-following"), 100);
			clikearJavaScript(btnSeguir);
		} catch (Exception e) {
			System.out.println("Error siguiendo usuario " + e);
		}

	}

	private void responderPerfil() {
		WebElement comentario = esperarElemento(By.cssSelector("div.activity-content > p"), 100);
		String comentarioUsuario = comentario.getText();
		String respuestaInteligente = CleverBot.obtenerRespuestaIA(comentarioUsuario);
		comentar(respuestaInteligente, By.id(TEXTBOX_PERFIL_COMENTARIO), By.id("comment-button-text"));

	}

	private void agradecerSeguidor() {
		try {
			navegar(TARINGA_URL_HOME + getUsuarioNotificacion());
			comentar(Util.obtenerAgradecimientoAleatorio(), By.id(TEXTBOX_SEGUIDOR), By.cssSelector("button[class='my-shout-add btn a floatR wall']"));
		} catch (Exception e) {
			System.out.println("Error agradeciendo seguidor: " + e);
		}

	}

	private void responderShout() {
		System.out.println("''Shout''");
		try {
			List<WebElement> listaComentarios = driver.findElements(By.className("comment-text"));
			String ultimoComentario = getUltimoComentario(getUsuarioNotificacion(), listaComentarios);
			String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
			List<WebElement> todosLinks = driver.findElements(By.cssSelector("a.hastipsy"));
			WebElement ultimoElemento = obtenerEnlace(Util.getIdComentario(driver.getCurrentUrl()), todosLinks);
			if (ultimoElemento != null) {
				clikearJavaScript(ultimoElemento);
				StringBuilder selectorCaja = new StringBuilder();
				// Se saca el numero de shout a partir del boton de comentar
				WebElement numeroShout = esperarElemento(By.cssSelector("button#comment-button-text"), 60);
				String onClick = numeroShout.getAttribute("onclick");
				selectorCaja.append("textarea#body_comm_reply_").append(Util.getNumeroShout(onClick));
				comentar(respuesta, By.cssSelector(selectorCaja.toString()), By.cssSelector("button.btn.g.require-login"));
			} else {
				System.out.println("Nulo llego el ultimo elemento");
			}

		} catch (Exception e) {
			System.out.println("(Main) Excepcion 'Respondiendo shout' => (" + e + ")");
		}
	}

	private void responderCharlaPerfil() {
		try {
			System.out.println("(RESPONDER CHARLA PERFIL)");
			List<WebElement> listaComentarios = esperarElementos(By.className("comment-text"), 100);
			String ultimoComentario = getUltimoComentario(getUsuarioNotificacion(), listaComentarios);
			String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
			List<WebElement> todosLosLinks = esperarElementos(By.cssSelector("a.hastipsy"), 100);
			WebElement ultimoElemento = obtenerEnlace(Util.getIdComentario(driver.getCurrentUrl()), todosLosLinks);
			if (ultimoElemento != null) {
				clikearJavaScript(ultimoElemento);
				StringBuilder selectorCaja = new StringBuilder();
				// Se saca el numero de shout a partir del boton de comentar
				WebElement numeroShout = esperarElemento(By.cssSelector("button#comment-button-text"), 60);
				String onClick = numeroShout.getAttribute("onclick");
				selectorCaja.append("textarea#body_comm_reply_").append(Util.getNumeroShout(onClick));
				comentar(respuesta, By.cssSelector(selectorCaja.toString()), By.id("comment-button-text"));
			} else {
				System.out.println("Nulo llego el ultimo elemento");
			}

		} catch (Exception e) {
			System.out.println("(Main) Excepcion 'Respondiendo Perfil' => (" + e + ")");
		}

	}

	private WebElement obtenerEnlace(String idComentario, List<WebElement> listaRespuestas) {
		WebElement ultimo = null;
		if (listaRespuestas != null && !idComentario.isEmpty()) {
			for (WebElement webElement : listaRespuestas) {
				String onClick = webElement.getAttribute("onclick");
				if (onClick != null) {
					if (onClick.indexOf(idComentario) > 0 && onClick.indexOf("this") > 0) {
						ultimo = webElement;
					}
				}
			}
		}
		return ultimo;
	}

	private void reponderPost() {
		List<WebElement> listaComentarios = esperarElementos(By.className("comment-text"), 60);
		String ultimoComentario = getUltimoComentario(getUsuarioNotificacion(), listaComentarios);
		String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
		List<WebElement> listaRespuestas = esperarElementos(By.cssSelector("a.hastipsy"), 60);
		WebElement ultimoElemento = getUltimaRespuestaPost(listaRespuestas, getUsuarioNotificacion(), Util.getIdComentario(driver.getCurrentUrl()));
		if (ultimoElemento != null) {
			clikearJavaScript(ultimoElemento);
			StringBuilder selectorCaja = new StringBuilder();
			selectorCaja.append("textarea#body_comm_reply_").append(Util.getNumeroPost(driver.getCurrentUrl(), getUsuarioCuenta()));
			comentar(respuesta, By.cssSelector(selectorCaja.toString()), By.cssSelector("button.btn.g.require-login"));
		}
	}

	public WebElement getUltimaRespuestaPost(List<WebElement> lista, String usuarioNotificacion, String idComentarioUrl) {
		// Trabajando con los primeros 50 enlaces para optimizar el codigo
		WebElement ultimoElemento = null;
		int contador = 0;
		if (lista != null) {
			for (WebElement e : lista) {
				contador++;
				String onclick = e.getAttribute("onclick");
				if (onclick != null && !onclick.isEmpty()) {
					System.out.println("Encontre " + contador + ")" + onclick);
					if (onclick.indexOf("replyBox") > 0 && onclick.indexOf(usuarioNotificacion) > 0 && onclick.indexOf(idComentarioUrl) > 0) {
						if (contador >= 50) {
							break;
						}
						System.out.println("Encontramos uno que cumple con todo!");
						ultimoElemento = e;

					} // Encuentra los matches
				}// No esta vacio
			} // For
		}// Param lista no vacio

		return ultimoElemento;

	}

	// Encontro uno que cumple ->@-g3nius- hace instantes @yiyorock No sé... No
	// tengo una.(
	public String getUltimoComentario(String usuarioNotificacion, List<WebElement> elementos) {
		String ultimoComentario = "No encontre un comentario que cumpla todo";
		String comentario = "";
		String usuarioNotif = getUsuarioNotificacion();
		System.out.println("El usuario de la notificacion es " + usuarioNotif);
		String myUserr = "@" + getUsuarioCuenta();
		int contador = 0;
		try {
			for (WebElement elemento : elementos) {
				contador++;
				comentario = elemento.getText().replaceAll("\\r\\n|\\r|\\n", " ");
				;
				if (comentario.indexOf("instantes") > 0 && comentario.indexOf(usuarioNotif) >= 0) {
					String coment[] = comentario.split("instantes");
					if (coment[1].indexOf(myUserr) > 0) {
						coment[1] = coment[1].replaceAll(myUserr, " ");
					}
					ultimoComentario = coment[1].trim();
				}
				if (contador >= 30) {

					return ultimoComentario;
				}
			}
		} catch (Exception e) {
			System.out.println("Excepcion " + e);
		}

		System.out.println("El ultimo comentario detectado es " + ultimoComentario);

		return ultimoComentario;

	}

	public void setTipoNotificacion(String tipoNotificacion) {

		if (tipoNotificacion.indexOf(NOTIFICACION_FOLLOW) > 0) {
			this.tipoNotificacion = "seguidor";
		} else if (tipoNotificacion.indexOf(NOTIFICACION_COMENTARIO) > 0 || tipoNotificacion.indexOf("mencionó") > 0 || tipoNotificacion.indexOf("respondió") > 0) {
			this.tipoNotificacion = "comentario-respuesta-post";
		} else if (tipoNotificacion.indexOf(NOTIFICACION_DISLIKE) > 0) {
			this.tipoNotificacion = "dislike";
		} else if (tipoNotificacion.indexOf(NOTIFICACION_LIKE) > 0) {
			this.tipoNotificacion = "like";
		} else if (tipoNotificacion.indexOf(NOTIFICACION_COMENTARIO_PERFIL) > 0) {
			this.tipoNotificacion = "comentario-perfil";
		} else if (tipoNotificacion.indexOf(NOTIFICACION_CHARLA) > 0) {
			this.tipoNotificacion = "comentario-respuesta-charla";
		} else {
			this.tipoNotificacion = "dislike";
		}
		System.out.println("La notificacion es == " + getTipoNotificacion());
	}

	public WebElement esperarElemento(final By locatorKey, long timeout) {
		Wait<WebDriver> wait = new WebDriverWait(driver, timeout);
		WebElement element = wait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				return driver.findElement(locatorKey); // Devuelve el elemento
														// buscado
			}
		});

		return element;
	}

	public List<WebElement> esperarElementos(final By locatorKey, long timeout) {
		Wait<WebDriver> wait = new WebDriverWait(driver, timeout);
		List<WebElement> elementos = wait.until(new Function<WebDriver, List<WebElement>>() {
			public List<WebElement> apply(WebDriver driver) {
				return driver.findElements(locatorKey); // Devuelve el elemento
														// buscado
			}
		});

		return elementos;
	}

	public void comentar(String comentario, By selectorCaja, By selectorBoton) {

		try {
			WebElement cajaDeTexto = esperarElemento(selectorCaja, 100);
			cajaDeTexto.sendKeys(comentario);
			WebElement botonEnviar = esperarElemento(selectorBoton, 100);
			botonEnviar.click();
			navegar(TARINGA_URL_HOME);
		} catch (Exception e) {
			System.out.println("(Comentar) Error => (" + e + ")");
		}

	}

	public void clikearJavaScript(WebElement ultimoElemento) {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click();", ultimoElemento);
			System.out.println("(CLick JavaScript)");
		} catch (Exception e) {
			System.out.println("(JavaScript) Excepcion =>" + e);
		}

	}

	private boolean isShout(String currentUrl) {
		return currentUrl.indexOf(SECCION_MI_TARINGA) > 0;
	}

	public void navegar(String url) {
		if (driver == null) {
			driver = new FirefoxDriver();
		}
		try {
			driver.get(url);
		} catch (Exception e) {
			System.out.println("Excepcion metodo navegar: ((" + e + "))");
		}

	}

	public void setUsuarioCuenta(String usuarioCuenta) {

		this.usuarioCuenta = usuarioCuenta;
	}

	public String getUsuarioCuenta() {
		return usuarioCuenta;
	}

	public void setUsuarioNotificacion(String notificacion) {

		String[] usuario = null;
		if (!notificacion.isEmpty() && notificacion != null) {
			String[] quitoArroba = notificacion.split("@");
			usuario = quitoArroba[1].split(" ");

		}
		this.usuarioNotificacion = usuario[0];
	}

	public String getUsuarioNotificacion() {
		return usuarioNotificacion;
	}

	public static WebDriver getDriver() {
		if (driver == null) {
			driver = new FirefoxDriver();
		}
		return Robot.driver;
	}

	public String getTipoNotificacion() {
		return tipoNotificacion;
	}

}
