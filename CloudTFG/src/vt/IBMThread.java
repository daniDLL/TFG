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

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import pipeline.Data;
import Test.Analysis;

public class IBMThread implements Runnable{

	private File audioPart = null;
	private int numPart;
	private Vector<Pair<String,Integer>> transcribedPart;
	private static final String c_username = "870434a7-be31-4ed1-a9bc-77dab5a401a5";
	private static final String c_pass = "GyHXEeuqfGXW";
	private static final String url_api = "https://stream.watsonplatform.net/speech-to-text/api";

	public IBMThread(File audioPart, int numPart, Vector<Pair<String,Integer>> transcribedPart){
		this.audioPart = audioPart;
		this.numPart = numPart;
		this.transcribedPart = transcribedPart;
	}

	public String curlRequest(String file) throws IOException, InterruptedException{

		String command = "curl -u " + c_username + ":" + c_pass + " -X POST --data-binary"
				+ " @" + file + " -H Content-Type:audio/l16;rate=16000"
				+ " " + url_api + "/v1/recognize?continuous=true";
		//System.out.println(command);
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
				.replaceAll("result_index: 0", "").replaceAll("alternatives: confidence: [0-9]\\.[0-9]* transcript: ","")
				.replaceAll("results: ", "");

		return transcribedText;
	}

	public void run() {
		int responseCode = -1;

		String e = audioPart.getName().split("\\.")[0];

		try{
			/*Authenticator.setDefault (new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication (c_username, c_pass.toCharArray());
				}
			});

			String recognizeURL = url_api + "/v1/recognize?continuous=true";
			URL obj = new URL(recognizeURL);

			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "audio/l16; rate=16000;");
			//con.setRequestProperty("X-logging", "0");
			con.setRequestProperty("Transfer-encoding", "chunked");
			//con.setRequestProperty("Connection", "Keep-Alive");

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			String crlf = "\r\n";
			String name = e;

			wr.writeBytes("----WebKitFormBoundaryE19zNvXGzXaLvS5C" + crlf);
			wr.writeBytes(crlf);
			wr.writeBytes("Content-Disposition: form-data; name=\"body\";filename=\"" + name + "\";" + crlf);
			wr.writeBytes("Content-Type: audio/wav" + crlf);
			wr.flush();

			FileInputStream inputStream = new FileInputStream(audioPart);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				wr.write(buffer, 0, bytesRead);
			}
			wr.flush();
			inputStream.close();
			wr.writeBytes("----WebKitFormBoundaryE19zNvXGzXaLvS5C" + crlf);
			wr.flush();
			wr.close();

			System.out.println("[IBMWS] T" + (numPart+1) + " - Starting recognizer ...");
			responseCode = con.getResponseCode();
			if(responseCode != 200) throw new IOException();
			//System.out.println("[IBMWS] Sending 'POST' request to URL : " + recognizeURL);
			System.out.println("[IBMWS] T" + (numPart+1) + " - Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String jsonText = response.toString();

			JSONParser parser = new JSONParser();
			JSONObject j = (JSONObject)parser.parse(jsonText);
			String result = j.get("results").toString();
			String transcribedText = result.replaceAll("\\[","")
					.replaceAll("\\{","").replaceAll("\\]","").replaceAll("\\}","")
					.replaceAll("\"","").replaceAll("alternatives:transcript:","")
					.replaceAll(",","").replaceAll("final:true","").replaceAll("   ","");

			transcribedPart.add(new Pair<String,Integer>(transcribedText,(Integer)numPart));*/
			System.out.println("[IBMWS] T" + (numPart+1) + " - Starting recognizer ...");
			String result = curlRequest(audioPart.getPath());
			System.out.println("[IBMWS] T" + (numPart+1) + " - Done work ..." );

			transcribedPart.add(new Pair<String,Integer>(result,(Integer)numPart));
		}
		catch(IOException e1){
			e1.printStackTrace();
			if(responseCode != 200){
				try {
					String result = curlRequest(audioPart.getPath());

					transcribedPart.add(new Pair<String,Integer>(result,(Integer)numPart));
				} 
				catch (IOException e2) {
					e2.printStackTrace();
				} 
				catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
		catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		/*catch (ParseException e1) {
			e1.printStackTrace();
		}*/
	}

}
