Translate DITA Files using Google Translate API

1. Building

   Build using ant (http://ant.apache.org).  The build.xml file will
   create an executable jar, translateDita.jar, that embeds all
   dependent classes.


2. Running

   java -jar translateDita.jar target-language source-dir [api-key]

   target-language -- will be passed to Google Translate, e.g. ja for Japanese
   source-dir -- directory containing DITA files
   api-key -- OAuth 1.0 API Key for Google Translate
  
   Example: java -jar translateDita.jar ja c:\ditadocs

   If the API key is omitted, the program will look for a file named
   .googletranslateapikey in the current directory and attempt to read
   the API key from this file.

   The program will process all files with extension ".xml" in the
   source directory, and in any subdirectories.  If an xml file
   contains DITA XML (i.e. the root element is "concept", "task", or
   "reference"), then the program will extract all text nodes and pass
   them to Google Translate.  THE ORIGINAL TEXT NODES WILL BE REPLACED
   WITH THE TRANSLATED TEXT AND THE TRANSLATED FILE WILL BE UPDATED IN
   PLACE.

   Note that the Google Translate API has no free usage and requires
   you to set up a billing account before it can be used.

   
3. Known Limitations

   Output files are always written using UTF-8 character encoding.
   This is assumed to be a non-issue.

   The program currently does not translate the navtitle attributes in
   ditamap files.  We plan to fix this.

   The program does not take a file argument, only a directory
   argument.  We plan to fix this.

   The program does not handle Google API errors gracefully,
   especially rate limit errors.  Workaround is to wait and retry
   manually.

