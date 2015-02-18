package falldetection.communication.timeseries;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.app.Activity;
import android.util.Log;

/* A class takes a DataFileWriter object and then uploads the associated
 * file to the web server listed here.
 */


public class DataFileUploader extends Activity {

	DataFileWriter fileWriter;


	HttpURLConnection connection = null;
	DataOutputStream outputStream = null;
	DataInputStream inputStream = null;

	//String pathToOurFile = "/data/data/com.example.fileuploader/files/hello_file";
	String urlServer = "http://107.20.150.132/handle_upload.php";
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****";
	String filePath;
	
	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1 * 1024 * 1024;
	
	FileInputStream fileInputStream = null;
	
	URL url;
	
	public DataFileUploader( DataFileWriter fileWriter ){
		
		
		this.fileWriter = fileWriter;
		
		initializeServerInformation( );
		
		//uploadFile( );
	}
	
	public void initializeServerInformation( ){
		
		Log.i("inside initialize","before everything");
		
		try {
			
			url = new URL(urlServer);
		
			Log.i("INITALIZING","URL");
			
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			Log.i("INITALIZING","URL ERROR");
			e1.printStackTrace();
		}
		
		Log.i("inside initialize","http connection");
		
		try {
			
			if( url == null){
				
				Log.i("URL IS","NULL");
			}
			
			connection = (HttpURLConnection) url.openConnection();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Log.i("inside","AFTER_CONNECTION");

		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + boundary);
	}
	
	public void uploadFile( ){
		
		File file = null;
		
		try {
			  
			Log.i("inside","TRYING");

			Log.i("fileName = ",fileWriter.getFileName( ) );
			
			file = new File( fileWriter.getFilePath( ), fileWriter.getFileName( ) );
			 
		} catch( Exception e){
		
			Log.i("file try","FAILED");
			Log.i("filePath = ",fileWriter.getFilePath( ) );
			Log.i("fileName = ",fileWriter.getFileName( ) );
		}
		
		try{
			 
			fileInputStream = new FileInputStream( file );
				
			Log.i("inside","GOT_FILE");
				
		} catch( Exception e){
				
			Log.i("caught","FILE_STREAM");
		}		

		Log.i("inside","before outputStream");
		
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	
		        try {
		        	
		        	Log.i("inside thread","NOW");
		        	
		            //Your code goes here
		        	outputStream = new DataOutputStream(connection.getOutputStream());
		        	
		        	outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		
		        	outputStream
							.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
									+ fileWriter.getFileName( ) + "\"" + lineEnd);
					
		        	outputStream.writeBytes(lineEnd);
					
					Log.i("inside","after outputStream");
					
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					
					Log.i("inside","before_read_file");
					
					// Read file
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					
					Log.i("inside","after_read_file");
					
					while (bytesRead > 0) {
						outputStream.write(buffer, 0, bufferSize); 
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
					}

					Log.i("inside","AFTER_WHILE");
					
					outputStream.writeBytes(lineEnd);
					outputStream.writeBytes(twoHyphens + boundary + twoHyphens
							+ lineEnd);
					
					// Responses from the server (code and message)
					int serverResponseCode = connection.getResponseCode();
					String serverResponseMessage = connection.getResponseMessage();

					Log.i("server response","IS = " + String.valueOf( serverResponseCode ));
					
					//fileInputStream.close();
					//outputStream.flush();
					//outputStream.close();
					 
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		    }});
		
			thread.start( );
			
			file.deleteOnExit( );
	}
		
	public DataFileWriter getFileWriter() {
		
		return fileWriter;
	}

	public void setFileWriter(DataFileWriter fileWriter) {
	
		this.fileWriter = fileWriter;
	}
}
