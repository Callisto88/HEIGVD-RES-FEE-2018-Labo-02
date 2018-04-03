package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the client side of the protocol specification (version
 * 1).
 *
 * @author Olivier Liechti
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

    private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

    private Socket sock;
    private InputStream input;
    private OutputStream output;
    private String version;
    private final int BUFFER_SIZE = 1024;
    
    //private List<Student> students = new ArrayList<>();
    //private Array<Student> ls = new Array<>();

    public RouletteV1ClientImpl() {
        sock = new Socket();
        version = RouletteV1Protocol.VERSION;

    }

    @Override
    public void connect(String server, int port) throws IOException {
        try {

            SocketAddress sa = new InetSocketAddress(server, port);
            sock.connect(sa);

            input = sock.getInputStream();
            output = sock.getOutputStream();
            
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            //responseBuffer.reset();
            int newBytes = input.read(buffer);
            
            responseBuffer.write(buffer, 0, newBytes);
            //System.out.println(responseBuffer.toString());
            //responseBuffer.toString().replace("\n", "").replace("\r", "");
//            responseBuffer.flush();
            
            
        } catch (IOException io) {
            io.printStackTrace();
        }

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disconnect() throws IOException {
        try {
            output.write((RouletteV1Protocol.CMD_BYE + "\n").getBytes());
            //output.flush();
            sock.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isConnected() {
        return sock.isConnected();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadStudent(String fullname) throws IOException {
        try {
            //output.flush();
            output.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());
            //output.flush();
            output.write((fullname + "\n").getBytes());
            //output.flush();
            output.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());
            //output.flush();
            
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int newBytes = input.read(buffer);
            //input.reset();
            
            responseBuffer.write(buffer, 0, newBytes);
            
            System.out.println(responseBuffer.toString());
            
            responseBuffer = new ByteArrayOutputStream();
            buffer = new byte[BUFFER_SIZE];
            newBytes = input.read(buffer);
            //input.reset();
            
            responseBuffer.write(buffer, 0, newBytes);
            
            System.out.println(responseBuffer.toString());
            
        } catch (IOException io) {
            io.printStackTrace();
        }

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadStudents(List<Student> students) throws IOException {
        try{
            output.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());
            //output.flush();
            for(Student s : students){
                output.write((s.getFullname() + "\n").getBytes());
                //output.flush();
            }
            output.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());
             
        }catch(IOException io){
            io.printStackTrace();
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Student pickRandomStudent() throws EmptyStoreException, IOException {
        output.write((RouletteV1Protocol.CMD_RANDOM + "\n").getBytes());
        //output.flush();
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int newBytes = input.read(buffer);
           
        responseBuffer.write(buffer, 0, newBytes);
        //System.out.println(responseBuffer.toString());
        
        RandomCommandResponse rcr = JsonObjectMapper.parseJson(responseBuffer.toString(), RandomCommandResponse.class);
        //responseBuffer.flush();
        
        if(rcr.getError()!= null){
            throw new EmptyStoreException();
        }
        
        return new Student(rcr.getFullname());
    }

    @Override
    public int getNumberOfStudents() throws IOException {
        int numberOfStudents = 0;
        try{  
            output.write((RouletteV1Protocol.CMD_INFO + "\n").getBytes());
            //output.flush();
            
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int newBytes = input.read(buffer);

            responseBuffer.write(buffer, 0, newBytes);
            InfoCommandResponse icr = JsonObjectMapper.parseJson(responseBuffer.toString(), InfoCommandResponse.class);
            //responseBuffer.flush();
            
            //System.out.println(icr.getNumberOfStudents());

            numberOfStudents = icr.getNumberOfStudents();
   
        }catch(IOException io){
            io.printStackTrace();
        }
        
        finally{
            return numberOfStudents;
        }
    }

    @Override
    public String getProtocolVersion() throws IOException {
        
        String version;

        output.write((RouletteV1Protocol.CMD_INFO + "\n").getBytes());
        //output.flush();
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int newBytes = input.read(buffer);
        
        responseBuffer.write(buffer, 0, newBytes);
        InfoCommandResponse icr = JsonObjectMapper.parseJson(responseBuffer.toString(), InfoCommandResponse.class);
        //responseBuffer.flush();

        version = icr.getProtocolVersion();
        return version;

    }

}
