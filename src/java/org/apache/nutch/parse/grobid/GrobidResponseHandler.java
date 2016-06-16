package org.apache.nutch.parse.grobid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;

public class GrobidResponseHandler extends DefaultHandler{

	static final Logger LOG = LoggerFactory.getLogger(GrobidResponseHandler.class);
	public String articleTitle;
	public String articleAbstract;

	String temp;
	boolean inTitle=false;
	boolean inAbstract=false;


	public void processResponse(String resp) throws SAXException, IOException, ParserConfigurationException{
		articleTitle="";
		articleAbstract="";

		SAXParserFactory spfac = SAXParserFactory.newInstance();
		SAXParser sp = spfac.newSAXParser();
		sp.parse(new ByteArrayInputStream(resp.getBytes()),this);
	}

	public void startElement(String uri, String localName,String qName, 
                Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("title")){
			inTitle=true;
			LOG.info("In title..");
		}
		if (qName.equalsIgnoreCase("abstract")){
			inAbstract=true;
			LOG.info("In abstract..");
		}
	}

	public void endElement(String uri, String localName,
		String qName) throws SAXException {

		if (inTitle){
			articleTitle=new String(temp);
			temp="";
			LOG.info("Article Title: "+ articleTitle);
			inTitle=false;
		}
		if (inAbstract){
			articleAbstract=new String(temp);
			temp="";
			LOG.info("Article Abstract: "+ articleAbstract);
			inAbstract=false;
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		temp = new String(ch, start, length);
	}



}


