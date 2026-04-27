
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class HTTPServer {

    public static final int PORT = 80;
    public static final String IP = "127.0.0.1";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;
	public static final Charset ENCODING = StandardCharsets.ISO_8859_1; // encoding to use for reading/writing

    public static void main(String[] args) throws IOException {

        System.out.println("server is listening to port 80");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while(true){

                Socket socket = serverSocket.accept();
                System.out.println("get connection from IP: " + socket.getRemoteSocketAddress());

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                // read in HTTP request
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

                String line = bufferedReader.readLine();
                String fileName = "server_folder/index.html";
                while(!line.isEmpty()){
                    System.out.println(line);

                    if(line.startsWith("GET")) {
                        String[] split = line.split(" ");
                        System.out.println("HERE");

                        if(split[1].equals("/")){
                            fileName = "server_folder/index.html";
                        } else {
                            fileName = "server_folder" + split[1];
                        }
                    }
                    line = bufferedReader.readLine();


                }
                System.out.println("Filename: " + fileName);


                try {
                    File file = new File(fileName);
                    byte[] bodyBytes = Files.readAllBytes(file.toPath());

                    String contentType = "application/octet-stream";
                    String lower = fileName.toLowerCase();
                    if (lower.endsWith(".html") || lower.endsWith(".htm")) contentType = "text/html; charset=ISO-8859-1";
                    else if (lower.endsWith(".png")) contentType = "image/png";
                    else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";

                    dataOutputStream.writeBytes("HTTP/1.1 200 OK" + CRLF);
                    dataOutputStream.writeBytes("Content-Type: " + contentType + CRLF);
                    dataOutputStream.writeBytes("Content-Length: " + bodyBytes.length + EOH);
                    dataOutputStream.write(bodyBytes);
                    dataOutputStream.flush();

                    socket.close();
                } catch (NoSuchFileException e){
                    String response = "The file does not exist";
                    dataOutputStream.writeBytes("HTTP/1.1 404 Not Found" + CRLF);
                    dataOutputStream.writeBytes("Content-Type: text/html; charset=ISO-8859-1" + CRLF);
                    dataOutputStream.writeBytes("Content-Length: " + response.getBytes(StandardCharsets.ISO_8859_1).length + EOH);
                    dataOutputStream.write(response.getBytes(StandardCharsets.ISO_8859_1));
                    dataOutputStream.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                PrintWriter printWriter = getPrintWriter(dataOutputStream);
                printWriter.flush();
                }
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }




    }

    private static PrintWriter getPrintWriter(DataOutputStream dataOutputStream) {
        String response = "The file does not exist";
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
        printWriter.print("Error: HTTP/1.1 404 Not Found" + CRLF);
        printWriter.print("Content-Type: text/html; charset=ISO-8859-1" + CRLF);
        printWriter.print("Content-Length: " + response.length() + EOH); //close the file after recieving data
        printWriter.print(response);
        return printWriter;
    }


}
