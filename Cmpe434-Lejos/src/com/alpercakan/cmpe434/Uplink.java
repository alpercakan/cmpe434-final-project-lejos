package com.alpercakan.cmpe434;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Uplink {
	
	public static ServerSocket serverSocket;
	public static DataOutputStream dataOutputStream;
	
	static final int BT_PORT_NUMBER = 1234;
	static final int BT_PACKET_START = -2, BT_PACKET_END = -3;
	static final int BT_DATA_HEADING = -13, BT_DATA_XY = -14, BT_DATA_MAP_INFO = -15, BT_DATA_MODE = -16, BT_DATA_POSSIBLE = -17, BT_DATA_DEBUG = -18, BT_DATA_READ_MAP = -19, BT_DATA_INTEG_MAP = -50;
	static final int BT_MAPPING_MODE = -8, BT_EXEC_MODE = -9;
	
	public static void init() {
		try {
			serverSocket = new ServerSocket(BT_PORT_NUMBER);
			Socket client = serverSocket.accept();
			
			OutputStream outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);
		} catch (IOException e) {
			System.out.println("Bluetooth balant˝ problemi.");
			Utils.errorExit();
		}
	}
	
	static void packetStart(int packetType) throws IOException {
		dataOutputStream.writeInt(BT_PACKET_START);
		dataOutputStream.writeInt(packetType);
	}
	
	static void packetEnd() throws IOException {
		dataOutputStream.writeInt(BT_PACKET_END);
		dataOutputStream.flush();
	}
	
	static void sendIntegratedMap() {
		try {
			packetStart(BT_DATA_INTEG_MAP);
			
			ArrayList mapPoints = new ArrayList();
			
			for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
				for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
					if (TaskExecution.cells[x][y] != null && TaskExecution.cells[x][y].isVisited) {
						mapPoints.add(TaskExecution.cells[x][y]);
					}
				}
			}
		
			dataOutputStream.writeInt(mapPoints.size());
			
			for (int i = 0; i < mapPoints.size(); ++i) {
				Cell c = (Cell) mapPoints.get(i);
				dataOutputStream.writeInt(c.x);
				dataOutputStream.writeInt(c.y);
				
				dataOutputStream.writeInt(c.color);
				
				for (int j = 0; j < 4; ++j) {
					dataOutputStream.writeInt((c.dists[j] > Pilot.MARGINAL_STEP) ? 0 : 1);
				}
			}
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	static void sendReadMap() {
		try {
			ArrayList readPoints = new ArrayList();
			
			for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
				for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
					if (TaskExecution.readMap[x][y] != null && TaskExecution.readMap[x][y].isVisited) {
						readPoints.add(TaskExecution.readMap[x][y]);
					}
				}
			}
			
			packetStart(BT_DATA_READ_MAP);
		
			dataOutputStream.writeInt(readPoints.size());
			
			for (int i = 0; i < readPoints.size(); ++i) {
				Cell c = (Cell) readPoints.get(i);
				dataOutputStream.writeInt(c.x);
				dataOutputStream.writeInt(c.y);
				
				dataOutputStream.writeInt(c.color);
				
				for (int j = 0; j < 4; ++j) {
					dataOutputStream.writeInt((c.dists[j] > Pilot.MARGINAL_STEP) ? 0 : 1);
				}
			}
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	static void debug(String msg) {
		/*try {
			packetStart(BT_DATA_DEBUG);
		
			dataOutputStream.writeChars(msg);
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}*/
	}
	
	static void sendHeading(int heading) {
		try {
			packetStart(BT_DATA_HEADING);
		
			dataOutputStream.writeInt(heading);
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	static void sendPossibleList(ArrayList possible) {
		try {
			packetStart(BT_DATA_POSSIBLE);
		
			dataOutputStream.writeInt(possible.size());
			
			for (int i = 0; i < possible.size(); ++i) {
				Pose pose = (Pose) possible.get(i);
				dataOutputStream.writeInt(pose.x);
				dataOutputStream.writeInt(pose.y);
				dataOutputStream.writeInt(pose.heading);
			}
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	static void sendXY(int x, int y) {
		try {
			packetStart(BT_DATA_XY);
		
			dataOutputStream.writeInt(x);
			dataOutputStream.writeInt(y);
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	public static void sendMapInfo(int x, int y, int color, int heading, float []dists) {
		try {
			packetStart(BT_DATA_MAP_INFO);
		
			dataOutputStream.writeInt(x);
			dataOutputStream.writeInt(y);
			dataOutputStream.writeInt(color);
			dataOutputStream.writeInt(heading);
			
			for (int i = 0; i < 4; ++i)
				dataOutputStream.writeInt((dists[i] > Pilot.MARGINAL_STEP) ? 0 : 1);
			
			packetEnd();
		} catch (IOException e) {
			System.out.println("Balant˝ hatas˝, veri gˆnderilemedi.");
		}
	}
	
	public static void notifyMode(int mode) {
		try {
			packetStart(BT_DATA_MODE);
			dataOutputStream.writeInt(mode);
			packetEnd();
		} catch (IOException e) {
			System.out.println("Bluetooth connection problem, please try again.");
			Utils.errorExit();
		}
	}
	
	public static void cleanup() {
		try {
			if (dataOutputStream != null)
				dataOutputStream.close();
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			System.out.println("Bluetooth balant˝s˝ d¸zg¸n bir ˛ekilde kapat˝lamad˝.");
			Utils.errorExit();
		}
	}
}
