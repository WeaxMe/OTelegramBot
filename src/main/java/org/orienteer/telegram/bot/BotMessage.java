package org.orienteer.telegram.bot;

/**
 * @author  Vitaliy Gonchar
 */
interface BotMessage {
    String MAIN_MENU_MSG                  = "Change options and I will try to find it.";
    String MAIN_MENU_ERROR_MSG            = "Don't send me word! \nPlease change option and send me word later...";
    String NEW_GLOBAL_SEARCH_BUT          = "New global search";
    String NEW_CLASS_SEARCH_BUT           = "New class search";
    String BACK                           = "Back";
    String CLASS_SEARCH_MSG               = "Send me word and I will try to find it in %s";
    String START_SEARCH_MSG               = "Start search...";
    String CLASS_MENU_MSG                 = "Choose class in the list.";
    String ERROR_MSG                      = "I don't understand you :(";
    String SEARCH_MSG                     = "Send me word and I will try to find it in database.";
    String SEARCH_RESULT_SUCCESS_MSG      = "To get information click on link.";
    String SEARCH_RESULT_FAILED_MSG       = "I cannot found something!";
    String CLASS_DESCRIPTION_MSG          = "Class description: ";
    String CLASS_DOCUMENTS                = "Class documents: ";
    String SEARCH_FAILED_CLASS_BY_NAME    = "Cannot found class by this class name.";
    String SHORT_DOCUMENT_DESCRIPTION_MSG = "Short document description: ";
    String FAILED_DOCUMENT_BY_RID         = "Cannot found document by this id.";
    String DOCUMENT_DETAILS_MSG           = "Details of document: ";
    String CLASS_BUT                      = "Class: ";
    String HTML_STRONG_TEXT               = "<strong>%s</strong>";
    String SEARCH_FIELD_NAMES_RESULT      = "In field names: ";
    String SEARCH_FIELD_VALUES_RESULT     = "In field values: ";
    String SEARCH_DOCUMENT_NAMES_RESULT   = "In document names: ";
    String SEARCH_CLASS_NAMES_RESULT      = "In class names: ";
    String NEXT_RESULT_BUT                = "Next result of search";
    String PREVIOUS_RESULT_BUT            = "Previous result of search";
    String ABOUT_MSG                      = "OTelegramBot is a Telegram bot for platform Orienteer.\n" +
                                            "This bot can search information from database.\n" +
                                            "For start using bot: /start\n" +
                                            "Author: Vitaliy Gonchar";
}
