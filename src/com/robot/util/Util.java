package com.robot.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public final class Util {

	public static String obtenerAgradecimientoAleatorio() {
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
	public static String obtenerNumeroComentario(String url){
		String []numero = url.split("#comment-");
		return numero[1].trim();
	}
	
	public static String getNumeroPost(String url, String usuarioBot) {
		if (url.indexOf(usuarioBot) > 0) {
			url = url.replaceAll(usuarioBot, "/");
			url = url.replaceAll("\\?", "/");
		}
		String[] resultante = url.split("/");
		return resultante[5];
	}
	
	public static String getNumeroShout(String x) {
		x = x.replaceAll("\\)", "");
		String soloNumero[] =x.split(",");
		return soloNumero[1].trim();
	}
	
	

	public static String getIdComentario(String url) {
		String idComentario = null;
		if (!url.isEmpty() && url != null) {
			String resultante[] = url.split("#comment-");
			idComentario = resultante[1];
		}
		return idComentario;
	}

	public static Properties leerConfiguracion() {
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


	

	

}
