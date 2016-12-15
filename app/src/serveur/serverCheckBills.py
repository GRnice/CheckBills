import socket, select
from threading import Thread
import math
import os
#import signal

hashmapClientSock = dict()

class ClientRequest:
    def __init__(self):
        self.state = None

    def setwaitingfor(self,state):
        self.state = state

    def waitingfor(self):
        return self.state
        

class Server(Thread):
    def __init__(self,port,sizeBuffer,maxClientSocket):
        Thread.__init__(self)
        self.PORT = port
        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # liste des patients connectés (socket)
        self.serverOnline = False

    def receive_signal(signum,stack):
        pass

    def stopServer(self):
        self.serverOnline = False
    
    def run(self):
        self.serverOnline = True
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.PORT))
        server_socket.listen(self.maxClientSocket)
        # Add server socket to the list of readable connections
        self.CONNECTION_LIST.append(server_socket)
        
        print("Server started on port " + str(self.PORT) + " [ok]")
        print("=============SERVEUR ONLINE=============")
        checksum = 0
        while self.serverOnline:
        # Get the list sockets which are ready to be read through select

            read_sockets,write_sockets,error_sockets = select.select(self.CONNECTION_LIST,[],[])
            
            for sock in read_sockets:
                if sock == server_socket:
                    print("Client (%s) is connected" % sock)
                    sockfd, addr = server_socket.accept()
                    self.CONNECTION_LIST.append(sockfd)
                    hashmapClientSock[sockfd] = ClientRequest()
                    
                else:
                    # Data received from client, process it
                    try:
                        data = sock.recv(self.RECV_BUFFER)
                        clientrequest = hashmapClientSock[sock]
                        message = None
                        if len(data) > 0:
                            try:
                                message = data.decode('utf-8')
                                print(message)
                                if ("DATE" in message[0:4]):
                                    tabdata = message.split('*')
                                    date = tabdata[1]
                                    montant = tabdata[3]
                                    sizeImage = int(tabdata[5])

                            except:
                                print(data[0])
                                checksum = checksum + len(data)
                                print(checksum)
                                if not os.path.exists("c:\\Users\\Remy\\Desktop\\fileTmp.png"):
                                    file = open("c:\\Users\\Remy\\Desktop\\fileTmp.png",'wb')
                                    file.close()
                                    
                                file = open("c:\\Users\\Remy\\Desktop\\fileTmp.jpeg",'ab')
                                file.write(data)
                                file.close()


                        else:
                            print("Client (%s) is offline" % sock)
                            index = self.CONNECTION_LIST.index(sock)
                            self.CONNECTION_LIST.pop(index)
                            sock.close()


                    except Exception as e:
                        print(e)
                        print("(Exception) Client (%s) is offline" % sock)
                        index = self.CONNECTION_LIST.index(sock)
                        self.CONNECTION_LIST.pop(index)
                        sock.close()

             
        server_socket.close()
        print("=============SERVEUR OFFLINE=============")

if __name__ == "__main__":
    serve = Server(3200,4096,200) # sur le port 3000
    serve.start()
    #signal.signal(signal.SIGINT,serve.receive_signal)
    serve.join()
