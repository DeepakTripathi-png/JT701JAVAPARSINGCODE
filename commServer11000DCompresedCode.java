import java.io.*;
import java.net.*;
import java.text.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class commServer11000D {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    class ConnectionHandler implements Runnable {
        private Socket receivedSocketConn1;

        ConnectionHandler(Socket receivedSocketConn1) {
            this.receivedSocketConn1 = receivedSocketConn1;
        }

        public void run() {
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(receivedSocketConn1.getOutputStream()));
                 DataInputStream r = new DataInputStream(new BufferedInputStream(receivedSocketConn1.getInputStream()))) {
                receivedSocketConn1.setSoTimeout(300000);
                while (true) {
                    byte startByte = r.readByte();
                    if (startByte == 0x24) {
                        handleStartByte24(r, w);
                    } else if (startByte == 0x28) {
                        handleStartByte28(r, w);
                    } else {
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            } finally {
                try {
                    receivedSocketConn1.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }

        private void handleStartByte24(DataInputStream r, BufferedWriter w) throws IOException {
            String fullmessage = "24";
            byte[] bytedeviceID = new byte[5];
            r.readFully(bytedeviceID);
            String deviceNumber = bytesToHex(bytedeviceID);
            fullmessage += " " + bytesToHex(bytedeviceID);
            byte protocolVersion = r.readByte();
            int protocolo = Integer.parseInt(Integer.toHexString(protocolVersion));
            fullmessage += " " + Integer.toHexString(protocolVersion & 0xff);
            byte devicendataType = r.readByte();
            int deviceType = devicendataType >> 4;
            int dataType = devicendataType & 0b00001111;
            fullmessage += " " + Integer.toHexString(devicendataType & 0xff);
            int dataLength = r.readUnsignedShort();
            fullmessage += " " + String.format("%04X", dataLength);
            byte[] dateByte = new byte[3];
            r.readFully(dateByte);
            fullmessage += " " + bytesToHex(dateByte);
            byte[] timeByte = new byte[3];
            r.readFully(timeByte);
            fullmessage += " " + bytesToHex(timeByte);
            byte[] latitudeByte = new byte[4];
            r.readFully(latitudeByte);
            fullmessage += " " + bytesToHex(latitudeByte);
            float latitdueDegreeFloat = parseLatitude(latitudeByte);
            byte[] longitudeByte = new byte[5];
            r.readFully(longitudeByte);
            fullmessage += " " + bytesToHex(longitudeByte);
            float longituDegreeFloat = parseLongitude(longitudeByte);
            int speedByte = r.readUnsignedByte();
            fullmessage += " " + String.format("%02X", speedByte);
            int directionByte = r.readUnsignedByte();
            fullmessage += " " + String.format("%02X", directionByte);
            byte[] mileageByte = new byte[4];
            r.readFully(mileageByte);
            fullmessage += " " + bytesToHex(mileageByte);
            byte gpsSatByte = r.readByte();
            fullmessage += " " + String.format("%02X", gpsSatByte);
            byte[] vehicleIDByte = new byte[4];
            r.readFully(vehicleIDByte);
            fullmessage += " " + bytesToHex(vehicleIDByte);
            byte[] deviceStatusByte = new byte[2];
            r.readFully(deviceStatusByte);
            fullmessage += " " + bytesToHex(deviceStatusByte);
            int intDeviceStatus = ((deviceStatusByte[0] & 0xff) << 8) | (deviceStatusByte[1] & 0xff);
            String binaryDeviceStatus = String.format("%16s", Integer.toBinaryString(intDeviceStatus)).replace(" ", "0");
            int batByte = r.readUnsignedByte();
            fullmessage += " " + String.format("%02X", batByte);
            byte[] cellIDLacByte = new byte[4];
            r.readFully(cellIDLacByte);
            fullmessage += " " + bytesToHex(cellIDLacByte);
            byte gsmSignalByte = r.readByte();
            fullmessage += " " + String.format("%02X", gsmSignalByte);
            byte geoFenceAlarmByte = r.readByte();
            fullmessage += " " + String.format("%02X", geoFenceAlarmByte);
            byte expandedstatus = r.readByte();
            fullmessage += " " + String.format("%02X", expandedstatus);
            byte[] reserveByte = new byte[2];
            r.readFully(reserveByte);
            fullmessage += " " + bytesToHex(reserveByte);
            byte[] reserve2Byte = new byte[8];
            r.readFully(reserve2Byte);
            fullmessage += " " + bytesToHex(reserve2Byte);
            byte[] cellidByte = new byte[2];
            r.readFully(cellidByte);
            fullmessage += " " + bytesToHex(cellidByte);
            byte[] mccByte = new byte[2];
            r.readFully(mccByte);
            fullmessage += " " + bytesToHex(mccByte);
            byte[] mncByte = new byte[1];
            r.readFully(mncByte);
            fullmessage += " " + bytesToHex(mncByte);
            byte serialNumberByte = r.readByte();
            String s = Integer.toHexString(serialNumberByte & 0xFF);
            int serialNumberDecimal = Integer.parseInt("" + s, 16);
            fullmessage += " " + String.format("%02X", serialNumberDecimal);
            String mysqlDateTime = parseDateTime(dateByte, timeByte);
            int mileage = ((mileageByte[0] & 0xff) << 24) | ((mileageByte[1] & 0xff) << 16) | ((mileageByte[2] & 0xff) << 8) | (mileageByte[3] & 0xff);
            String insertQuery1 = "INSERT INTO maindata SET " +
                    "deviceID='" + deviceNumber +
                    "',protocolVersion=" + Integer.toHexString(protocolVersion) +
                    ",deviceType=" + Integer.toHexString(deviceType) +
                    ",dataType=" + Integer.toHexString(dataType) +
                    ",dateTime='" + mysqlDateTime +
                    "',latitude=" + latitdueDegreeFloat +
                    ",longitude=" + longituDegreeFloat +
                    ",locationIndicator='" + longitudeString.substring(9, 10) +
                    "',speed=" + speedByte * 1.852 +
                    ",direction=" + directionByte * 2 +
                    ",mileage=" + mileage +
                    ",satquality=" + gpsSatByte +
                    ",deviceStatus='" + bytesToHex(deviceStatusByte) +
                    "',lbsStatus='" + binaryDeviceStatus.substring(15, 16) +
                    "',entergeofenceAlarm='" + binaryDeviceStatus.substring(14, 15) +
                    "',exitgeofenceAlarm='" + binaryDeviceStatus.substring(13, 14) +
                    "',steelcutAlarm='" + binaryDeviceStatus.substring(12, 13) +
                    "',vibrationAlarm='" + binaryDeviceStatus.substring(11, 12) +
                    "',tobeconfirmed='" + binaryDeviceStatus.substring(10, 11) +
                    "',steelstringStatus='" + binaryDeviceStatus.substring(9, 10) +
                    "',motorlockStatus='" + binaryDeviceStatus.substring(8, 9) +
                    "',unlockingAlarm='" + binaryDeviceStatus.substring(7, 8) +
                    "',wrongpassAlarm='" + binaryDeviceStatus.substring(6, 7) +
                    "',unauthorizedRFIDAlarm='" + binaryDeviceStatus.substring(5, 6) +
                    "',lowBattAlarm='" + binaryDeviceStatus.substring(4, 5) +
                    "',openBackCapAlarm='" + binaryDeviceStatus.substring(3, 4) +
                    "',backCapStatus='" + binaryDeviceStatus.substring(2, 3) +
                    "',motorFaultAlarm='" + binaryDeviceStatus.substring(1, 2) +
                    "',reserved='" + binaryDeviceStatus.substring(0, 1) +
                    "',battery=" + batByte +
                    ",cellID='" + bytesToHex(cellIDLacByte) +
                    "',gsmquality=" + gsmSignalByte +
                    ",rawData='" + fullmessage +
                    "',insertDateTime=now()";
            if (protocolo < 19) {
                if (binaryDeviceStatus.substring(10, 11).equals("1")) {
                    w.write("(P35)");
                }
            } else {
                w.write("(P69,0," + serialNumberDecimal + ")");
            }
            w.flush();
        }

        private void handleStartByte28(DataInputStream r, BufferedWriter w) throws IOException {
            String completeMessage = "" + (char) r.readByte();
            byte nextByte;
            while ((nextByte = r.readByte()) != 0x29) {
                completeMessage += (char) nextByte;
            }
            if (completeMessage.contains("P43")) {
                handleP43(completeMessage, w);
            } else if (completeMessage.contains("P45")) {
                handleP45(completeMessage, w);
            } else if (completeMessage.contains(",P22,2")) {
                handleP22(w);
            }
        }

        private void handleP43(String completeMessage, BufferedWriter w) throws IOException {
            String[] resultSplit = completeMessage.replace("(", "").replace(")", "").split(",");
            String deviceNumber = resultSplit[0];
            String unlockStatus = resultSplit[2];
            String passwordStatus = resultSplit[3];
            if (unlockStatus.equals("1") && passwordStatus.equals("0")) {
                String selectOta = "Select lockotaID,otaCommand From lockota Where deviceID='" + deviceNumber + "' And otaStatus='s'";
                System.out.println("Select selectOta :" + selectOta);
            }
        }

        private void handleP45(String completeMessage, BufferedWriter w) throws IOException {
            String[] resultSplit = completeMessage.replace("(", "").replace(")", "").split(",");
            String deviceNumber = resultSplit[0];
            String daymonthyear = resultSplit[2];
            String hourminutesecond = resultSplit[3];
            String latitudeString = resultSplit[4];
            String latIndicator = resultSplit[5];
            String longitudeString = resultSplit[6];
            String longIndicator = resultSplit[7];
            String gpsStatus = resultSplit[8];
            String speed = resultSplit[9];
            String direction = resultSplit[10];
            String eventSource = resultSplit[11];
            String unlockstatus = resultSplit[12];
            String idcard = resultSplit[13];
            String passwordstatus = resultSplit[14];
            String passwordverify = resultSplit[15];
            String serialnumber = resultSplit[16];
            if (protocolo < 19) {
                w.write("(P46)");
            } else {
                w.write("(P46)(P69,0," + serialnumber + ")");
            }
            w.flush();
            float floatLatitude = Float.parseFloat(latitudeString);
            float floatLongitude = Float.parseFloat(longitudeString);
            if (latIndicator.equals("S")) {
                floatLatitude = -1 * floatLatitude;
            }
            if (longIndicator.equals("W")) {
                floatLongitude = -1 * floatLongitude;
            }
            String mysqlDateTime = parseDateTime(daymonthyear, hourminutesecond);
            String insertQuery1 = "INSERT INTO lockdata SET " +
                    "deviceID='" + deviceNumber +
                    "',dateTime='" + mysqlDateTime +
                    "',latitude=" + floatLatitude +
                    ",longitude=" + floatLongitude +
                    ",speed='" + speed +
                    "',course='" + direction +
                    "',eventsource='" + eventSource +
                    "',unlockstatus='" + unlockstatus +
                    "',idcard='" + idcard +
                    "',passwordstatus='" + passwordstatus +
                    "',passwordverify='" + passwordverify +
                    "',serialnumber='" + serialnumber +
                    "',rawData='" + completeMessage +
                    "',insertDateTime=now()";
            System.out.println("\n insertQuery1 lock and unlock:" + insertQuery1);
        }

        private void handleP22(BufferedWriter w) throws IOException {
            ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
            String replyContent = String.format("(P22,%s)", currentDateTime.format(formatter));
            w.write(replyContent);
            w.flush();
        }

        private float parseLatitude(byte[] latitudeByte) {
            String latitudeString = bytesToHex(latitudeByte);
            String latitudeDegreeString = latitudeString.substring(0, 2);
            String latitudeMinuteString = latitudeString.substring(2, 4);
            String latitudeSecondString = latitudeString.substring(4, 8);
            String latitudeFullMinuteString = latitudeMinuteString + "." + latitudeSecondString;
            float latitdueDegreeFloat = Float.parseFloat(latitudeDegreeString);
            float latitudeFullMinuteFloat = Float.parseFloat(latitudeFullMinuteString) / 60;
            return latitdueDegreeFloat + latitudeFullMinuteFloat;
        }

        private float parseLongitude(byte[] longitudeByte) {
            String longitudeString = bytesToHex(longitudeByte);
            String longitudeDegreeString = longitudeString.substring(0, 3);
            String longitudeMinuteString = longitudeString.substring(3, 5);
            String longitudeSecondString = longitudeString.substring(5, 9);
            String longitudeFullMinuteString = longitudeMinuteString + "." + longitudeSecondString;
            float longituDegreeFloat = Float.parseFloat(longitudeDegreeString);
            float longitudeFullMinuteFloat = Float.parseFloat(longitudeFullMinuteString) / 60;
            return longituDegreeFloat + longitudeFullMinuteFloat;
        }

        private String parseDateTime(byte[] dateByte, byte[] timeByte) {
            String dateString = bytesToHex(dateByte);
            String timeString = bytesToHex(timeByte);
            String dayString = dateString.substring(0, 2);
            String monthString = dateString.substring(2, 4);
            String yearString = dateString.substring(4, 6);
            String hourseString = timeString.substring(0, 2);
            String minuteString = timeString.substring(2, 4);
            String secondString = timeString.substring(4, 6);
            String mysqlDate = "20" + yearString + "-" + monthString + "-" + dayString;
            String mysqlTime = hourseString + ":" + minuteString + ":" + secondString;
            return mysqlDate + " " + mysqlTime;
        }

        private String parseDateTime(String daymonthyear, String hourminutesecond) {
            String dayString = daymonthyear.substring(0, 2);
            String monthString = daymonthyear.substring(2, 4);
            String yearString = daymonthyear.substring(4, 6);
            String hourseString = hourminutesecond.substring(0, 2);
            String minuteString = hourminutesecond.substring(2, 4);
            String secondString = hourminutesecond.substring(4, 6);
            String mysqlDate = "20" + yearString + "-" + monthString + "-" + dayString;
            String mysqlTime = hourseString + ":" + minuteString + ":" + secondString;
            return mysqlDate + " " + mysqlTime;
        }
    }

    public static void main(String[] args) {
        new commServer11000D();
    }

    commServer11000D() {
        try (ServerSocket serverSocketConn = new ServerSocket(11000)) {
            serverSocketConn.setReceiveBufferSize(2048);
            while (true) {
                try {
                    Socket socketConn1 = serverSocketConn.accept();
                    new Thread(new ConnectionHandler(socketConn1)).start();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}