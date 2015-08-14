package picodedTests.webTemplateEngines.FormGenerator;

import picoded.conv.ConvertJSON;
import picoded.webTemplateEngines.*;
import picoded.webTemplateEngines.FormGenerator.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;

public class FormWrapperTemplates_test {
	
	private String getWrapperTemplatedJsonString(String jsonKeyName){
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/"+jsonKeyName+".js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			FormGenerator formGen = new FormGenerator();
			FormNode node = new FormNode(formGen, jsonMap, null);
			return formGen.wrapperInterface(false, node.getString("wrapper", "div")).apply(node).toString();
		}catch(Exception ex){
			return "";
		}
	}
	
	private String getFullTemplatedJsonWithData(String jsonKeyName){
		File jsonFile = new File("./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/"+jsonKeyName+".js");
		File jsonDataFile = new File("./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/"+jsonKeyName+"_data.js");
		try{
			String jsonFileString = FileUtils.readFileToString(jsonFile);
			Map<String, Object> jsonMap = ConvertJSON.toMap(jsonFileString);
			
			String jsonDataString = FileUtils.readFileToString(jsonDataFile);
			Map<String, Object> jsonDataMap = ConvertJSON.toMap(jsonDataString);
			
			FormGenerator formGen = new FormGenerator();
			return formGen.build(jsonMap, jsonDataMap, true).toString();
//			FormNode node = new FormNode(formGen, jsonMap, null);
			
			
		}catch(Exception ex){
			return "";
		}
	}
	
	private String getHtmlString(String jsonKeyName){
		switch(jsonKeyName){
			case "div": return getStandardDivWrapper();
			case "divWithLabel": return getLabelWrapper();
			case "divWithChild": return getChildWrapper();
			case "fullTest": return getFullTestStringWithLabel();
			case "fullTestNoLabel": return getFullTestStringWithoutSecondIterationLabel();
		}
		
		return "";
	}
	
	private String getStandardDivWrapper(){
		return "<div class='pf_div'></div>";
	}
	
	private String getLabelWrapper(){
		return "<div class='pf_label'></div>";
	}
	
	
	private String getChildWrapper(){
		return "<div class='pf_child'></div>";
	}
	
	private String getFullTestStringWithLabel(){
		return "<div class='pf_div'>"+
					"<div class='pf_label'>TextField</div>"+
					"<div class='pf_child'>"+
						"<div class='pf_div'>"+
							"<div class='pf_div'></div>"+
							"<div class='pf_child'>"+
								"<div class='pf_div'>"+
									"<div class='pf_label'>Title Label</div>"+
									"<h3 class='pf_header'>Title</h3>"+
								"</div>"+
								"<div class='pf_div'>"+
									"<input name='data' type='text' value='Person A' class='pf_inputText'></input>"+
								"</div>"+
							"</div>"+
						"</div>"+
						"<div class='pf_div'>"+
							"<div class='pf_div'></div>"+
							"<div class='pf_child'>"+
								"<div class='pf_div'>"+
									"<div class='pf_label'>Title Label</div>"+
									"<h3 class='pf_header'>Title</h3>"+
								"</div>"+
								"<div class='pf_div'>"+
									"<input name='data' type='text' value='Person B' class='pf_inputText'></input>"+
								"</div>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>";
	}
	
	private String getFullTestStringWithoutSecondIterationLabel(){
		return "<div class='pf_div'>"+
				"<div class='pf_label'>TextField</div>"+
				"<div class='pf_child'>"+
					"<div class='pf_div'>"+
						"<div class='pf_div'></div>"+
						"<div class='pf_child'>"+
							"<div class='pf_div'>"+
								"<h3 class='pf_header'>Title</h3>"+
							"</div>"+
							"<div class='pf_div'>"+
								"<input name='data' type='text' value='Person A' class='pf_inputText'></input>"+
							"</div>"+
						"</div>"+
					"</div>"+
					"<div class='pf_div'>"+
						"<div class='pf_div'></div>"+
						"<div class='pf_child'>"+
							"<div class='pf_div'>"+
								"<h3 class='pf_header'>Title</h3>"+
							"</div>"+
							"<div class='pf_div'>"+
								"<input name='data' type='text' value='Person B' class='pf_inputText'></input>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>"+
			"</div>";
	}
	
	@Test
	public void standardDivWrapperTest(){
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("div");
		String rawHtmlString = getHtmlString("div");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void divWithLabelWrapperTest(){
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("divWithLabel");
		String rawHtmlString = getHtmlString("divWithLabel");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void divWithChildrenWrapperTest(){
		String jsonTemplatedOutput = getWrapperTemplatedJsonString("divWithChild");
		String rawHtmlString = getHtmlString("divWithChild");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtmlString, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void fullTestOfWrapperAndInputWithoutLabel(){
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("fullTestNoLabel");
		String rawHtml = getHtmlString("fullTestNoLabel");
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	@Test
	public void fullTestOfWrapperAndInput(){
		String jsonTemplatedOutput = getFullTemplatedJsonWithData("fullTest");
		String rawHtml = getHtmlString("fullTest");
		
		//for debugging
		File output = new File("./test-files/test-specific/htmlGenerator/FormWrapperTemplates_test/debugFile.html");
		try{
			FileWriter fw = new FileWriter(output);
			String cleanedString = jsonTemplatedOutput.replace("><", ">\n<");
			fw.write(cleanedString);
			fw.flush();
			fw.close();
		}catch(Exception ex){
			
		}
		//for debugging
		
		boolean compliancyCheck = htmlTagCompliancyCheck(rawHtml, jsonTemplatedOutput);
		assertTrue(compliancyCheck);
	}
	
	/// Prototype lenientStringLookup
//	@Test
	public int lenientStringLookup(String source, String lookup) {
		int lowestOffset = -1;
		
		String singleTag = "";
		String cleanedTag = "";
		if(lookup.startsWith("<")){
			singleTag = lookup.substring(lookup.indexOf('<'), lookup.indexOf('>') + 1);
			cleanedTag = singleTag.substring(1,  singleTag.length() - 1);
		}else{
			char nextCharToBreak = lookup.indexOf('<') < lookup.indexOf('>') ? '<' : '>';
			singleTag = lookup.substring(0, lookup.indexOf(nextCharToBreak));
		}
		lookup = lookup.substring(singleTag.length(), lookup.length());
		
		String[] lookupArray = cleanedTag.split(" ");
		for(int a=0; a<lookupArray.length; ++a) {
			if(!source.contains(lookupArray[a])){
				return -1;
			}else{
				int index = source.indexOf(lookupArray[a]);
				lowestOffset = index > lowestOffset ? index + lookupArray[a].length() : lowestOffset;
			}
		}

		if(lookup.length() > 0){
			return lenientStringLookup(source, lookup);
		}else{
			return lowestOffset;
		}
	}
	
	public boolean htmlTagCompliancyCheck(String source, String lookup){
		String[] rawHtmlSplit = source.split("(>|<|=|\\s+|\"|\'|/)"); //this becomse "required params"
		
		for(String req:rawHtmlSplit){
			if(!lookup.contains(req)){
				return false;
			}
		}
		
		return true;
	}
	
}
