import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowLogParserTest {

    @TempDir
    Path tempDir;

    private File lookupTableFile;
    private File protocolNumbersFile;
    private File flowLogFile;
    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        lookupTableFile = tempDir.resolve("lookup.csv").toFile();
        protocolNumbersFile = tempDir.resolve("protocols.csv").toFile();
        flowLogFile = tempDir.resolve("flowlog.txt").toFile();
        outputFile = tempDir.resolve("output.txt").toFile();
    }

    @Test
    void testLoadLookupTable() throws IOException {
        writeToFile(lookupTableFile,
                "25,tcp,sv_P1\n" +
                        "68,udp,sv_P2\n" +
                        "443,tcp,sv_P2"
        );

        FlowLogParser.loadLookupTable(lookupTableFile.getPath());

        //Here, by processing a flow log entry we are testing since can't directly access private lookupTable
        writeToFile(flowLogFile, "2 123456789012 eni-1234567890 10.0.1.4 10.0.2.5 12345 25 6 5 100 1234567890 1234567891 ACCEPT OK");
        FlowLogParser.processFlowLogFile(flowLogFile.getPath());
        FlowLogParser.outputResults(outputFile.getPath());

        List<String> output = Files.readAllLines(outputFile.toPath());
        assertTrue(output.contains("sv_P1,1"));
    }

    @Test
    void testLoadProtocolMap() throws IOException {
        writeToFile(protocolNumbersFile,
                "Decimal,Keyword,Protocol,Reference\n" +
                        "1,ICMP,Internet Control Message,RFC 792\n" +
                        "6,TCP,Transmission Control,RFC 793"
        );

        FlowLogParser.loadProtocolMap(protocolNumbersFile.getPath());

        assertEquals("tcp", FlowLogParser.mapProtocolNumberToName("6"));
        assertEquals("icmp", FlowLogParser.mapProtocolNumberToName("1"));
        assertEquals("unknown", FlowLogParser.mapProtocolNumberToName("99"));
    }

    @Test
    void testProcessFlowLogFile() throws IOException {
        writeToFile(lookupTableFile, "443,tcp,sv_P2");
        writeToFile(protocolNumbersFile,
                "Decimal,Keyword,Protocol,Reference\n" +
                        "6,TCP,Transmission Control,RFC 793"
        );
        writeToFile(flowLogFile,
                "2 123456789012 eni-1234567890 10.0.1.4 10.0.2.5 443 12345 6 5 100 1234567890 1234567891 ACCEPT OK\n" +
                        "2 123456789012 eni-1234567890 10.0.1.4 10.0.2.5 80 8080 6 5 100 1234567890 1234567891 ACCEPT OK"
        );

        FlowLogParser.loadLookupTable(lookupTableFile.getPath());
        FlowLogParser.loadProtocolMap(protocolNumbersFile.getPath());
        FlowLogParser.processFlowLogFile(flowLogFile.getPath());
        FlowLogParser.outputResults(outputFile.getPath());

        List<String> output = Files.readAllLines(outputFile.toPath());
        assertFalse(output.contains("sv_P2,1"));
        assertTrue(output.contains("Untagged,2"));
        assertTrue(output.contains("12345,tcp,1"));
        assertTrue(output.contains("8080,tcp,1"));
    }

    @Test
    void testValidateInputFiles() throws IOException {
        // Create the files
        lookupTableFile.createNewFile();
        protocolNumbersFile.createNewFile();
        flowLogFile.createNewFile();
        outputFile.createNewFile();

        assertTrue(FlowLogParser.validateInputFiles(
                lookupTableFile.getPath(),
                protocolNumbersFile.getPath(),
                flowLogFile.getPath(),
                outputFile.getPath()
        ), "File validation failed for existing files");


    }


    @Test
    void testMainMethodWithInvalidArguments() {

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        FlowLogParser.main(new String[]{});

        String output = out.toString();
        assertTrue(output.contains("Usage: java FlowLogParser <lookup-table-file> <protocol-numbers-file> <flow-log-file>"));


        System.setOut(System.out);
    }

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }
}