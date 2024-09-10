# radx-rad-metadata-compiler
This tool is designed to transform RADx-rad CSV metadata files into the JSON-LD format suitable for upload to Data Hub. 

## Input parameters
You need to specify:
1. **Input Metadata CSV Path** [Required] (c): Specify the path to your input csv file containing the metadata you need to transform. The input path can be either a file path or a directory path. If it is a directory path, the tool will transform all CSV files within that directory. Therefore, please ensure that any non-relevant CSV files are removed beforehand.
2. **Output Transformed Metadata Directory** [Required] (o): Specify the directory path where you want the transformed metadata files to be saved.
3. **RADx Metadata Specification Path** [Optional] (t): Provide the path to the RADx Metadata Specification JSON file. If not provided, the default version of the [RADx Metadata Specification](https://cedar.metadatacenter.org/templates/edit/https://repo.metadatacenter.org/templates/c691629c-1183-4425-9a12-26201eab1a10?folderId=https:%2F%2Frepo.metadatacenter.org%2Ffolders%2F5ac6dcb6-7a9b-4a75-a945-60ae43750953) will be used.

4. **CSV to Template Mappings Path** [Optional] (m): Indicate the path to the spreadsheet that contains the mapping instructions. These instructions dictate how the input metadata fields correspond to the RADx Metadata Specification. If not provided, the default [mappings](https://docs.google.com/spreadsheets/d/1R2PkJCNFtg8zm-V2iXK56u5WNoo3_hFz/edit#gid=951510363) will be used.

   The spreadsheet is organized into two main columns: `RADx-rad Field Label` and `RADx Metadata Specification Path`. For instance, the `nih_project_id` field is matched with the `Study Identifier` within the `Data File Parent Studies` element. Hence, the entry for the RADx Metadata Specification Path column should be `/Data File Parent Studies/Study Identifier`.  
   Here is an example [mappings spreadsheet](https://docs.google.com/spreadsheets/d/1R2PkJCNFtg8zm-V2iXK56u5WNoo3_hFz/edit#gid=951510363).

## Execute through shell file
The repository includes a tool for metadata transformation. The tool is packaged in a zip file [rrmc.zip](https://github.com/bmir-radx/radx-rad-metadata-compiler/releases/download/v1.0.0/rrmc.zip), which contains the shell script (rrmc) and two essential files for the transformation process: the RADx Metadata Specification and a spreadsheet that mapping the fields in radx-rad CSV template to CEDAR template.
Download rrmc.zip and then unzip. Move the extracted rrmc tool to into a location on your executable path.

### Make the Shell Script executable
1. Navigate to the directory where the `rrmc` shell script is located.
```
cd /user/local/bin/
```
2. Make the script executable:
```
chmod +x rrmc
```

### Usage

Type `./rrmc -h` to show options.

### Transforming Multiple Spreadsheets
e.g., for transforming all spreadsheets stored in a folder named radx-rad-spreadsheets, and saving all transformed metadata in a folder named radx-rad-output, use the command:

```
./rrmc \
   -c ../radx-rad-metadata \
   -t ../RADxTemplate.json \
   -m ../radx-rad-mapping.xlsx \
   -o ../radx-rad-output
```
### Transforming a Single Spreadsheet
For transforming a single spreadsheet RADxRadExampleSheet.xlsx to the JSON-LD format:

```
./rrmc  \
   -c ../RADxRadExampleSheet.csv \
   -t ../RADxTemplate.json \
   -m ../radx-rad-mapping.xlsx \
   -o ../radx-rad-output"
```
## Execute through JAR file
You can use the tool for metadata transformation directly by executing the JAR file. The JAR file can be downloaded from the following link:

[Download radx-rad-metadata-compiler.jar](https://github.com/bmir-radx/radx-rad-metadata-compiler/releases/download/v1.0.1/radx-rad-metadata-compiler.jar)

### Usage

After downloading the JAR file, you can execute it via the command line using the following syntax:

```bash
java -jar radx-rad-metadata-compiler.jar -c=<Input_Metadata_CSV_Path> -o=<Output_Directory> [-t=<Metadata_Specification_Path>] [-m=<CSV_to_Template_Mappings_Path>]
```

## Execute through Maven command

### Building the Library
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

### Usage
First, navigate to the Application Directory

    cd radx-rad-metadata-compiler

To transform your spreadsheet to JSON-LD, use the following Maven command. 
```
mvn exec:java \
  -Dexec.args="-s <input_metadata_spreadsheet_path> \
               -t <RADx_metadata_specification_path> \
               -m <mappings_spreadsheet_path> \
               -o <output_transformed_metadata_directory>"
```

### Transforming Multiple Spreadsheets
e.g., for transforming all spreadsheets stored in a folder named radx-rad-spreadsheets, and saving all transformed metadata in a folder named radx-rad-output, use the command:

```
mvn exec:java \
  -Dexec.args="-s ../radx-rad-spreadsheets \
               -t ../RADxTemplate.json \
               -m ../radx-rad-mapping.xlsx \
               -o ../radx-rad-output"
```
### Transforming a Single Spreadsheet
For transforming a single spreadsheet RADxRadExampleSheet.xlsx to the JSON-LD format:

```
mvn exec:java \
  -Dexec.args="-s ../RADxRadExampleSheet.xlsx \
               -t ../RADxTemplate.json \
               -m ../radx-rad-mapping.xlsx \
               -o ../radx-rad-output"
```
