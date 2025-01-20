<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class SocketServer extends Command
{
    protected $signature = 'socket:server';
    protected $description = 'Run the socket server';

    public function handle()
    {
        $serverSocket = stream_socket_server("tcp://0.0.0.0:11000", $errno, $errstr);

        if (!$serverSocket) {
            Log::error("Error creating socket: $errstr ($errno)");
            return;
        }

        Log::info("Socket server started on port 11000");

        while ($clientSocket = @stream_socket_accept($serverSocket)) {
            $this->handleClient($clientSocket);
        }

        fclose($serverSocket);
    }

    protected function handleClient($clientSocket)
    {
        $buffer = '';
        $this->logConnection($clientSocket);

        while (!feof($clientSocket)) {
            $data = fread($clientSocket, 1024);
            if ($data === false) {
                break;
            }
            $buffer .= $data;

            // Process the data here
            $this->processData($buffer, $clientSocket);
        }

        fclose($clientSocket);
    }

    protected function logConnection($clientSocket)
    {
        $address = stream_socket_get_name($clientSocket, true);
        Log::info("Client connected: $address");
    }

    protected function processData($data, $clientSocket)
    {
        $startByte = ord($data[0]);

        if ($startByte == 0x24) { // '$'
            $this->processDataType1($data, $clientSocket);
        } elseif ($startByte == 0x28) { // '('
            $this->processDataType2($data, $clientSocket);
        } else {
            Log::warning("Unknown start byte: $startByte");
        }
    }

    protected function processDataType1($data, $clientSocket)
    {
        $deviceID = substr($data, 1, 5);
        $protocolVersion = ord($data[6]);
        $deviceType = (ord($data[7]) >> 4);
        $dataType = (ord($data[7]) & 0b00001111);
        $dataLength = unpack('n', substr($data, 8, 2))[1];

        // Extract other fields as needed...
        $dateByte = substr($data, 10, 3);
        $timeByte = substr($data, 13, 3);
        $latitudeByte = substr($data, 16, 4);
        $longitudeByte = substr($data, 20, 5);
        $speedByte = ord($data[25]);
        $directionByte = ord($data[26]);
        $mileageByte = substr($data, 27, 4);
        $deviceStatusByte = substr($data, 31, 2);
        $batteryByte = ord($data[33]);
        $cellIDByte = substr($data, 34, 4);
        $gsmSignalByte = ord($data[38]);

        // Process latitude and longitude
        $latitude = $this->processLatitude($latitudeByte);
        $longitude = $this->processLongitude($longitudeByte);

        // Insert into database
        DB::table('maindata')->insert([
            'deviceID' => $deviceID,
            'protocolVersion' => $protocolVersion,
            'deviceType' => $deviceType,
            'dataType' => $dataType,
            'dateTime' => $this->formatDateTime($dateByte, $timeByte),
            'latitude' => $latitude,
            'longitude' => $longitude,
            'speed' => $speedByte * 1.852, // Convert to km/h
            'direction' => $directionByte * 2, // Example conversion
            'mileage' => $this->convertMileage($mileageByte),
            'deviceStatus' => $this->bytesToHex($deviceStatusByte),
            'battery' => $batteryByte,
            'cellID' => $this->bytesToHex($cellIDByte),
            'gsmSignal' => $gsmSignalByte,
            'insertDateTime' => now(),
        ]);

        // Send response back to the client
        $serialNumberDecimal = $this->getSerialNumber($data);
        fwrite($clientSocket, "(P69,0," . $serialNumberDecimal . ")");
    }

    protected function processDataType2($data, $clientSocket)
    {
        // Handle messages starting with '('
        $completeMessage = '';
        $completeMessage .= chr(ord($data[0]));
        $nextByte = ord($data[1]);
        $completeMessage .= chr($nextByte);

        while ($nextByte != 0x29) { // Until we find the closing ')'
            $nextByte = ord(fread($clientSocket, 1));
            $completeMessage .= chr($nextByte);
        }

        Log::info("Complete 0x28 message is: " . $completeMessage);

        // Process specific commands based on the complete message
        if (strpos($completeMessage, "P43") !== false) {
            Log::info("P43 processing");
            // Handle P43 logic here
        }

        if (strpos($completeMessage, "P45") !== false) {
            Log::info("P45 processing");
            // Handle P45 logic here
        }

        if (strpos($completeMessage, ",P22,2") !== false) {
            Log::info("Processing for P22 time request");
            // Send response for P22
            fwrite($clientSocket, "(P22," . now()->format('dmyHis') . ")");
        }
    }

    protected function processLatitude($latitudeByte)
    {
        // Convert latitude bytes to float
        $latitudeString = $this->bytesToHex($latitudeByte);
        $latitudeDegreeString = substr($latitudeString, 0, 2);
        $latitudeMinuteString = substr($latitudeString, 2, 4);
        $latitudeSecondString = substr($latitudeString, 4, 8);
        $latitudeFullMinuteString = $latitudeMinuteString . "." . $latitudeSecondString;

        $latitudeDegreeFloat = (float)hexdec($latitudeDegreeString);
        $latitudeFullMinuteFloat = (float)hexdec($latitudeFullMinuteString) / 60;
        return $latitudeDegreeFloat + $latitudeFullMinuteFloat;
    }

    protected function processLongitude($longitudeByte)
    {
        // Convert longitude bytes to float
        $longitudeString = $this->bytesToHex($longitudeByte);
        $longitudeDegreeString = substr($longitudeString, 0, 3);
        $longitudeMinuteString = substr($longitudeString, 3, 5);
        $longitudeSecondString = substr($longitudeString, 5, 9);
        $longitudeFullMinuteString = $longitudeMinuteString . "." . $longitudeSecondString;

        $longitudeDegreeFloat = (float)hexdec($longitudeDegreeString);
        $longitudeFullMinuteFloat = (float)hexdec($longitudeFullMinuteString) / 60;
        return $longitudeDegreeFloat + $longitudeFullMinuteFloat;
    }

    protected function formatDateTime($dateByte, $timeByte)
    {
        // Convert date and time bytes to MySQL datetime format
        $dateString = $this->bytesToHex($dateByte);
        $timeString = $this->bytesToHex($timeByte);
        $dayString = substr($dateString, 0, 2);
        $monthString = substr($dateString, 2, 4);
        $yearString = substr($dateString, 4, 6);
        $hourString = substr($timeString, 0, 2);
        $minuteString = substr($timeString, 2, 4);
        $secondString = substr($timeString, 4, 6);

        return "20" . $yearString . "-" . $monthString . "-" . $dayString . " " . $hourString . ":" . $minuteString . ":" . $secondString;
    }

    protected function convertMileage($mileageByte)
    {
        // Convert mileage bytes to integer
        return (($mileageByte[0] & 0xff) << 24) | (($mileageByte[1] & 0xff) << 16) | (($mileageByte[2] & 0xff) << 8) | ($mileageByte[3] & 0xff);
    }

    protected function bytesToHex($bytes)
    {
        return bin2hex($bytes);
    }

    protected function getSerialNumber($data)
    {
        // Extract serial number from data
        return hexdec(substr($data, -2)); // Example
    }
}