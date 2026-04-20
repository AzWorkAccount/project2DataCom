
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HTTPClient {

    public static final int PORT = 80;
    public static final String SERVER_ADDR = "127.0.0.1" ; // "www.google.com";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;
	public static final Charset ENCODING = StandardCharsets.ISO_8859_1; // encoding to use for reading/writing data

    public static final int CHUNK_SIZE = 512;				// size of fragment to process


    public static void main(String[] args) {

        System.out.println("client is requesting ... ");
        try {
            Socket socket = new Socket(SERVER_ADDR, PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

			// HINT: If the content length is not given in the HTTP respond's header,
			// use while((N_bytes = reader.read(buffer, 0, CHUNK_SIZE)) != -1 ){}

            // Generate HTTP request message
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
            printWriter.print("GET / HTTP/1.1" + CRLF);
            printWriter.print("Host: " + SERVER_ADDR + CRLF);
            printWriter.print("Connection: close" + CRLF); //close the file after recieving data
            printWriter.print("Accept: */*" + EOH);
            printWriter.flush();

            // read in HTTP request
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            String line = bufferedReader.readLine();
            while(!line.isEmpty()){
                System.out.println(line);
                line = bufferedReader.readLine();
            }
            char[] buffer = new char[100];
            bufferedReader.read(buffer);
            System.out.println("Msg from server " + new String(buffer));

            printWriter.close();
            socket.close();

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
