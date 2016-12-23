import socket, select
from threading import Thread
import math
import os
import Bdd
import signal

hashmapClientSock = dict()

class ClientRequest:
    def __init__(self):
        self.state = 0  ## 0, 1, 2
        self.ticketInfo = None  ## ID*6146b8a7edfd942a*DATE*23/12/2016 12:34 PM*MO ...

##    def setwaitingfor(self,state):
##        self.state = state
##
##    def waitingfor(self):
##        return self.state
      

class Server(Thread):
    def __init__(self,port,sizeBuffer,maxClientSocket):
        Thread.__init__(self)
        self.PORT = port
        self.RECV_BUFFER = sizeBuffer
        self.maxClientSocket = maxClientSocket
        self.CONNECTION_LIST = [] # liste des patients connectés (socket)
        self.serverOnline = False
        self.bddTicket = Bdd.BaseDeDonneeTicket()
        self.bddBoutique = Bdd.BaseDeDonneeBoutique()

    def receive_signal(signum):
        self.serverOnline = False

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
                                if ("ID" in message[0:2]):
                                    clientrequest.ticketInfo = message
                                    clientrequest.state+=1
                                    print("client State ", clientrequest.state)

                                elif("REQUEST_ALL_BOUTIQUES" in message[0:21]):
                                    print("requestBoutique du tel")
                                    print(type(self.bddBoutique.getListBoutique()))
                                    sock.send((self.bddBoutique.getListBoutique() + "\r\n").encode('utf-8'))
                                    sock.send("BOUTIQUECHECK\r\n".encode('utf-8'))

                                elif("NEWBOUTIQUE" in message[0:11]):  # NEWBOUTIQUE*nomDeLaBoutique*LONG*longitude*LAT*latitude
                                     self.bddBoutique.insertToTable(message) # pas test encore av le smartphone
                                        
                                    

                            except Exception as e:
                                checksum = checksum + len(data)  
                                print(checksum)
                                if not os.path.exists("./" + clientrequest.ticketInfo.split("*")[9] + ".png"):  ## change the pic Name, on peut mettre (idTel + date) comme nom sinon  
                                    file = open("./" + clientrequest.ticketInfo.split("*")[9] + ".png",'wb')
                                    file.close()
                                    
                                file = open("./" + clientrequest.ticketInfo.split("*")[9] + ".png",'ab')
                                file.write(data)
                                if(clientrequest.ticketInfo != None):
                                    self.writeImgInText(clientrequest.ticketInfo.split("*")[9], data) ## [9] --> nomDuTicket
                                    print("IMAGEBYTES", clientrequest.ticketInfo.split("*")[13])
                                file.close()
                                print("FIN close IMAGE")
                                if(checksum == clientrequest.ticketInfo.split("*")[13]):
                                    clientrequest.state += 1
                                    print("client State ", clientrequest.state)

                                    if(clientrequest.state == 2):
                                        sock.send("IMAGECHECK\r\n".encode('utf-8'))
                                        print("insertion")
                                        self.bddTicket.insertToTable(clientrequest.ticketInfo)
                                        self.bddTicket.readTable()  
                                        clientrequest.state = 0  ## reset state
                                            


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


    def writeImgInText(self, nomFichier, data):
        with open (nomFichier + ".txt", "ab") as fp:
            fp.write(data)
        fp.close()

if __name__ == "__main__":
    serve = Server(3200,4096,200) # sur le port 3000
    serve.start()
    signal.signal(signal.SIGINT,serve.receive_signal)
    serve.join()
