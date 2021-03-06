#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sqlite3 as lite
import csv
import time
import os
import datetime
from random import randint 

    ## y c'est longitude  (2... 7)
    ## x ces latitude (48. ...)

class BaseDeDonneeBoutique:
    def __init__(self):
        self.conn = lite.connect("boutiques.db",check_same_thread=False)
        self.cur = self.conn.cursor()
        self.createTable()

    def createTable(self):
        try:
            self.cur.execute(  ## sure superKey is the solution ??
            "CREATE TABLE Boutiques(idBoutique INTEGER PRIMARY KEY AUTOINCREMENT, nomBoutique TEXT, longitude TEXT, latitude TEXT, CONSTRAINT superkey UNIQUE (longitude, latitude))")

            #### les boutiques de base
            b1 = "NOM*Carrefour Nice TNL*LONGITUDE*7.287063*LATITUDE*43.706891"
            b2 = "NOM*Fnac Nice*LONGITUDE*7.266561*LATITUDE*43.703172"   
            b3 = "NOM*Auchan Nice La trinité*LONGITUDE*7.318041*LATITUDE*43.743102"  
            

            self.insertToTable(b1) 
            self.insertToTable(b2)
            self.insertToTable(b3)
            self.conn.commit()
            print("Table Boutiques created")
            return 0
        
        except:
            print("Table Boutiques NOT created")
            return 1

    def insertToTable(self, boutiqueInfo):
        info = boutiqueInfo.split("*")  # IDBOUTIQUE*1*NOM*Carrefour*LONGITUDE*12.1212*LATITUDE*7.1212  --- id generer auto
        try:
            self.cur.execute("INSERT INTO Boutiques(nomBoutique, longitude, latitude) VALUES (?,?,?)",
                            (info[1], info[3], info[5]))  
            self.conn.commit()
            #print("insertGood")
            return 0
        except:
            #print("insertNotGood, arg =",boutiqueInfo)
            return 1

    def getListBoutique(self): ## IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string 
        strSendToClient = ""
        self.cur.execute("SELECT idBoutique, nomBoutique FROM Boutiques")
        rows = self.cur.fetchall()
        for row in rows:
            strSendToClient = strSendToClient + "IDBOUTIQUE*" + str(row[0]) + "*NOM*" + row[1] + "_"
        #print("BOUTIQUE SENT ", strSendToClient.strip("_"))
        return strSendToClient.strip("_")

    def getIdBoutique(self, nameBoutique):
        self.cur.execute("SELECT idBoutique FROM Boutiques WHERE nomBoutique = ?", (nameBoutique, ))
        rows = self.cur.fetchall()
        if(len(rows) == 0):
            print("get Id not good")
            return -1

        idBoutique = rows[len(rows) - 1][0]
        return int(idBoutique)
  


    def getRestBoutique(self, size):  ## retourn IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string 
        strSendToClient = ""
        self.cur.execute("SELECT idBoutique, nomBoutique FROM Boutiques WHERE idBoutique > ?", (size, ))
        rows = self.cur.fetchall()
        if(len(rows) == 0):
            return -1

        for row in rows:
            strSendToClient = strSendToClient + "IDBOUTIQUE*" + str(row[0]) + "*NOM*" + row[1] + "_"
        return strSendToClient.strip("_")

    def getBoutiqueForSelectedTickets(self, identifiant):
        try:
            self.cur.execute("SELECT * FROM Boutiques WHERE idBoutique = ?",(identifiant, ))
            boutique = self.cur.fetchone()
            print(boutique)
            return 0
        except:
            return 1

    def getLatLongForId(self, listId):
        listRes = []
        for elt in listId:
            try:
                self.cur.execute("SELECT latitude, longitude FROM Boutiques WHERE idBoutique = ?",(elt, ))
                boutique = self.cur.fetchone()
                listRes.append(boutique[0])
                listRes.append(boutique[1])
                
            except:
                print("getLongLat Not good")
        return listRes
        
            
        
    def readTable(self):
        self.cur.execute("SELECT * FROM Boutiques")
        rows = self.cur.fetchall()
        #print("ROWS TYPE ", type(rows))
        for row in rows: 
            #print("row TYPE ", type(row))
            print(row)

    def getAllLongLat(self):
        listRes = []  ## [long1, lat1, long2, lat2 ..]
        self.cur.execute("SELECT longitude, latitude FROM Boutiques")
        rows = self.cur.fetchall()
        for row in rows: 
            listRes.append(row[0])
            listRes.append(row[1])
        return listRes
        
        
    def latLongToCsv(self, listIdBoutiques):  
        with open('DataForKmean.csv', 'w', encoding = "utf-8") as fp:

            listLongLat = self.getLatLongForId(listIdBoutiques)  
            depart = 0
            fin = 2

            for i in range(len(listLongLat)):

                ##print(listLongLat[depart:fin])
                if(fin == len(listLongLat)):
                    fp.write(str(listLongLat[fin - 1]) + ";" + str(listLongLat[depart]))
                    break

                else :
                    fp.write(str(listLongLat[fin - 1]) + ";" + str(listLongLat[depart]) + "\n")
                    depart += 2
                    fin += 2


    def insertJeanMedecinBoutiques(self):
        #### insert les boutiques pour les tests
        b1 = "NOM*Monoprix Nice Jean Medecin*LONGITUDE*7.266982*LATITUDE*43.702563"   
        b2 = "NOM*Armand Thierry Nice Jean Medecin*LONGITUDE*7.266239*LATITUDE*43.703090"  
        b3 = "NOM*Etam Thierry Nice Jean Medecin*LONGITUDE*7.266821*LATITUDE*43.702755"   
        b4 = "NOM*Ciné Pathé Nice Jean Medecin*LONGITUDE*7.267004*LATITUDE*43.701904"
        b5 = "NOM*Pimkie Nice Jean Medecin*LONGITUDE*7.268275*LATITUDE*43.700495"  
        b6 = "NOM*Promod Nice Jean Medecin*LONGITUDE*7.268373*LATITUDE*43.700299"
        b7 = "NOM*Macdo Nice Jean Medecin*LONGITUDE*7.268469*LATITUDE*43.700158"
        b8 = "NOM*Zara Nice Jean Medecin*LONGITUDE*7.268940*LATITUDE*43.699455" 

        self.insertToTable(b1)
        self.insertToTable(b2)
        self.insertToTable(b3)
        self.insertToTable(b4)
        self.insertToTable(b5)
        self.insertToTable(b6)
        self.insertToTable(b7)
        self.insertToTable(b8)

        
        

class BaseDeDonneeTicket:

    def __init__(self):
        self.conn = lite.connect("tickets.db",check_same_thread=False)
        self.cur = self.conn.cursor()
        self.createTable()
        self.listBoutiquesId = []  ## pr kmean
        self.listGenerationData = [] ## [intGen, DateJour, offSet] , intGen = 0,1,2,3  ==> 0 pas de gen, 1 le matin, ... et jour [2016-03-12]  ..
        self.readUpdateFile()  
        
        
    def createTable(self):
        try:
            self.cur.execute(
                "CREATE TABLE Tickets(idTel TEXT, date INTEGER, montant TEXT, idBoutique INTEGER, title TEXT, typeBill TEXT, sizeImage TEXT, imageFile TEXT, PRIMARY KEY(idTel, date) FOREIGN KEY(idBoutique) REFERENCES Boutiques(idBoutique))")
            self.conn.commit()
            print("Table Tickets created")
            return 0
        except:
            print("Table Tickets NOT created")
            return 1

    
    def writeUpdateFile(self, listGenerationData):  # [periodeJournee, jour, time, offSet]
        with open('update.txt', 'w', encoding = "utf-8") as fp:
            fp.write(str(listGenerationData[0]) + "," + str(listGenerationData[1]) + "," + str(listGenerationData[2]) + "," + str(listGenerationData[3]))
            

    def readUpdateFile(self): ## met a jour la list [periodeJournee, jour, time, offSet]
        try:
            with open('update.txt', 'r') as fp:
                content = fp.readline()
                myList = content.split(",")
                print(myList)
                if(len(self.listGenerationData) == 4):  ## la bonne taille
                    #print("READING ")
                    for i in range(0,4) :
                        self.listGenerationData[i] = myList[i]

                elif(len(self.listGenerationData) == 0):  ## qd je re Run, ou qd je lis une premiere fois le fichier update
                    #print("debugREADING ")
                    for i in range(0,4) :
                        self.listGenerationData.append(myList[i])

                #print("READING listGenerationData ", self.listGenerationData)
                    
        except IOError as e:
            print("Unable to open file") #Does not exist OR no read permissions
            


    def insertToTable(self, ticketInfo):

        info = ticketInfo.split("*")  # ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (info[3],))
        temps = tempsCur.fetchone()
        #print(type(temps[0]))
        
        try:
            self.cur.execute("INSERT INTO Tickets(idTel, date, montant, idBoutique, title, typeBill, sizeImage, imageFile) VALUES (?,?,?,?,?,?,?,?)",
                            (info[1], int(temps[0]), info[5], int(info[7]), info[9], info[11], info[13], info[15]+".txt"))  
            self.conn.commit()
            #print("insertGood")
            return 0
        except:
            print("insertNotGood, arg =", ticketInfo)
            return 1

    def getIdFromTableAsTime(self, strDateDebutFin): ## REQUEST_ALL_ZONES_INFLUENCES*2016-12-12 09:20:00*2016-12-12 13:00:00
        myList = strDateDebutFin.split("*")
        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (myList[1],))
        temps = tempsCur.fetchone()
        timeDepart = int(temps[0])

        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (myList[2],))
        temps = tempsCur.fetchone()
        timeFin = int(temps[0])
        print("TIME ", timeDepart, timeFin)
        self.listBoutiquesId = []
        
        try:
            self.cur.execute("SELECT * FROM Tickets WHERE Tickets.date >= ? AND Tickets.date <= ?", (timeDepart, timeFin))
            tickets = self.cur.fetchall()  ## then get Long Lat from boutiques Table
            for ticket in tickets:
                #print(ticket)
                self.listBoutiquesId.append(ticket[3])
            return 0
        except:
            print("get with time not good")
            return 1


    def modifyTicket(self, modifiedTicket):
        infoStr = modifiedTicket[6:]   # MODIF*ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
        info = infoStr.split("*")  # ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
        #print("INFO :", info)
        telID = info[1]
        dateCreation = info[3]
        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (dateCreation,))
        temps = tempsCur.fetchone()
        tck1 = ""
        #print(temps[0])
        
        try:
            self.cur.execute("SELECT sizeImage, imageFile FROM Tickets WHERE idTel = ? AND date = ?", (telID, temps[0]))
            tck1 = self.cur.fetchone()
            ##print("ticket infos" , tck1[0], tck1[1])
            self.cur.execute("DELETE FROM Tickets WHERE idTel = ? AND date = ?", (telID, temps[0]))
            self.conn.commit()
            
        except Exception as e:
            print("modify not good ", e)
            return 1

        ##print(infoStr + "*SIZEIMAGE*" + str(tck1[0]) + "*IMAGENAME*" + str(tck1[1]))
        self.insertToTable(infoStr + "*SIZEIMAGE*" + str(tck1[0]) + "*IMAGENAME*" + str(tck1[1]))
        return 0



    def deletFromTable(self, idSmartPhone, dateCreation):  ## appeler que pr les vrai tickets, donc le file Name doit exister dans le repertoire
        tempsEnSec = self.cur.execute("SELECT strftime( \"%s\", ?)", (dateCreation,))
        temps = tempsEnSec.fetchone()
        try:
            self.cur.execute("SELECT imageFile FROM Tickets WHERE idTel = ? AND date = ?", (idSmartPhone, temps[0]))
            fileName = self.cur.fetchone()
            os.remove(str(fileName[0]))
            os.remove(str(fileName[0].replace(".txt", ".png")))
            

            self.cur.execute("DELETE FROM Tickets WHERE idTel = ? AND date = ?", (idSmartPhone, temps[0]))
            
            self.conn.commit()
            return 0

        except Exception as e:
            print("del not good", e)
            return 1
        

    def readTable(self):
        res = "idTel date montant idBoutique title typeBill sizeImage imageFile\n"
        self.cur.execute("SELECT * FROM Tickets")
        rows = self.cur.fetchall()
        for row in rows:

            #print("TEMPS en heures ", self.formatStrToDate(row[1]))  ## row est un tuple .. immutable
            resTmp = ""
            for i in range(len(row)):
                if i == 1: ## column of the date of the ticket in seconds
                    resTmp += str(self.formatStrToDate(row[i])) + " "
                    
                elif i == len(row) - 1:
                    resTmp += str(row[i]) + "\n"
                    
                else :
                    resTmp += str(row[i]) + " "
            res = res + resTmp
            
        print(res)

    def formatStrToDate(self, dateInSec):
        return time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime(dateInSec))
    
    def listingAllBoutiquesId(self):  ## retourne la liste des idBoutiques
        self.listBoutiquesId = []
        self.cur.execute("SELECT idBoutique FROM TICKETS")
        rows = self.cur.fetchall()
        for row in rows:
            self.listBoutiquesId.append(row[0])
        return slef.listBoutiquesId

    def getPeriode(self, heure):
        if(int(heure) >= 8 and int(heure) <= 11):
            return 1
        elif(int(heure) >= 12 and int(heure) <= 17):
            return 2
        elif(int(heure) >= 18 and int(heure) <= 23):
            return 3
        elif(int(heure) >= 0 and int(heure) < 8) :
            return 4
        else :
            return 0


    def insertTicketsForKmean(self, tempsActuelle, lineCounter):  ## methode qui genere les tickets pr le kmean
        heure = tempsActuelle.split(" ")[1].split(":")[0]
        offSet = int(lineCounter)
        for i in range(350):
            minutGen = randint(0, 59)
            secGen = randint(0, 59)

            if(minutGen < 10) :
                minutGen = "0" + str(minutGen)
            if(secGen < 10) :
                secGen = "0" + str(secGen)
            
            titre = "Ticket" + str(offSet)
            typeBill = randint(0,3)
            imageSize = randint(1000, 2500)
            imageName = "IMG-" + titre
            montant = randint(0,100)
            idTel = "idTel" + str(randint(1,10))
            idBoutique = randint(4,11)

            if(int(heure) >= 8 and int(heure) <= 11):
                hourGen = randint(8, 11)
                if(hourGen < 10):
                    hourGen = "0" + str(hourGen)

            elif(int(heure) >= 12 and int(heure) <= 17):
                hourGen = randint(12, 17)

            elif(int(heure) >= 18 and int(heure) <= 23):
                hourGen = randint(18, 23)

            elif(int(heure) >= 0 and int(heure) < 8):
                hourGen = "0" + str(randint(0, 7))

            offSet += 1
            dateGenerated = tempsActuelle.split(" ")[0] + " " + str(hourGen) + ":" + str(minutGen) + ":" + str(secGen)
    
            ticketToInsert = "ID*"+ idTel + "*DATE*" + dateGenerated + "*MONTANT*" + str(montant) + "." + str(randint(0,9)) +  "*IDBOUTIQUE*" + str(idBoutique) + "*TITRE*" + str(titre) + "*TYPEBILL*" + str(typeBill) + "*SIZEIMAGE*" + str(imageSize) + "*IMAGENAME*" + imageName
            self.insertToTable(ticketToInsert)
        return offSet


        

    def generateTestTicketsJM(self):
        #ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
        tempsActuelle = datetime.datetime.now()
        tempsActuelle = tempsActuelle.strftime('%Y-%m-%d %H:%M:%S')
        jour = tempsActuelle.split(" ")[0]
        time = tempsActuelle.split(" ")[1]
        
        heure = time.split(":")[0]
        minute = time.split(":")[1]
        periodeJournee = self.getPeriode(heure)
        self.readUpdateFile()  ## met a jour la list

    
        if(len(self.listGenerationData) == 0):  ## si la list des donnee est vide, je genere
            offSet = self.insertTicketsForKmean(tempsActuelle, 0)  ## 2ieme arg: offset des lignes

            print("First SET -------------------")
            self.listGenerationData.append(periodeJournee)
            self.listGenerationData.append(jour)
            self.listGenerationData.append(time)
            self.listGenerationData.append(offSet)  
            self.writeUpdateFile(self.listGenerationData)  #[periodeJournee, jour, time, offSet], offSet pour le titre des tickets fitif

        elif (len(self.listGenerationData) != 0 and len(self.listGenerationData) == 4):  ## pas vide, je rajoute des tickets
            
            
            if( (str(self.listGenerationData[1]) == str(jour) and int(self.listGenerationData[0]) != int(periodeJournee) and periodeJournee != 0) or str(self.listGenerationData[1]) != str(jour) ): ## meme jour mais diff periodes OR si c'est pas le meme jour  --> je genere
                offSetInList = self.listGenerationData[3]

                offSet = self.insertTicketsForKmean(tempsActuelle, offSetInList)  ## recupere le offset du precedent et mettre a jour mon offset
                self.listGenerationData[0] = periodeJournee  # met a jour la list [periodeJournee, jour, time, offSet]
                self.listGenerationData[1] = jour
                self.listGenerationData[2] = heure
                self.listGenerationData[3] = offSet
                print("OFFset ", self.listGenerationData[3])
                self.writeUpdateFile(self.listGenerationData)  ## met a jour le fichier
                print("list ", self.listGenerationData)
                print("jour ", jour)
                print("periodeJournee ", periodeJournee)
                print("-----------------------------------")


## BDD stock toutes les donnees sauf les fichier contenant les hexa des photos, il lit des tables SQL et ecrit des fichiers .csv "lat long" pr appliquer le Kmeans dessus

#### TICKETS test
# ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
## change time to format :  YYYY-MM-DD hh:mm:ss

tck1 = "ID*idTel1*DATE*2016-12-12 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier1"
tck2 = "ID*idTel1*DATE*2016-12-12 13:15:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre2*TYPEBILL*0*SIZEIMAGE*1544*IMAGENAME*nomFichier2"
tck3 = "ID*idTel2*DATE*2016-12-12 13:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre3*TYPEBILL*2*SIZEIMAGE*9588*IMAGENAME*nomFichier3"
tck4 = "ID*idTel2*DATE*2016-12-12 17:19:44*MONTANT*50*IDBOUTIQUE*2*TITRE*xxtitre1*TYPEBILL*3*SIZEIMAGE*1215*IMAGENAME*nomFichier4"
tck5 = "ID*6146b8a7edfd942a*DATE*2016-12-23 08:51:33*MONTANT*45*IDBOUTIQUE*1*TITRE*unTicket*TYPEBILL*3*SIZEIMAGE*6855*IMAGENAME*nomFichier12"

#bd = BaseDeDonneeTicket()
#bdBoutique = BaseDeDonneeBoutique()
#bdBoutique.readTable()

#bd.deletFromTable("72c76fc4bed8ec03", "2017-02-16 16:44:03")
#bd.readTable()
#bd.deletFromTable("6146b8a7edfd942a", "2017-02-17 09:50:26")
#bd.deletFromTable("6146b8a7edfd942a", "2017-02-17 09:49:59")
######
####bdBoutique.insertJeanMedecinBoutiques()
############
####
##print(bdBoutique.getIdBoutique("Auchan Nice La trinité"))
##print("AAA" + str(bdBoutique.getRestBoutique(13)))

##bd.generateTestTicketsJM()
##
##bd.insertToTable(tck1)
##bd.insertToTable(tck2)
##bd.insertToTable(tck3)
##bd.insertToTable(tck4)
##bd.insertToTable(tck5)
##print("---------------READDDDDDDTABLEEEEEE--")
#bd.readTable()
##print("---------------READDDDDDDTABLEEEEEE--")
#print("-----------------")
##bd.readTable()

##
##modifiedTicket = "MODIF*ID*6146b8a7edfd942a*DATE*2016-12-23 08:51:33*MONTANT*71*IDBOUTIQUE*10*TITRE*unTicket*TYPEBILL*3"
##bd.modifyTicket(modifiedTicket)
##print("-----------------")
##bd.readTable()

#bdBoutique.readTable()
##print("-----------------")
###print(bdBoutique.getLatLongForId(bd.listingBoutiqueId()))  ## for writing input for Kmean
###bd.formatStrToDate(1481544764)
##
#bd.getIdFromTableAsTime("REQUEST_ALL_ZONES_INFLUENCES*2016-12-12 09:20:00*2016-12-12 13:00:00")
#bdBoutique.latLongToCsv(bd.listBoutiquesId)
#bdBoutique.getBoutiqueForSelectedTickets(1)


######## BOUTIQUES test

##b5 = "NEWBOUTIQUE*nomDeLaBoutique*LONG*7.5165168*LAT*43.9681"
##b7 = "NEWBOUTIQUE*FLUNCH*LONG*7.959594*LAT*42.221095"
####b77 = "NEWBOUTIQUE*nomDeLaBoutique222*LONG*1212.12121*LAT*333.1212122"
####bdBoutique.insertToTable(b3)
####bdBoutique.insertToTable(b4)
##bdBoutique.insertToTable(b5)
##bdBoutique.insertToTable(b7)
##bdBoutique.readTable()

##tck6 = "ID*idTel22222*DATE*2016-12-25 13:12:44*MONTANT*50*IDBOUTIQUE*4*TITRE*xxtitre3*TYPEBILL*2*SIZEIMAGE*9588*IMAGENAME*nomFichier3"
##tck7 = "ID*idTel245888*DATE*2016-12-25 17:19:44*MONTANT*50*IDBOUTIQUE*5*TITRE*xxtitre1*TYPEBILL*3*SIZEIMAGE*1215*IMAGENAME*nomFichier4"
##bd.insertToTable(tck6)
##bd.insertToTable(tck7)
##bd.getIdFromTableAsTime("REQUEST_ALL_ZONES_INFLUENCES*2016-12-25 12:20:00*2016-12-25 18:00:00")
##bdBoutique.latLongToCsv(bd.listBoutiquesId)


##listRes = bdBoutique.getAllLongLat()
##print(listRes)


#bdBoutique.latLongToCsv(bd.listingAllBoutiqueId())
##print(bdBoutique.getListBoutique())  ## send au tel
## http://stackoverflow.com/questions/6951052/differences-between-key-superkey-minimal-superkey-candidate-key-and-primary-k 
