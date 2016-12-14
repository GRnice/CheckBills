import socket, select
from threading import Thread
import math
#import signal

class ClientRequest:
    def __init__(self):
        pass

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
        while self.serverOnline:
        # Get the list sockets which are ready to be read through select

            read_sockets,write_sockets,error_sockets = select.select(self.CONNECTION_LIST,[],[])
            print(read_sockets)
            for sock in read_sockets:
                if sock == server_socket:
                    print("Client (%s) is connected" % sock)
                    sockfd, addr = server_socket.accept()
                    self.CONNECTION_LIST.append(sockfd)
                    
                else:
                    # Data received from client, process it
                    try:
                        data = sock.recv(self.RECV_BUFFER)
                        data = data.decode('utf-8')
                        #print(data)
                        if len(data) > 0:
                            # IMAGE*sizeImage
                            if (len(data) > 5 and data[0:5] == "IMAGE"):
                                tabdata = data.split("*")
                                sizeImage = int(tabdata[1])

                                nbIteration = math.ceil(sizeImage / self.RECV_BUFFER)
                                with open("./fileTmp.bmp","wb") as file:

                                    for i in range(0,nbIteration):
                                        dataImage = sock.recv(self.RECV_BUFFER)
                                        dataImage = dataImage.decode('utf-8')
                                        dataImage = dataImage.split(",")
                                        for elt in dataImage:
                                            file.write(int(elt))
                                
                            # DATE*contenu
                            elif (len(data) > 4 and data[0:4] == "DATE"):
                                tabdata = data.split("*")
                                contenu = tabdata[1]
                                #print(contenu)


                            elif (len(data) > 7 and data[0:7] == "MONTANT"):
                                tabdata = data.split("*")
                                contenu = tabdata[1]
                                #print(contenu)
                                
                                

                        else:
                            print("Client (%s) is offline" % sock)
                            index = self.CONNECTION_LIST.index(sock)
                            self.CONNECTION_LIST.pop(index)
                            sock.close()


                    except:
                        print("Client (%s) is offline" % sock)
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
