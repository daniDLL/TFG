package vt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.ReportingResult;

/**
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta JAVE es transcodificar ficheros de video
 * o audio. Su utilización en esta clase es extraer el audio de un video (transcodificar un video a audio).
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class AdegaTool extends Filtro{

	private static final String pisixde_path = "./";
	private static final String conf_path = pisixde_path + "example.properties";
	
	private String out = "";

	/**
	 * Constructor de JaveTool con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public AdegaTool(BufferPipe bpi, BufferPipe bpo, Semaphore S, String output) {
		super(bpi, bpo, S, "Adega");
		out = output;
	}

	/**
	 * Constructor de JaveTool con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public AdegaTool(BufferPipe bp, DataSink dsk, Semaphore S, String output){
		super(bp, dsk, S, "Adega");
		out = output;
	}

	/**
	 * Constructor de JaveTool con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public AdegaTool(DataSource ds, BufferPipe bp, String output){
		super(ds, bp, "Adega");
		out = output;
	}

	private Vector<String> loadTerms(File f){
		Vector<String> v = new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(f));

			String linea;
			while((linea = br.readLine()) != null){
				if(linea.isEmpty() || linea.contains("task_id")){
					continue;
				}
				else{
					linea = linea.substring(0,1).toUpperCase() + linea.subSequence(1,linea.length());
					v.add(linea);
				}
			}
			br.close();
			return v;
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			return v;
		}
		catch (IOException e) {
			e.printStackTrace();
			return v;
		}
	}

	private void saveContextFile(File context, Vector<String> terms){
		try{
			PrintWriter pw = new PrintWriter(new FileWriter(context));

			for(String t : terms){
				pw.println("1.0-" + t);
			}

			pw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private void savePropertiesFile(String term, File f, String resourceName){
		File sprop = new File(conf_path);

		try{
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			BufferedReader br = new BufferedReader(new FileReader(sprop));

			String linea;
			while((linea = br.readLine()) != null){
				if(linea.startsWith("context")){
					pw.println("context=" + resourceName + ".context");
				}
				else if(linea.startsWith("simple_term")){
					pw.println("simple_term=" + term);
				}
				else if(linea.startsWith("uri_expansion")){
					pw.println("uri_expansion=" + "http://dbpedia.org/resource/" + term);
				}
				else if(linea.startsWith("file_xml")){
					pw.println("file_xml=" + resourceName + "_" + term + ".xml");
				}
				else{
					pw.println(linea);
				}
			}
			
			pw.close();
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Este método realiza la extracción del audio del fichero leído de la entrada de datos.
	 */
	public void doWork() {
		File f = ((DataBlock) dt.getData()).getFile();
		String uri = ((DataBlock) dt.getData()).getUri();
		String e = ((DataBlock) dt.getData()).getResourceName();

		System.out.println("[Adega] Starting ...");
		Date date = new Date();


		//generar context
		File fcontext = new File(e + ".context");
		Vector<String> terms = loadTerms(f);
		saveContextFile(fcontext, terms);

		//generar properties
		Vector<File> vf = new Vector<File>();

		for(String t : terms){
			File nf = new File(e + "_" + t + ".properties");
			savePropertiesFile(t, nf, e);
			vf.add(nf);
		}

		//lanzar script pisixde.sh
		/*try{
			String command = "sh " + pisixde_path + "pisixde.sh ";
			
			for(File fp : vf){
				command += fp.getName() + " ";
			}
			
			System.out.println("command: " + command);
			
			Process proc = Runtime.getRuntime().exec(command);
			String salida = "";

			BufferedReader salida_descarga = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			while((salida=salida_descarga.readLine())!=null){
				System.out.println(salida);
			}
			proc.waitFor();
		}
		catch(IOException e1){
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/


		Date ndate = new Date();

		double time = (ndate.getTime()-date.getTime())/1000.0;
		System.out.println("[Adega] Time elapsed: " + time + " sec.");
 
		Analysis.addTime(e, id, time, -1);
		
		Data d = new DataContainer(); d.setData(new DataBlock(f, uri, e));
		writeToOutput(d);
	}

	/**
	 * Este método devuelve información referente al estado del Filtro.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Filter.
	 * @see ReportingResult
	 */
	public ReportingResult report() {
		return null;
	}

}
