import sys

pattern = sys.argv[1]
filename = sys.argv[2]

data = [line.split() for  line in open(filename,'r')]
for k in range(len(data)-len(pattern)):
    s = ''.join([data[k+j][2] for j in range(len(pattern))])
    if s == pattern:
        for j in range(len(pattern)):
            print data[k+j][0]
            