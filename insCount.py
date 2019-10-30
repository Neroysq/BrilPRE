import json
import sys

fileName = sys.argv[1]
data = json.load(open(fileName))

print(len(data["functions"][0]["instrs"]))

