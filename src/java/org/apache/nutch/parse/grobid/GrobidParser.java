/**
 * @author jachermocilla@gmail.com
 */
 

package org.apache.nutch.parse.grobid;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.nutch.net.protocols.Response;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.protocol.Content;
import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * GrobidParser class 
 * Nutch parse plugin for research papers Content Type : application/pdf
 * using grobid service
 */
public class GrobidParser implements Parser {

  private String grobidEndPoint = "http://10.0.3.229:8080/processFulltextDocument";
  private String tempFilename = "/home/ubuntu/input.pdf";

  private static final Logger LOG = LoggerFactory.getLogger(GrobidParser.class);
  private Configuration conf;

  /** Creates a new instance of GrobidParser */
  public GrobidParser() {
  }

  public ParseResult getParse(final Content content) {

    String resultText = null;
    String resultTitle = null;
    Outlink[] outlinks = null;
    List<Outlink> outLinksList = new ArrayList<Outlink>();

    try {
      final String contentLen = content.getMetadata().get(
          Response.CONTENT_LENGTH);
      final int len = Integer.parseInt(contentLen);

      LOG.info("PDF File Size: " + len);

      final byte[] contentInBytes = content.getContent();

      LOG.info("Saving content..");
      Files.delete(Paths.get(tempFilename));
      Files.write(Paths.get(tempFilename),contentInBytes,StandardOpenOption.CREATE);


      LOG.info("Constructing POST request..");
      File f = new File(tempFilename);
      PostMethod filePost = new PostMethod(grobidEndPoint);
      Part[] parts = {new FilePart("input", f)};
      filePost.setRequestEntity(
            new MultipartRequestEntity(parts, filePost.getParams())
         );
      HttpClient client = new HttpClient();
      int status = client.executeMethod(filePost);

      //LOG.info(filePost.getResponseBodyAsString());

	//this file should be saved

      LOG.info("Parsing Grobid response...");
      GrobidResponseHandler h = new GrobidResponseHandler();
      h.processResponse(filePost.getResponseBodyAsString());
      resultTitle = h.articleTitle;
      resultText = h.articleAbstract + h.articleBody;

      LOG.info("Article Title: " + resultTitle);	
      LOG.info("Article Abstract: " + resultText);	

    } catch (Exception e) {
      return new ParseStatus(ParseStatus.FAILED,
          "Can't be handled as pdf document. " + e).getEmptyParseResult(
          content.getUrl(), getConf());
    }

    if (resultText == null) {
      resultText = "";
    }

    if (resultTitle == null) {
      resultTitle = content.getUrl();
    }

    outlinks = (Outlink[]) outLinksList.toArray(new Outlink[0]);
    final ParseData parseData = new ParseData(ParseStatus.STATUS_SUCCESS,
        resultTitle, outlinks, content.getMetadata());

    LOG.info("Paper file parsed sucessfully !!");


    
    return ParseResult.createParseResult(content.getUrl(), new ParseImpl(
        resultText, parseData));
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return this.conf;
  }

}
