/*
 * CSVManager.java
 * 
 * Copyright 2019 Nor Dogroth
 * 
 */
import java.util.*;
import java.io.*;
import javax.swing.text.JTextComponent;



public class CSVManager {
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Default Properties
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	public static String  defaultSeparator = "\t";
	public static boolean defaultHasHeaderLine = false;
	public static boolean defaultAllowErrorOutput = true;
	public static boolean defaultIgnoreOutOfBounds = true;
	public static boolean defaultIgnoreDiffColumns = true;
	public static boolean defaultPrintStackTrace = true;
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Instance Properties
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	
	private File file;
	private String separator;
	private int numColumns;
	private boolean hasHeaderLine;
	private boolean allowErrorOutput;
	private boolean ignoreOutOfBounds;
	private boolean ignoreDiffColumns;
	
	protected JTextComponent errorOutputComponent;
	protected ArrayList<String[]> dataTable;
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Constructors
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	
	public CSVManager (File file) {
		this.file = file;
		init();
	}
	public CSVManager (File file, String separator) {
		this.file = file;
		init();
		this.separator = separator;
	}
	
	public CSVManager (String pathName) {
		this.file = new File(pathName);
		init();
	}
	public CSVManager (String pathName, String separator) {
		this.file = new File(pathName);
		init();
		this.separator = separator;
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Getter
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	public File file() { return this.file; }
	public String separator() { return this.separator; }
	public int numColumns() { return this.numColumns; }
	public boolean hasHeaderLine() { return this.hasHeaderLine; }
	public boolean allowErrorOutput() { return this.allowErrorOutput; }
	public boolean ignoreOutOfBounds() { return this.ignoreOutOfBounds; }
	public boolean ignoreDiffColumns() { return this.ignoreDiffColumns; }
	//~ public ArrayList<String[]> dataTable() { return this.dataTable; }
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Setter
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	public void setSeparator (String separator) {
		this.separator = separator;
	}
	public void setHasHeaderLine (boolean hasHeaderLine) {
		this.hasHeaderLine = hasHeaderLine;
	}
	public void setFile (File file) {	// This allows usage of the same instance for different csv files.
		this.file = file;
	}
	public void setFile (String pathName) {
		this.file = new File(pathName);
	}
	public void setOutputComponent (JTextComponent errorOutputComponent) {	// If one uses this, error output will added to this component.
		this.errorOutputComponent = errorOutputComponent;
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Definition Methods
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

	private void init() {
		this.dataTable = new ArrayList<String[]>();
		numColumns = 0;
		initDefaults();
	}
	
	protected void initDefaults() {
		this.separator = CSVManager.defaultSeparator;
		this.hasHeaderLine = CSVManager.defaultHasHeaderLine;
		this.allowErrorOutput = CSVManager.defaultAllowErrorOutput;
		this.ignoreOutOfBounds = CSVManager.defaultIgnoreOutOfBounds;
		this.ignoreDiffColumns = CSVManager.defaultIgnoreDiffColumns;
	}
	
	public void resetOutputComponent () {	// Errors will be written to console output.
		this.errorOutputComponent = null;
	}
	
	public void resetData() {				// This will completely vanish all loaded data; use carefully!
		dataTable = new ArrayList<String[]>();
		numColumns = 0;
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Operating Methods
// ––––––––––––––––––––––––––––––––––––––– READING ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
		
	public boolean readCSV(boolean resetDataBeforeReading) {
			// read the completed CSV file with current parameters
		BufferedReader reader = null;
		String line = null;
		if (resetDataBeforeReading)
			resetData();
		
		try {
			reader = new BufferedReader(new FileReader(this.file));
			if (this.hasHeaderLine && this.size() == 0)
				this.addHeaderCSVLine(reader.readLine());	
			
			while ((line = reader.readLine()) != null) {
				this.addCSVLine(line);
			}
			return true;
		}
		catch(FileNotFoundException e) {			
			errorOutput("File not found: " + file.getPath() + ".");
			if (CSVManager.defaultPrintStackTrace) e.printStackTrace();
		}
		catch(IOException e) {
			errorOutput("Error while reading file: " + file.getPath() + ".");
			if (CSVManager.defaultPrintStackTrace) e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean readCSV() {
		return readCSV(false);		// default: call read method without deleting existing data
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Operating Methods
// ––––––––––––––––––––––––––––––––––––––– WRITING ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	
	public boolean writeCSV(boolean append) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, append));
			writer.write(this.toString());
			writer.close();
			return true;
		}
		catch (IOException e) {
			errorOutput("Error writing to file: " + file.getPath() + ".");
			if (CSVManager.defaultPrintStackTrace) e.printStackTrace();
			return false;
		}
	}
	
	public boolean writeCSV() {
		return writeCSV(false);		// default: overwrite file data when writing to file
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Operating Methods
// ––––––––––––––––––––––––––––––––––––––– EDITING ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	
	public void addDataLine (int position, String[] dataLine) {
		if (position<0 || position>this.dataTable.size())
			position=this.dataTable.size();					// invalid position -> just add at end of Table
			
		this.dataTable.add(position, dataLine);		
		if (!ignoreDiffColumns)
			if (this.numColumns != 0 && numColumns != dataLine.length)	// notice different columns as bad CSV style
				errorOutput("Mismatch detected in fields per line ("+numColumns+" vs. "+dataLine.length);
		
		this.numColumns = dataLine.length;
	}
	
	public void addDataLine (String[] dataLine) {
		this.addDataLine(-1, dataLine);
	}
	
	public void addDataLines (int position, List<String[]> dataLineList) {
		for (int i = 0; i<dataLineList.size(); i++)
			addDataLine(position+i, dataLineList.get(i));
	}
	
	public void addDataLines (List<String[]> dataLineList) {
		for (int i = 0; i<dataLineList.size(); i++)
			addDataLine(dataLineList.get(i));
	}
	
	public void addCSVLine(int position, String CSVLine) {	
		String[] dataLine = CSVLine.split(this.separator);
		addDataLine(position, dataLine);
	}
	
	public void addCSVLine(String CSVLine) {	
		addCSVLine(-1,CSVLine);
	}
	
	public void addCSVLines (int position, List<String> CSVLineList) {
		for (int i = 0; i<CSVLineList.size(); i++)
			addCSVLine(position+i, CSVLineList.get(i));
	}
	
	public void addCSVLines (List<String> CSVLineList) {
		for (int i = 0; i<CSVLineList.size(); i++)
			addCSVLine(CSVLineList.get(i));
	}
	
	public void addHeaderDataLine (String[] headerDataLine) {
		this.addDataLine(0,headerDataLine);
		if (!this.hasHeaderLine)
			setHasHeaderLine(true);
	}
	
	public void addHeaderCSVLine (String CSVHeaderLine) {
		String[] headerDataLine = CSVHeaderLine.split(this.separator);
		addHeaderDataLine(headerDataLine);
	}
	
	public void addIntegerLine (int position, int[] intLine) {
		String[] dataLine = new String[intLine.length];
		for (int i=0; i<intLine.length; i++)
			dataLine[i] = Integer.toString(intLine[i]);
		addDataLine(position, dataLine);
	}
	
	public void addIntegerLine (int[] intLine) {
		addIntegerLine(-1,intLine);
	}
	
	public void addIntegerLines (int position, List<int[]> intTable) {
		for (int i=0; i<intTable.size(); i++)
			addIntegerLine(position, intTable.get(i));
	}
	
	public void addIntegerLines (List<int[]> intTable) {
		addIntegerLines(-1,intTable);
	}
	
	public void addDoubleLine (int position, double[] doubleLine) {
		String[] dataLine = new String[doubleLine.length];
		for (int i=0; i<doubleLine.length; i++)
			dataLine[i] = Double.toString(doubleLine[i]);
		addDataLine(position, dataLine);
	}
	
	public void addDoubleLine (double[] doubleLine) {
		addDoubleLine(-1, doubleLine);
	}
	
	public void addDoubleLines (int position, List<double[]> doubleTable) {
		for (int i=0; i<doubleTable.size(); i++)
			addDoubleLine(position, doubleTable.get(i));
	}
	
	public void addDoubleLines (List<double[]> doubleTable) {
		addDoubleLines(-1, doubleTable);
	}
	
	public boolean removeLine (int lineIndex) {
		if (getDataLine(lineIndex) == null)
			return false;
		
		this.dataTable.remove(this.dataTable.get(lineIndex));
		return true;
	}
	
	public boolean removeLine (String[] dataLine) {
		if (this.dataTable.contains(dataLine)) {
			this.dataTable.remove(dataLine);
			return true;
		}
		return false;
	}
	
	public boolean removeHeaderLine () {
		if (this.hasHeaderLine && this.size()>0) {
			this.dataTable.remove(getHeaderLine());		
			setHasHeaderLine(false);
			return true;
		}
		return false;
	}
	
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Access Methods
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

	public int size() {
		return dataTable.size();
	}
	public int numLines() {
		if (this.hasHeaderLine)	
			return dataTable.size()-1;
		else
			return dataTable.size();
	}
	public int numColumnsInLine(int lineIndex) {
		if (getDataLine(lineIndex) == null)
			return 0;
		else
			return getDataLine(lineIndex).length;
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Access Methods
// ––––––––––––––––––––––––––––––––––––– DATA ACCESS ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
	// These methods allow you to access the loaded data
	// Use index 0 to access the first data line/column entry. Headers are taken into account by the access methods.
	public String[] getHeaderLine() {
		if (this.hasHeaderLine)
			return this.dataTable.get(0);
		else
			return null;
	}
	
	public String[] getDataLine(int lineIndex) {		// return one line as String[]
		if (this.hasHeaderLine)
			lineIndex++;								// first data line is always at index 0  -> if header is on position 0 we must add +1
			
		try {
			return this.dataTable.get(lineIndex);
		}
		catch (IndexOutOfBoundsException e) {			
			if (!ignoreOutOfBounds) {
				errorOutput("Line index out of bounds: " + lineIndex);
				if (CSVManager.defaultPrintStackTrace)
					e.printStackTrace(); 
			}
			return null;
		}
	}
	
	public String getDataField(int lineIndex, int columnIndex) {
		if (getDataLine(lineIndex) == null)
			return null;	// no additional error handling necessary
			
		if (this.hasHeaderLine)
			lineIndex++;	
			
		try {
			return this.dataTable.get(lineIndex)[columnIndex];
		}
		catch (IndexOutOfBoundsException e) {			
			if (!ignoreOutOfBounds) {
				errorOutput("Column index out of bounds: " + columnIndex);
				if (CSVManager.defaultPrintStackTrace)
					e.printStackTrace(); 
			}
			return null;
		}
	}
	
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Access Methods
// –––––––––––––––––––––––––––––––––––––– TO STRING –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

	// You can access certain lines as strings or the entire data table or just the header line
	// These ToString methods will use the defined separator of this instance
	
	public String lineToString(int lineIndex) {
		if (getDataLine(lineIndex) == null)
			return null;
			
			//~ System.out.println("LineIndex: " + lineIndex);	//TEST
		StringBuilder lineStringBuilder = new StringBuilder(getDataField(lineIndex, 0));
			
		for (int i=1; i<numColumnsInLine(lineIndex); i++)
			lineStringBuilder.append(this.separator + getDataField(lineIndex, i));
		
		//~ String lineString = lineStringBuilder.toString();
		return lineStringBuilder.toString();
	}
	
	public String headerToString() {
		if (this.hasHeaderLine)
			return lineToString(-1);
		else
			return "There is no header line";
	}
	
	public String toString() {
		String dataString;
		if (this.hasHeaderLine)
			dataString = headerToString() + System.lineSeparator() + lineToString(0);
		else
			dataString = lineToString(0);
		
		for (int i=1; i<this.numLines(); i++)
			dataString += System.lineSeparator() + lineToString(i);
		
		return dataString;
	}
	

// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
// Helping Methods
// ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
			
	protected void errorOutput(String errorMessage) {
			// used to manage error output that can be controlled with "allowErrorOutput" and by defining a certain "errorOutputComponent"
		if (!this.allowErrorOutput)
			return;
		if (this.errorOutputComponent == null)
			System.out.println(errorMessage);
		else
			errorOutputComponent.setText(errorOutputComponent.getText() + System.lineSeparator() + errorMessage);
	}
	
	
	
	
	
	
	
	
	
public static void main (String[] args) {}
}

