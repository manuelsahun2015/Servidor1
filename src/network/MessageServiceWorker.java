package network;

import utils.ConectorDB;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class  MessageServiceWorker implements Runnable{

	private MessageService mService;
	private ServerSocket sServer;
	private Socket sClient;
	private DataOutputStream doStream;
	private ObjectInputStream oiStream;
	private boolean active;
	private String comando;
	private String login;
	private String register;
	private ConectorDB conn;
	private String newMessage;
	private Object newObject;
	private int i;

	public MessageServiceWorker(MessageService mService, ServerSocket sServer, ConectorDB conn) {
		this.mService = mService;
		this.sServer = sServer;
		this.conn = conn;
		active = true;

	}

	// Escolta peticions de connexio i llegeix els missatges dels clients
	public void run() {
		int i = 1;
		int x = 0;
		int n,m,a,b,d,f;
		ResultSet consulta;
		String loginLogin = new String();
		String loginPassword = new String();
		String registerLogin = new String();
		String registerPassword = new String();
		String registerMail = new String();


		conn.connect();

		while (active) {

			Boolean succesfull = FALSE;
			try {
				// Esperem peticions de connexio
				sClient = sServer.accept();
				System.out.println("hola2");
				// Atenem les connexions
				//diStream = new DataInputStream(sClient.getInputStream());
				System.out.println("hola6");
				oiStream = new ObjectInputStream(sClient.getInputStream());
				newObject = receiveObject();
				System.out.println("hola5");
				if(newObject instanceof String) {
					newMessage = (String) newObject;
				}

				System.out.println("hola3");
				// Informem a MessageService que sha rebut un nou missatge
				// ell informara al controlador i el controlador actualitzara la vista.
				mService.messageReceived("[" + getCurrentTime()+ "] " + newMessage);

				comando = newMessage;

				System.out.println("message received: " + comando);

				switch (comando) {
					//controlar error limpiar las string
					case ("LOGIN"):
						System.out.println("hola4");
						x = 1;
						loginLogin = "";
						loginPassword = "";
						break;

					case ("REGISTER"):
						x = 2;
						registerLogin = "";
						registerMail = "";
						registerPassword = "";
						break;

					case ("GUARDAR"):
						System.out.println("HOLA2");
						x = 3;
						sendLogged("save");
						break;

					//default:
						//break;
				}
						if(x == 1){
							System.out.println("if login");
							newObject = receiveObject();
							System.out.println("aqui no entra");
							if(newObject instanceof String) {
								newMessage = (String) newObject;
							}
							System.out.println("mensaje que llega" + newMessage);
							login = newMessage;
							for (n = 0; n < login.length() && login.charAt(n) != '#'; n++) {
								char c = login.charAt(n);
								loginLogin += c;
							}
							for (m = n + 1; m < login.length() && login.charAt(m) != '#'; m++) {
								char c = login.charAt(m);
								loginPassword += c;
							}
							System.out.println(loginLogin);
							System.out.println(loginPassword);

							consulta = conn.selectQuery("SELECT * FROM usuarios");
							try {
								while (consulta.next()) {
									if((consulta.getObject("Login").equals(loginLogin)) && (consulta.getObject("Password").equals(loginPassword))){
										mService.messageReceived("login succesfull");
										succesfull = TRUE; //dejamos en TRUE para q siga dentro el jugador.
										sendLogged("logged");
										break;
									}
								}
								if(!succesfull) {
									mService.messageReceived("login failed");
									sendLogged("login failed");
								}
							} catch (SQLException e) {
								//TODO Auto-generated catch block
								System.out.println("Problema al recuperar les dades...");
							}
						}
						if(x == 2){
							newObject = receiveObject();
							if(newObject instanceof String) {
								newMessage = (String) newObject;
							}
							register = newMessage;
							for (a = 0; a < register.length() && register.charAt(a) != '#'; a++) {
								char c = register.charAt(a);
								registerLogin += c;
							}
							for (b = a + 1; b < register.length() && register.charAt(b) != '#'; b++) {
								char c = register.charAt(b);
								registerPassword += c;
							}
							for (d = b + 1; d < register.length() && register.charAt(d) != '#'; d++) {
								char c = register.charAt(d);
								registerMail += c;
							}
							System.out.println(registerLogin);
							System.out.println(registerMail);
							System.out.println(registerPassword);

							conn.insertQuery("INSERT INTO `usuarios` (`Login`, `Password`, `Mail`) VALUES ('"+registerLogin+"', '"+registerPassword+"', '"+registerMail+"')");
							sendLogged("registered");
						}
						if (x == 3){
							System.out.println("HOlA1");
							newObject = receiveObject();
							System.out.println("HOLA3");
							if(newObject instanceof String) {
								newMessage = (String) newObject;
								System.out.println("codigo de guardado: " + newMessage);
							}
							System.out.println("before send logged");
							try {
								System.out.println(loginLogin);

								String path = new String("/Users/ManuSahun/Desktop/"+loginLogin+".txt");
								System.out.println(path);

								conn.updateQuery("UPDATE usuarios SET Partida ='"+path+"' WHERE Login='"+loginLogin+"'");

								FileWriter fichero = new FileWriter("/Users/ManuSahun/Desktop/"+loginLogin+".txt");
								PrintWriter pw = new PrintWriter(fichero);
								pw.println(newMessage);
								fichero.close();

							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
							sendLogged("recieved");	//cambiar posteriormente a un guardado succesfull
						}
				// Tanquem el socket del client
				sClient.close();
			} catch (IOException e) { } catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		conn.disconnect();
	}

	// Operacio privada per generar la data de recepcio dels missatges
	private String getCurrentTime() {
		return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
	}

	public void stopListening() {
		active = false;
	}


	public void sendLogged(String message) {
		try {
			System.out.println("sending message: " + message);
			doStream = new DataOutputStream(sClient.getOutputStream());
			doStream.writeUTF(message);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "SERVER CONNECTION ERROR (message not sent)");
		}
	}

	private Object receiveObject() throws IOException, ClassNotFoundException {
		return oiStream.readObject();
	}

}
