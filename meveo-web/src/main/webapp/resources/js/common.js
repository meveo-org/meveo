function delayDisable(button) {
	console.log("delayDisable");
	setTimeout(function() {
		button.disabled = true;
	}, 100);
}

function handleUpdateWithMessageComplete(formId, xhr, status, args) {
	console.log("handleUpdateWithMessageComplete with id=" + formId);
	var result = args.result;
	if (PF != null && typeof (result) != "undefined" && !result) {
		PF(formId).show();
	}
}