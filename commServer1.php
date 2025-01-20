<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Facades\Log;

class CommServer extends Command
{
    protected $signature = 'comm:server';
    protected $description = 'Run a communication server on port 11000';

    protected $hexArray = "0123456789ABCDEF";

    public function __construct()
    {
        parent::__construct();
    }

    public function handle()
    {
        $port = 11000;
        $address = '0.0.0.0';

        $this->info("Starting server on $address:$port");

        // Create socket
        $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
        if (!$socket) {
            Log::error("Socket creation failed: " . socket_strerror(socket_last_error()));
            return;
        }

        // Bind socket to address and port
        if (!socket_bind($socket, $address, $port)) {
            Log::error("Socket bind failed: " . socket_strerror(socket_last_error()));
            return;
        }

        // Start listening
        if (!socket_listen($socket)) {
            Log::error("Socket listen failed: " . socket_strerror(socket_last_error()));
            return;
        }

        $this->info("Server running... Waiting for connections.");

        while (true) {
            $clientSocket = socket_accept($socket);
            if (!$clientSocket) {
                Log::error("Socket accept failed: " . socket_strerror(socket_last_error()));
                continue;
            }

            $this->info("Client connected.");
            $this->handleClient($clientSocket);
            socket_close($clientSocket);
        }
    }

    protected function handleClient($socket)
    {
        $data = socket_read($socket, 2048);

        if ($data === false) {
            Log::error("Failed to read data from client.");
            return;
        }

        $this->info("Received data: " . $this->bytesToHex($data));

        // Placeholder for further processing logic
        // Example: Extract deviceID, reportType, etc.

        $response = "ACK";
        socket_write($socket, $response, strlen($response));
        $this->info("Response sent: $response");
    }

    protected function bytesToHex($bytes)
    {
        $hexChars = [];
        foreach (str_split($bytes) as $byte) {
            $v = ord($byte) & 0xFF;
            $hexChars[] = $this->hexArray[$v >> 4];
            $hexChars[] = $this->hexArray[$v & 0x0F];
        }
        return implode('', $hexChars);
    }
}
