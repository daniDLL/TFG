package vt;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;
import java.util.Date;
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
public class JaveTool extends Filtro{
	
	private String out = "";

	/**
	 * Constructor de JaveTool con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public JaveTool(BufferPipe bpi, BufferPipe bpo, Semaphore S, String output) {
		super(bpi, bpo, S, "Jave");
		out = output;
	}
	
	/**
	 * Constructor de JaveTool con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public JaveTool(BufferPipe bp, DataSink dsk, Semaphore S, String output){
		super(bp, dsk, S, "Jave");
		out = output;
	}
	
	/**
	 * Constructor de JaveTool con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public JaveTool(DataSource ds, BufferPipe bp, String output){
		super(ds, bp, "Jave");
		out = output;
	}
	
	/**
	 * Este método realiza la extracción del audio del fichero leído de la entrada de datos.
	 */
	public void doWork() {
		File source = ((DataBlock) dt.getData()).getFile();
		String uri = ((DataBlock) dt.getData()).getUri();

		String e = source.getName().split("\\.")[0];
		
		File target = new File(out + e + ".wav");
		
		System.out.println("[Jave] Loading Video: " + e + " ...");
		System.out.println("[Jave] Starting conversion ...");
		Date date = new Date();
		
		//Configuraciones del audio para el modelo acustico utilizado por sphinx.
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le"); 								//pcm signed 16 bit little endian format
		audio.setSamplingRate(new Integer(16000));					//sampling rate 16Khz
		audio.setChannels(1);										//1 channel (mono).
		
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		
		Encoder encoder = new Encoder();
		
		try {
			encoder.encode(source, target, attrs);
			
		} catch (IllegalArgumentException e1){
			e1.printStackTrace();
		}
		catch(EncoderException e1) {
			e1.printStackTrace();
		}
		
		Date ndate = new Date();

		double time = (ndate.getTime()-date.getTime())/1000.0;
		System.out.println("[Jave] Time elapsed: " + time + " sec.");
		
		Analysis.addTime(e, id, time, -1);
		
		//Eliminamos el fichero de video, dado que ya no lo necesitamos en las tareas siguientes
		//y asi eliminamos los ficheros residuales del sistema.
		//source.delete();
		//Escribimos la salida en el pipe.
		Data d = new DataContainer();
		d.setData(new DataBlock((File) target, uri, e));
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
