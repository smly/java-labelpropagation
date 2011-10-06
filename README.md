## java-labelpropagation

an Java implementation of GFHF ([Zhu and Ghahramani, 2002]).

 Iterate
  1. \hat{Y}^{(t+1)} \leftArrow D^{-1} W \hat{Y}^{(t)}
  2. \hat{Y}^{(t+1)}_l \leftArrow Y_l
 until convergence to \hat{Y}^{(\infty)}

## Usage

 $ mvn compile
 $ mvn package
 $ cat data/sample.json
 [2, 1, [[1, 1.0], [3, 1.0]]]
 [3, 0, [[1, 1.0], [2, 1.0], [4, 1.0]]]
 [4, 0, [[3, 1.0], [5, 1.0], [8, 1.0]]]
 [5, 0, [[4, 1.0], [6, 1.0], [7, 1.0]]]
 [6, 2, [[5, 1.0], [7, 1.0]]]
 [7, 0, [[5, 1.0], [6, 1.0]]]
 [8, 0, [[4, 1.0], [9, 1.0]]]
 [9, 2, [[8, 1.0]]]
 $ java -classpath target/labelprop-1.0-SNAPSHOT-jar-with-dependencies.jar \
    org.ooxo.LProp \
    -a GFHF \
    -m 100 \
    -e 10e-5 \
    data/sample.json
 Number of vertices:            9
 Number of class labels:        2
 Number of unlabeled vertices:  6
 Numebr of labeled vertices:    3
 eps:                          1e-5
 max iteration:                100
 .............................
 iter = 29, eps = 9.918212890613898E-5
 [1,1,[1,0.8706],[2,0.1294]]
 [2,1,[1,1.0000],[2,0.0000]]
 [3,1,[1,0.7412],[2,0.2588]]
 [4,2,[1,0.3529],[2,0.6470]]
 [5,2,[1,0.1412],[2,0.8588]]
 [6,2,[1,0.0000],[2,1.0000]]
 [7,2,[1,0.0706],[2,0.9294]]
 [8,2,[1,0.1765],[2,0.8235]]
 [9,2,[1,0.0000],[2,1.0000]]

## References

- Chapelle O, Schölkopf B and Zien A: Semi-Supervised Learning, 508, MIT Press, Cambridge, MA, USA, (2006).
- http://mitpress.mit.edu/catalog/item/default.asp?ttype=2&tid=11015
