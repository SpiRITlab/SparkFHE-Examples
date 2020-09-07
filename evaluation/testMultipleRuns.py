import os
import sys
import getopt
import time
import signal
import math

def runTheProgram(n, libraryScheme, rowSize, colSize, resultFileName):
    print("Output redirected to", resultFileName)
    for i in range( 1, n+1 ):
        os.system("./runTest.bash %s %s %s >> %s" % (libraryScheme, rowSize, colSize, resultFileName))

    print("DONE")

def collectOperationRuntime( resultFileName ):
    FILE = open( resultFileName )
    TIME_MAP = dict()
    for line in FILE:
        line = line.strip()
        word = line.split(':')
        if len(line) == 0:
            pass
        elif word[0] == "TIMEINFO":
            if word[1] in TIME_MAP:
                runtimeList = TIME_MAP[word[1]]
                runtimeList.append(float(word[2]))
                TIME_MAP[word[1]] = runtimeList
            else:
                TIME_MAP[word[1]] = [float(word[2])]
        else:
            pass

    FILE.close()

    print("Runtime Map:")
    print(TIME_MAP)
    return TIME_MAP

def processResults( TIME_MAP, resultFileName ):
    f = open(resultFileName, 'w+')
    f.write(str(TIME_MAP))
    f.write("\n\n")
    f.write('Time Statistics for Each Operation: ')
    f.write("\n")
    for key in TIME_MAP.keys():
        sum_time = 0
        #calculate average
        for value in TIME_MAP[key]:
            sum_time += value
        avg_time=sum_time/len(TIME_MAP[key])
        f.write('{0:35}'.format(key)+str(avg_time)+ ' (AVG)\n')

        # calculate standard deviation
        sum_diff=0
        for value in TIME_MAP[key]:
            sum_diff+=math.pow(value - avg_time, 2)
        sd_time=math.sqrt(sum_diff/len(TIME_MAP[key]))
        f.write('{0:35}'.format(key)+str(sd_time)+ ' (SD)\n')

    f.close()

def createDir(dirname):
    if not os.path.exists(dirname):
        os.makedirs(dirname)

def printHelp():
    print('USAGE: ./testMultipleRuns.py -n <N> -s <libraryScheme> -r <rowSize> -c <columnSize> -o <outputfile>')
    print('Options for <libraryScheme>: SEAL-BFV | SEAL-CKKS | HELIB-BGV | HELIB-CKKS')
    sys.exit(2)

def main():
    if len(sys.argv)<2:
        printHelp()

    n = 0
    libraryScheme = ''
    rowSize = ''
    colSize = ''
    resultFileName = ''

    try:
        opts, args = getopt.getopt(sys.argv[1:], "n:s:r:c:") #o:
    except getopt.GetoptError:
        printHelp()
    for opt, arg in opts:
        if opt == '-n':
            n = int(arg)
        elif opt == '-s':
            libraryScheme = arg
        elif opt == '-r':
            rowSize = int(arg)
        elif opt == '-c':
            colSize = int(arg)
#        elif opt == '-o':
#            resultFileName = arg
        else:
            printHelp()
    
    print(n)
    print(libraryScheme)
    print(rowSize)
    print(colSize)
    resultFileName = "results/result_"+str(n)+"_"+libraryScheme+"_"+str(rowSize)+"_"+str(colSize)+".txt"
    print(resultFileName)
    

    runTheProgram(n, libraryScheme, rowSize, colSize, resultFileName)
    TIME_MAP = collectOperationRuntime( resultFileName )
    processResults( TIME_MAP, resultFileName)


main()
