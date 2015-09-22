package vt;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import Test.InputSourceFromDirectory;
import Test.URLInputSource;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.Filter;
import pipeline.Pipeline;
import pipeline.ReportingResult;

/**
 * Esta clase implementa la interfaz Pipeline definiendo todos los componentes del sistema e iniciando la ejecución.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class VideoLabelling implements Pipeline{
	
	private final static int urlinputs = 1;
	private final static int urlsinputs = 2;
	private static String inputfile;
	private static String[] inputfiles;
	private int typeinput;
	private static String destinationFolder;
	private Thread t1,t2,t3,t4,t5,t6;
	
	private String out = "./tmpFiles2/";

	public VideoLabelling(){
		//this.inputfile = url;
		//this.destinationFolder = output;
		//this.typeinput = urlinputs;
	}
	
	public VideoLabelling(String url, String output){
		this.inputfile = url;
		this.destinationFolder = output;
		this.typeinput = urlinputs;
	}
	
	public VideoLabelling(String[] urls, String output){
		this.inputfiles = urls;
		this.destinationFolder = output;
		this.typeinput = urlsinputs;
	}
	
	private DataSource DataSourceFactory(int type){
		if(type == urlinputs){
			return new URLInputSource(inputfile);
		}
		else{
			return new URLInputSource(inputfiles);
		}
	}
	
	/**
	 * Este método define todos los componentes del sistema.
	 */
	public void design(){
		//DataSource ds = DataSourceFactory(typeinput);
		DataSource ds = new InputSourceFromDirectory(new File("./fileInput/"));
		DataSink dsk = new OutputSource("./results/",6);
		Semaphore S1 = new Semaphore(0);
		Semaphore S2 = new Semaphore(0);
		Semaphore S3 = new Semaphore(0);
		Semaphore S4 = new Semaphore(0);
		Semaphore S5 = new Semaphore(0);
		BufferPipe bp1 = new Pipe(S1,1);
		BufferPipe bp2 = new Pipe(S2,2);
		BufferPipe bp3 = new Pipe(S3,3);
		BufferPipe bp4 = new Pipe(S4,4);
		BufferPipe bp5 = new Pipe(S5,5);
		//Data d = new DataContainer();
		//d.setData(new DataBlock(new File("./tmpFiles/results/Chimps_have_feelings_and_thoughts_They_should_also_have_rights-transcribed-voting_majority_keywords.txt"),
		//								"https://www.youtube.com/watch?v=Fxt_MZKMdes",
		//								"Chimps_have_feelings_and_thoughts_They_should_also_have_rights"));
		//bp1.write(d);
		//BufferPipe bp2 = new Pipe(S2,2);
		//BufferPipe bp3 = new Pipe(S3,3);
		/*Data d = new DataContainer();
		d.setData(new File("./tmpFiles/virtual_reality_subtitle.txt"));
		bp3.write(d);*/
		//Filter f1 = new YoutubeDLTool(ds,bp1);
		Filter f2 = new JaveTool(ds,bp2,out);
		Filter f3 = new IBMWS(bp2,bp3,S2,out);
		//Filter f3 = new SphinxTool(bp2,bp3,S2);
		Filter f4 = new JateTool(bp3,bp4,S3,out);
		Filter f5 = new PybossaTool(bp4,bp5,S4,out);
		Filter f6 = new AdegaTool(bp5,dsk,S5,out);
		//t1 = new Thread(f1);
		t2 = new Thread(f2);
		t3 = new Thread(f3);
		t4 = new Thread(f4);
		t5 = new Thread(f5);
		t6 = new Thread(f6);
	}

	/**
	 * Este método inicia la ejecución de los procesos del sistema.
	 */
	public void run() {
		Analysis.startGlobalClock();
		design();
		//t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		try{
			//t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
		
		/*File f = new File("./hola.txt");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(f.getPath());*/
	}

	/**
	 * Este método devuelve información referente al estado del Pipeline.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Pipeline.
	 * @see ReportingResult
	 */
	public ReportingResult report() {
		return null;
	}
	
	/**
	 * Método principal del programa.
	 * 
	 * @param args los argumentos de entrada al programa.
	 */
	public static void main(String[] args){
		//inputfile = args[0];
		//destinationFolder = args[1];
		//Analysis.startGlobalClock();
		new VideoLabelling().run();
	}

}
