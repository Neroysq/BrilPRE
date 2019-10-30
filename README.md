# BrilPRE

## Tool Usage
```bash
~/G/BrilPRE ❯❯❯ ./brilpre                                                       
Missing required parameters: <inputFile>, <outputFile>
Usage: BrilPRE [-hV] <inputFile> <outputFile>
A partial redundancy elimination pass for Bril
      <inputFile>    The source code file in JSON.
      <outputFile>   The output code file in JSON.
  -h, --help         Show this help message and exit.
  -V, --version      Print version information and exit.
```

## Evaluate
```bash
python3 evaluate.py ./test/ ./eval.sh ./tmp.csv
```