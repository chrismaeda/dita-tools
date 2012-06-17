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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.google.translate.api.v2.core.Translator;
import org.google.translate.api.v2.core.TranslatorException;
import org.google.translate.api.v2.core.model.Language;
import org.google.translate.api.v2.core.model.Translation;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.ContentFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Translate a single DITA file.
 * Currently only handles <concept> files.
 */
public class DitaTranslator
{

	private final static String DOC_LANGUAGE = "en";

	private ContentFilter filter = null;
	private SAXBuilder builder = null;
	private Translator translator = null;
	private String apiKey = null;
    private String language = null;

	public DitaTranslator(String api_key, String lang)
	{
		apiKey = api_key;
		language = lang;
		translator = new Translator(apiKey);

		filter = new ContentFilter(ContentFilter.TEXT);
		// Allow elements through the filter
		filter.setElementVisible(true);
		// Allow text nodes through the filter
		filter.setTextVisible(true);

		builder = new SAXBuilder();
		builder.setFeature("http://xml.org/sax/features/validation", false);
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	}

	protected void translateTopicXML(Document doc, File outputXmlFile)
		throws JDOMException, IOException
	{
		// caller should have verified that root element 
		// is a DITA topic: concept, task, or reference
		Element rootElement = doc.getRootElement();

		// XXX handle case where xml:lang attr is not present
		rootElement.getAttribute("lang", Namespace.XML_NAMESPACE).setValue(language);

		process(rootElement);

		XMLOutputter xmlOutput = new XMLOutputter();

		// force use of UTF-8 in output
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(outputXmlFile);
			osw = new OutputStreamWriter(fos, "UTF-8");
			
			xmlOutput.output(doc, osw);
		}
		finally {
			if (osw != null) {
				try { osw.close(); } catch (Exception x) {}
			}
			if (fos != null) {
				try { fos.close(); } catch (Exception x) {}
			}
		}
	}

	protected void translateDitaMap(Document doc, File outputXmlFile)
		throws JDOMException, IOException
	{
		// Not yet...
		// scan map and translate navtitle attributes ???
	}

	/**
	 * 
	 * @param xmlFile
	 *            the xml file to be translated
	 * @param outputXmlFile
	 *            the output file
	 */
	public void translateXML(File xmlFile, File outputXmlFile) {

		try {
			Document doc = builder.build(xmlFile);

			Element rootElement = doc.getRootElement();
			String rootElementName = rootElement.getName();

			if (rootElementName.equalsIgnoreCase("concept")
				|| rootElementName.equalsIgnoreCase("task")
				|| rootElementName.equalsIgnoreCase("reference"))
				{
					translateTopicXML(doc, outputXmlFile);
				}
			else if (rootElementName.equalsIgnoreCase("map"))
				{
					translateDitaMap(doc, outputXmlFile);
				}
			else {
				System.err.println("Skipping file; Unknown element: " + rootElementName);
			}

		}
		catch (JDOMException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return true is the language args is valid
	 */
	public boolean isLanguageValid(String lang) {
		try {
			Language[] languages = translator.languages(lang);
			if (languages != null && languages.length > 0) {
				return true;
			}
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (TranslatorException e) {
			System.err.println("ERROR: Invalid language.");
			// e.printStackTrace();
		}
		return false;
	}

	/**
	 * Translate an XML element by grabbing each text node
	 * and sending it to Google Translate, and replacing the
     * source text with the translated text.
     */
	private void process(Element element)
	{
		// filter selects elements and text nodes
		List<Content> children = element.getContent(filter);
		Iterator<Content> iterator = children.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof Element) {
				Element child = (Element) o;
				process(child);
			} else {
				// Due to filter, the only other possibility is Text
				Text text = (Text) o;
				handleText(text);
			}
		}
	}

	private void handleText(Text text) {
		if (text.getTextTrim().length() > 0) {
			try {
				Translation fromEnglish = translator.translate(text.getText(), DOC_LANGUAGE, language);

				// String translatedValue = fromEnglish.getTranslatedText();
				// System.out.println(translatedValue);
				// printCharacters(translatedValue);
				// text.setText(translatedValue);

				// XXX better handling of API errors; in particular, wait
				//  and retry when hitting a short term API rate limit.

				text.setText(fromEnglish.getTranslatedText());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (TranslatorException e) {
				e.printStackTrace();
			}

		}

	}

	// used to display the hex values of the chars
	// private void printCharacters(String s) {
	// for (char c : s.toCharArray()) {
	// String h = "000" + Integer.toHexString(c);
	// h = h.substring(h.length() - 4);
	// System.out.println("U+" + h + ": <" + c);
	// }
	// }
}
