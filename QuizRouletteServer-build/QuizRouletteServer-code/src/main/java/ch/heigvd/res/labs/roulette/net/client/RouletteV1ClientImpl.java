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

    protected Socket sock;
    protected InputStream input;
    protected OutputStream output;
    private String version;
    private final int BUFFER_SIZE = 1024;

    public RouletteV1ClientImpl() {
        sock = new Socket();
        version = RouletteV1Protocol.VERSION;

    }

    /* Methode pour créer une connection client-serveur */
    @Override
    public void connect(String server, int port) throws IOException {
        try {

            /* Création du socket */
            SocketAddress sa = new InetSocketAddress(server, port);
            sock.connect(sa);

            input = sock.getInputStream();
            output = sock.getOutputStream();

            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];

            int newBytes = input.read(buffer);

            responseBuffer.write(buffer, 0, newBytes);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /* Methode pour arrêter la connection client-serveur */
    @Override
    public void disconnect() throws IOException {
        try {
            /* Envoie la commande de la requête */
            output.write((RouletteV1Protocol.CMD_BYE + "\n").getBytes());
            //output.flush();
            sock.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /* Méthode pour savoir si la connection client-serveur est encore up */
    @Override
    public boolean isConnected() {
        return sock.isConnected();
    }

    /* Méthode pour stocker un étudient dans la liste du serveur */
    @Override
    public void loadStudent(String fullname) throws IOException {
        try {
            /* Envoie la commande de la requête */
            output.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());
            /* Ajoute l'étudient dans le serveur */
            output.write((fullname + "\n").getBytes());
            /* Envoie la commande du message de fin de l'ajout */
            output.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());

            /* Bloque pour vide le buffer du message du serveur */
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int newBytes = input.read(buffer);

            responseBuffer.write(buffer, 0, newBytes);

            System.out.println(responseBuffer.toString());

            responseBuffer = new ByteArrayOutputStream();
            buffer = new byte[BUFFER_SIZE];
            newBytes = input.read(buffer);

            responseBuffer.write(buffer, 0, newBytes);

            System.out.println(responseBuffer.toString());

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /* Méthode pour stocker une liste d'étudients pour la liste du serveur */
    @Override
    public void loadStudents(List<Student> students) throws IOException {
        try {

            /* Envoie la commande de la requête */
            output.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());

            /* Vide le buffer du message de retour du serveur */
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String reponse = br.readLine();

            /* Ajoute la liste des étudiants */
            for (Student s : students) {
                output.write((s.getFullname() + "\n").getBytes());
            }

            /* Envoie la commande du message de fin de l'ajout */
            output.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /* Méthode qui retourne un étudient au hasard stocké dans la liste des étudients du serveur */
    @Override
    public Student pickRandomStudent() throws EmptyStoreException, IOException {

        /* Envoie la commande de la requête */
        output.write((RouletteV1Protocol.CMD_RANDOM + "\n").getBytes());

        /* Bloque pour vide le buffer du message du serveur */
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int newBytes = input.read(buffer);

        responseBuffer.write(buffer, 0, newBytes);

        /* Parse la réponse du serveur */
        RandomCommandResponse rcr = JsonObjectMapper.parseJson(responseBuffer.toString(), RandomCommandResponse.class);

        if (rcr.getError() != null) {
            throw new EmptyStoreException();
        }

        return new Student(rcr.getFullname());
    }

    /* Méthode qui retourne le nombre d'étudients stockés dans la liste du serveur */
    @Override
    public int getNumberOfStudents() throws IOException {
        int numberOfStudents = 0;
        try {
            /* Envoie la commande de la requête */
            output.write((RouletteV1Protocol.CMD_INFO + "\n").getBytes());

            /* Bloque pour vide le buffer du message du serveur */
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int newBytes = input.read(buffer);

            responseBuffer.write(buffer, 0, newBytes);
            /* Parse la réponse du serveur */
            InfoCommandResponse icr = JsonObjectMapper.parseJson(responseBuffer.toString(), InfoCommandResponse.class);

            numberOfStudents = icr.getNumberOfStudents();

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            return numberOfStudents;
        }
    }

    /* Méthode qui retourne la version du protocole du serveur */
    @Override
    public String getProtocolVersion() throws IOException {

        String version;

        /* Envoie la commande de la requête */
        output.write((RouletteV1Protocol.CMD_INFO + "\n").getBytes());

        /* Bloque pour vide le buffer du message du serveur */
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int newBytes = input.read(buffer);

        responseBuffer.write(buffer, 0, newBytes);

        /* Parse la réponse du serveur */
        InfoCommandResponse icr = JsonObjectMapper.parseJson(responseBuffer.toString(), InfoCommandResponse.class);

        version = icr.getProtocolVersion();
        return version;

    }

}
