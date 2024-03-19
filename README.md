# radx-rad-metadata-compiler
This tool is designed to transform RADx-rad spreadsheets into the JSON-LD format suitable for upload to Data Hub. 
## Building the Library
To build the code in this repository you must have the following items installed:

+ [Java 17](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ Apache's [Maven](http://maven.apache.org/index.html).

Get a copy of the latest code:

    git clone https://github.com/bmir-radx/radx-rad-metadata-compiler.git

Change into the `radx-rad-metadata-compiler` directory:

    cd radx-rad-metadata-compiler

Then, build it with Maven:

    mvn clean install

## Usage
First, navigate to the Application Directory

    cd radx-rad-metadata-compiler

To transform your spreadsheet to JSON-LD, use the following Maven command. 
```
mvn exec:java 
  -Dexec.args="-s <input_spreadsheet_path> 
               -o <output_transformed_metadata_directory>"
```
You need to specify the paths to your input spreadsheet file and the output directory where the transformed metadata will be saved. The input spreadsheet path can be either a file path or a directory path. If it is a directory path, the tool will transform all spreadsheets within that directory. Therefore, please ensure that any non-relevant spreadsheets are removed beforehand.

### Transforming Multiple Spreadsheets
e.g., for transforming all spreadsheets stored in a folder named radx-rad-spreadsheets, and saving all transformed metadata in a folder named radx-rad-output, use the command:
```
mvn exec:java -Dexec.args="-s ../radx-rad-spreadsheets -o ../radx-rad-output"
```
### Transforming a Single Spreadsheet
For transforming a single spreadsheet RADxRadExampleSheet.xlsx to the JSON-LD format:
```
mvn exec:java -Dexec.args="-s ../RADxRadExampleSheet.xlsx -o ../radx-rad-output"
```
