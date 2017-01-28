#!/usr/bin/env python
# -*- coding: utf-8 -*-

## y c'est longitude  (2... 7)
## x ces latitude (48. ...)

import numpy as np
import matplotlib.pyplot as plt
import csv
import random as rd
from scipy.cluster.vq import kmeans2
from pyclustering.cluster import xmeans
from pyclustering.cluster import optics
import warnings

class Centroid:
    def __init__(self, level, idx, lat, longi):
        self.poids = level   
        self.longitude = longi  ## Y
        self.latitude = lat  ## X
        self.index = idx
        self.listCluster = []

class Data: 
    def __init__(self):
        self.npList = [] 
        self.list = []
        self.listCentroid = []    ## list d'obj centroid, dont chque centroides a une list de cluster (list de tickets)
    
    ## attribuer le poid a chque centroide

    def readCsv(self):
        with open('Datarealist2.csv', 'r') as fp:
            reader = csv.reader(fp, delimiter = ';')
            
            for row in reader:
                if row :
                    self.list.append([float(row[0]), float(row[1])])  

        self.npList = np.array(self.list).astype(np.float)
        fp.close()


    def getClusters(self):
        opticinstance = optics.optics(self.list, 0.1, 5)  ##### REVOIR PARAM en fct des donnees ds self.list
        opticinstance.process()
        print(len(opticinstance.get_clusters())) ## nombre de centroides
        return opticinstance.get_clusters()


    def applyKmean(self, listClusters):
        if(len(listClusters) != 0):
            for idx in range(len(listClusters)):
                self.listCentroid.append(Centroid(0, idx, 0.0, 0.0))  ## init du centroid

                for i in range(len(listClusters[idx])):  ## opticinstance.get_clusters[idx][i]  --> represente l'indice des coordo.
                    self.listCentroid[idx].listCluster.append(self.list[listClusters[idx][i]])    ## affectation des clusters aux centroids

                centroid, label = kmeans2(np.array(self.listCentroid[idx].listCluster).astype(np.float), 1, 1)
                print("centroid generer par kmeans2", centroid)
                self.listCentroid[idx].latitude = float(centroid[0][0])  ## affectation du reste : lat, long et poids du centroid
                self.listCentroid[idx].longitude = float(centroid[0][1])
                self.listCentroid[idx].poids = len(listClusters[idx])
                
                ## reaffectation du centroids + test sur 4 boucles
            self.toString()

        else:
            centroids, label = ("no centroids", "no labels")

    def toString(self):
        for i in range(len(self.listCentroid)):
            elt = self.listCentroid[i]
            
            if(len(elt.listCluster) != 0):
                print("le centre ",i ," ayant pour long: ", elt.longitude, "et pour lat: "  , elt.latitude)
                print("ses clusters sont ", elt.listCluster)
                print("et son poids est de ", len(elt.listCluster), "poids par FCT ", elt.poids, "\n")
            print(">>>>>>>>>>>>>>  fin centroid")



#### Reading all lat,long from the file
##data = Data()
##data.readCsv()
##print("data list ", len(data.list))
##print("data type ", type(data.list[0][0]))
##
#### Optics ####  to specify number of clusters generated for kmean2
##listCluster = data.getClusters()
##print("getOptics.getClusters() ", listCluster)
##
#### Kmean
##data.applyKmean(listCluster)
