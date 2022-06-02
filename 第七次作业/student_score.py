import sys

from pyspark import SparkConf, SparkContext

if len(sys.argv) != 4:
    print("Wrong numbers of arguments!")
    sys.exit(-1)

conf = SparkConf().setMaster("local").setAppName("StudentScore")
sc = SparkContext(conf=conf)

textData = sc.textFile(sys.argv[1])
lineData = textData.map(lambda line: line.split(","))

# Average score
lineData.filter(lambda line: line[3] == "必修")
compulsoryScore = lineData.filter(lambda line: line[3] == "必修")
# Use (score,count) tuple to store both element and key numbers.
scoreData = compulsoryScore.map(lambda line: (line[1], (int(line[4]), 1)))
scoreSum = scoreData.reduceByKey(lambda x, y: (x[0] + y[0], x[1] + y[1]))
avgData = scoreSum.mapValues(lambda x: x[0] / x[1])
avgData.saveAsTextFile(sys.argv[2])

# Student number in different score range

rangeNumber = [
    avgData.filter(lambda x: 90 <= x[1] <= 100).count(),
    avgData.filter(lambda x: 80 <= x[1] < 90).count(),
    avgData.filter(lambda x: 70 <= x[1] < 80).count(),
    avgData.filter(lambda x: 60 <= x[1] < 70).count(),
    avgData.filter(lambda x: 0 <= x[1] < 60).count()
]
sc.parallelize(rangeNumber).saveAsTextFile(sys.argv[3])