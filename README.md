# radx-rad-metadata-compiler
This tool is designed to transform RADx-rad spreadsheets into the JSON-LD format suitable for upload to Data Hub. 
## Building the Library
To build the code in this repository you must have the following items installed:

+ [Java 17](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ Apache's [Maven](http://maven.apache.org/index.html).

Get a copy of the latest code:

    git clone https://github.com/bmir-radx/radx-rad-metadata-compiler.git

Change into the radx-rad-metadata-complier directory:

    cd radx-rad-metadata-compiler

The build it with Maven:

    mvn clean install

## Usage
The compiler can be executed through Maven's Exec:java joal.
Navigate to the Application Directory

    cd radx-rad-metadata-compiler

Then, using the following Maven command to transform your spreadsheet to JSON-LD. You need to specify the paths to your spreadsheet file, and the output file where transformed metadata will be saved.
The input spreadsheet path could be a file path or a directory path. If it is a directory path, it will transform all spreadsheets in that directory. So, please make sure that non-relavant spreadsheet are removed before doing so.
```
    mvn exec:java 
      -Dexec.args="-s <input_spreadsheet_path> 
                   -o <output_transformed_metadata_path> 
```
