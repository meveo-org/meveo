var runJsonata = function (inputData, expressionInput) {
    //the inputData is the JSON File and the expressionInput is the jsonata expression.
    if (expressionInput != null) {
        var functionResult = "";
        inputData = JSON.parse(inputData);
        //set the data to valid format.

        var expression = jsonata(expressionInput);
        functionResult = expression.evaluate(inputData);
        return JSON.stringify(functionResult, null, 2);
    }
};