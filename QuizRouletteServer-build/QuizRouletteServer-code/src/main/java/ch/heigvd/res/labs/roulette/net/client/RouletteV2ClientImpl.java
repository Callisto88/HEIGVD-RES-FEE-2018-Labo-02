package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.data.StudentsList;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV2Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the client side of the protocol specification (version
 * 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientImpl extends RouletteV1ClientImpl implements IRouletteV2Client {

  /* Methode qui effece les étudients stockés sur le serveur */
  @Override
  public void clearDataStore() throws IOException {
    PrintWriter pw = new PrintWriter(output);

    /* Envoie la commande de la requête */
    pw.println(RouletteV2Protocol.CMD_CLEAR);
    pw.flush();

    /* Vide le buffer du message de retour du serveur */
    BufferedReader br = new BufferedReader(new InputStreamReader(input));
    String reponse = br.readLine();
  }

  /*
   *   Demande la liste des étudiants aux serveurs
   *   Return la liste d'étudient avec l'objet List<Student>
   */
  @Override
  public List<Student> listStudents() throws IOException {
    PrintWriter pw = new PrintWriter(output);

    /* Envoie la commande de la requête */
    pw.println(RouletteV2Protocol.CMD_LIST);
    pw.flush();

    /* Lis la réponse du serveur dans le buffer */
    BufferedReader br = new BufferedReader(new InputStreamReader(input));
    String reponse = br.readLine();

    /* Parse le Json reçu du serveur */
    StudentsList sl = JsonObjectMapper.parseJson(reponse, StudentsList.class);

    return sl.getStudents();
  }

}
