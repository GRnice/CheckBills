import numpy as np
from scipy.cluster.vq import vq, kmeans, whiten, kmeans2
import matplotlib.pyplot as plt

coordinates = np.array([
           [1.0, 5.5],  ## lat, long
           [10.00, 11.1],
           [19.1, 25.5]
           ])

x, y = kmeans2(whiten(coordinates), 3, iter = 20)
print("x ", x)
print("y ", y)

plt.scatter(coordinates[:,0], coordinates[:,1], c=y);
plt.show()
