import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.text.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

public class commServer11000D {
 
  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
  
  public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for ( int j = 0; j < bytes.length; j++ ) {
          int v = bytes[j] & 0xFF;
          hexChars[j * 2] = hexArray[v >>> 4];
          hexChars[j * 2 + 1] = hexArray[v & 0x0F];
      }
      return new String(hexChars);
  }  
  
  class ConnectionHandler implements Runnable {


    private Socket receivedSocketConn1;
    ConnectionHandler(Socket receivedSocketConn1) {
      this.receivedSocketConn1=receivedSocketConn1;
	}


     Date connCreated = null;
     String deviceID=null,reportType=null,dateTime=null,dateTimer = null,gpsDateTimer=null,latitude=null,longitude=null,BB=null,deviceVolt=null,chksum=null,serialKey=null;
     
     
     

    
    public void run() { 
		BufferedWriter w = null;
		DataInputStream r = null;  
		String message="";
		try {

			PrintStream out = System.out; 
			BufferedWriter fout = null;
			w =  new BufferedWriter(new OutputStreamWriter(receivedSocketConn1.getOutputStream()));
			 
			r = new DataInputStream(new BufferedInputStream(receivedSocketConn1.getInputStream()));
		 
			int m = 0, count=0;
			int nextChar=0;
			String deviceNumber="";
			System.out.println("*******************************");
			System.out.println("*******************************");
			System.out.println("** ENTRO UNA CONEXION NUEVA  **");
			System.out.println("*******************************");
			System.out.println("*******************************");
			System.out.println( "\n\n\n THE device"+" "+ receivedSocketConn1.getInetAddress() +":"+receivedSocketConn1.getPort()+" IS CONNECTED ");
			receivedSocketConn1.setSoTimeout(300000);   //300seconds
			 
			int canRead =0; 
			int i=0;
			int protocolo=19;
			//String hexChar ="";
			 
			while (true) {	
			
				byte startByte = r.readByte();
				System.out.println(" startByte is hex : 0x" + Integer.toHexString(startByte));
				String fullmessage = "24";
				if(startByte==0x24){
					byte[] bytedeviceID = new byte[5];
					r.readFully(bytedeviceID);
					System.out.println(" bytedeviceID is hex : 0x" + bytesToHex(bytedeviceID)); 
					deviceNumber =bytesToHex(bytedeviceID);
					fullmessage=fullmessage+" "+bytesToHex(bytedeviceID);
					byte protocolVersion = r.readByte();
							  protocolo=Integer.parseInt(Integer.toHexString(protocolVersion));
					System.out.println("protocolVersion:" + Integer.toHexString(protocolVersion));
					fullmessage=fullmessage+" "+Integer.toHexString(protocolVersion & 0xff);
							  
					  
					byte devicendataType = r.readByte();
					System.out.println("devicendataType:" + Integer.toHexString(devicendataType));
					int deviceType = devicendataType >> 4;
					int dataType = devicendataType & 0b00001111;
					fullmessage=fullmessage+" "+Integer.toHexString(devicendataType & 0xff);

					int dataLength = r.readUnsignedShort();
					System.out.println("dataLength:" + Integer.toHexString(dataLength));
					fullmessage=fullmessage+" "+String.format("%04X", dataLength);

					byte[] dateByte = new byte[3];
					r.readFully(dateByte);
					System.out.println(" dateByte is hex : 0x" + bytesToHex(dateByte));
					fullmessage=fullmessage+" "+bytesToHex(dateByte);

					byte[] timeByte = new byte[3];
					r.readFully(timeByte);
					System.out.println(" timeByte is hex : 0x" + bytesToHex(timeByte));
					fullmessage=fullmessage+" "+bytesToHex(timeByte);

					byte[] latitudeByte = new byte[4];
					r.readFully(latitudeByte);
					System.out.println(" latitudeByte is hex : 0x" + bytesToHex(latitudeByte));
					fullmessage=fullmessage+" "+bytesToHex(latitudeByte);
					String latitudeString = bytesToHex(latitudeByte);
					String latitudeDegreeString = latitudeString.substring(0,2);
					System.out.println(" latitudeDegreeString : " + latitudeDegreeString);
					String latitudeMinuteString  = latitudeString.substring(2,4);
					System.out.println(" latitudeMinuteString : " + latitudeMinuteString);
					String latitudeSecondString  = latitudeString.substring(4,8);
					System.out.println(" latitudeSecondString : " + latitudeSecondString);
					String latitudeFullMinuteString = latitudeMinuteString+"."+latitudeSecondString;
					float latitdueDegreeFloat = Float.parseFloat(latitudeDegreeString);
					System.out.println(" latitdueDegreeFloat : " + latitdueDegreeFloat);
					float latitudeFullMinuteFloat = Float.parseFloat(latitudeFullMinuteString);
					System.out.println(" latitudeFullMinuteFloat : " + latitudeFullMinuteFloat);
					latitudeFullMinuteFloat=latitudeFullMinuteFloat/60;
					System.out.println(" latitudeFullMinuteFloat minute : " + latitudeFullMinuteFloat);
					latitdueDegreeFloat=latitdueDegreeFloat+latitudeFullMinuteFloat;
					System.out.println("Full latitudeFul : " + latitdueDegreeFloat);


					byte[] longitudeByte = new byte[5];
					r.readFully(longitudeByte);
					System.out.println(" longitudeByte is hex : 0x" + bytesToHex(longitudeByte));
					fullmessage=fullmessage+" "+bytesToHex(longitudeByte);
					String longitudeString = bytesToHex(longitudeByte);
					String longitudeDegreeString = longitudeString.substring(0,3);
					System.out.println(" longitudeDegreeString : " + longitudeDegreeString);
					String longitudeMinuteString  = longitudeString.substring(3,5);
					System.out.println(" longitudeMinuteString : " + longitudeMinuteString);
					String longitudeSecondString  = longitudeString.substring(5,9);
					System.out.println(" longitudeSecondString : " + longitudeSecondString);
					String longitudeFullMinuteString = longitudeMinuteString+"."+longitudeSecondString;
					float longituDegreeFloat = Float.parseFloat(longitudeDegreeString);
					System.out.println(" longitudeDegreeFloat : " + longituDegreeFloat);
					float longitudeFullMinuteFloat = Float.parseFloat(longitudeFullMinuteString);
					System.out.println(" longitudeFullMinuteFloat : " + longitudeFullMinuteFloat);
					longitudeFullMinuteFloat=longitudeFullMinuteFloat/60;
					System.out.println(" longitudeFullMinuteFloat minute : " + longitudeFullMinuteFloat);
					longituDegreeFloat=longituDegreeFloat+longitudeFullMinuteFloat;
					System.out.println("Full longitudeFul : " + longituDegreeFloat);
					  
					String locationIndicator = longitudeString.substring(9,10);
					System.out.println("locationIndicator : " + locationIndicator);

					int intlocationIndicator = Integer.parseInt(locationIndicator, 16);

					System.out.println("Converted intlocationIndicator number is : " + intlocationIndicator);
     
					String binarylocationIndicator = Integer.toBinaryString(intlocationIndicator);

					System.out.printf("Hexadecimal locationIndicator to Binary conversion of %s is %s %n", locationIndicator, binarylocationIndicator );

					String longindicator =  binarylocationIndicator.substring(1,2);	    	      
					System.out.println("longindicator : " + longindicator);

					if(longindicator.equals("0")){
					  longituDegreeFloat=-1*longituDegreeFloat;
					}

					String latindicator =  binarylocationIndicator.substring(2,3);	    	      
					System.out.println("latindicator : " + latindicator);



					if(latindicator.equals("0")){
					  latitdueDegreeFloat=-1*latitdueDegreeFloat;
					}

					int speedByte = r.readUnsignedByte();
					System.out.println(" speedByte is hex : 0x" + Integer.toHexString(speedByte));
					fullmessage=fullmessage+" "+String.format("%02X", speedByte);

					int directionByte = r.readUnsignedByte();
					System.out.println(" directionByte is hex : 0x" + Integer.toHexString(directionByte));
					fullmessage=fullmessage+" "+String.format("%02X", directionByte);
					  
					  
					byte[] mileageByte = new byte[4];
					r.readFully(mileageByte);
					System.out.println(" mileageByte is hex : 0x" + bytesToHex(mileageByte));
					fullmessage=fullmessage+" "+bytesToHex(mileageByte);

					byte gpsSatByte = r.readByte() ;
					System.out.println(" gpsSatByte is hex : 0x" + Integer.toHexString(gpsSatByte));
					fullmessage=fullmessage+" "+String.format("%02X", gpsSatByte);


					byte[] vehicleIDByte = new byte[4];
					r.readFully(vehicleIDByte);
					System.out.println(" vehicleIDByte is hex : 0x" + bytesToHex(vehicleIDByte));
					fullmessage=fullmessage+" "+bytesToHex(vehicleIDByte);

					byte[] deviceStatusByte = new byte[2];
					r.readFully(deviceStatusByte);
					System.out.println(" deviceStatusByte is hex : 0x" + bytesToHex(deviceStatusByte));
					fullmessage=fullmessage+" "+bytesToHex(deviceStatusByte);


					int intDeviceStatus = ((deviceStatusByte[0] & 0xff) << 8)  | (deviceStatusByte[1] & 0xff);


					String binaryDeviceStatus=String.format("%16s",  Integer.toBinaryString(intDeviceStatus)).replace(" ", "0");

					
					System.out.println("Device Status binary "+binaryDeviceStatus); 

					String reserved =  binaryDeviceStatus.substring(0,1);
					String motorFaultAlarm =  binaryDeviceStatus.substring(1,2);
					String backCapStatus =  binaryDeviceStatus.substring(2,3);
					String openBackCapAlarm =  binaryDeviceStatus.substring(3,4);
					String lowBattAlarm =  binaryDeviceStatus.substring(4,5);
					String unauthorizedRFIDAlarm =  binaryDeviceStatus.substring(5,6);
					String wrongpassAlarm =  binaryDeviceStatus.substring(6,7);
					String unlockingAlarm =  binaryDeviceStatus.substring(7,8);
					String motorlockStatus =  binaryDeviceStatus.substring(8,9);
					String steelstringStatus =  binaryDeviceStatus.substring(9,10);
					String tobeconfirmed =  binaryDeviceStatus.substring(10,11);
					String vibrationAlarm =  binaryDeviceStatus.substring(11,12);
					String steelcutAlarm =  binaryDeviceStatus.substring(12,13);
					String exitgeofenceAlarm =  binaryDeviceStatus.substring(13,14);
					String entergeofenceAlarm =  binaryDeviceStatus.substring(14,15);
					String lbsStatus =  binaryDeviceStatus.substring(15,16);

					int batByte = r.readUnsignedByte();
					System.out.println(" batByte is hex : 0x" + Integer.toHexString(batByte));
					fullmessage=fullmessage+" "+String.format("%02X", batByte);

					byte[] cellIDLacByte = new byte[4];
					r.readFully(cellIDLacByte);
					System.out.println(" cellIDLacByte is hex : 0x" + bytesToHex(cellIDLacByte));
					fullmessage=fullmessage+" "+bytesToHex(cellIDLacByte);

					byte gsmSignalByte = r.readByte() ;
					System.out.println(" gsmSignalByte is hex : 0x" + Integer.toHexString(gsmSignalByte));
					fullmessage=fullmessage+" "+String.format("%02X", gsmSignalByte);
					System.out.println("********** ok 1");
					byte geoFenceAlarmByte = r.readByte() ;
					System.out.println(" geoFenceAlarmByte is hex : 0x" + Integer.toHexString(geoFenceAlarmByte));
					fullmessage=fullmessage+" "+String.format("%02X", geoFenceAlarmByte);

					System.out.println("********** ok 2");
					byte expandedstatus = r.readByte() ;
					System.out.println(" expandedstatus is hex : 0x" + Integer.toHexString(expandedstatus));
					fullmessage=fullmessage+" "+String.format("%02X",expandedstatus);

					System.out.println("********** ok 3");
					byte[] reserveByte = new byte[2];
					r.readFully(reserveByte);
					System.out.println(" reserveByte is hex : 0x" + bytesToHex(reserveByte));
					fullmessage=fullmessage+" "+bytesToHex(reserveByte);
						  
					byte[] reserve2Byte = new byte[8];
					r.readFully(reserve2Byte);
					System.out.println(" reserve2Byte is hex : 0x" + bytesToHex(reserve2Byte));
					fullmessage=fullmessage+" "+bytesToHex(reserve2Byte);
						  
					byte[] cellidByte = new byte[2];
					r.readFully(cellidByte);
					System.out.println(" cellidByte is hex : 0x" + bytesToHex(cellidByte));
					fullmessage=fullmessage+" "+bytesToHex(cellidByte);
						  
					byte[] mccByte = new byte[2];
					r.readFully(mccByte);
					System.out.println(" mccByte is hex : 0x" + bytesToHex(mccByte));
					fullmessage=fullmessage+" "+bytesToHex(mccByte);
								  
					byte[] mncByte = new byte[1];
					r.readFully(mncByte);
					System.out.println(" mncByte is hex : 0x" + bytesToHex(mncByte));
					fullmessage=fullmessage+" "+bytesToHex(mncByte);

							  
					byte serialNumberByte = r.readByte() ;
					System.out.println(" serialNumberByte is hex : 0x" + Integer.toHexString(serialNumberByte & 0xFF));
					String s=Integer.toHexString(serialNumberByte  & 0xFF);
					System.out.println(s+"***1");
					int serialNumberDecimal=Integer.parseInt(""+s,16);
					System.out.println(serialNumberDecimal);
					fullmessage=fullmessage+" "+String.format("%02X",serialNumberDecimal);

					String dateString = bytesToHex(dateByte);
					String timeString = bytesToHex(timeByte);
					String dayString = dateString.substring(0,2);
					String monthString = dateString.substring(2,4);
					String yearString = dateString.substring(4,6);


					String hourseString = timeString.substring(0,2);
					String minuteString = timeString.substring(2,4);
					String secondString = timeString.substring(4,6);

					String mysqlDate="20"+yearString+"-"+monthString+"-"+dayString;
					String mysqlTime=hourseString+":"+minuteString+":"+secondString;

					System.out.println("mysqlDate:"+mysqlDate);
					System.out.println("mysqlTime:"+mysqlTime);
					String mysqlDateTime = mysqlDate+" "+mysqlTime;
					System.out.println("mysqlDateTime :"+mysqlDateTime);

					int mileage = ((mileageByte[0] & 0xff) << 24) | ((mileageByte[1] & 0xff) << 16) |
						  ((mileageByte[2] & 0xff) << 8)  | (mileageByte[3] & 0xff);	
					String insertQuery1 ="INSERT INTO maindata SET " +
					"deviceID='"+deviceNumber+
					"',protocolVersion="+Integer.toHexString(protocolVersion)+
					",deviceType="+ Integer.toHexString(deviceType)+
					",dataType="+ Integer.toHexString(dataType)+
					",dateTime='"+ mysqlDateTime+
					"',latitude="+latitdueDegreeFloat+
					",longitude="+longituDegreeFloat+
					",locationIndicator='"+locationIndicator+
					"',speed="+speedByte *1.852+
					",direction="+directionByte * 2+
					",mileage="+mileage+
					",satquality="+gpsSatByte+
					",deviceStatus='"+bytesToHex(deviceStatusByte)+
					"',lbsStatus='"+lbsStatus+
					"',entergeofenceAlarm='"+entergeofenceAlarm+
					"',exitgeofenceAlarm='"+exitgeofenceAlarm+
					"',steelcutAlarm='"+steelcutAlarm+
					"',vibrationAlarm='"+vibrationAlarm+
					"',tobeconfirmed='"+tobeconfirmed+
					"',steelstringStatus='"+steelstringStatus+
					"',motorlockStatus='"+motorlockStatus+
					"',unlockingAlarm='"+unlockingAlarm+
					"',wrongpassAlarm='"+wrongpassAlarm+
					"',unauthorizedRFIDAlarm='"+unauthorizedRFIDAlarm+
					"',lowBattAlarm='"+lowBattAlarm+
					"',openBackCapAlarm='"+openBackCapAlarm+
					"',backCapStatus='"+backCapStatus+
					"',motorFaultAlarm='"+motorFaultAlarm+
					"',reserved='"+reserved+
					"',battery="+ batByte+
					",cellID='"+ bytesToHex(cellIDLacByte)+ 
					"',gsmquality="+gsmSignalByte+
					",rawData='"+fullmessage+
					"',insertDateTime=now()";		
					
						 
					System.out.println("\n insertQuery1:"+insertQuery1);
				    if(protocolo<19){
						if(tobeconfirmed.equals("1")){
							w.write("(P35)");
							w.flush();
						}
					}else{
						w.write("(P69,0,"+serialNumberDecimal+")");
						w.flush();
					}
									  	
					String selectOta = " Select lockotaID,otaCommand "+
								 " From lockota "+		                                        
								 " Where deviceID='"+ deviceNumber +"' And otaStatus='a'";
					System.out.println("Select selectOta :"+selectOta);	
					int lockotaID=0;
					String otaCommand="";

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");					  
					Instant instant = Instant.now();
					OffsetDateTime currentDateTime = instant.atOffset( ZoneOffset.of("+00:00") );
					System.out.println("UTC currentDateTime: " + currentDateTime.format(formatter));
						  
					System.out.println("Db closing 24");
				  }
			  
			  
				else if(startByte==0x28){
					System.out.println(" startByte is 28");
					String completeMessage ="";
					completeMessage=completeMessage+(char)startByte;
					byte nextByte = r.readByte() ;
					completeMessage=completeMessage+(char)nextByte;
					while(nextByte!=0x29){
					nextByte = r.readByte() ;
					completeMessage=completeMessage+(char)nextByte;
					}
					System.out.println("Complete 0x28 message is :"+completeMessage);
					  
					if(completeMessage.contains("P43")){
						System.out.println("P43 processing");
						completeMessage = completeMessage.replace("(", "");
						completeMessage = completeMessage.replace(")", "");
						String[] resultSplit = completeMessage.split(",");
						deviceNumber=resultSplit[0];
						String unlockStatus=resultSplit[2];
						String passwordStatus=resultSplit[3];
						System.out.println("deviceNumber " +deviceNumber);
						System.out.println("unlockStatus " +unlockStatus);
						System.out.println("passwordStatus " +passwordStatus);

					if(unlockStatus.equals("1") && passwordStatus.equals("0")){

						System.out.println("can do updates unlockStatus " +unlockStatus);
						System.out.println("can do updates passwordStatus " +passwordStatus); 	
						String selectOta = " Select lockotaID,otaCommand "+
										 " From lockota "+		                                        
										 " Where deviceID='"+ deviceNumber +"' And otaStatus='s'";
						System.out.println("Select selectOta :"+selectOta);
						// ResultSet rs2 = stmt2.executeQuery(selectOta);	
						int lockotaID=0;
						String otaCommand="";

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");					  
						Instant instant = Instant.now();
						OffsetDateTime currentDateTime = instant.atOffset( ZoneOffset.of("+00:00") );
						System.out.println("UTC currentDateTime: " + currentDateTime.format(formatter));
						}
						System.out.println("Db closing P43");
					}

					if(completeMessage.contains("P45")){
					  
					  
						
						System.out.println("P45 processing");
						completeMessage = completeMessage.replace("(", "");
						completeMessage = completeMessage.replace(")", "");
						String[] resultSplit = completeMessage.split(",");
						deviceNumber=resultSplit[0];
						System.out.println("deviceNumber " +deviceNumber);
						String daymonthyear=resultSplit[2];
						System.out.println("daymonthyear " +daymonthyear);
						String hourminutesecond=resultSplit[3];
						System.out.println("hourminutesecond " +hourminutesecond);
						String latitudeString=resultSplit[4];
						System.out.println("latitudeString " +latitudeString);
						String latIndicator=resultSplit[5];
						System.out.println("latIndicator " +latIndicator);
						String longitudeString=resultSplit[6];
						System.out.println("longitudeString " +longitudeString);
						String longIndicator=resultSplit[7];
						System.out.println("longIndicator " +longIndicator);
						String gpsStatus=resultSplit[8];
						System.out.println("gpsStatus " +gpsStatus);
						String speed=resultSplit[9];
						System.out.println("speed " +speed);
						String direction=resultSplit[10];
						System.out.println("direction" +direction);        				  
						String eventSource=resultSplit[11];
						System.out.println("eventSource" +eventSource);
						String unlockstatus=resultSplit[12];
						System.out.println("unlockstatus" +unlockstatus);
						String idcard=resultSplit[13];
						System.out.println("idcard" +idcard);
						String passwordstatus=resultSplit[14];
						System.out.println("passwordstatus" +passwordstatus);
						String passwordverify=resultSplit[15];
						System.out.println("passwordverify" +passwordverify);
						String serialnumber=resultSplit[16];
						System.out.println("serialnumber" +serialnumber);
									  
					    System.out.println("Processing for P45 and sending P46");
					    if(protocolo<19){
							w.write("(P46)");
							w.flush();
						} else {
							w.write("(P46)");
							w.write("(P69,0,"+serialnumber+")");
							w.flush();
						}
					 
					  
					    System.out.println("Processing for P46 sent");
					  
					    float floatLatitude = Float.parseFloat(latitudeString);
					    float floatLongitude = Float.parseFloat(longitudeString);
					  
						if(latIndicator.equals("S")){
						  floatLatitude=-1*floatLatitude;
						}
						if(longIndicator.equals("W")){
						  floatLongitude=-1*floatLongitude;
						}
					   
						String dateString = daymonthyear;
						String timeString = hourminutesecond;
						String dayString = dateString.substring(0,2);
						String monthString = dateString.substring(2,4);
						String yearString = dateString.substring(4,6);


						String hourseString = timeString.substring(0,2);
						String minuteString = timeString.substring(2,4);
						String secondString = timeString.substring(4,6);

						String mysqlDate="20"+yearString+"-"+monthString+"-"+dayString;
						String mysqlTime=hourseString+":"+minuteString+":"+secondString;

						System.out.println("mysqlDate:"+mysqlDate);
						System.out.println("mysqlTime:"+mysqlTime);
						String mysqlDateTime = mysqlDate+" "+mysqlTime;
						System.out.println("mysqlDateTime :"+mysqlDateTime);

						String insertQuery1 ="INSERT INTO lockdata SET " +
						"deviceID='"+deviceNumber+
						"',dateTime='"+mysqlDateTime+
						"',latitude="+floatLatitude+
						",longitude="+floatLongitude+
						",speed='"+speed+
						"',course='"+direction+
						"',eventsource='"+eventSource+
						"',unlockstatus='"+unlockstatus+
						"',idcard='"+idcard+
						"',passwordstatus='"+passwordstatus+
						"',passwordverify='"+passwordverify+
						"',serialnumber='"+serialnumber+
						"',rawData='"+completeMessage+
						"',insertDateTime=now()";



						System.out.println("\n insertQuery1 lock and unlock:"+insertQuery1);
						
						System.out.println("Db closing P45");
					 
					  
					}

					if(completeMessage.contains(",P22,2")){
						ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
						String replyContent = String.format("(P22,%s)", currentDateTime.format(formatter));
						System.out.println("Processing for P22 time request");
						w.write(replyContent);
						w.flush();
						System.out.println("Processing for P22 time request response");
					}

					  
					}
				else{
					
					System.out.println("abnormal data");
					byte nextByte = r.readByte() ;
					break;
					
				}
				 
				System.out.println("\n\nTowards end before closing connection");
			  }
				 
			  }
			  catch (SocketTimeoutException ex)  
			  { 
				   System.out.println("MyError:SocketTimeoutException has been caught in in the main first try");
				   ex.printStackTrace();
			  }  
			  catch (IOException ex)  
			  { 
				   System.out.println("MyError:IOException has been caught in in the main first try");
				   ex.printStackTrace();
			  }  
			  catch (Exception ex)  
			  { 
				   System.out.println( "\n\n\n THE device had exception problem"+" "+ receivedSocketConn1.getInetAddress() +":"+receivedSocketConn1.getPort()+" IS CONNECTED ");
			 
				   System.out.println("MyError:Exception has been caught in in the main first try");
				   ex.printStackTrace(System.out);
			  }      
			  finally{
				try {
					System.out.println( "\n\n\n THE device is in finally"+" "+ receivedSocketConn1.getInetAddress() +":"+receivedSocketConn1.getPort()+" IS CONNECTED ");
			 
					if ( w != null ){
							System.out.println("SI ENTRO ACA ");
						w.close();
						r.close();
						receivedSocketConn1.close();
					}
					else{
						System.out.println("MyError:w is null in finally close");
					}
				}
				catch(IOException ex){
				   System.out.println("MyError:IOException has been caught in w in finally close");
				   ex.printStackTrace(System.out);
				}
			  }
		  }
		  
    
   
   }



   public static void main(String[] args) {
      new commServer11000D();
   }
    commServer11000D() { 
      try 
      {
			   final ServerSocket serverSocketConn = new ServerSocket(11000);	
			   serverSocketConn.setReceiveBufferSize(2048);			   
			   while (true) 
					{
						try 
						{
					            Socket socketConn1 = serverSocketConn.accept();
                                new Thread(new ConnectionHandler(socketConn1)).start();			            
						}
						catch(Exception e)
						{
							System.out.println("MyError:Socket Accepting has been caught in main loop."+e.toString());
						    e.printStackTrace(System.out);
						}
					}
      } 
      catch (Exception e) 
      {
         System.out.println("MyError:Socket Conn has been caught in main loop."+e.toString());
         e.printStackTrace(System.out);
      }
   }
}

 
