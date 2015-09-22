package Test;

import java.util.Date;

import pipeline.Data;
import pipeline.DataSource;
import vt.DataBlock;
import vt.DataContainer;

/**
 * Esta clase implementa la interfaz DataSource. De esta forma instanciamos la entrada de datos del sistema "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class URLInputSource implements DataSource{

	private String url;
	private String[] urls;
	private boolean empty = false;
	private boolean multipleURLs = false;
	private int actual = 0;
	
	/**
	 * Constructor del URLInputSource.
	 * 
	 * @param inputfile URL del video.
	 */
	public URLInputSource(String url){
		this.url = url;
		this.multipleURLs = false;
	}
	
	/**
	 * Constructor del URLInputSource.
	 * 
	 * @param inputfile URL del video.
	 */
	public URLInputSource(String[] urls){
		this.urls = urls;
		this.multipleURLs = true;
		this.actual = 0;
	}

	/**
	 * Este método lee una URI (una por línea) del fichero y la devuelve como tipo Data.
	 */
	public Data read() {
		Date d = new Date();
		if(multipleURLs){
			if(actual < urls.length){
				Data dc = new DataContainer();
				dc.setData(new DataBlock(urls[actual]));
				Analysis.startLocalClock(urls[actual], d);
				actual++;
				return dc;
			}
			else{
				return null;
			}
		}
		else{
			if(!empty){
				empty = true;
				Data dc = new DataContainer();
				dc.setData(new DataBlock(url));
				Analysis.startLocalClock(url, d);
				return dc;
			}
			else{
				return null;
			}
		}
	}

	/**
	 * UNIMPLEMENTED
	 */
	public void setProperties() {
		
	}
}