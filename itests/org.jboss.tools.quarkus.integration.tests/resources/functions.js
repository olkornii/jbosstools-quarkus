function getWizards(){
	if ($( "#home" ).hasClass("hidden")){
		return null;
	}
	return getEachInnerText($( "#wizards" ).find('a'));
}

function clickWizard(name){
	$( "#wizards" ).find('a').filter(":contains('"+name+"')").click();
}

//Internal functions from now on. Do not call them from Java tests.

function getEachInnerText(topElement){
	var resultString="";
	var prefix = "";
	topElement.each(function(index, element){
		resultString += prefix+element.textContent;
		prefix=";";
	});
	return resultString;
}