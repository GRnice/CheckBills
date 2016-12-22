#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sqlite3 as lite

class BaseDeDonneeBoutique:
    def __init__(self):
        self.conn = lite.connect("boutiques.db")
        self.cur = self.conn.cursor()

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


    def readTable(self):   
        self.cur.execute("SELECT * FROM Boutiques")
        rows = self.cur.fetchall()
        for row in rows:
            print(row)
        

class BaseDeDonneeTicket:

    def __init__(self):
        self.conn = lite.connect("tickets.db")
        self.cur = self.conn.cursor()

    def createTable(self):
        try:
            self.cur.execute(
            "CREATE TABLE Tickets(idTel TEXT, date TEXT, montant TEXT, idBoutique TEXT, title TEXT, typeBill TEXT,imageFile TEXT, PRIMARY KEY(idTel, date) FOREIGN KEY(idBoutique) REFERENCES Boutiques(idBoutique))")
            self.conn.commit()
            print("Table Tickets created")
            return 0
        except:
            print("Table Tickets NOT created")
            return 1

    def insertToTable(self, ticketInfo):
        info = ticketInfo.split("*")  # ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1
        try:
            self.cur.execute("INSERT INTO Tickets(idTel, date, montant, idBoutique, title, typeBill, imageFile) VALUES (?,?,?,?,?,?,?)",
                            (info[1], info[3], info[5], int(info[7]), info[9], info[11], info[9]+".txt"))  
            self.conn.commit()
            print("insertGood")
            return 0
        except:
            print("insertNotGood")
            return 1

    def deleteFromTable(self, idTel, date): 
        try:
            self.cur.execute("DELETE FROM Tickets WHERE idTel = ? AND date = ?", (idTel,date) )
            self.conn.commit()
            print("deleteIsGood")
            return 0
        except:
            print("deleteNotGood")
            return 1

    def getFromTable(self, idTel, date): 
        try:
            self.cur.execute("SELECT idTel, date, montant, idBoutique, title, typeBill, imageFile FROM Tickets WHERE idTel = ? AND date = ?",(idTel,date))
            tck1 = self.cur.fetchone()
            print(tck1)
            return 0

        except:
            print("invalid telId OR date")
            return 1

    def readTable(self):   
        self.cur.execute("SELECT * FROM Tickets")
        rows = self.cur.fetchall()
        for row in rows:
            print(row)
                
                

#### TICKETS test
tck1 = "ID*idTel1*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre1*TYPEBILL*1"
tck2 = "ID*idTel1*DATE*12/12/2015 12:15:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre2*TYPEBILL*0"
tck3 = "ID*idTel2*DATE*12/12/2015 12:12:44*MONTANT*50*IDBOUTIQUE*1*TITRE*xxtitre3*TYPEBILL*2"
tck4 = "ID*idTel2*DATE*12/12/2015 12:19:44*MONTANT*50*IDBOUTIQUE*2*TITRE*xxtitre1*TYPEBILL*3"

##bd = BaseDeDonneeTicket()
##bd.createTable()
##bd.insertToTable(tck1)
##bd.insertToTable(tck2)
##bd.insertToTable(tck3)
##bd.insertToTable(tck4)

##bd.deleteFromTable("idTel1", "12/12/2015 12:12:44")
##bd.getFromTable("idTel2", "12/12/2015 12:12:44")

##bd.readTable()


###### BOUTIQUES test
##bdBoutique = BaseDeDonneeBoutique()
##bdBoutique.createTable()

b3 = "NOM*Intermarché*LONGITUDE*19.5*LATITUDE*20.1212"
b4 = "NOM*Intermarché*LONGITUDE*19.5*LATITUDE*20.1212"
##bdBoutique.insertToTable(b3)
##bdBoutique.insertToTable(b4)
##print(bdBoutique.getListBoutique())  ## send au tel

## http://stackoverflow.com/questions/6951052/differences-between-key-superkey-minimal-superkey-candidate-key-and-primary-k 
