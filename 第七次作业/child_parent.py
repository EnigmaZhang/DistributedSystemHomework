import sys

from pyspark import SparkConf, SparkContext

def valueProcess(v):
    child = []
    parent = []
    pair = []
    for i in v:
        if (i[1] == "T"):
            parent.append(i[0])
        else:
            child.append(i[0])
    for i in child:
        for j in parent:
            pair.append((i, j))

    return pair

if len(sys.argv) != 3:
    print("Wrong numbers of arguments!")
    sys.exit(-1)

conf = SparkConf().setMaster("local").setAppName("ChildParent")
sc = SparkContext(conf=conf)

textData = sc.textFile(sys.argv[1])
lineData = textData.map(lambda line: line.split(","))

childData = lineData.map(lambda x: (x[0], x[1]))
parentData = lineData.map(lambda x: (x[1], x[0]))
grandData = parentData.join(childData).map(lambda x: x[1])
grandData.saveAsTextFile(sys.argv[2])
# # Add both realtionships at the same time
# relationshipData = lineData.flatMap(lambda line: [(line[0], (line[1], "T")), (line[1], (line[0], "F"))])
# groupRelationship = relationshipData.groupByKey().mapValues(valueProcess).flatMap(lambda x: x[1])
# groupRelationship.saveAsTextFile(sys.argv[2])
