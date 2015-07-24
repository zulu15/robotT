package com.robot.taringa;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class Robot {
	private String usuarioCuenta = null;
	public static WebDriver driver = null;
	private String usuarioNotificacion = null;
	private String tipoNotificacion;
	private static final String NOTIFICACION_COMENTARIO_PERFIL = "publicó un mensaje en tu perfil";
	private static final String NOTIFICACION_FOLLOW = " te está siguiendo";
	private static final String NOTIFICACION_COMENTARIO = "respuesta";
	private static final String NOTIFICACION_CHARLA = "comentó";
	private static final String NOTIFICACION_LIKE = "le gustó tu comentario";
	private static final String NOTIFICACION_DISLIKE = "no le gustó tu comentario";

	public void iniciarSesion() {
		try {
			Properties archivoPropiedades = null;
			if (driver == null) {
				// Se instancia el driver para evitar un null pointer
				System.out.println("Iniciando driver...");
				driver = new FirefoxDriver();
				driver.get("https://www.taringa.net/login?redirect=%2F");
				archivoPropiedades = leerConfiguracion();
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
			WebElement esperandoCarga = esperarElemento(By.className("box-crear-post"), 250);
			// Esperamos que cargue el home
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error iniciando sesion: ((" + e + "))");
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

	public Properties leerConfiguracion() {
		Properties prop = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("login.properties");
		if (input != null) {
			try {
				prop = new Properties();
				prop.load(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return prop;
	}

	public void comentar(String comentario, By selectorCaja, By selectorBoton) {

		try {
			WebElement cajaDeTexto = esperarElemento(selectorCaja, 100);
			cajaDeTexto.sendKeys(comentario);
			WebElement botonEnviar = esperarElemento(selectorBoton, 100);
			botonEnviar.click();
			System.out.println("Se envio el comentario..");
		} catch (Exception e) {
			System.out.println("Error comentando: ((" + e + "))");
		}

	}

	/*
	 * ##########################################################################
	 * ##############
	 * ###########################################################
	 * #############################
	 */

	public void clikearJavaScript(WebElement ultimoElemento) {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click();", ultimoElemento);
			System.out.println("Mande el click..");
		} catch (Exception e) {
			System.out.println("Excepcion durante el click con JAVASCRIPT" + e);
		}

	}

	public String getNumeroPost(String url) {
		String[] resultante = url.split("/");
		return resultante[5];
	}

	public WebElement getUltimaRespuestaPost(List<WebElement> lista,String usuarioNotificacion,String idComentarioUrl) {
		WebElement ultimoElemento = null;
		int contador = 0;
		if (lista != null) {
			for (WebElement e : lista) {
				contador++;
				String onclick = e.getAttribute("onclick");
				if (onclick != null && !onclick.isEmpty()) {
					System.out.println("Encontre " + contador + ")" + onclick);
						if(onclick.indexOf("replyBox") > 0
								&& onclick.indexOf(usuarioNotificacion)>0
									 && onclick.indexOf(idComentarioUrl)>0){
							System.out.println("Encontramos uno que cumpla todo!");
								ultimoElemento = e;
							
						} //Encuentra los matches
					}//No esta vacio
				} //For
			}//Param lista no vacio

		return ultimoElemento;

	}
	
	/*
	 * ##########################################################################
	 * ##############
	 * ###########################################################
	 * #############################
	 */
	public String getUltimoComentario(final String usuarioNotificacion, List<WebElement> elementoss) {
		System.out.println("El usuario que recibimos es: " + usuarioNotificacion);
		int contador = 0;
		for (WebElement e : elementoss) {
			contador++;
			System.out.println("Elemento (" + contador + ") [[" + e.getText() + "]]");
			String nuevoComentario = e.getText().replaceAll("\\r\\n|\\r|\\n", " ");
			if (nuevoComentario.indexOf("instantes") > 0 && nuevoComentario.indexOf(usuarioNotificacion) > 0) {
				System.out.println("Encontro uno que cumple ->" + nuevoComentario);
				String coment[] = nuevoComentario.split("instantes");
				return coment[1].trim(); //Agregue trim ultimo dia
			} else {
				System.out.println(contador + ")Elemento no coincide con los criterios");
			}

		}
		return "Error obteniendo ultimo comentario usuario: " + usuarioNotificacion;

	}

	public String getIdComentario(String url) {
		String idComentario = null;
		if (!url.isEmpty() && url != null) {
			String resultante[] = url.split("#comment-");
			idComentario = resultante[1];
		}
		return idComentario;
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

	public static WebDriver getDriver() {
		if (driver == null) {
			driver = new FirefoxDriver();
		}
		return Robot.driver;
	}

	public String getTipoNotificacion() {
		return tipoNotificacion;
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
		System.out.println("El tipo de notificacion es ((" + this.tipoNotificacion + "))");
	}

	public void setUsuarioNotificacion(String notificacion) {

		String[] soloUsuario = null;
		if (!notificacion.isEmpty() && notificacion != null) {
			String[] quitamosAtrasArroba = notificacion.split("@");
			soloUsuario = quitamosAtrasArroba[1].split(" ");
			System.out.println("Obtuvimos el usuario: " + soloUsuario[0]);
			this.usuarioNotificacion = soloUsuario[0];
		}

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

	public String obtenerAgradecimientoAleatorio() {
		ArrayList<String> frasesAleatorias = null;
		try {
			frasesAleatorias = new ArrayList<String>();
			String cadena;
			FileReader f = new FileReader("bot-info/frasesUsuario");
			BufferedReader b = new BufferedReader(f);
			while ((cadena = b.readLine()) != null) {
				frasesAleatorias.add(cadena);
			}
			b.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return frasesAleatorias.get((int) (Math.random() * 21));

	}
}
