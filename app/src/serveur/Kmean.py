import numpy as np
from scipy.cluster.vq import vq, kmeans, whiten, kmeans2
import matplotlib.pyplot as plt
import csv
import random as rd

class Data:
    def __init__(self):
        self.npList = [] ## Apres ca sera un np.adarray
    
    
    def readCsv(self):
        with open('Data.csv', 'r', newline='', encoding = "utf-8-sig") as fp:
            reader = csv.reader(fp, delimiter = ';')
            
            for row in reader:
                if row :
                    self.npList.append([float(row[0]),float(row[1])])

        #print(self.npList)
        self.npList = np.array(self.npList).astype(np.float)
        fp.close()
                
        

##coordinates = np.array([
##           [1.0, 5.5],  ## lat, long
##           [10.00, 11.1],
##           [19.1, 25.5]
##           ])

data = Data()
data.readCsv()
#print(data.npList)

centroids, label = kmeans2(data.npList, 2, 5)  ## 2 centroids, 5 iteration
print(centroids)
print(label)


for i in range(len(data.npList)):
    print("coordinates ", data.npList[i], "Label ", label[i])
    plt.plot(data.npList[i][0], data.npList[i][1], "ro")

plt.scatter(centroids[:,0], centroids[:,1], c="blue", s=100)
plt.show()


#plt.scatter(x[:,0], x[:,1], c='r', s = 100);
##x, y = kmeans2(whiten(Kmeans.npList), 3, iter = 20)
#whitened = whiten(Kmeans.npList)
#book = array((whitened[0],whitened[2]))
#x, y = kmeans(whiten(Kmeans.npList), 3)
##print("x ", x[:,0])
##plt.scatter(x[:,0], x[:,1], c='r', s = 100);
##plt.show()



##LAT1 = 43.61206
##LONG1 =  7.078978
##
##LAT2 = 43.58127
##LONG2 = 7.075789
##
##LAT3 = 43.59404
##LONG3 = 7.126531
##
##for i in range(1000):
##    x = rd.uniform(LAT1, LAT2)
##    y = rd.uniform(LONG1, LONG2)
##    print(str(x) + ";" + str(y))
##
##for x in range(1000):
##    x = rd.uniform(LAT2, LAT3)
##    y = rd.uniform(LONG2, LONG3)
##    print(str(x) + ";" + str(y))
