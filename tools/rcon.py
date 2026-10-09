"""Minimal RCON client for the dev server. Usage: RCON_PASSWORD=... python3 tools/rcon.py "<command>" ["sleep 2"] ...

Connects to 127.0.0.1 on RCON_PORT (default 25576, this project's dev server port).
"""
import os
import socket
import struct
import sys
import time


def packet(request_id, kind, body):
    data = struct.pack('<ii', request_id, kind) + body.encode() + b'\x00\x00'
    return struct.pack('<i', len(data)) + data


def exactly(sock, size):
    data = b''
    while len(data) < size:
        chunk = sock.recv(size - len(data))
        if not chunk:
            raise ConnectionError('RCON connection closed')
        data += chunk
    return data


def read(sock):
    """Returns (request id, body). The server answers a failed login with request id -1."""
    length = struct.unpack('<i', exactly(sock, 4))[0]
    data = exactly(sock, length)
    return struct.unpack('<i', data[:4])[0], data[8:-2].decode('utf-8', 'replace')


def main():
    sock = socket.create_connection(('127.0.0.1', int(os.environ.get('RCON_PORT', '25576'))), timeout=60)
    sock.sendall(packet(1, 3, os.environ['RCON_PASSWORD']))
    if read(sock)[0] == -1:
        sys.exit('RCON login failed: wrong password')
    for command in sys.argv[1:]:
        if command.startswith('sleep '):
            time.sleep(float(command.split()[1]))
            continue
        sock.sendall(packet(2, 2, command))
        print(read(sock)[1])


if __name__ == '__main__':
    main()
