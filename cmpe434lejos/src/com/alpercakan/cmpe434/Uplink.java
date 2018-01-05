package com.alpercakan.cmpe434;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Uplink {
	
	public static ServerSocket serverSocket;
	public static DataOutputStream dataOutputStream;
	
	public static void init() {
		try {
			serverSocket = new ServerSocket(1234);
			Socket client = serverSocket.accept();
			
			OutputStream outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);
		} catch (IOException e) {
			System.out.println("Bluetooth bağlantı problemi.");
			Utils.errorExit();
		}
	}
	
	public static void sendMapInfo(int x, int y, int color, int heading, RadarData radarData) {
		try {
			dataOutputStream.writeInt(-2);
		
			dataOutputStream.writeInt(x);
			dataOutputStream.writeInt(y);
			dataOutputStream.writeInt(color);
			dataOutputStream.writeInt(heading);
			
			for (int i = 0; i < 4; ++i)
				dataOutputStream.writeInt((radarData.dists[i] > Pilot.MARGINAL_STEP) ? 0 : 1);
			
			dataOutputStream.writeInt(-3);
		
			dataOutputStream.flush();
		} catch (IOException e) {
			System.out.println("Bağlantı hatası, veri gönderilemedi.");
		}
	}
	
	public static void cleanup() {
		try {
			dataOutputStream.close();
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Bluetooth bağlantısı düzgün bir şekilde kapatılamadı.");
			Utils.errorExit();
		}
	}
}
