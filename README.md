# Movie-Recommender-System
Utilized Item Collaborative Filtering algorithm with Hadoop to recommend movies based on user's rating history

## What's recommender system

Recommender system is attempting to predict items that users may be interested in, or help people find information that my interest them.

## How it works

Recommender system is going to do recommendations, so it needs find out some relation between already-has items and items to be recommended.

There are several different [approaches](https://en.wikipedia.org/wiki/Recommender_system)<sup>[1]</sup>: collaborative filtering, content-based filtering, and hybrid recommender systems

For collaborative filtering, there are two main ways to realize:

1. *User Collaborative Filtering* (UserCF): A form of collaborative filtering based on the similarity between **users** calculated using people's ratings of those items.

|    User   | Movie 1   | Movie 2   | Movie 3   | Movie 4   |
|   :---:   |   :---:   |   :---:   |   :---:   |   :---:   |
| A         |    10     |     4     |     9     |     9     |
| B         |    5.5    |     8     |     5     |     5     |
| C         |    8      |     2     |     8.5   | recommend |

> Example 1

In this example, because user A and B both rate movie 1 and 3 as higher scores, we may think these two uses are similar. Since A gives movie 4 a higher score, then we recommend movie 4 to user C.

2. *Item Collaborative Filtering* (ItemCF): A form of collaborative filtering based on the similarity between **items** calculated using people's ratings of those items.

|    User   | Movie 1   | Movie 2   | Movie 3   |
|   :---:   |   :---:   |   :---:   |   :---:   |
| A         |    10     |     4     |     9     |
| B         |    9      |     5     |     9     |
| C         |    8      |     2     | recommend |

> Example 2

In this example, both movie 1 and 3 get higher scores based on ratings of user A and B, then we think these two movies are similar. Now C rates movie 1 as a higher score too, then we recommend movie 3 to C.

*Note: I just use these two example to explain UserCF and ItemCF, if we want to realize collaborative filtering algorithm, actually we indeed need big data to do such prediction.*

In this project, I use ItemCF.

But question is how to describe relationship between different items? And how to define relationship between different movies?

We use following two matrices:

### 1. Co-occurrence Matrix

We use co-occurrence matrix to represent the relation between different movies.

| User    | Movie 1 | Movie 2 | Movie 3 | Movie 4 | Movie 5 |
| :-----: | :-----: | :-----: | :-----: | :-----: | :-----: |
| A       |  1      | 1       |         |1        |         |
| B       |  1      | 1       | 1       |         |         |
| C       |         | 1       | 1       |         |1        |
| D       |         | 1       |         |1        |         |

> Example 3-1: 1 means user rates on that movie

Despite considering scores, we firstly consider the amount of people who watched the same movie. So we can have following co-occurrence matrix:

|         | Movie 1 | Movie 2 | Movie 3 | Movie 4 | Movie 5 |
| :-----: | :-----: | :-----: | :-----: | :-----: | :-----: |
| Movie 1 |  2      | 2       | 1       |1        |0        |
| Movie 2 |  2      | 4       | 2       |2        |1        |
| Movie 3 |  1      | 2       | 2       |0        |1        |
| Movie 4 |  1      | 2       | 0       |2        |0        |
| Movie 5 |  0      | 1       | 1       |0        |1        |

> co-occurrence matrix: value<Movie 1, Movie 2> == 2, means there are two people watched these two movies

It's not enough, we just know the "watched" relation between different movies, how to tell the difference between movies towards each user? 

We need to import users' rating history, that's rating matrix.

### 2. Rating Matrix

| User    | Movie 1 | Movie 2 | Movie 3 | Movie 4 | Movie 5 |
| :-----: | :-----: | :-----: | :-----: | :-----: | :-----: |
| A       |  9      | 4       |         |8        |         |
| B       |  3      | 7       | 8       |         |         |
| C       |         | 8       | 7       |         |4        |
| D       |         | 5       |         |8        |         |

> Example 3-2: values are the scores users gives to that movie

We can transform it to rating matrix, for example, from user B's ratings:

|         | User B  |    
| :-----: | :-----: |
| Movie 1 |  3      |
| Movie 2 |  7      |
| Movie 3 |  8      |
| Movie 4 |  0      |
| Movie 5 |  0      |

> Rating matrix of user B

It's very explicit.

## Calculation

### 1. Normalize co-occurrence matrix:

We need to compare relations between different movies, so we cannot use absolute values but relative values. 

So we can normalize above co-occurrence matrix to:

|         | Movie 1 | Movie 2 | Movie 3 | Movie 4 | Movie 5 |
| :-----: | :-----: | :-----: | :-----: | :-----: | :-----: |
| Movie 1 |  2/6    | 2/6     | 1/6     |1/6      |0        |
| Movie 2 |  2/11   | 4/11    | 2/11    |2/11     |1/11     |
| Movie 3 |  1/6    | 2/6     | 2/6     |0        |1/6      |
| Movie 4 |  1/5    | 2/5     | 0       |2/5      |0        |
| Movie 5 |  0      | 1/3     | 1/3     |0        |1/3      |

> Normalized co-occurrence matrix

Now we can see in original co-occurrence matrix, value<Movie 1, Movie 2> == value<Movie 2, Movie 1> == 2, but after normalization, they have different values, which just show the different relation between movie 1 and 2.

### 2.Matrix multiplication

Now we can multiply normalized co-occurrence matrix and rating matrix.

![](https://github.com/Raymond-JRLin/Movie-Recommender-System/blob/master/images/matrix%20multiplication%20of%20ItemCF.png?raw=true)

> Matrix multiplication

Then we can get result:

|         | User B  |    
| :-----: | :-----: |
| Movie 1 |  4.66   |
| Movie 2 |  4.54   |
| Movie 3 |  5.5    |
| Movie 4 |  3.4    |
| Movie 5 |  5(recommend) |

> Predicted rating matrix of user B

For user B, he/she does not watch movie 4 and 5, because movie 5 has higher predicted score, so we recommend movie 5 to B.

Actually, I do more in codes, like dividing data by user, matrix transpose, but I do not include those in this brief explanation, you can check my codes for more details.


## Data

The [small test data](https://github.com/Raymond-JRLin/Movie-Recommender-System/tree/master/input) is what I created to test, including 5 users and 7 movies.

The [big test data](https://github.com/Raymond-JRLin/Movie-Recommender-System/tree/master/bigDataInput) concludes 100 movies with hundreds people's ratings, which comes from Netflix Prize Challenge.

Each movie data file contains all those users' ratings on that movie.

If you want to check what those movies exactly are, you can check [movie data set](https://github.com/Raymond-JRLin/Movie-Recommender-System/tree/master/MovieBigDataSet) in my repo.


## Prerequisite

You need to have [Docker](https://store.docker.com/search?type=edition&offering=community) installed in your laptop.

For Mac: download [here](https://docs.docker.com/docker-for-mac/)

For Linux:

```
sudo apt-get update

sudo apt-get install wget

sudo curl -sSL https://get.daocloud.io/docker | sh # users in mainland of China can use instead: sudo curl -sSL https://get.docker.com (https://get.docker.com/) | sh

sudo docker info
```

## How to run

### 1. Run Docker

If it shows 

>docker is running

, it means you already ran docker successfully.

For users in mainland of China, you can consider using acceleration service provided by cloud tech company.

### 2. Create a new directory in ~/src and Clone this repo 
```
cd ~/src # open ~/src, if there doesn't exist, then use mkdir to create one

mkdir recommender

cd recommender

git clone https://github.com/Raymond-JRLin/Movie-Recommender-System.git
```

### 3. Create Hadoop cluster nodes (for Mac/Linux)
```
docker pull joway/hadoop-cluster # pull docker image

git clone https://github.com/Raymond-JRLin/Hadoop-Cluster-Docker.git # clone related code

sudo docker network create --driver=bridge hadoop #create a bridge for hadoop nodes to communicate
```

### 4. Run Hadoop
```
cd hadoop-cluster-docker

./start-container.sh # open and enter docker container

./start-hadoop.sh # start Hadoop
```
If it shows 

>root@hadoop-master:#

, it means you already in docker and run Hadoop right now.

### 5. Run recommender system

*Attention: I have small (named /input) and big test data (/bigDataInput). You'd better test small data first and then big data, also attention to use corresponding file names.*

Make sure now you are in repo directory:

```
hdfs dfs -mkdir /input # create a directory for inputs

hdfs dfs -put input/* /input  # upload user's ratings

hdfs dfs -rm -r /dataDividedByUser

hdfs dfs -rm -r /coOccurrenceMatrix

hdfs dfs -rm -r /Normalize

hdfs dfs -rm -r /Multiplication

hdfs dfs -rm -r /Sum # make sure there's no these directories

cd src/main/java/ # enter our codes files directory

hadoop com.sun.tools.javac.Main *.java # compile jave source code

jar cf recommender.jar *.class # make jar file

hadoop jar recommender.jar Driver /input /dataDividedByUser /coOccurrenceMatrix /Normalize /Multiplication /Sum # run jar

# args0: original dataset

# args1: output directory for DividerByUser job

# args2: output directory for coOccurrenceMatrixBuilder job

# args3: output directory for Normalize job

# args4: output directory for Multiplication job

# args5: output directory for Sum job

```

### 6. Check results

```
hdfs dfs -cat /Sum/*

hdfs dfs -ls / # if you got correct results, you can see there are corresponding outputs in those directories
```

For example, if you run small test data, you should have following results (you can follow step 7 to download results to compare):

> 1:10001	2.095238095238095

> 1:10002	2.4230769230769234

>1:10003	2.294117647058824

>1:10004	1.861111111111111

>1:10005	1.55

>1:10006	1.8

>1:10007	1.25

>2:10001	2.1666666666666665

>2:10002	2.5000000000000004

>2:10003	2.4411764705882355

>2:10004	2.0

>2:10005	1.55

>2:10006	2.05

>2:10007	1.0

>3:10001	1.9047619047619047

>3:10002	1.4230769230769234

>3:10003	1.4411764705882355

>3:10004	2.111111111111111

>3:10005	2.6

>3:10006	1.6500000000000001

>3:10007	3.875

>4:10001	3.0

>4:10002	2.8461538461538463

>4:10003	3.1470588235294117

>4:10004	3.0555555555555554

>4:10005	2.6

>4:10006	3.3

>4:10007	2.375

>5:10001	3.2380952380952377

>5:10002	3.2692307692307696

>5:10003	3.3235294117647065

>5:10004	3.277777777777778

>5:10005	3.2

>5:10006	3.45

>5:10007	2.875

1 - 5 are users, 10001 - 10007 are movies.

### 7. Download results if you want
```
hdfs dfs -get <src> <localDest> # src: the addresss of original file you want download, localDest: name of download file you wanna give
```

## Reference
1. [Wikipedia](https://en.wikipedia.org/wiki/Recommender_system)
