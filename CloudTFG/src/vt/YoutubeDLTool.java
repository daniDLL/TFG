package vt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.ReportingResult;

/**
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta YoutubeDL es descargar videos 
 * alojados en diferentes servidores, solamente utilizando la URI del video.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class YoutubeDLTool extends Filtro{

	/**
	 * Constructor de YoutubeDLTool con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public YoutubeDLTool(BufferPipe bpi, BufferPipe bpo, Semaphore S) {
		super(bpi, bpo, S, "YoutubeDL");
	}
	
	/**
	 * Constructor de YoutubeDLTool con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public YoutubeDLTool(BufferPipe bp, DataSink dsk, Semaphore S){
		super(bp, dsk, S, "YoutubeDL");
	}
	
	/**
	 * Constructor de YoutubeDLTool con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public YoutubeDLTool(DataSource ds, BufferPipe bp){
		super(ds, bp, "YoutubeDL");
	}
	
	/**
	 * Este método genera el comando para descargar el video con la URI <uri>.
	 * 
	 * @param uri la URI del video a descargar.
	 * @param title el nombre que queremos darle al fichero a descargar.
	 * @return
	 */
	private String getDownloadCommand(String uri, String title){
		return "youtube-dl -o " + title + ".%(ext)s -f best " + uri;
	}

	/**
	 * Este método obtiene el título de un video.
	 * 
	 * @param uri la URI del video.
	 * @return devuelve el título del video con URI <uri>.
	 */
	private String getVideoTitle(String uri){
		String command = "youtube-dl --get-title " + uri;
		String title = "";
		try {
			Process p = Runtime.getRuntime().exec(command);

			String salida = "";

			BufferedReader salida_descarga = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while((salida=salida_descarga.readLine())!=null){
				title += salida;
			}
			p.waitFor();

		}
		catch (IOException e1){
			e1.printStackTrace();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		title = "./tmpFiles/" + title.replaceAll("\\s","_").replaceAll("\\.","").replaceAll("-","").replaceAll("#","").replaceAll(":","");
		
		return title;
	}
	
	/**
	 * Este método obtiene la extensión de un video sabiendo su URI (escogiendo el de mejor calidad "best" en
	 * el caso de Youtube como servidor de alojamiento).
	 * 
	 * @param uri la URI del video.
	 * @return devuelve la extensión del video con URI <uri>.
	 */
	private String getVideoExtension(String uri){
		String command = "youtube-dl --list-formats " + uri;
		String extension = "";
		try {
			Process p = Runtime.getRuntime().exec(command);

			String salida = "";

			BufferedReader salida_descarga = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while((salida=salida_descarga.readLine())!=null){
				if(salida.contains("best")){
					salida = salida.replaceAll("\\s+"," ");
					String[] s = salida.split(" ");
					extension = s[1];
				}
				else if(salida.contains("unknown")){
					salida = salida.replaceAll("\\s+"," ");
					String[] s = salida.split(" ");
					extension = s[1];
				}
			}
			p.waitFor();

		} catch (IOException e1){
			e1.printStackTrace();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		return extension;
	}

	/**
	 * Este método realiza la descarga del video con la URI leída como entrada de datos al filtro.
	 */
	public void doWork(){
		
		String uri = ((DataBlock) dt.getData()).getUri();
		File video = null;

		System.out.println("[YoutubeDL] Starting download ...");
		Date date = new Date();
		
		try {
			String title = getVideoTitle(uri);
			String ext = getVideoExtension(uri);
			
			//Comprobar si ya existe un fichero con el mismo nombre.
			File root = new File(".");
			final Pattern pat = Pattern.compile(title + "\\.\\w+");
		    File[] fl = root.listFiles(new FileFilter(){
		        @Override
		        public boolean accept(File file) {
		            return pat.matcher(file.getName()).matches();
		        }
		    });
		    
		    if(fl.length != 0){
		    	title += new Date().toGMTString().replaceAll("\\s","");
		    }
			
			String download = getDownloadCommand(uri,title);
			
			System.out.println("command: " + download);

			Process proc = Runtime.getRuntime().exec(download);
			String salida = "";

			BufferedReader salida_descarga = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			while((salida=salida_descarga.readLine())!=null){
				System.out.println(salida);
			}
			proc.waitFor();

			video = new File(title + "." + ext);
		} catch (IOException e1){
			e1.printStackTrace();
		}
		catch( InterruptedException e) {
			e.printStackTrace();
		}
		
		Date ndate = new Date();

		double time = (ndate.getTime()-date.getTime())/1000.0;
		System.out.println("[YoutubeDL] Time elapsed: " + time + " sec.");
		System.out.println("YOutubeDL video path: " + video.getAbsolutePath() );
		
		String e = video.getName().split("\\.")[0];
		
		Analysis.addTime(uri, id, time, -1);
		
		//Escribimos la salida en el pipe.
		Data d = new DataContainer();
		d.setData(new DataBlock((File) video, uri, e));
		writeToOutput(d);
	}

	/**
	 * Este método devuelve información referente al estado del Filtro.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Filter.
	 * @see ReportingResult
	 */
	public ReportingResult report(){
		return null;
	}

}
