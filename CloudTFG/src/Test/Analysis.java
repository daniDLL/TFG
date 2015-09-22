package Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Analysis {

	private static Map<String,Vector<MapContainer>> m = new HashMap<String,Vector<MapContainer>>();
	private static Map<String,Date> localClockMap = new HashMap<String,Date>();
	private static Vector<String> keys = new Vector<String>();
	
	private static Date d_initial_global;
	private static Date d_final_global;
	
	private static String out = "./tmpFiles2/";

	protected Analysis(){
		//Para restringir instanciación.
	}
	
	public static void addTime(String element, String idProcess, double time, int type){
		if(m.get(element) != null){
			if(idProcess == null){ //Tiempo total
				m.get(element).add(new MapContainer(MapContainer.tTotal, idProcess, time, MapContainer.RWPROCESS));
			}
			else{
				if(type == MapContainer.READ){
					m.get(element).add(new MapContainer(MapContainer.tBUFFER, idProcess, time, MapContainer.READ));
				}
				else if(type == MapContainer.WRITE){
					m.get(element).add(new MapContainer(MapContainer.tBUFFER, idProcess, time, MapContainer.WRITE));
				}
				else{
					m.get(element).add(new MapContainer(MapContainer.tPROCESS, idProcess, time, MapContainer.RWPROCESS));
				}
			}
		}
		else{
			Vector<MapContainer> vm = new Vector<MapContainer>();
			if(type == MapContainer.READ){
				vm.add(new MapContainer(MapContainer.tBUFFER, idProcess, time, MapContainer.READ));
			}
			else if(type == MapContainer.WRITE){
				vm.add(new MapContainer(MapContainer.tBUFFER, idProcess, time, MapContainer.WRITE));
			}
			else{
				vm.add(new MapContainer(MapContainer.tPROCESS, idProcess, time, MapContainer.RWPROCESS));
			}
			m.put(element, vm);
			keys.add(element);
		}
	}
	
	public static void addSize(String element, String idPipe, double size){
		if(m.get(element) != null){
			m.get(element).add(new MapContainer(MapContainer.tPIPE, idPipe, size, MapContainer.RWPROCESS));
		}
		else{
			Vector<MapContainer> vm = new Vector<MapContainer>();
			vm.add(new MapContainer(MapContainer.tPIPE, idPipe, size, MapContainer.RWPROCESS));
			m.put(element, vm);
			keys.add(element);
		}
	}
	
	public static void printInformation(){
		boolean init = true;
		
		String total_data = "";
		for(String key : keys){
			Vector<MapContainer> vm = m.get(key);
			if(init){
				String data = "";
				String data2 = "";
				double tProcess = 0.0;
				//System.out.printf("Video name");
				data2 += "Video name"; 
				for(MapContainer mc : vm){
					if(mc.getType() == MapContainer.tPROCESS){
						//System.out.printf("\t\tProcess%d",mc.getId());
						data2 += "\t\tProcess-" + mc.getId();
						data += "\t\t" + mc.getValue() + "s";
						tProcess += mc.getValue();
					}
					else if(mc.getType() == MapContainer.tPIPE){
						//System.out.printf("\t\tPipe%d",mc.getId());
						data2 += "\t\tPipe-" + mc.getId();
						data += "\t\t" + mc.getValue()/1000000.0 + "MB";
					}
					else if(mc.getType() == MapContainer.tTotal){
						//System.out.printf("\t\ttProcess");
						data2 += "\t\ttProcess";
						data += "\t\t" + tProcess + "s";
						//System.out.printf("\t\ttTotal");
						data2 += "\t\ttTotal";
						data += "\t\t" + mc.getValue() + "s";
					}
					else if(mc.getType() == MapContainer.tBUFFER){
						if(mc.getRW() == MapContainer.READ){
							data2 += "\t\ttBRead";
						}
						else if(mc.getRW() == MapContainer.WRITE){
							data2 += "\t\ttBWrite";
						}
						data += "\t\t" + mc.getValue() + "s";
					}
				}
				data = data2 + "\n" + key + data + "\n";
				//System.out.printf("\n");
				init = false;
				//System.out.print(key);
				//System.out.println(data);
				total_data += data; 
			}
			else{
				String data = "";
				double tProcess = 0.0;
				//System.out.print(key);
				data += key;
				for(MapContainer mc : vm){
					if(mc.getType() == MapContainer.tPROCESS){
						data += "\t\t" + mc.getValue() + "s";
						tProcess += mc.getValue();
					}
					else if(mc.getType() == MapContainer.tPIPE){
						data += "\t\t" + mc.getValue()/1000000 + "MB";
					}
					else if(mc.getType() == MapContainer.tTotal){
						data += "\t\t" + tProcess + "s";
						data += "\t\t" + mc.getValue() + "s";
					}
					else if(mc.getType() == MapContainer.tBUFFER){
						data += "\t\t" + mc.getValue() + "s";
					}
				}
				//System.out.println(data);
				total_data += data + "\n";
			}
		}
		System.out.print(total_data);
		File fout = new File(out + "results_time" + ".txt");
		try {
			PrintWriter pw =  new PrintWriter(fout);
			pw.println(total_data);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Total elapsed time: " + getGlobalElapsedTime() + "s.");
	}
	
	public static void startGlobalClock(){
		d_initial_global = new Date();
	}
	
	public static double getGlobalElapsedTime(){
		d_final_global = new Date();
		return (d_final_global.getTime() - d_initial_global.getTime())/1000.0;
	}
	
	public static void startLocalClock(String element, Date d){
		localClockMap.put(element, d);
	}
	
	public static Date getLocalElapsedTime(String element){
		return localClockMap.get(element);
	}
	
}

class MapContainer{
	
	public static final int tPROCESS = 0;
	public static final int tPIPE = 1;
	public static final int tTotal = -1;
	public static final int READ = 1;
	public static final int WRITE = 0;
	public static final int RWPROCESS = -1;
	public static final int tBUFFER = 2;
	
	private int type;
	private String id;
	private double value;
	private int rw;
	
	public MapContainer(int type, String id, double value, int rw){
		this.type = type;
		this.id = id;
		this.value = value;
		this.rw = rw;
	}
	
	public String getId() {
		return id;
	}

	public double getValue() {
		return value;
	}

	public int getType() {
		return type;
	}
	
	public int getRW() {
		return rw;
	}
	
}
