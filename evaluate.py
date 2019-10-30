#!/usr/bin/env python3
import argparse
import json
import sys
import os
import subprocess

def findAllJSONs(dir) :
    rtn = []
    for f in os.listdir(dir) :
        f = os.path.join(dir, f)
        if os.path.isfile(f) :
            if f.lower().endswith(".bril"):
                print(f)
                rtn.append(f)
        elif os.path.isdir(f) :
            rtn += findAllJSONs(f)
    return rtn


parser = argparse.ArgumentParser(description='An evaluation tool for BrilPRE')
parser.add_argument('input_path', help='The path for test cases')
parser.add_argument('eval_script', help='The script to evaluate each teat case')
parser.add_argument('output_file', help='The output file')

args = parser.parse_args()
print(args.input_path)
testcases = findAllJSONs(args.input_path)

print(testcases)
result = {}
for testcase in testcases :
    print(testcase)
    # input()

    jsonname  = testcase[:-5] + ".json"
    brilCmd = "bril2json"
    print(brilCmd)
    q = subprocess.run(brilCmd.split(" "), stdin = open(testcase, 'r'), stdout = open(jsonname, 'w'))
    #q.communicate()
    testcase = jsonname

    brilpreCmd = "./brilpre " + testcase + " " + testcase + ".pre"
    q = subprocess.run(brilpreCmd.split(" "))
    #q.communicate()

    p = subprocess.Popen([args.eval_script, testcase], stdout=subprocess.PIPE)
    output, error = p.communicate()
    result[testcase] = [output.decode("UTF-8")]
    p = subprocess.Popen([args.eval_script, testcase + ".pre"], stdout=subprocess.PIPE)
    output, error = p.communicate()
    result[testcase].append(output.decode("UTF-8"))
    # print(result[testcase].decode("UTF-8"))

for i in result :
    print(i)

def diff(x, y) :
    return [str(x), str(y), "na" if x == 0 else "{:.1%}".format((y - x) / x)]


with open(args.output_file, mode='w') as output_file :
    import csv
    csv_writer = csv.writer(output_file, delimiter = ',', quotechar = '"', quoting=csv.QUOTE_MINIMAL)
    csv_writer.writerow(["testcase", "LoC before","LoC after", "diff",  "#instr before", "#instr after", "diff", "#comp instr before", "#comp instr after", "diff"])
    for testcase, output in result.items():
        row = [testcase]
        before = output[0].split('\n')
        after = output[1].split('\n')
        # print(before)
        # print(after)
        row += diff(int(before[0]), int(after[0]))
        row += diff(int(before[-3].split(":")[1]), int(after[-3].split(":")[1]))
        row += diff(int(before[-2].split(":")[1]), int(after[-2].split(":")[1]))
        csv_writer.writerow(row)
