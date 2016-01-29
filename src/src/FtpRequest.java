package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class FtpRequest implements Runnable {

	private Socket socket;

	public FtpRequest(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		processRequest();
	}

	public void processRequest() {
		try {
			byte[] buffer = new byte[50];
			String msg;
			
			System.out.println("processRequest");
			
			OutputStream os = socket.getOutputStream();
			os.write(new String("220 OK").getBytes("UTF-8"));
			os.flush();
// tableau de byte, split espace et switch sur le premier élément / bufferedreader
			do {
				InputStream is = this.socket.getInputStream();
				is.read(buffer);
				
				//BufferedReader reader = new BufferedReader();
				
				msg = new String(buffer, "UTF-8");
				System.out.println(buffer);
				System.out.println(msg);
				
				switch (msg) {
				case "USER":
					processUSER();
					break;

				case "PASS":
					processPASS();
					break;

				case "RETR":
					processRETR();
					break;

				case "STOR":
					processSTOR();
					break;

				case "LIST":
					processLIST();
					break;

				default:
					break;
				}
			} while (msg != "QUIT");
			
			processQUIT();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean processUSER() {
		System.out.println("processUSER");
		return false;

	}

	public boolean processPASS() {
		System.out.println("processPASS");
		return false;

	}

	public boolean processRETR() {
		System.out.println("processRETR");
		return false;

	}

	public boolean processSTOR() {
		System.out.println("processSTOR");
		return false;

	}

	public boolean processLIST() {
		System.out.println("processLIST");
		return false;

	}

	public String processQUIT() {
		System.out.println("processQUIT");
		try {
			this.socket.close();
			return "QUIT";
		} catch (IOException e) {
			e.printStackTrace();
			return "Erreur";
		}
	}
}
