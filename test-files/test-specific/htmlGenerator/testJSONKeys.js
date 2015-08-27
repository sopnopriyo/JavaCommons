[	
	{
		"type": "title",
		"label": "This is the label for a title object",
		"text":"This is the actual input of a title object",
		"inputCss":"color:red; font-size:25;"
	},
	{
		"type":"dropdown",
		"options":["Dropdown Option 1", "Dropdown Option 2", "Dropdown Option 3"],
		"field":"exampleListDropdown",
		"label":"Example Dropdown using List"
	},
	{
		"type":"dropdown",
		"options":{
			"varForOption1":"Option 1",
			"varForOption2":"Option 2",
			"varForOption3":"Option 3"
		},
		"field":"exampleMapDropdown",
		"label":"Example Dropdown using LinkedHashMap"
	},
	{
		"type":"text",
		"field":"exampleTextInputField",
		"label":"This is a text input field"
	},
	{
		"type":"dropdownWithOthers",
		"field":"exampleDropdownWithHiddenField",
		"functionName":"exampleDropdownOnChange",
		"label":"Dropdown with hidden text input field",
		"options":["Hidden Field 1", "Hidden Field 2", "Shown Field 3"],
		"othersOption":"Shown Field 3",
		"textField":"exampleDropdownTextField"
	}
]