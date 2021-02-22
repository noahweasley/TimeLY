package com.projects.timely.error;

 class ErrorMessage {
    private String dialogMessage;
    private boolean showSuggestions;
    private String suggestion1;
    private String suggestion2;
    private int suggestionCount;

    @SuppressWarnings("unused")
    public ErrorMessage(String dialogMessage, boolean showSuggestions, String suggestion1,
                        String suggestion2, int suggestionCount) {
        this.dialogMessage = dialogMessage;
        this.showSuggestions = showSuggestions;
        this.suggestion1 = suggestion1;
        this.suggestion2 = suggestion2;
        this.suggestionCount = suggestionCount;
    }

    public ErrorMessage(){

    }

    public  String getDialogMessage() {
        return dialogMessage;
    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    public  boolean isShowSuggestions() {
        return showSuggestions;
    }

    public void setShowSuggestions(boolean showSuggestions) {
        this.showSuggestions = showSuggestions;
    }

    public  String getSuggestion1() {
        return suggestion1;
    }

    public void setSuggestion1(String suggestion1) {
        this.suggestion1 = suggestion1;
    }

    public String getSuggestion2() {
        return suggestion2;
    }

    public  void setSuggestion2(String suggestion2) {
        this.suggestion2 = suggestion2;
    }

    public int getSuggestionCount() {
        return suggestionCount;
    }

    public void setSuggestionCount(int suggestionCount) {
        this.suggestionCount = suggestionCount;
    }


}
