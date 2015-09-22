package vt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta IBMWS es transcribir ficheros de audio
 * a texto utilizando los servicios web suministrados por IBM Bluemix.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class IBMWS extends Filtro{
	
	private String out = "";

	/**
	 * Constructor de IBMWS con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public IBMWS(BufferPipe bpi, BufferPipe bpo, Semaphore S, String output) {
		super(bpi, bpo, S, "IBMWS");
		out = output;
	}

	/**
	 * Constructor de IBMWS con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public IBMWS(BufferPipe bp, DataSink dsk, Semaphore S, String output){
		super(bp, dsk, S, "IBMWS");
		out = output;
	}

	/**
	 * Constructor de IBMWS con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public IBMWS(DataSource ds, BufferPipe bp, String output){
		super(ds, bp, "IBMWS");
		out = output;
	}

	/*public String curlRequest(String file) throws IOException, InterruptedException{

		String command = "curl -u 260912a9-1271-43b5-a8e6-f72d13aac0e4:GjOigAKbZELD -X POST --data-binary"
				+ " @" + file + " -H Content-Type:audio/l16;rate=16000"
				+ " https://stream.watsonplatform.net/speech-to-text-beta/api/v1/recognize?continuous=true";
		System.out.println(command);
		Process p = Runtime.getRuntime().exec(command);
		String result = "";
		String salida = "";

		BufferedReader salida_descarga = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		while((salida=salida_descarga.readLine())!=null){
			result += salida;
		}
		p.waitFor();

		String transcribedText = result.replaceAll("\\[","").replaceAll("\\{","").replaceAll("\\]","")
				.replaceAll("\\}","").replaceAll("\"","")
				.replaceAll(",","").replaceAll("final: true","").replaceAll("   ","")
				.replaceAll("result_index: 0", "").replaceAll("alternatives: transcript: ","")
				.replaceAll("results: ", "");

		return transcribedText;
	}*/

	private File splitAudio(File sourceFile, File destinationFile, int startSecond, int secondsToSplit) {
		AudioInputStream inputStream = null;
		AudioInputStream shortenedStream = null;
		try{
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
			AudioFormat format = fileFormat.getFormat();
			inputStream = AudioSystem.getAudioInputStream(sourceFile);
			int bytesPerSecond = format.getFrameSize() * (int)format.getFrameRate();
			inputStream.skip(startSecond * bytesPerSecond);
			long framesOfAudioToCopy = secondsToSplit * (int)format.getFrameRate();
			shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
			AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);
		} 
		catch (Exception e){
			e.printStackTrace();
		} 
		finally{
			if (inputStream != null) try { inputStream.close(); } catch (Exception e) { e.printStackTrace(); }
			if (shortenedStream != null) try { shortenedStream.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		return destinationFile;
	}

	/**
	 * Este método realiza la transcripción del fichero leído de la entrada de datos.
	 */
	public void doWork() {
		File audio = ((DataBlock) dt.getData()).getFile();
		String uri = ((DataBlock) dt.getData()).getUri();

		String e = ((DataBlock) dt.getData()).getResourceName();
		
		System.out.println("[IBMWS] Loading Audio: " + e + " ...");

		File transcribed = null;

		try{
			Date date = new Date();
			
			//Calcular tamaño en segundos del audio.
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio);
			AudioFormat format = audioInputStream.getFormat();
			long frames = audioInputStream.getFrameLength();
			double durationInSeconds = (frames + 0.0) / format.getFrameRate();

			int part = -1;
			double rest = -1.0;
			Vector<Thread> vT = new Vector<Thread>();
			Vector<Pair<String,Integer>> transText = new Vector<Pair<String,Integer>>();
			if((durationInSeconds % 180) != 0){
				part = (int)durationInSeconds / 180;
				rest = durationInSeconds - (180 * part);
				
				int startSecond = 0;
				int numPart = 0;
				for(int i = 0; i < part; i++){
					File file = splitAudio(audio, new File("./tmpFiles/" + e + "_part" + (numPart+1) + ".wav"), startSecond, 180);
					IBMThread ibmt = new IBMThread(file,numPart,transText);
					Thread T = new Thread(ibmt);
					vT.add(T);
					startSecond += 180;
					numPart++;
				}
				File file = splitAudio(audio, new File("./tmpFiles/" + e + "_part" + (numPart+1) + ".wav"), startSecond, (int)Math.ceil(rest));
				IBMThread ibmt = new IBMThread(file,numPart,transText);
				Thread T = new Thread(ibmt);
				vT.add(T);
			}
			else{
				part = (int)durationInSeconds / 180;
				
				int startSecond = 0;
				int numPart = 0;
				for(int i = 0; i < part; i++){
					File file = splitAudio(audio, new File("./tmpFiles/" + e + "_part" + (numPart+1) + ".wav"), startSecond, 180);
					IBMThread ibmt = new IBMThread(file,numPart,transText);
					Thread T = new Thread(ibmt);
					vT.add(T);
					startSecond += 180;
					numPart++;
				}
			}
			
			for(int i = 0; i < vT.size(); i++){
				vT.get(i).start();
			}

			try{
				for(int i = 0; i < vT.size(); i++){
					vT.get(i).join();
				}
			}
			catch(InterruptedException e1){
				e1.printStackTrace();
			}

			String transcribedText = "";
			for(int i = 0; i < transText.size(); i++){
				for(int j = 0; j < transText.size(); j++){
					if(transText.get(j).getR() == i){
						transcribedText += transText.get(j).getL();
						break;
					}
				}
			}

			transcribed = new File(out + e + "-transcribed.txt");

			if (!transcribed.exists()) {
				transcribed.createNewFile();
			}
			else{
				transcribed.delete();
				transcribed.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(transcribed));
			bw.write(transcribedText);
			bw.close();

			Date ndate = new Date();
			double time = (ndate.getTime()-date.getTime())/1000.0;
			System.out.println("[IBMWS] Time elapsed: " + time + " sec.");

			Analysis.addTime(e, id, time, -1);

			Data d = new DataContainer();
			d.setData(new DataBlock(transcribed, uri, e));
			writeToOutput(d);
		}
		catch(IOException e1 ){
			e1.printStackTrace();
		}
		catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}
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
