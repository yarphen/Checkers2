package checkers.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import checkers.pojo.ChangeObject;
/**
* Created by mykhaylo sheremet on 11.12.2016.
*/
public class NetworkClient {

	private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

	public static final String CONNECTION_CLOSED = null;

	private static final boolean DEBUG = false;
	
	private static final boolean LOG = false;

	private Socket socket;

    private BufferedReader reader;

    private BufferedWriter writer;

    private boolean connected;

	private ObjectMapper mapper = new ObjectMapper();

    public NetworkClient(Socket socket){
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),DEFAULT_ENCODING));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),DEFAULT_ENCODING));
            connected = true;
        } catch (Exception e){
        	processException(e);
            endConnection(e);
        }
    }

    private void processException(Throwable e) {
		if (DEBUG){
			e.printStackTrace();
		}
	}

	public ChangeObject read(){
		try {
			String json = reader.readLine();
			log("Read: %s",json);
			ChangeObject object = mapper.readValue(json, ChangeObject.class);
			return object;
		} catch (JsonGenerationException e) {
			processException(e);
			endConnection(null);
			return null;
		} catch (JsonMappingException e) {
			processException(e);
			endConnection(null);
			return null;
		} catch (IOException e) {
			processException(e);
			endConnection(null);
			return null;
		} catch (NullPointerException e) {
			processException(e);
			endConnection(null);
			return null;
		}
	}
    private void log(String format, Object...objects) {
		if (LOG){
			System.out.println(String.format(format, objects));
		}
	}

	public void write(ChangeObject object){
		try {
			String json = mapper.writeValueAsString(object);
			log("Write: %s",json);
			writer.write(json);
			writer.newLine();
			writer.flush();
		} catch (JsonGenerationException e) {
			processException(e);
			endConnection(null);
		} catch (JsonMappingException e) {
			processException(e);
			endConnection(null);
		} catch (IOException e) {
			processException(e);
			endConnection(null);
		}
	}

    public void endConnection(Exception cause){
        if(cause != null)
            cause.printStackTrace();
        connected = false;
        try {
            if (writer != null) {
            	writer.close();
            }
        } catch (Exception e){
        	processException(e);
        }

        try {
            if(reader != null)
            	reader.close();
        } catch (Exception e){
        	processException(e);
        }

        try {
            if(socket != null)
                socket.close();
        } catch (Exception e){
        	processException(e);
        }
    }

    public boolean isConnected() {
        return connected;
    }
    
    public String getIP(){
    	return socket.getInetAddress().getHostAddress();
    }
}
