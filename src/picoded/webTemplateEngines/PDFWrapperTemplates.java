package picoded.webTemplateEngines;

import java.util.HashMap;
import java.util.Map;

public class PDFWrapperTemplates {
	
	public static FormWrapperInterface default_pdf=(node)->{
		String[] prefixSuffix = new String[2];
		
		//generating prefix
		StringBuilder prefix = new StringBuilder();
		String classString = FormGenerator.getWrapperClassString(node);
		String cssString = FormGenerator.getWrapperCssString(node);
		prefix.append("<"+HtmlTag.DIV+""+classString+cssString+">\n");	
		
		//generating suffix
		StringBuilder suffix = new StringBuilder("</"+HtmlTag.DIV+">\n");
		
		prefixSuffix[0] = prefix.toString();
		prefixSuffix[1] = suffix.toString();
		
		return prefixSuffix;
	};
	
	public static FormWrapperInterface none_pdf=(node)->{
		String[] prefixSuffix = new String[]{"", ""};
		
		return prefixSuffix;
	};
	
	private static String getWrapperClassName(FormNode node){
		if(node.containsKey("wrapperClass")){
			return node.getString("wrapperClass");
		}else{
			return "pf_wrapperClass";
		}
	}
	
	public static Map<String, FormWrapperInterface> defaultPDFWrapperTemplates(){
		Map<String, FormWrapperInterface> defaultTemplates = new HashMap<String, FormWrapperInterface>();
		
		defaultTemplates.put("div", default_pdf);
		defaultTemplates.put("none", none_pdf);
		
		return defaultTemplates;
	}
}