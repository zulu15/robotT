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
	public static WebDriver driver = null;
	private String usuarioNotificacion = null;
	private String tipoNotificacion = null;
	private static final String NOTIFICACION_COMENTARIO_PERFIL = "publicó un mensaje en tu perfil";
	private static final String NOTIFICACION_FOLLOW = " te está siguiendo";
	private static final String NOTIFICACION_COMENTARIO = "respuesta";
	private static final String NOTIFICACION_CHARLA = "comentó";
	private static final String NOTIFICACION_LIKE = "le gustó tu comentario";
	private static final String NOTIFICACION_DISLIKE = "no le gustó tu comentario";
	private static final String TEXTBOX_SEGUIDOR = "my-shout-body-wall";
	private static final String TEXTBOX_PERFIL_COMENTARIO = "body_comm";
	private static final String TARINGA_URL_HOME = "http://www.taringa.net/";
	private static final String NOTIFICACION = "notification";

	public Robot() {
		iniciarSesion();
		automatizarProcesos();
	}
	

	private void automatizarProcesos() {
		while (true) {
			try {
				System.out.println("(MAIN) Esperando por notificaciones..");
				WebElement notificaciones = esperarElemento(By.className(NOTIFICACION), 1800);
				String notificacion = notificaciones.getText();
				System.out.println("(MAIN) Llego una notificacion => (" + notificacion + ")");
				setTipoNotificacion(notificacion);
				if (!getTipoNotificacion().equals("like") && !getTipoNotificacion().equals("dislike")) {
					notificaciones.click();
					setUsuarioNotificacion(notificacion);
					if (getTipoNotificacion().equals("comentario-perfil")) {
						responderPerfil();
					}
					if (getTipoNotificacion().equals("seguidor")) {
						agradecerSeguidor();
					}
					if (getTipoNotificacion().equals("comentario-respuesta-charla")) {
						responderCharla();

					}
					if (getTipoNotificacion().equals("comentario-respuesta-post")) {
						reponderPost();
					}// Fin comentario-respuesta-post

				}// Fin validacion 'dislike && likes'
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

	private void responderPerfil() {
		WebElement comentario = esperarElemento(By.cssSelector("div.activity-content > p"), 100);
		String comentarioUsuario = comentario.getText();
		String respuestaInteligente = CleverBot.obtenerRespuestaIA(comentarioUsuario);
		comentar(respuestaInteligente, By.id(TEXTBOX_PERFIL_COMENTARIO), By.id("comment-button-text"));

	}

	private void reponderPost() {
		List<WebElement> listaComentarios = driver.findElements(By.className("comment-text"));
		String ultimoComentario = getUltimoComentario(getUsuarioNotificacion(), listaComentarios);
		String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
		List<WebElement> listaRespuestas = driver.findElements(By.cssSelector("a.hastipsy"));
		WebElement ultimoElemento = getUltimaRespuestaPost(listaRespuestas, getUsuarioNotificacion(), Util.getIdComentario(driver.getCurrentUrl()),respuesta);
		System.out.println("El ultimo elemento seria.. " + ultimoElemento.getAttribute("onclick"));
		if (ultimoElemento != null) {
			clikearJavaScript(ultimoElemento);
			StringBuilder selectorCaja = new StringBuilder();
			selectorCaja.append("textarea#body_comm_reply_").append(Util.getNumeroPost(driver.getCurrentUrl(), getUsuarioCuenta()));
			comentar(respuesta, By.cssSelector(selectorCaja.toString()), By.cssSelector("button.btn.g.require-login"));
		}
	}

	private void responderCharla() {
		try {
			List<WebElement> listaComentarios = driver.findElements(By.className("comment-text"));
			String ultimoComentario = getUltimoComentario(getUsuarioNotificacion(), listaComentarios);
			String respuesta = CleverBot.obtenerRespuestaIA(ultimoComentario);
			List<WebElement> listaRespuestas = driver.findElements(By.cssSelector("a.hastipsy"));
			WebElement ultimoElemento = getUltimaRespuestaPost(listaRespuestas, getUsuarioNotificacion(), Util.getIdComentario(driver.getCurrentUrl()), respuesta);
			System.out.println("El ultimo elemento seria.. " + ultimoElemento.getAttribute("onclick"));
			if (ultimoElemento != null) {
				clikearJavaScript(ultimoElemento);
				StringBuilder selectorCaja = new StringBuilder();
				selectorCaja.append("textarea#body_comm_reply_").append(Util.getNumeroPost(driver.getCurrentUrl(), getUsuarioCuenta()));
				comentar(respuesta, By.cssSelector(selectorCaja.toString()), By.cssSelector("button.btn.g.require-login"));
			}

		} catch (Exception e) {
			System.out.println("(Main) Excepcion 'comentario-respuesta-charla' => (" + e + ")");
		}

	}

	private void agradecerSeguidor() {
		try {
			navegar(TARINGA_URL_HOME + getUsuarioNotificacion());
			comentar(Util.obtenerAgradecimientoAleatorio(), By.id(TEXTBOX_SEGUIDOR), By.cssSelector("button[class='my-shout-add btn a floatR wall']"));
		} catch (Exception e) {
			System.out.println("Error agradeciendo seguidor: " + e);
		}

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
			String usuario = archivoPropiedades.getProperty("usuario");
			setUsuarioCuenta(usuario);
			String password = archivoPropiedades.getProperty("password");
			WebElement formUsuario = driver.findElement(By.name("nick"));
			formUsuario.sendKeys(usuario);
			// Dentro del login se ingresa el usuario
			WebElement formPassword = driver.findElement(By.name("pass"));
			formPassword.sendKeys(password);
			formPassword.submit();
		} catch (Exception e) {
			System.out.println(e);
		}
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

	public void comentar(final String comentario,final By selectorCaja,final By selectorBoton) {

		try {
			WebElement cajaDeTexto = esperarElemento(selectorCaja, 100);
			cajaDeTexto.sendKeys(comentario);
			WebElement botonEnviar = esperarElemento(selectorBoton, 100);
			botonEnviar.click();
			System.out.println("Se envio el comentario..");
		} catch (Exception e) {
			System.out.println("(Comentar) Error => (" + e + ")");
		}

	}

	// ## ULTIMOS METODOS AGREGADOS ##

	public void clikearJavaScript(WebElement ultimoElemento) {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click();", ultimoElemento);
			System.out.println("Mande el click..");
		} catch (Exception e) {
			System.out.println("(JavaScript) Excepcion =>" + e);
		}

	}

	public WebElement getUltimaRespuestaPost(List<WebElement> lista, String usuarioNotificacion, String idComentarioUrl, String respuesta) {
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
		if (ultimoElemento == null) {
			String respuestaPerzonalizada="@"+getUsuarioNotificacion()+" "+respuesta;
			comentar(respuestaPerzonalizada, By.id(TEXTBOX_PERFIL_COMENTARIO), By.id("comment-button-text"));
		}
		return ultimoElemento;

	}


	//Encontro uno que cumple ->@-g3nius- hace instantes @yiyorock No sé... No tengo una.
	public String getUltimoComentario(String usuarioNotificacion, List<WebElement> elementoss) {
		// Trabajando con los primeros 15 elementos de la lista para optimizar
		// rendimiento
		String myUserr = "@" + getUsuarioCuenta();
		int contador = 0;
		while (contador <= 30) {
			for (WebElement e : elementoss) {
				contador++;
				System.out.println("Elemento (" + contador + ") [[" + e.getText() + "]]");
				String nuevoComentario = e.getText().replaceAll("\\r\\n|\\r|\\n", " ");
				if (nuevoComentario.indexOf("instantes") > 0 && nuevoComentario.indexOf(usuarioNotificacion) > 0 
						&& nuevoComentario.indexOf("@"+getUsuarioCuenta())!=0) {
					String coment[] = nuevoComentario.split("instantes");
					if (coment[1].indexOf(myUserr) > 0) {
						coment[1] = coment[1].replaceAll(myUserr, " ");
					}
					return coment[1].trim(); // Agregue trim ultimo dia
				}
			}
		}

		return "Error obteniendo ultimo comentario usuario: " + usuarioNotificacion;

	}

	// ## FIN ULTIMOS METODOS AGREGADOS ##

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
		System.out.println("(setTipoNotificacion) Notificacion = (" + this.tipoNotificacion + ")");
	}

	public void setUsuarioNotificacion(String notificacion) {
		String usuario = null;
		if (!notificacion.isEmpty() && notificacion != null) {
			String[] divido = notificacion.split(" ");
			usuario = divido[0].replace("@", "");
			System.out.println("(Usuario notificacion) => " + usuario);
			this.usuarioNotificacion = usuario;

		}
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

	public String getUsuarioNotificacion() {
		return usuarioNotificacion;
	}

	public String getUsuarioCuenta() {
		return usuarioCuenta;
	}

	public void setUsuarioCuenta(String usuarioCuenta) {

		this.usuarioCuenta = usuarioCuenta;
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

}
