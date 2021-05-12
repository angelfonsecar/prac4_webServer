import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;


public class ServidorWeb
{
    public static final int PUERTO=3500;
    ServerSocket ss;
    private final ExecutorService pool;

    class Manejador implements Runnable
    {
        protected Socket socket;
        protected PrintWriter pw;
        protected BufferedOutputStream bos;
        protected BufferedReader br;
        protected String FileName;

        public Manejador(Socket _socket) {
            this.socket=_socket;
        }

        public void run() {
            try{

                br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bos=new BufferedOutputStream(socket.getOutputStream());
                pw=new PrintWriter(new OutputStreamWriter(bos));
                String line=br.readLine();


                if(line==null)
                {
                    pw.print("<html><head><title>Servidor WEB");
                    pw.print("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>");
                    pw.print("</body></html>");
                    socket.close();
                    return;
                }

                System.out.println("\nCliente Conectado desde: "+socket.getInetAddress());
                System.out.println("Por el puerto: "+socket.getPort());
                System.out.println("Datos: "+line+"\r\n\r\n");

                if(line.toUpperCase().startsWith("GET")){
                    System.out.println("Atendiendo petición GET");
                    handleGET(line, false);
                }
                else if(line.toUpperCase().startsWith("HEAD")){
                    System.out.println("Atendiendo petición HEAD");
                    handleGET(line, true);
                }
                else if(line.toUpperCase().startsWith("POST")){
                    System.out.println("Atendiendo petición POST");
                    handlePOST(line);
                }
                else if(line.toUpperCase().startsWith("PUT")){
                    System.out.println("Atendiendo petición PUT");
                }else
                    pw.println("HTTP/1.0 501 Esa no se la vengo manejando, jefe");

                pw.flush();
                bos.flush();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            try{
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        public void getArch(String line) {
            int i;
            int f;
            i=line.indexOf("/");
            f=line.indexOf(" ",i);
            FileName=line.substring(i+1,f);
        }
        public void SendA(String fileName,Socket sc)
        {
            //System.out.println(fileName);
            int fSize = 0;
            byte[] buffer = new byte[4096];
            try{
                DataOutputStream out =new DataOutputStream(sc.getOutputStream());

                //sendHeader();
                FileInputStream f = new FileInputStream(fileName);
                int x = 0;
                while((x = f.read(buffer))>0)
                {
                    //		System.out.println(x);
                    out.write(buffer,0,x);
                }
                out.flush();
                f.close();
            }catch(FileNotFoundException e){
                //msg.printErr("Transaction::sendResponse():1", "El archivo no existe: " + fileName);
            }catch(IOException e){
                //			System.out.println(e.getMessage());
                //msg.printErr("Transaction::sendResponse():2", "Error en la lectura del archivo: " + fileName);
            }

        }

        public void sendHeader(BufferedInputStream bis) throws IOException {
            int tam_archivo=bis.available();

            String sb = "";
            sb = sb+"HTTP/1.0 200 ok\n";
            sb = sb +"Server: Axel Server/1.0 \n";
            sb = sb +"Date: " + new Date()+" \n";
            sb = sb +"Content-Type: ";

            String ext = FileName.substring(FileName.lastIndexOf('.')+1);   //obtenemos la extensión del recurso solicitado
            switch (ext){
                case "html" -> sb = sb +"text/html";
                case "jpg" -> sb+="image/jpeg";
                case "png" -> sb+="image/png";
                case "doc" -> sb+="application/msword";
                case "pdf" -> sb+="application/pdf";
                case "xls" -> sb+="application/vnd.ms-excel";
                case "ppt" -> sb+="application/vnd.ms-powerpoint";
                //default -> sb = sb +"text/html";
            }sb+=" \n";

            sb = sb +"Content-Length: "+tam_archivo+" \n";
            sb = sb +"\n";
            System.out.println("\nEncabezado respuesta:\n*****************\n" + sb + "*****************");
            bos.write(sb.getBytes());
            bos.flush();

            //out.println("HTTP/1.0 200 ok");
            //out.println("Server: Axel Server/1.0");
            //out.println("Date: " + new Date());
            //out.println("Content-Type: text/html");
            //out.println("Content-Length: " + mifichero.length());
            //out.println("\n");
        }

        public void SendA(String arg, boolean isHead) {
            try{
                BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(FileName));
                int tam_bloque=0;
                if(bis2.available()>=1024)
                {
                    tam_bloque=1024;
                }
                else
                {
                    bis2.available();
                }

                sendHeader(bis2);

                if(!isHead){
                    byte[] buf=new byte[1024];
                    int b_leidos;
                    while((b_leidos=bis2.read(buf,0,buf.length))!=-1)
                        bos.write(buf,0,b_leidos);

                    bos.flush();
                }
                bis2.close();

            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }

        }

        public void handleGET(String line, boolean isHead){
            if(!line.contains("?"))
            {
                getArch(line);
                if(FileName.compareTo("")==0)
                    FileName = "Formulario.html";

                System.out.println(FileName);
                SendA(FileName, isHead);

            }else{
                StringTokenizer tokens=new StringTokenizer(line,"?");
                String req_a=tokens.nextToken();
                String req=tokens.nextToken();
                System.out.println("Token1: "+req_a+"\r\n\r\n");
                System.out.println("Token2: "+req+"\r\n\r\n");
                pw.println("HTTP/1.0 200 Okay");
                pw.flush();
                pw.println();
                pw.flush();
                pw.print("<html><head><title>SERVIDOR WEB");
                pw.flush();
                pw.print("</title></head><body bgcolor=\"#AACCFF\"><center><h1><br>Parametros Obtenidos...</br></h1>");
                pw.flush();
                pw.print("<h3><b>"+req+"</b></h3>");
                pw.flush();
                pw.print("</center></body></html>");
                pw.flush();
            }
        }

        public void handlePOST(String line) throws IOException {
            String info="";

            //handleGET("GET /school.png HTTP/1.1", false);     //si enviamos una respuesta antes de leer, sí nos muestra la info
            while ((line = br.readLine()) != null) {
                info=line;
                System.out.println(info);
            }
            System.out.println("info = " + info);
        }
    }
    public ServidorWeb() throws Exception
    {
        System.out.println("Iniciando Servidor.......");
        this.ss=new ServerSocket(PUERTO);
        System.out.println("Servidor iniciado:---OK");
        System.out.println("Esperando por Cliente....");
        pool = Executors.newFixedThreadPool(2);
        for(;;)
        {
            Socket accept = null;
            try {
                accept=ss.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pool.execute(new Manejador(accept));

        }

    }



    public static void main(String[] args) throws Exception{
        ServidorWeb sWEB=new ServidorWeb();
    }

}