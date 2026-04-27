
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

            // read response headers and body using raw bytes to preserve binary data
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);

            // read headers until CRLFCRLF
            ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
            int b;
            while ((b = bis.read()) != -1) {
                headerBaos.write(b);
                byte[] tmp = headerBaos.toByteArray();
                int len = tmp.length;
                if (len >= 4 && tmp[len-4] == 13 && tmp[len-3] == 10 && tmp[len-2] == 13 && tmp[len-1] == 10) {
                    break;
                }
            }

            String headerStr = new String(headerBaos.toByteArray(), ENCODING);
            String[] headerLines = headerStr.split(CRLF);
            boolean errorFlag = false;
            int length = -1;
            for (String hl : headerLines) {
                if (hl.isEmpty()) continue;
                System.out.println(hl);
                if (hl.contains("Error")) errorFlag = true;
                if (hl.toLowerCase().startsWith("content-length:")) {
                    String[] split = hl.split(":");
                    length = Integer.parseInt(split[1].trim());
                }
            }

            byte[] bodyBytes = new byte[0];
            if (!errorFlag) {
                if (length >= 0) {
                    bodyBytes = new byte[length];
                    int read = 0;
                    while (read < length) {
                        int r = bis.read(bodyBytes, read, length - read);
                        if (r == -1) break;
                        read += r;
                    }
                } else {
                    ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
                    byte[] buf = new byte[CHUNK_SIZE];
                    int r;
                    while ((r = bis.read(buf)) != -1) {
                        bodyBaos.write(buf, 0, r);
                    }
                    bodyBytes = bodyBaos.toByteArray();
                }

                if (fileName.contains("/")){
                    String[] hold = fileName.split("/");
                    fileName = hold[hold.length -1];
                }
                String newFile = ("client_folder\\" + fileName);
                Files.write(Paths.get(newFile), bodyBytes);
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
