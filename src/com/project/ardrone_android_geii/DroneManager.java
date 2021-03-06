package com.project.ardrone_android_geii;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;



/**

 * Class who handle communication with the Drone (UDP Protocols and AT Commands)

 */
public class DroneManager {


	ScheduledExecutorService mScheduleTaskExecutor = Executors.newScheduledThreadPool(2);

	public DatagramPacket udp_packet;
	public DatagramSocket udp_socket;
	public InetAddress adresse;
	public Drone_UI mJoy;


	ThreadsManager mThreads;
	SensorData_Receiver NavRec;


	public byte[] buffer;



	String CommandeGenere = "";
	String sAdresse;
	String sTemps;


	static int iPitch = 0;
	static int iAltitude = 0;
	static int iBatterie = 0;
	static int iYaw = 0;
	static int iRoll = 0;
	static int iSpeed = 0;

	int iVal = 0;
	int iPortDrone;

	int iSecondeU = 0;
	int iSecondeD = 0;
	int iMinuteU = 0;
	int iMinuteD = 0;
	int icSecondeU = 0;
	int icSecondeD = 0;

	public DroneManager(){

	}

	/**

	 *	This constructor receives as parameter the address and port of the drone 

		to instantiate and a command to send. 

		It instantiates GereThreads class that manages most of the necessary 

		threads in the application and NavdataReceiver class that get drone status and 

		data.

		It also creates the socket and packet with CreationSocketPacket () 

		function, and starts immediatly WatchDog thread to avoid losing the 

		connection.

	 */
	public DroneManager(String CommandeStart, String Adresse, int iPort, Drone_UI joyActivity){			// CONSTRUCTEUR RECOI l'objet de classe MainActivity en parametre

		sAdresse = Adresse;
		iPortDrone = iPort;
		CommandeGenere = CommandeStart;
		mJoy = joyActivity;


		mThreads = new ThreadsManager(this);

		udp_socket = CreationSocketPacket(CommandeStart, iPort, Adresse); // Socket creation

		NavRec = new SensorData_Receiver(udp_socket); 

		mThreads.StartDonnee();


		mThreads.StartWatchDog();


	}

	/**

	 *	This method created a socket and a packet. It is performed only once in

the constructor of JoyActivity.

 Indeed, two objects are required for UDP communication, a socket and a 

packet as respective class DatagramSocket and DatagramPacket  (classes 

available in the Android libraries).

	@param ComandeUDP start command
	@param port The Port to use
	@param host The Address IP target 

	 */
	public DatagramSocket CreationSocketPacket(String ComandeUDP, int port, String host){
		buffer = new byte[512];

		try {											// TRY CREATION SOCKET
			udp_socket = new DatagramSocket();


		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}	



		try {													// INSERTION TRAME
			buffer = ComandeUDP.concat("\r").getBytes("ASCII");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		try {												// INSERTION ADRESSE
			adresse = InetAddress.getByName(host);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		udp_packet = new DatagramPacket(buffer, buffer.length, adresse, port);
		return udp_socket;

	}

	
	/**

	 *	This function simply receives the UDP frame to be sent and send it with 

the socket and packet created with CreationSocketPacket (). It must be 

executed in a thread.

	 */
	public void EnvoiTrameUDP(String ComandeUDP){	


		try {																// INSERTION TRAME
			buffer = ComandeUDP.concat("\r").getBytes("ASCII");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		udp_packet.setData(buffer);							// Changement de la commande du packet

		try {
			udp_socket.send(udp_packet);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	/**

	 *	This function resets the variables (chronometer sequence, flight’s

Boolean ..) and then start Piloting threads, which use 

EnvoiTrameUDP() function to send the AT command.

	 */
	public void decollage() { 


	


		mThreads.bWatchDog = false;
		iVal = 1;
		iSecondeU = 0;
		iSecondeD = 0;
		iMinuteU = 0;
		iMinuteD = 0;
		icSecondeU = 0;
		icSecondeD = 0;

		AttenteMs(50);
		mThreads.Startdecollage();

		AttenteMs(50);
		mThreads.StartPilotage();


		mScheduleTaskExecutor.scheduleAtFixedRate(mThreads.mTaskChrono, 0, 10,TimeUnit.MILLISECONDS);


	}
	
	/**

	 *	This function stops the steering threads using a boolean, it stops the

	timer and starts the Atterissage and Watchdog threads (the emergency landing 

	function is the same, only the thread and thus the associated command 

	change).

	 */
	
	public void atterissage() { 


		mScheduleTaskExecutor.shutdownNow();
		mScheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
		mThreads.bWatchDog = true;

		AttenteMs(50);


		mThreads.StartAtterissage();
		AttenteMs(50);
		mThreads.StartWatchDog();








	}

	public void emergency() {				// Emergency button

		mScheduleTaskExecutor.shutdownNow();
		mScheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();

		mThreads.bWatchDog = true;

		mThreads.StartEmergency();

		AttenteMs(50);



	}


	public void AttenteMs(int iMSeconde) { 


		try {
			TimeUnit.MILLISECONDS.sleep(iMSeconde);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void IncrementSeq() { // Sequence Variable Incrementation
		if (iVal >= 1000) {
			iVal = 1;
		} else {

			iVal++;

		}

	}


}





