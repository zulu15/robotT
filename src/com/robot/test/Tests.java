package com.robot.test;

import java.util.List;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Tests {
	public static WebDriver driver;

	public static void main(String[] args) {
		try {

			
				String comentarioAnidado = "hace @-g3nius- instantes @yiyorock No sé... No tengo una.";
				System.out.println(comentarioAnidado.indexOf("@-g3nius-"));
			
			
		} catch (Exception e) {
			System.out.println("Excepcion " + e);
		}

	}
	private static String obtenerNumeroPost(String url) {
		if(url.indexOf("-g3nius-")>0){
			url=url.replaceAll("-g3nius-", "/");
			url=url.replaceAll("\\?", "/");
		}
		String []resultante = url.split("/");
		int contador=0;
		for(int i=0;i<resultante.length;i++){
			contador++;
			System.out.println(contador+") "+resultante[i]);
		}
	
	
		return resultante[5];
	}
/*
 * FUnca
 */
	private static String obtenerSolamenteComentario(final String comentario,final String usuario) {
		String comentarioFormateado = comentario.replaceAll("\\r\\n|\\r|\\n", " ");
		System.out.println("El comentario formateado es: "+comentarioFormateado);
		if(comentarioFormateado.indexOf(usuario)>0 && comentarioFormateado.indexOf("instantes")>0){
			String coment[] = comentarioFormateado.split("instantes");
			return coment[1];
		}
		return "No lo encontramos";
	}

	public static String obtenerComentario(String comentario, String usuario) {

		if (comentario.indexOf("hace instantes") > 0 && comentario.indexOf(usuario) > 0) {
			String borroUsuario = comentario.replace(usuario, "");
			String soloComentario[] = borroUsuario.split("hace instantes");
			for (String e : soloComentario) {
				System.out.println("Prueba ->" + e);

			}
			return soloComentario[1];
		}
		return "Fallo la condicion";

	}

	public static String getUsuarioNotificacion(String notificacion) {

		String usuario=null;
		if (!notificacion.isEmpty() && notificacion != null) {
			String[] divido = notificacion.split(" ");
			usuario = divido[0].replace("@", "");
		}
		return usuario;

	}

	public static String getIdComentario(String url) {
		String idComentario = null;
		if (!url.isEmpty() && url != null) {
			String resultante[] = url.split("#comment-");
			idComentario = resultante[1];
		}
		return idComentario;
	}

	public WebElement getUltimoComentario(List<WebElement> elementoss) {
		int contador = 0;
		for (WebElement e : elementoss) {
			int total = 0;
			contador++;
			System.out.println("Elemento (" + contador + ") [[" + e.getText() + "]]");
			String h = (String) e.getAttribute("onclick");
			System.out.println(contador + ") onClick=" + h);
			if (h != null) {
				if (h.indexOf("@corpussenex") > 0 && h.indexOf("34885897") > 0 && h.indexOf("this") > 0) {
					total++;
					System.out.println("Encontramos algo ^__^ en total llevo: " + total);
					return e;
				}
			}

		}
		return null;

	}

	/**
	 * Devuelve el ultimo elemento que encontramos de respuesta 'span'
	 */

	public WebElement ultimaRespuesta(List<WebElement> lista,String usuarioNotificacion,String idComentarioUrl) {
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

	public void clikearJavaScript(WebElement ultimoElemento) {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click();", ultimoElemento);
			System.out.println("Mande el click..");
		} catch (Exception e) {
			System.out.println("Excepcion durante el click con JAVASCRIPT" + e);
		}

	}

}
