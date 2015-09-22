package vt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.ReportingResult;



/**
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta PybossaTool es 
 * generar las tareas correspondientes a los ficheros resultantes del pipe en el proyecto Pybossa para
 * ser validados por usuarios.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class PybossaTool extends Filtro{

	private static final String server_address = "127.0.0.1:5000";
	private static final String api_key = "76b0bc80-4d31-498f-aaf4-c0f021cebe1b";
	private static final int num_answers = 2;
	private static final int project_id = 4;
	
	private String out = "";

	/**
	 * Constructor de PybossaTool con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public PybossaTool(BufferPipe bpi, BufferPipe bpo, Semaphore S, String output) {
		super(bpi, bpo, S, "Pybossa");
		out = output;
	}

	/**
	 * Constructor de PybossaTool con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public PybossaTool(BufferPipe bp, DataSink dsk, Semaphore S, String output){
		super(bp, dsk, S, "Pybossa");
		out = output;
	}

	/**
	 * Constructor de PybossaTool con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public PybossaTool(DataSource ds, BufferPipe bp, String output){
		super(ds, bp, "Pybossa");
		out = output;
	}

	/**
	 * 
	 * @param mt
	 * @return
	 */
	private Vector<String> readTerms(File mt){
		Vector<String> terms = new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mt));

			String linea;
			while((linea = br.readLine()) != null){
				terms.add(linea);
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return terms;
	}

	/**
	 * 
	 * @param terms
	 * @param videoURL
	 * @return
	 */
	private String buildJSONData(Vector<String> terms, String videoURL){
		String json = "{\"info\" : {\"oembed\" : \"" + videoURL + "\", \"keywords\" : {";

		for(int i = 0; i < terms.size(); i++){
			String t = terms.get(i);

			if(i == terms.size() - 1){ //último elemento.
				json += "\"k" + (i + 1) + "\" : \"" + t + "\"";
			}
			else{
				json += "\"k" + (i + 1) + "\" : \"" + t + "\",";
			}
		}
		json += "}}, \"project_id\":\"" + project_id + "\", \"n_answers\":\"" + num_answers + "\"}";

		return json;
	}

	/**
	 * 
	 * @param terms
	 * @param videoURL
	 * @return
	 */
	private int sendRequest(Vector<String> terms, String videoURL){

		try{

			String url = "http://localhost:5000/api/task?api_key=" + api_key;
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			String crlf = "\r\n";

			//write data
			String data = buildJSONData(terms,videoURL);
			wr.writeBytes(data);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			System.out.println("code: " + responseCode);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String jsonText = response.toString();
			System.out.println(jsonText);

			JSONParser parser = new JSONParser();
			JSONObject j = (JSONObject)parser.parse(jsonText);

			Object result = j.get("id");
			if(result == null){ //error
				return -1;
			}

			return Integer.parseInt(result.toString());
			
		}
		catch(IOException e1){
			e1.printStackTrace();
			return -1;
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Este método genera las tareas en el proyecto Pybossa mediante la API proporcionada por Pybossa.
	 */
	public void doWork() {
		File majorityTerms = ((DataBlock) dt.getData()).getFile();
		String e = ((DataBlock) dt.getData()).getResourceName();
		String URL = ((DataBlock) dt.getData()).getUri();
		
		Date date = new Date();
		System.out.println("[Pybossa] Starting ...");

		Vector<String> terms = readTerms(majorityTerms);

		int id_task = -1;//sendRequest(terms,URL);

		if(id_task != -1){
			File pybossa_file = new File(out + e + "-pybossa_task_created.txt");
			
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(pybossa_file));
				
				pw.println("task_id:" + id_task);
				pw.println();
				
				for(String t : terms){
					pw.println(t);
				}
				pw.close();
				
			} 
			catch (IOException e1) {
				e1.printStackTrace();
			}
			
			Data d = new DataContainer(); d.setData(new DataBlock(pybossa_file, ((DataBlock) dt.getData()).getUri(), ((DataBlock) dt.getData()).getResourceName()));
			writeToOutput(d);
		}
		else{ //si algo falla, pasamos el fichero origen.
			Data d = new DataContainer(); d.setData(new DataBlock(majorityTerms, ((DataBlock) dt.getData()).getUri(), ((DataBlock) dt.getData()).getResourceName()));
			writeToOutput(d);
		}
		
		Date ndate = new Date();

		double time = (ndate.getTime()-date.getTime())/1000.0;
		System.out.println("[Pybossa] Time elapsed: " + time + " sec.");
		
		Analysis.addTime(e, id, time, -1);
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
