package picoded.webTemplateEngines.FormGenerator;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.CalendarDeserializer;
import com.hazelcast.instance.Node;

import picoded.conv.ConvertJSON;
import picoded.conv.ListValueConv;
import picoded.conv.GenericConvert;
import picoded.conv.RegexUtils;
import picoded.conv.DateConv;
import picoded.conv.DateConv.ISODateFormat;
import picoded.struct.CaseInsensitiveHashMap;

public class FormInputTemplates {
	
	public static StringBuilder displayDiv(FormNode node, String pfiClass) {
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getStringValue();
		
		if (node.getString("type", "").equalsIgnoreCase("dropdown")) {
			String tempfieldValue = getCorrectFieldValue(node);
			
			if (tempfieldValue != null && !tempfieldValue.isEmpty()) {
				fieldValue = tempfieldValue;
			}
		}
		
		//thousands separator first
		if (node.getString("type", "").equalsIgnoreCase("number")) {
			boolean thousandsSeparate = node.getBoolean("thousandsSeparator", true);
			if (thousandsSeparate) {
				fieldValue = thousandsSeparator(fieldValue);
			}
		} else if (node.getString("type", "").equalsIgnoreCase("text")) {
			boolean thousandsSeparate = node.getBoolean("thousandsSeparator", false);
			if (thousandsSeparate) {
				fieldValue = thousandsSeparator(fieldValue);
			}
		}
		
		String displayPrefix = node.getString("displayPrefix", "");
		String displaySuffix = node.getString("displaySuffix", "");
		fieldValue = displayPrefix + fieldValue + displaySuffix;
		
		String textAndField = text + fieldValue;
		// if(textAndField == null || textAndField.length() <= 0) {
		// 	return new StringBuilder();
		// }
		
		StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.DIV, pfiClass, null);
		return sbArr[0].append(textAndField).append(sbArr[1]);
	}
	
	//dropdown/select handling
	private static String getCorrectFieldValue(FormNode node) {
		String fieldValue = node.getStringValue();
		
		Object rawObject = node.get("options");
		Map<String, String> keyValPair = null;
		if (rawObject != null) {
			keyValPair = optionsKeyNamePair(rawObject);
		}
		
		if (keyValPair != null) {
			return keyValPair.get(fieldValue);
		}
		
		return "";
	}
	
	private static String thousandsSeparator(String value) {
		String ret = "";
		if (value != null && !value.isEmpty()) {
			if(value.equalsIgnoreCase("0")){
				ret = "0.00";
			}else{
				boolean reappendBrackets = false;
				String tempValue = value;
				if(value.startsWith("(")){
					tempValue = tempValue.substring(1, tempValue.length());
					tempValue = tempValue.substring(0, tempValue.length() - 1);
					reappendBrackets = true;
				}
				
				DecimalFormat df = new DecimalFormat("#,###.00");
				BigDecimal bigD = new BigDecimal(tempValue);
				bigD.setScale(2);
				
				try {
					ret = df.format(bigD);
					if(reappendBrackets){
						ret = "(" + ret + ")";
					}
				} catch (Exception e) {
					//silent fail, fallback to default value
					ret = value;
				}
			}
		}
		return ret;
	}
	
	protected static FormInputInterface div = (node) -> {
		return FormInputTemplates.displayDiv(node, "pfi_div pfi_input");
	};
	
	protected static FormInputInterface header = (node) -> {
		String text = node.getString(JsonKeys.TEXT, "");
		String fieldValue = node.getStringValue() != null ? node.getStringValue() : "";
		StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.HEADER, "pfi_header pfi_input", null);
		return sbArr[0].append(text).append(fieldValue).append(sbArr[1]);
	};
	
	protected static FormInputInterface select = (node) -> {
		Map<String, String> paramsMap = new HashMap<String, String>();
		
		StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.SELECT, "pfi_select pfi_input", paramsMap);
		StringBuilder ret = sbArr[0];
		
		// Prepeare the option key value list
		List<String> keyList = new ArrayList<String>();
		List<String> nmeList = new ArrayList<String>();
		
		// Generates the dropdown list, using either map or list
		//---------------------------------------------------------
		Object dropDownObject = node.get(JsonKeys.OPTIONS);
		nmeList = dropdownNameList(dropDownObject);
		keyList = dropdownKeyList(dropDownObject);
		
		// Use the generated list, to populate the option set
		//---------------------------------------------------------
		String selectedKey = node.getStringValue();
		createDropdownHTMLString(ret, keyList, nmeList, selectedKey);
		
		ret.append(sbArr[1]);
		
		return ret;
	};
	
	protected static FormInputInterface input_text = (node) -> {
		CaseInsensitiveHashMap<String, String> paramMap = new CaseInsensitiveHashMap<String, String>();
		
		String fieldValue = node.getStringValue();
		
		paramMap.put(HtmlTag.TYPE, "text");
		if (fieldValue != null && fieldValue.length() >= 0) {
			paramMap.put(HtmlTag.VALUE, fieldValue);
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.INPUT, "pfi_inputText pfi_input", paramMap);
		return sbArr[0].append(sbArr[1]);
	};
	
	protected static FormInputInterface input_number = (node) -> {
		CaseInsensitiveHashMap<String, String> paramMap = new CaseInsensitiveHashMap<String, String>();
		String fieldValue = node.getStringValue();
		
		paramMap.put(HtmlTag.TYPE, "number");
		if (fieldValue != null && fieldValue.length() >= 0) {
			paramMap.put(HtmlTag.VALUE, fieldValue);
		}
		
		StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.INPUT, "pfi_inputNumber pfi_input", paramMap);
		return sbArr[0].append(sbArr[1]);
	};
	
	protected static FormInputInterface input_textarea = (node) -> {
		return input_textarea(node, false);
	};
	
	protected static StringBuilder input_textarea(FormNode node, boolean displayMode) {
		CaseInsensitiveHashMap<String, String> paramMap = new CaseInsensitiveHashMap<String, String>();
		String fieldValue = node.getStringValue();
		
		StringBuilder[] sbArr = new StringBuilder[2];
		sbArr[0] = new StringBuilder();
		sbArr[1] = new StringBuilder();
		
		if (!displayMode) {
			sbArr = node.defaultHtmlInput(HtmlTag.TEXTAREA, "pfi_inputTextBox pfi_input", null);
			sbArr[0].append(fieldValue);
		} else {
			String[] fieldValParaSplit = fieldValue.split("\n");
			for(String para : fieldValParaSplit){
				Map<String, Object> newParaMap = new HashMap<String, Object>();
				newParaMap.put(node.getFieldName(), para);
				FormNode paraNode = new FormNode(node._formGenerator, node, newParaMap);
				StringBuilder[] newPara = new StringBuilder[2];
				newPara = paraNode.defaultHtmlInput("pre", "pfi_inputTextBox pfi_input pfi_display", null);
				sbArr[0].append(newPara[0]);
				sbArr[0].append(para);
				sbArr[0].append(newPara[1]);
			}
		}
		return sbArr[0].append(sbArr[1]);
	}
	
	protected static FormInputInterface dropdownWithOthers = (node) -> {
		return dropdownWithOthers(node, false);
	};
	
	protected static StringBuilder dropdownWithOthers(FormNode node, boolean displayMode) {
		if (!displayMode) {
			
			Map<String, String> funcMap = new HashMap<String, String>();
			String funcName = node.getString(JsonKeys.FUNCTION_NAME, "OnChangeDefaultFuncName");
			String nodeName = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(node.getString(JsonKeys.FIELD))
				.toLowerCase();
			funcMap.put("onchange", funcName + "()"); //get this value from map
			funcMap.put("id", nodeName);
			
			StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.SELECT, "pf_select", funcMap);
			StringBuilder ret = new StringBuilder(getDropDownOthersJavascriptFunction(node) + sbArr[0].toString());
			
			// Prepeare the option key value list
			List<String> keyList = new ArrayList<String>();
			List<String> nmeList = new ArrayList<String>();
			
			// Generates the dropdown list, using either map or list
			//---------------------------------------------------------
			Object dropDownObject = node.get(JsonKeys.OPTIONS);
			nmeList = dropdownNameList(dropDownObject);
			keyList = dropdownKeyList(dropDownObject);
			
			// Use the generated list, to populate the option set
			//---------------------------------------------------------
			String selectedKey = node.getStringValue();
			createDropdownHTMLString(ret, keyList, nmeList, selectedKey);
			
			ret.append(sbArr[1]);
			
			//append inputtexthere
			String inputTextFieldName = node.getString("textField", "dropdownfieldname");
			Map<String, String> inputParamMap = new HashMap<String, String>();
			inputParamMap.put("style", "display:none");
			inputParamMap.put("type", "text");
			inputParamMap.put("name", inputTextFieldName);
			inputParamMap.put("id", inputTextFieldName);
			StringBuilder[] inputTextArr = node.defaultHtmlInput(HtmlTag.INPUT, "pf_inputText", inputParamMap);
			ret.append(inputTextArr[0].toString() + inputTextArr[1].toString());
			
			return ret;
		} else {
			StringBuilder ret = new StringBuilder();
			Map<String, String> funcMap = new HashMap<String, String>();
			String nodeName = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(node.getString(JsonKeys.FIELD))
				.toLowerCase();
			funcMap.put("id", nodeName);
			StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.DIV, "pf_select", funcMap);
			
			ret.append(sbArr[0]);
			
			String val = node.getStringValue();
			String valLowercased = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(val).toLowerCase();
			
			Object dropDownObject = node.get(JsonKeys.OPTIONS);
			List<String> keyList = dropdownKeyList(dropDownObject);
			List<String> nameList = dropdownNameList(dropDownObject);
			
			if (!keyList.contains(valLowercased)) {
				ret.append("Others: " + val);
			} else {
				ret.append(val);
			}
			
			ret.append(sbArr[1]);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static FormInputInterface checkbox = (node) -> {
		return createCheckbox(node, false, "pf_div pf_checkboxSet");
	};
	
	@SuppressWarnings("unchecked")
	protected static StringBuilder createCheckbox(FormNode node, boolean displayMode, String pfiClass) {
		
		CaseInsensitiveHashMap<String, String> paramMap = new CaseInsensitiveHashMap<String, String>();
		paramMap.put(HtmlTag.TYPE, JsonKeys.CHECKBOX);
		
		List<String> checkboxSelections = new ArrayList<String>();
		if (!node.getFieldName().isEmpty()) {
			Object nodeDefaultVal = node.getRawFieldValue();
			
			if (nodeDefaultVal != null) {
				if (nodeDefaultVal instanceof String) {
					String nodeValString = (String) nodeDefaultVal;
					if (nodeValString.contains("[")) {
						List<Object> nodeValMap = ConvertJSON.toList(nodeValString);
						for (Object obj : nodeValMap) {
							String sanitisedSelection = RegexUtils
								.removeAllNonAlphaNumeric_allowCommonSeparators((String) obj);
							sanitisedSelection = RegexUtils.removeAllWhiteSpace(sanitisedSelection);
							checkboxSelections.add(sanitisedSelection.toLowerCase());
						}
					} else {
						String sanitisedSelection = RegexUtils
							.removeAllNonAlphaNumeric_allowCommonSeparators((String) nodeDefaultVal);
						sanitisedSelection = RegexUtils.removeAllWhiteSpace(sanitisedSelection);
						checkboxSelections.add(sanitisedSelection.toLowerCase());
					}
				} else if (nodeDefaultVal instanceof List) {
					for (String str : (List<String>) nodeDefaultVal) {
						String sanitisedSelection = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(str);
						sanitisedSelection = RegexUtils.removeAllWhiteSpace(sanitisedSelection);
						checkboxSelections.add(sanitisedSelection.toLowerCase());
					}
				}
			}
		}
		
		Map<String, String> keyNamePair = new HashMap<String, String>();
		Object optionsObject = node.get(JsonKeys.OPTIONS);
		keyNamePair = optionsKeyNamePair(optionsObject);
		
		String realName = node.getFieldName();
		realName = realName.replace("_dummy", "");
		FormNode tempNode = new FormNode(node._formGenerator, node);
		tempNode.replace("field", realName + "_dummy");
		
		StringBuilder ret = new StringBuilder();
		for (String key : keyNamePair.keySet()) {
			if (!displayMode) {
				CaseInsensitiveHashMap<String, String> tempMap = new CaseInsensitiveHashMap<String, String>(paramMap);
				tempMap.put("value", key);
				String key_sanitised = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(key);
				key_sanitised = RegexUtils.removeAllWhiteSpace(key).toLowerCase();
				for (String selection : checkboxSelections) {
					if (key_sanitised.equalsIgnoreCase(selection)) {
						tempMap.put("checked", "checked");
					}
				}
				
				//generate onchange function
				if (realName != null && !realName.isEmpty()) {
					int x = 0;
					String onChangeFunctionString = "saveCheckboxValueToHiddenField('" + realName + "_dummy', '" + realName
						+ "')";
					tempMap.put("onchange", onChangeFunctionString);
				}
				
				StringBuilder[] sbArr = tempNode.defaultHtmlInput(HtmlTag.INPUT, "pfi_inputCheckbox pfi_input", tempMap);
				ret.append("<div class=\"pfc_inputCheckboxWrap\">");
				ret.append("<label class=\"pfi_inputCheckbox_label\">");
				ret.append(sbArr[0]);
				ret.append(sbArr[1]);
				ret.append("<div class=\"pfi_inputCheckbox_labelTextPrefix\"></div>");
				ret.append("<div class=\"pfi_inputCheckbox_labelText\">");
				ret.append(keyNamePair.get(key));
				ret.append("</div>");
				ret.append("</label>");
				ret.append("</div>");
			} else {
				StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.DIV, "pfi_inputCheckbox pfi_input", null);
				
				ret.append("<div class=\"pfc_inputCheckboxWrap_display\">");
				
				boolean found = false;
				for (String selection : checkboxSelections) {
					if (key.equalsIgnoreCase(selection)) {
						sbArr[0].append("<div class=\"pf_displayCheckbox pf_displayCheckbox_selected\"></div>");
						found = true;
					}
				}
				
				if (!found) {
					sbArr[0].append("<div class=\"pf_displayCheckbox pf_displayCheckbox_unselected\"></div>");
				}
				
				sbArr[0].append("<div class=\"pf_displayCheckbox_text\">" + keyNamePair.get(key) + "</div>");
				
				ret.append(sbArr[0]);
				ret.append(sbArr[1]);
				
				ret.append("</div>");
			}
		}
		
		//generate hidden input here
		String jsonValue = "";
		if (checkboxSelections != null) {
			jsonValue = ConvertJSON.fromList(checkboxSelections);
		}
		String hiddenInputTag = "";
		if (checkboxSelections != null && checkboxSelections.size() > 0) {
			hiddenInputTag = "<input type=\"text\" class=\"pfi_input\" style=\"display:none\" name=\"" + realName
				+ "\" value='" + jsonValue + "'></input>";
		} else {
			hiddenInputTag = "<input type=\"text\" class=\"pfi_input\" style=\"display:none\" name=\"" + realName
				+ "\"></input>";
		}
		StringBuilder[] wrapper = tempNode.defaultHtmlInput(HtmlTag.DIV, pfiClass, null);
		ret = wrapper[0].append(ret);
		ret.append(hiddenInputTag);
		ret.append(wrapper[1]);
		
		return ret;
	}
	
	protected static FormInputInterface table = (node) -> {
		return tableWrapper(node, false);
	};
	
	@SuppressWarnings("unchecked")
	protected static StringBuilder tableWrapper(FormNode node, boolean displayMode) {
		StringBuilder ret = new StringBuilder();
		
		//<table> tags
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper(HtmlTag.TABLE, node.prefix_standard() + "div", null);
		
		//table header/label
		ret.append("<thead>");
		if (node.containsKey("tableHeader")) {
			ret.append("<tr><th>" + node.getString("tableHeader") + "</th></tr>");
		}
		
		List<Object> tableHeaders = getTableHeaders(node);
		if (tableHeaders != null && tableHeaders.size() > 0) {
			ret.append("<tr>");
			for (Object header : tableHeaders) {
				ret.append("<th>" + (String) header + "</th>");
			}
			ret.append("</tr>");
		}
		
		ret.append("</thead>");
		ret.append("<tbody>");
		
		boolean removeLabelFromSecondIteration = false;
		boolean firstIteration = true;
		
		//data
		List<Map<String, String>> childData = getTableChildrenData(node);
		CaseInsensitiveHashMap<String, Object> nodeValues = node._inputValue;
		List<CaseInsensitiveHashMap<String, Object>> clientsValues = (List<CaseInsensitiveHashMap<String, Object>>) node._inputValue
			.get(node.getFieldName());
		
		for (Map<String, Object> childValues : clientsValues) {
			ret.append("<tr>");
			for (Map<String, String> childMap : childData) {
				String childNodeType = childMap.get(JsonKeys.TYPE);
				
				FormNode childNode = new FormNode();
				childNode._inputValue = new CaseInsensitiveHashMap<String, Object>(childValues);
				childNode.putAll(childMap);
				childNode._formGenerator = node._formGenerator;
				
				if (removeLabelFromSecondIteration && !firstIteration) {
					//remove labels
					//TODO maybe
				}
				
				FormWrapperInterface func = node._formGenerator.wrapperInterface(displayMode, childNodeType);
				
				StringBuilder sb = func.apply(childNode);
				
				ret.append("<td>");
				ret.append(sb);
				ret.append("</td>");
			}
			ret.append("</tr>");
			
			firstIteration = false;
		}
		
		ret.append("</tbody>");
		
		//final squashing
		ret = wrapperArr[0].append(ret);
		ret.append(wrapperArr[1]);
		return ret;
	}
	
	protected static FormInputInterface verticalTable = (node) -> {
		return verticalTable(node, false);
	};
	
	@SuppressWarnings("unchecked")
	protected static StringBuilder verticalTable(FormNode node, boolean displayMode) {
		StringBuilder ret = new StringBuilder();
		
		//<table> tags
		StringBuilder[] wrapperArr = node.defaultHtmlWrapper(HtmlTag.TABLE, node.prefix_standard() + "div", null);
		
		//table header/label
		ret.append("<thead>");
		if (node.containsKey("tableHeader")) {
			ret.append("<tr><th>" + node.getString("tableHeader") + "</th></tr>");
		}
		ret.append("</thead>");
		ret.append("<tbody>");
		
		//
		List<Object> tableHeaders = getTableHeaders(node);
		
		//data
		List<Map<String, String>> childData = getTableChildrenData(node);
		CaseInsensitiveHashMap<String, Object> nodeValues = node._inputValue;
		List<CaseInsensitiveHashMap<String, Object>> clientsValues = (List<CaseInsensitiveHashMap<String, Object>>) node._inputValue
			.get(node.getFieldName());
		
		//for each header type, loop through child data values and pull out the corresponding data
		int childDataCounter = 0;
		for (Object tableHeaderRaw : tableHeaders) {
			String tableHeader = (String) tableHeaderRaw;
			
			ret.append("<tr>");
			ret.append("<th>" + tableHeader + "</th>");
			
			Map<String, String> childMap = childData.get(childDataCounter); //childmap is in declare
			String type = childMap.get(JsonKeys.TYPE);
			String field = childMap.get("field");
			for (Map<String, Object> dataValues : clientsValues) { //data values is in define
				if (dataValues.containsKey(field)) {
					FormNode childNode = new FormNode();
					childNode._inputValue = new CaseInsensitiveHashMap<String, Object>(dataValues);
					childNode.putAll(childMap);
					childNode._formGenerator = node._formGenerator;
					
					FormWrapperInterface func = node._formGenerator.wrapperInterface(displayMode, type);
					
					StringBuilder sb = func.apply(childNode);
					
					ret.append("<td>");
					ret.append(sb.toString());
					ret.append("</td>");
				}
			}
			++childDataCounter;
			
			ret.append("</tr>");
		}
		
		ret.append("</tbody>");
		
		//final squashing
		ret = wrapperArr[0].append(ret);
		ret.append(wrapperArr[1]);
		return ret;
	}
	
	protected static FormInputInterface image = (node) -> {
		return image(node, false);
	};
	
	//image has no data passed in
	protected static StringBuilder image(FormNode node, boolean displayMode) {
		StringBuilder ret = new StringBuilder();
		Map<String, String> params = new HashMap<String, String>();
		String srcPath = "";
		if (node.containsKey("relativePath")) {
			srcPath = node.getString("relativePath");
		}
		params.put("src", srcPath);
		
		String styleString = "";
		if (node.containsKey("style")) {
			styleString = node.getString("style");
			params.put("style", styleString);
		}
		
		StringBuilder[] inputArr = node.defaultHtmlInput("img", "pfi_image", params);
		
		ret.append(inputArr[0]);
		ret.append(inputArr[1]);
		
		return ret;
	}
	
	protected static FormInputInterface signature = (node) -> {
		return signature(node, false);
	};
	
	protected static StringBuilder signature(FormNode node, boolean displayMode) {
		StringBuilder ret = new StringBuilder();
		
		String fieldName = node.getFieldName();
		
		Map<String, String> paramsMap = new HashMap<String, String>();
		//paramsMap.put("style", "height:135px");
		paramsMap.put("id", fieldName);
		StringBuilder[] sb = node.defaultHtmlInput("div", "pfiw_sigBox", paramsMap);
		ret.append(sb[0]);
		
		String sigValue = GenericConvert.toString(node.getRawFieldValue());//will return a file path, e.g. output/outPNG_siga.png
		//for signature we use getRawFieldValue to bypass the sanitisation that occurs in getStringValue();
		StringBuilder sigImgString = new StringBuilder();
		if (sigValue != null && !sigValue.isEmpty()) {
			FormNode innerImgNode = new FormNode();
			innerImgNode._inputValue = node._inputValue;
			innerImgNode._formGenerator = node._formGenerator;
			innerImgNode.put("type", "image");
			innerImgNode.put("relativePath", sigValue);
			//innerImgNode.put("style", "height:80px");
			
			sigImgString = innerImgNode.fullHtml(false);
			
			ret.append(sigImgString);
		}
		
		ret.append(sb[1]);
		
		String dateInputId = fieldName + "_date";
		String textString = "Click along the line to sign.";
		String dateValue = "";
		
		if (sigValue != null && !sigValue.isEmpty()) {
			dateValue = node.getStringValue(dateInputId);
			if (dateValue != null && !dateValue.isEmpty()) {
				if (DateConv.isInMillisecondsFormat(dateValue)) {
					dateValue = DateConv.toISOFormat(Long.parseLong(dateValue), ISODateFormat.YYYYMMDD, "-");
				}
				
				if (!dateValue.isEmpty()) {
					textString = "Signed on: " + dateValue;
				}
			}
		}
		
		if (!displayMode) {
			ret.append("<h3 class=\"pfi_input pfi_inputSigDate\" name=\"" + dateInputId + "\" id=\"" + dateInputId
				+ "\" value=\"" + dateValue + "\">" + textString + "</h3>");
		} else {
			if (!dateValue.isEmpty()) {
				ret.append("<h3 class=\"pfi_input pfi_inputSigDate\" name=\"" + dateInputId + "\" id=\"" + dateInputId
					+ "\">" + textString + "</h3>");
			}
		}
		
		return ret;
	}
	
	protected static FormInputInterface datePicker = (node) -> {
		return datePicker(node, false);
	};
	
	protected static StringBuilder datePicker(FormNode node, boolean displayMode) {
		StringBuilder ret = new StringBuilder();
		
		CaseInsensitiveHashMap<String, String> paramMap = new CaseInsensitiveHashMap<String, String>();
		String fieldValue = node.getStringValue(); //i expect it to be in String milliseconds format
		String dateISO = "";
		String dateMilliseconds = "";
		
		String hiddenInputTag = "";
		if (fieldValue != null && fieldValue.length() > 0) {
			
			if (DateConv.isInMillisecondsFormat(fieldValue)) {
				dateISO = DateConv.toISOFormat(Long.parseLong(fieldValue), ISODateFormat.YYYYMMDD, "-");
				dateMilliseconds = fieldValue;
			} else {
				dateISO = fieldValue;
				dateMilliseconds = DateConv.toMillisecondsFormat(fieldValue, ISODateFormat.YYYYMMDD, "-");
			}
			
			paramMap.put(HtmlTag.VALUE, dateISO);
		}
		
		if (!displayMode) {
			paramMap.put(HtmlTag.TYPE, "date");
			
			if (node.containsKey("max")) {
				String maxDate = DateConv.changeISODateFormat(node.getString("max"), ISODateFormat.YYYYMMDD,
					ISODateFormat.YYYYMMDD, "-");
				if (maxDate != null && !maxDate.isEmpty()) {
					paramMap.put("max", maxDate);
				}
			}
			
			if (node.containsKey("min")) {
				String minDate = DateConv.changeISODateFormat(node.getString("min"), ISODateFormat.YYYYMMDD,
					ISODateFormat.YYYYMMDD, "-");
				if (minDate != null && !minDate.isEmpty()) {
					paramMap.put("min", minDate);
				}
			}
			
			//retrieve the name, and append _date to it
			String hiddenInputName = node.getFieldName(); //set the hidden input field to the name given
			if (hiddenInputName != null && !hiddenInputName.isEmpty()) {
				//node.replace("field", hiddenInputName + "_date");
				node.replace("field",  node.getFieldNameWithoutPrefix() + "_date");
				
				String onchangeFunctionString = "changeDateToEpochTime(this.value, '" + hiddenInputName + "')";
				paramMap.put("onchange", onchangeFunctionString);
			}
			
			//generate hidden input field
			hiddenInputTag = "<input class=\"pfi_input pfi_hidden_date_text_field\" type=\"text\" name=\""
				+ hiddenInputName + "\" style=\"display:none\" value=\"" + dateMilliseconds + "\"></input>";
			
			StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.INPUT, "pfi_inputDate pfi_input", paramMap);
			ret.append(sbArr[0]);
			ret.append(sbArr[1]);
			if (!displayMode) {
				ret.append(hiddenInputTag);
			}
		} else {
			node.replace("type", "text");
			if (!StringUtils.isNullOrEmpty(fieldValue) && DateConv.isInMillisecondsFormat(fieldValue)) {
				fieldValue = DateConv.toISOFormat(Long.parseLong(fieldValue), ISODateFormat.DDMMYYYY, "-");
			}
			
			StringBuilder[] sbArr = node.defaultHtmlInput(HtmlTag.DIV, "pfi_inputDate pfi_input", null);
			ret.append(sbArr[0].append(fieldValue).append(sbArr[1]));
		}
		return ret;
	}
	
	protected static FormInputInterface raw_html = (node) -> {
		StringBuilder sb = new StringBuilder();
		sb.append(node.getString(JsonKeys.HTML_INJECTION));
		return sb;
	};
	
	protected static Map<String, FormInputInterface> defaultInputTemplates() {
		Map<String, FormInputInterface> defaultTemplates = new CaseInsensitiveHashMap<String, FormInputInterface>();
		
		// Wildcard fallback
		defaultTemplates.put("*", FormInputTemplates.div);
		
		// Standard divs
		defaultTemplates.put(JsonKeys.DIV, FormInputTemplates.div);
		defaultTemplates.put(JsonKeys.TITLE, FormInputTemplates.header);
		defaultTemplates.put(JsonKeys.DROPDOWN, FormInputTemplates.select);
		defaultTemplates.put(JsonKeys.TEXT, FormInputTemplates.input_text);
		defaultTemplates.put(JsonKeys.TEXTAREA, FormInputTemplates.input_textarea);
		defaultTemplates.put(JsonKeys.HTML_INJECTION, FormInputTemplates.raw_html);
		defaultTemplates.put(JsonKeys.DROPDOWN_WITHOTHERS, FormInputTemplates.dropdownWithOthers);
		defaultTemplates.put(JsonKeys.CHECKBOX, FormInputTemplates.checkbox);
		defaultTemplates.put("table", FormInputTemplates.table);
		defaultTemplates.put("verticalTable", FormInputTemplates.verticalTable);
		defaultTemplates.put("image", FormInputTemplates.image);
		defaultTemplates.put("signature", FormInputTemplates.signature);
		defaultTemplates.put("date", FormInputTemplates.datePicker);
		defaultTemplates.put("number", FormInputTemplates.input_number);
		
		return defaultTemplates;
	}
	
	//////////////////////
	//
	//	Helper Functions
	//
	//////////////////////
	
	@SuppressWarnings("unchecked")
	private static List<String> dropdownNameList(Object dropDownObject) {
		List<String> nameList = null;
		if (dropDownObject instanceof List) {
			nameList = ListValueConv.objectToString((List<Object>) dropDownObject);
		} else if (dropDownObject instanceof Map) {
			nameList = new ArrayList<String>();
			
			Map<Object, Object> dropDownMap = (Map<Object, Object>) dropDownObject;
			for (Object keyObj : dropDownMap.keySet()) {
				String name = GenericConvert.toString(dropDownMap.get(keyObj), null);
				
				// Skip blank values
				if (name == null || name.length() <= 0) {
					continue;
				}
				
				//insert name
				nameList.add(name);
			}
		}
		
		return nameList;
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> dropdownKeyList(Object dropDownObject) {
		List<String> nameList = dropdownNameList(dropDownObject);
		List<String> keyList = new ArrayList<String>();
		
		if (dropDownObject instanceof List) {
			for (int a = 0; a < nameList.size(); ++a) {
				keyList.add(RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(nameList.get(a)).toLowerCase());
			}
		} else if (dropDownObject instanceof Map) {
			Map<Object, Object> dropDownMap = (Map<Object, Object>) dropDownObject;
			for (Object keyObj : dropDownMap.keySet()) {
				String key = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(
					GenericConvert.toString(keyObj, null)).toLowerCase();
				
				// Skip blank keys
				if (key == null || key.length() <= 0) {
					continue;
				}
				
				// Insert key
				keyList.add(key);
			}
		}
		
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> optionsKeyNamePair(Object optionsObject) {
		Map<String, String> ret = new LinkedHashMap<String, String>();
		
		if (optionsObject instanceof List) {
			List<String> nameList = ListValueConv.objectToString((List<Object>) optionsObject);
			for (String name : nameList) {
				String sanitisedName = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(name).toLowerCase();
				sanitisedName = RegexUtils.removeAllWhiteSpace(sanitisedName);
				ret.put(sanitisedName, name);
			}
		} else if (optionsObject instanceof Map) {
			Map<String, Object> optionsMap = (Map<String, Object>) optionsObject;
			for (String key : optionsMap.keySet()) {
				String sanitisedKey = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(key).toLowerCase();
				sanitisedKey = RegexUtils.removeAllWhiteSpace(sanitisedKey);
				ret.put(sanitisedKey, (String) optionsMap.get(key));
			}
			
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Map<String, String>> getTableChildrenData(FormNode node) {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		
		if (node.containsKey("children")) {
			List<Object> childrenList = (List<Object>) node.get("children");
			for (Object obj : childrenList) {
				if (obj instanceof Map) {
					ret.add((Map<String, String>) obj);
				}
			}
		} else {
			return null;
		}
		
		return ret;
	}
	
	private static void createDropdownHTMLString(StringBuilder sb, List<String> keyList, List<String> nameList,
		String selectedKey) {
		for (int a = 0; a < keyList.size(); ++a) {
			
			String key = keyList.get(a);
			String nme = nameList.get(a);
			
			sb.append("<" + HtmlTag.OPTION + " " + HtmlTag.VALUE + "=\"" + key + "\"");
			
			// Value is selected
			if (selectedKey != null && selectedKey.equalsIgnoreCase(key)) {
				sb.append(" " + HtmlTag.SELECTED + "=\"" + HtmlTag.SELECTED + "\"");
			}
			
			sb.append(">" + nme + "</" + HtmlTag.OPTION + ">");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> getTableFields(List<Object> children) {
		List<String> ret = new ArrayList<String>();
		for (Object childRaw : children) {
			if (childRaw instanceof Map) {
				Map<String, Object> childMap = (Map<String, Object>) childRaw;
				if (childMap.containsKey("field")) {
					ret.add(RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators((String) childMap.get("field"))
						.toLowerCase());
				}
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Object> getTableHeaders(FormNode node) {
		if (node.containsKey("headers")) {
			Object tableHeadersRaw = node.get("headers");
			
			if (!(tableHeadersRaw instanceof List)) {
				throw new IllegalArgumentException("'tableHeader' parameter found in defination was not a List: "
					+ tableHeadersRaw);
			}
			
			return (List<Object>) tableHeadersRaw;
		} else {
			return null;
		}
	}
	
	protected static String getDropDownOthersJavascriptFunction(FormNode node) {
		String dropDownField = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(node.getString(JsonKeys.FIELD))
			.toLowerCase();
		String inputField = node.getString(JsonKeys.DROPDOWN_WITHOTHERS_TEXTFIELD);
		String othersOptionToShowTextField = RegexUtils.removeAllNonAlphaNumeric_allowCommonSeparators(
			node.getString(JsonKeys.OTHERS_OPTION)).toLowerCase();
		String funcName = node.getString(JsonKeys.FUNCTION_NAME, "OnChangeDefaultFuncName");
		
		String injectedScript = "<script>" + "function " + funcName + "() {"
			+ "var dropDown = document.getElementById(\"" + dropDownField + "\");"
			+ "var inputField = document.getElementById(\"" + inputField + "\");" + "if(dropDown.value == \""
			+ othersOptionToShowTextField + "\"){" + //replace Others with val
			"inputField.style.display = \"inline\";" + //replace element by id
			"}else{" + "inputField.style.display = \"none\";" + //replace element by id
			"}" + "};" + "</script>";
		
		return injectedScript;
	}
}
