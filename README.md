# Flow Log Parser

This code parses AWS VPC flow log data and maps each row to a tag based on a lookup table. It generates counts for each tag and port/protocol combination.


## Requirements
( code written in java)
- Java Development Kit (JDK) 8 or higher 
- Input files in plain text format

## Input Files

1. **Lookup Table File**: CSV file with columns `dstport,protocol,tag`
2. **Protocol Numbers File**: CSV file mapping protocol numbers to names (from IANA)
3. **Flow Log File**: AWS VPC flow log data (version 2 format)

## Execution

1. Compile: javac FlowLogParser.java
2. Run: java FlowLogParser <lookup-table-file> <protocol-numbers-file> <flow-log-file>
Example for the files in repo: java FlowLogParser lookup.csv protocol-numbers.csv flowlog.txt


## Output
An `output.txt` file is generated after executing the program containing:

1. Count of matches for each tag
2. Count of matches for each port/protocol combination

## Assumptions

- The protocol numbers file is from IANA protocol number assignments downloaded protocol mapping from below link
- A port/protocol combination is tagged as "Untagged" if it is not found in the lookup table.

## References

- [AWS VPC Flow Logs Documentation](https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html)
- [IANA Protocol Number Assignments found in above link](http://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml)

## Troubleshooting

If there a trouble executing the program:
1. Make sure all of the input files are in the correct format and encoding.
2. Verify whether JRE is properly installed.
3. Ensure the paths of the input files are correct.
