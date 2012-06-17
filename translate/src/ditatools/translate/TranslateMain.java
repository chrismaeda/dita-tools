/*
   Copyright 2012 Brick Street Software, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package ditatools.translate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

public class TranslateMain
{
	private String API_KEY =  null;
	private String LANGUAGE = null;
	private DitaTranslator xmlTranslate;
	private FileFilter filter;

	public TranslateMain(String apiKey, String lang)
	{
		API_KEY = apiKey;
		LANGUAGE = lang;
		xmlTranslate = new DitaTranslator(API_KEY, LANGUAGE);
		filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(".xml") ||
						pathname.isDirectory() ) {
					return true;
				}
				return false;
			}
		};
	}

	public String getLanguage() { return LANGUAGE; }
	public void setLanguage(String val) { LANGUAGE = val; }

	public boolean isLanguageValid()
	{
		return xmlTranslate.isLanguageValid(LANGUAGE);
	}

	/**
	 * @param file the directory that contains the xml docs
	 */
	public void processFile(File file) {
		
		if (file.isFile()) {
			System.out.println("File: "+file.getName());
			xmlTranslate.translateXML(file, file);
		} 
		else if (file.isDirectory()) {
			File[] listOfFiles = file.listFiles(filter);
			if (listOfFiles != null && listOfFiles.length > 0) {
				for (int i = 0; i < listOfFiles.length; i++)
					processFile(listOfFiles[i]);
			}
		}
	}

	public static void main(String[] args)
	{
		if (args.length < 2) {
			System.err.println("Usage: language input-dir [api-key]");
			System.err.println("Example: ja c:\\user_guide");
			System.err.println("api-key may also be stored in a file named .googletranslateapikey in the current directory");
			System.exit(1);
		}

		// read Google Translate API key from command line or from file
		String apiKey = null;
		if (args.length == 3) {
			apiKey = args[2];
		} else {
			File apiKeyFile = new File(".googletranslateapikey");
			if (!apiKeyFile.exists()) {
				System.err.println("ERROR: A google api key needs to be specified.");
				System.exit(1);
			}
			
			try {
				BufferedReader in = new BufferedReader(new FileReader(apiKeyFile));
				String str;
				if ((str = in.readLine()) != null) {
					apiKey = str;
				}
				in.close();
				if (str == null || str.trim().isEmpty()) {
					System.err.println("ERROR: A google api key needs to be specified.");
				}
			} catch (IOException e) {
				System.err.println("ERROR: cannot read google api key file.");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}

		// create main object
		String lang = args[0];
		TranslateMain xlate = new TranslateMain(apiKey, lang);

		// validate language
		if (! xlate.isLanguageValid()) {
			System.err.println("ERROR: invalid language code");
			System.exit(1);
		}

		String inputDirStr = args[1];
		File inputDir = new File(inputDirStr);
		if (!inputDir.isDirectory()) {
			// XXX process single file??
			System.err.println("ERROR: Provided argument is not a directory.");
			System.exit(1);
		} 
		else {
			System.out.println("Start.");
			xlate.processFile(inputDir);
			System.out.println("Finish.");
		}
	}
}
