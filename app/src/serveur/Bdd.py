#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sqlite3 as lite

##comm.sendMessage("ID*" + idTel+"*DATE*" + myBill.getDate()+"*MONTANT*"+String.valueOf(myBill.getMontant())
##        +"*LONG*"+String.valueOf(boutique.getLongitude())+"*LAT*"+String.valueOf(boutique.getLatitude()));

class BaseDeDonnee:

    def __init__(self):
        self.conn = lite.connect("ma_base.db")
        self.cur = self.conn.cursor()


    def createTable(self):
        try:
            self.cur.execute(
            "CREATE TABLE Tickets(id INTEGER PRIMARY KEY,title TEXT, date TEXT, montant TEXT, longitude TEXT, latitude TEXT, imageBytes TEXT)" )
            self.conn.commit()
            print("Table Tickets created")
            return 0
        except:
            print("Table Tickets NOT created")
            return 1

    def insertToTable(self, idTicket):  ## APRES obj ticket en param
        try:
            self.cur.execute("INSERT INTO Tickets(id, title, date, montant, longitude, latitude,imageBytes) VALUES (?,?,?,?,?,?,?)",
                            (idTicket, "carrefourTicket", "123DCSD", "15.00", "6.00013", "7.12123","123 1212 367"))  ## tout est string
            self.conn.commit()
            print("insertGood")
            return 0
        except:
            print("insertNotGood")
            return 1

    def deleteFromTable(self, idTicket): 
        try:
            self.cur.execute("DELETE FROM Tickets WHERE id = ?", (idTicket,))
            self.conn.commit()
            print("deleteIsGood")
            return 0
        except:
            print("deleteNotGood")
            return 1

    def getFromTable(self, idTicket): 
        try:
            self.cur.execute("SELECT id, title, date, montant, longitude, latitude, imageBytes FROM Tickets WHERE id = ?",(idTicket,))
            tck1 = self.cur.fetchone()
            print(tck1)
            return 0

        except:
            print("invalid Ticket id")
            return 1

    def readTable(self):   
        self.cur.execute("SELECT * FROM Tickets")
        rows = self.cur.fetchall()
        for row in rows:
            print(row)
                
                
bd = BaseDeDonnee()
bd.createTable()
bd.insertToTable(1)
bd.insertToTable(2)
bd.insertToTable(3)
bd.deleteFromTable(3)

bd.getFromTable(2)
bd.readTable()
