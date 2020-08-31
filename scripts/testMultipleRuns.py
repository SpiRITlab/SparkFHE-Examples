import os
import sys
import getopt
import time
import signal
import math

def runTheProgram(n, paramFileName, fileName):
	print("Output redirected to", fileName)
	if len(paramFileName) == 0:
		CMD="../SecureDecisionTree "
	else:
		CMD="../SecureDecisionTree -C "+paramFileName

	os.system(":> " + fileName);
	for i in range( 1, n+1 ):
		os.system(CMD+" >> " + fileName)
	print("DONE")

def collectOperationRuntime( fileName ):
	FILE = open( fileName )
	FLAG = False
	COUNT = 0
	TIME_MAP = dict()
	for line in FILE:
		line = line.strip()
		word = line.split()
		if len(line) == 0:
			pass
		elif word[0] == "Function":
			FLAG = True
			COUNT += 1
		elif word[0] == "Overall":
			if word[0] in TIME_MAP:
				runtimeList = TIME_MAP[word[0]]
				runtimeList.append(float(word[2]))
				TIME_MAP[word[0]] = runtimeList
			else:
				TIME_MAP[word[0]] = [float(word[2])]
			FLAG = False
		elif FLAG == True:
			if len(word) == 6:
				if word[0] in TIME_MAP:
					runtimeList = TIME_MAP[word[0]]
					runtimeList.append(float(word[4]))
					TIME_MAP[word[0]] = runtimeList
				else:
					TIME_MAP[word[0]] = [float(word[4])]
			else:
				pass
		else:
			pass
	FILE.close()
	
	print("Runtime Map:")
	print(TIME_MAP)
	return TIME_MAP

def processResults( TIME_MAP, fileName ):
    f = open(fileName, 'w+')
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
	print('USAGE: ./testMultipleRuns.py -n <N> -o <outputfile> -p <paramfile>')
	sys.exit(2)

def main():
	if len(sys.argv)<2:
		printHelp()

	n = 0
	resultFileName = ''
	paramFileName = ''
	try:
		opts, args = getopt.getopt(sys.argv[1:], "n:o:p:")
	except getopt.GetoptError:
		printHelp()
	for opt, arg in opts:
		if opt == '-n':
			n = int(arg)
		elif opt == '-o':
			resultFileName = arg
		elif opt == '-p':
			paramFileName = arg
		else:
			printHelp()

	runTheProgram( n, paramFileName, resultFileName )
	TIME_MAP = collectOperationRuntime( resultFileName )
	processResults( TIME_MAP, resultFileName)



main()