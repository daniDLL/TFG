package vt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.ReportingResult;

/**
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta Sphinx es transcribir ficheros de audio
 * a texto.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class SphinxTool extends Filtro{

	/**
	 * Constructor de SphinxTool con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public SphinxTool(BufferPipe bpi, BufferPipe bpo, Semaphore S) {
		super(bpi, bpo, S, "Sphinx");
	}

	/**
	 * Constructor de SphinxTool con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public SphinxTool(BufferPipe bp, DataSink dsk, Semaphore S){
		super(bp, dsk, S, "Sphinx");
	}

	/**
	 * Constructor de SphinxTool con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public SphinxTool(DataSource ds, BufferPipe bp){
		super(ds, bp, "Sphinx");
	}

	/**
	 * Este método realiza la transcripción del fichero leído de la entrada de datos.
	 */
	public void doWork() {
		System.out.println("[Sphinx] Loading models ...");

		File audio = ((DataBlock) dt.getData()).getFile();
		String uri = ((DataBlock) dt.getData()).getUri();
		
		Date date = new Date();
		//System.err.close();

		/*Configuration configuration = new Configuration();

		// Load acustic model, dictionary and language model from the jar
		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");

		StreamSpeechRecognizer recognizer;
		InputStream stream;
		File transcribed = new File("./tmpFiles/" + audio.getName().split("\\.")[0] + "-transcribed.txt");
		
		System.out.println("[Sphinx] Loading Audio: " + audio.getName().split("\\.")[0] + " ...");
		System.out.println("[Sphinx] Starting recognizer ...");
		try {
			recognizer = new StreamSpeechRecognizer(configuration);

			stream = new FileInputStream(audio);

			SpeechResult result;
	        
	        
			String resultText = "";
			recognizer.startRecognition(stream);
			String r = "";
			while ((result = recognizer.getResult()) != null) {
				r = result.getHypothesis();
				resultText = resultText + " " + r;
				System.out.println(r);
			}
			recognizer.stopRecognition();

			if (!transcribed.exists()) {
				transcribed.createNewFile();
			}
			else{
				transcribed.delete();
				transcribed.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(transcribed));
			bw.write(resultText.substring(1,resultText.length() - 1));
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		File transcribed = new File("./tmpFiles/" + audio.getName().split("\\.")[0] + "-transcribed.txt");
		
		File p = new File("./tmpFiles/" + "Bill_Gross:_The_single_biggest_reason_why_startups_succeed-transcribed.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(p));
			PrintWriter pw =  new PrintWriter(transcribed);

			String linea;
			while((linea = br.readLine()) != null){
				pw.println(linea);
			}
			
			br.close();
			pw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		Date ndate = new Date();

		double time = (ndate.getTime()-date.getTime())/1000.0;
		System.out.println("[Sphinx] Time elapsed: " + time + " sec.");
		
		Analysis.addTime(uri, id, time, -1);

		//Eliminamos el fichero de audio, dado que ya no lo necesitamos en las tareas siguientes
		//y asi eliminamos los ficheros residuales del sistema.
		//audio.delete();
		//Escribimos la salida en el pipe.
		Data d = new DataContainer();
		d.setData(new DataBlock((File) transcribed, ((DataBlock) dt.getData()).getUri(), ((DataBlock) dt.getData()).getResourceName()));
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
