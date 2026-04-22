
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class HTTPClient {

    public static final int PORT = 80;
    public static final String SERVER_ADDR = "127.0.0.1"; //"www.google.com";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;
	public static final Charset ENCODING = StandardCharsets.ISO_8859_1; // encoding to use for reading/writing data

    public static final int CHUNK_SIZE = 512;				// size of fragment to process


    public static void main(String[] args) {

        System.out.println("client is requesting ... ");
        try {
            Scanner input = new Scanner(System.in);

            System.out.print("Server IP or host name: ");
            String addr = input.nextLine();

            System.out.print("File name: ");
            String fileName = input.nextLine();
            if(fileName.isEmpty()){
                fileName = "index.html";
            }

            Socket socket = new Socket(addr, PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

			// HINT: If the content length is not given in the HTTP respond's header,
			// use while((N_bytes = reader.read(buffer, 0, CHUNK_SIZE)) != -1 ){}

            // Generate HTTP request message
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, ENCODING));
            printWriter.print("GET /" + fileName + " HTTP/1.1" + CRLF);
            printWriter.print("Host: " + addr + CRLF);
            printWriter.print("Connection: close" + CRLF); //close the file after recieving data
            printWriter.print("Accept: */*" + EOH);
//            printWriter.print(fileName + CRLF);
            printWriter.flush();

            // read in HTTP request
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            int length = 100;

            String line = bufferedReader.readLine();
            boolean errorFlag = false;

            while(!line.isEmpty()){
                if(line.contains("Error")){
                    errorFlag = true;
                }
                System.out.println(line);
                line = bufferedReader.readLine();

                if(line.contains("Content-Length:")){
                    String[] split = line.split(": ");
                    length = Integer.parseInt(split[1]);
                }
            }

            char[] buffer = new char[length];
            //while((bufferedReader.read(buffer, 0, CHUNK_SIZE)) != -1 ){}

            if(!errorFlag) { // if there is no error write to file
                bufferedReader.read(buffer);
                if(fileName.contains("/")){
                    String[] hold = fileName.split("/");
                    fileName = hold[hold.length -1];
                }
                String newFile = ("client_folder\\" + fileName);
                String fi = new String(buffer);
                byte[] completeFile = fi.getBytes();

                Files.write(Paths.get(newFile), completeFile);
            }


            printWriter.close();
            socket.close();

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
