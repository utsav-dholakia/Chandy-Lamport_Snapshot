import os

filename = "../configuration_chandy.txt"
f = open(filename)
lines = []
linesToRead = 0
index = 0

for temp in f:
	if not temp.startswith('#'):
		linesToRead = int(temp.strip().split()[0])
		break
#print linesToRead

for line in f: 
	if index >= linesToRead:
		break
	if not line.startswith('#') and len(line.strip()) > 0:
		lines.append(line.strip().split())
		index = index + 1
#print lines

for line in lines:
	host = line[1]
	cmd = "osascript -e 'tell app \"Terminal\" to do script \"ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no uxd150130@" + host + " java -cp Chandy-Lamport_Snapshot/out/production/Chandy-Lamport_Snapshot/ dev.App \"'"
	os.system(cmd)