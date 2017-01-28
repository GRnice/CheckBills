#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sqlite3 as lite
import csv
import time
import os

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
            b1 = "NOM*Carrefour*LONGITUDE*12.1212*LATITUDE*7.1212"
            b2 = "NOM*Casino*LONGITUDE*14.1212*LATITUDE*7.2212"
            b3 = "NOM*Auchan*LONGITUDE*15.5*LATITUDE*19.01"

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
            print("insertGood")
            return 0
        except:
            print("insertNotGood")
            return 1

    def getListBoutique(self): ## IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string_IDBOUTIQUE*id*NOM*string 
        strSendToClient = ""
        self.cur.execute("SELECT idBoutique, nomBoutique FROM Boutiques")
        rows = self.cur.fetchall()
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
        
        
    def longLatToCsv(self, listIdBoutiques):  
        with open('Data.csv', 'w', encoding = "utf-8-sig") as fp:

            listLongLat = self.getLatLongForId(listIdBoutiques)  
            depart = 0
            fin = 2

            for i in range(len(listLongLat)):

                print(listLongLat[depart:fin])
                if(fin == len(listLongLat)):
                    fp.write(str(listLongLat[depart]) + ";" + str(listLongLat[fin-1]))
                    break

                else :
                    fp.write(str(listLongLat[depart]) + ";" + str(listLongLat[fin-1]) + "\n")
                    depart += 2
                    fin += 2
   

class BaseDeDonneeTicket:

    def __init__(self):
        self.conn = lite.connect("tickets.db",check_same_thread=False)
        self.cur = self.conn.cursor()
        self.createTable()

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

    def insertToTable(self, ticketInfo):

        info = ticketInfo.split("*")  # ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (info[3],))
        temps = tempsCur.fetchone()
        #print(type(temps[0]))
        
        try:
            self.cur.execute("INSERT INTO Tickets(idTel, date, montant, idBoutique, title, typeBill, sizeImage, imageFile) VALUES (?,?,?,?,?,?,?,?)",
                            (info[1], int(temps[0]), info[5], int(info[7]), info[9], info[11], info[13], info[15]+".txt"))  
            self.conn.commit()
            print("insertGood")
            return 0
        except:
            print("insertNotGood")
            return 1


    def getFromTableAsTime(self, time1, time2): ## donne les les tickets entre 2 temps
        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (time1,))
        temps = tempsCur.fetchone()
        timeDepart = int(temps[0])

        tempsCur = self.cur.execute("SELECT strftime( \"%s\", ?)", (time2,))
        temps = tempsCur.fetchone()
        timeFin = int(temps[0])
        print("TIME ", timeDepart, timeFin)
        
        try:
            self.cur.execute("SELECT * FROM Tickets WHERE Tickets.date >= ? AND Tickets.date <= ?", (timeDepart, timeFin))
            tickets = self.cur.fetchall()  ## then get Long Lat from boutiques Table
            for ticket in tickets:
                print(ticket)
            return 0
        except:
            print("get with time not good")
            return 1


    def getFromTable(self, idTel, date): 
        try:
            self.cur.execute("SELECT idTel, date, montant, idBoutique, title, typeBill, imageFile FROM Tickets WHERE idTel = ? AND date = ?",(idTel,date))
            tck1 = self.cur.fetchone()    ## faire fetchall avun appelle de readTable
            print(tck1)
            return 0

        except:
            print("invalid telId OR date")
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
    
    def listingBoutiqueId(self):  ## retourne la liste des idBoutiques
        listId = []
        self.cur.execute("SELECT idBoutique FROM TICKETS")
        rows = self.cur.fetchall()
        for row in rows:
            listId.append(row[0])
        return listId


## BDD stock toutes les donnees sauf les fichier contenant les hexa des photos, il lit des tables SQL et ecrit des fichiers .csv "lat long" pr appliquer le Kmeans dessus

#### TICKETS test
# ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier
## change time to format :  YYYY-MM-DD hh:mm:ss

tck1 = "ID*idTel1*DATE*2016-12-12 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1*SIZEIMAGE*1212*IMAGENAME*nomFichier1"
tck2 = "ID*idTel1*DATE*2016-12-12 13:15:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre2*TYPEBILL*0*SIZEIMAGE*1544*IMAGENAME*nomFichier2"
tck3 = "ID*idTel2*DATE*2016-12-12 13:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre3*TYPEBILL*2*SIZEIMAGE*9588*IMAGENAME*nomFichier3"
tck4 = "ID*idTel2*DATE*2016-12-12 17:19:44*MONTANT*50*IDBOUTIQUE*2*TITRE*xxtitre1*TYPEBILL*3*SIZEIMAGE*1215*IMAGENAME*nomFichier4"
tck5 = "ID*6146b8a7edfd942a*DATE*2016-12-23 08:51:33*MONTANT*45*IDBOUTIQUE*1*TITRE*unTicket*TYPEBILL*3*SIZEIMAGE*6855*IMAGENAME*nomFichier12"

##bd = BaseDeDonneeTicket()
##bdBoutique = BaseDeDonneeBoutique()
##
##bd.insertToTable(tck1)
##bd.insertToTable(tck2)
##bd.insertToTable(tck3)
##bd.insertToTable(tck4)
##bd.insertToTable(tck5)
##bd.readTable()
##print("-----------------")
##bdBoutique.readTable()
##print("-----------------")
###print(bdBoutique.getLatLongForId(bd.listingBoutiqueId()))  ## for writing input for Kmean
###bd.formatStrToDate(1481544764)
##
##bd.getFromTableAsTime("2016-12-12 13:00:00", "2016-12-12 18:00:00")
#bdBoutique.getBoutiqueForSelectedTickets(1)

#bd.deleteFromTable("idTel1", "12/12/2015 12:12:44")
#bd.getFromTable("idTel2", "12/12/2015 12:12:44")
#bdBoutique.readTable()

######## BOUTIQUES test
##b3 = "NOM*Intermarché*LONGITUDE*19.5*LATITUDE*20.1212"
##b4 = "NOM*St philippe*LONGITUDE*19.2*LATITUDE*20.1212"
##
##b5 = "NEWBOUTIQUE*nomDeLaBoutique*LONG*1212.1*LAT*333.2"
##b7 = "NEWBOUTIQUE*FLUNCH*LONG*1212.11*LAT*333.2"
##b77 = "NEWBOUTIQUE*nomDeLaBoutique222*LONG*1212.12121*LAT*333.1212122"
##b88 = "NEWBOUTIQUE*FLUNCH1212*LONG*1212.11*LAT*333.21111"
##bdBoutique.insertToTable(b3)
##bdBoutique.insertToTable(b4)
##bdBoutique.insertToTable(b5)
##bdBoutique.insertToTable(b77)
##bdBoutique.insertToTable(b88)
###bdBoutique.readTable()
##listRes = bdBoutique.getAllLongLat()
##print(listRes)


#bdBoutique.longLatToCsv(bd.listingBoutiqueId())
##print(bdBoutique.getListBoutique())  ## send au tel
## http://stackoverflow.com/questions/6951052/differences-between-key-superkey-minimal-superkey-candidate-key-and-primary-k 
