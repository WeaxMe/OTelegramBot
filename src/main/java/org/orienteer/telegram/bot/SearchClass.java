package org.orienteer.telegram.bot;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Vitaliy Gonchar
 */
class SearchClass {
//    private boolean globalSearch;
    private boolean globalClassSearch;
    private boolean globalClassNamesSearch;

    private final int N = 10;   // max result in one message
    private final String searchWord;
    private final String className;

    private final Map<String, OClass> CLASS_CACHE = Cache.getClassCache();
    private final Map<String, String> QUERY_CACHE = Cache.getQueryCache();

    private final BotMessage botMessage;

    public SearchClass(String searchWord, BotMessage botMessage) {
        this.searchWord = searchWord;
        this.className = null;
        this.botMessage = botMessage;
    }

    public SearchClass(String searchWord, String className, BotMessage botMessage) {
        this.searchWord = searchWord;
        this.className = CLASS_CACHE.containsKey(className) ? className : null;
        this.botMessage = botMessage;
    }

    public List<String> getResultOfSearch() {
        List<String> result = null;
//        if (globalSearch) {
//            result = getResultOfGlobalSearch();
//        } else
        if (globalClassSearch) {
            result = className != null ? getResultOfSearchInClass() : Arrays.asList(botMessage.ERROR_MSG);
        } else if (globalClassNamesSearch) {
            result = getResultListOfSearch(null, null, searchInClassNames());
        }
        return result;
    }

    private List<String> getResultListOfSearch(List<String> values,
                                               List<String> docs, List<String> classes) {
        List<String> resultList = new ArrayList<>();
        if (values == null) values = new ArrayList<>();
        if (docs == null) docs = new ArrayList<>();
        if (classes == null) classes = new ArrayList<>();

        if (values.size() > 0 || docs.size() > 0 || classes.size() > 0) {
            int counter = 0;
            if (classes.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_CLASS_NAMES_RESULT) + "\n";
                resultList.addAll(splitBigResult(classes, info, counter));
            }
            if (docs.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_DOCUMENT_NAMES_RESULT) + "\n";
                resultList.addAll(splitBigResult(docs, info, counter));
            }
            if (values.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_FIELD_VALUES_RESULT) + "\n";
                resultList.addAll(splitBigResult(values, info, counter));
            }
        } else resultList.add(String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_RESULT_FAILED_MSG));
        return resultList;
    }


//    private List<String> getResultOfGlobalSearch() {
//        return (List<String>) new DBClosure() {
//            @Override
//            protected Object execute(ODatabaseDocument db) {
//                List<String> fieldValuesList = new ArrayList<>();
//                List<String> documentNamesList = new ArrayList<>();
//                List<String> classesNamesList = new ArrayList<>();
//                classesNamesList.addAll(searchInClassNames());
//                for (OClass oClass : CLASS_CACHE.values()) {
//                    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(QUERY_CACHE.get(oClass.getName()));
//                    Iterable<ODocument> oDocuments = (Iterable<ODocument>) (query.getText().contains("?") ? db.query(query, searchWord): db.query(query));
//
//                    for (ODocument oDocument : oDocuments) {
//                        List<String> result = searchInFieldValues(oDocument);
//                        if (result != null) fieldValuesList.addAll(result);
//                        String doc = searchDocument(oDocument);
//                        if (doc != null) documentNamesList.add(doc);
//                    }
//                }
//                return getResultListOfSearch(fieldValuesList, documentNamesList, classesNamesList);
//            }
//        }.execute();
//    }


    private List<String> splitBigResult(List<String> bigResult, String info, int counter) {
        List<String> resultList = new ArrayList<>();
        String head = String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_RESULT_SUCCESS_MSG);
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        builder.append(info);
        for (String string : bigResult) {
            builder.append(string);
            counter++;
            if (counter % N == 0) {
                resultList.add(builder.toString());
                builder = new StringBuilder();
                builder.append(head);
                builder.append(info);
            }
        }
        if (counter % N != 0) resultList.add(builder.toString());
        return resultList;
    }

    /**
     * getResultOfSearch similar class names with word
     * @return result list of getResultOfSearch
     */
    private List<String> searchInClassNames() {
        List<String> resultList = new ArrayList<>();
        String searchClass;
        for (OClass oClass : CLASS_CACHE.values()) {
            if (isWordInLine(searchWord, oClass.getName())) {
                searchClass = "•  class name: " + oClass.getName() + " "
                        + BotState.GO_TO_CLASS.command + oClass.getName() + "\n";
                resultList.add(searchClass);
            }
        }
        return resultList;
    }


    /**
     * Build string with result of searchAll
     * @param oDocument getResultOfSearch document
     * @return string with result of searchAll
     */
    private String searchDocument(ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) return null;
        String documentLink = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        if (isWordInLine(searchWord, docName)) {
            builder.append("• ");
            builder.append(docName);
            builder.append(" ");
            builder.append(documentLink);
            builder.append("\n");
        }
       return builder.length() > 0 ? builder.toString() : null;
    }

    /**
     * getResultOfSearch similar words in field values
     * @param oDocument document where is getResultOfSearch
     * @return list of strings with result of getResultOfSearch
     */
    private List<String> searchInFieldValues(ODocument oDocument) {
        String searchValue;
        List<String> resultList = new ArrayList<>();
        String[] fieldNames = oDocument.fieldNames();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = "without document name";
        String documentLink = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        for (String name : fieldNames) {
            String fieldValue = oDocument.field(name, OType.STRING);
            if (name != null && fieldValue != null && isWordInLine(searchWord, fieldValue)) {
                searchValue = "• " + name + " : " + fieldValue + " " + documentLink + "\n";
                resultList.add(searchValue);
            }
        }
        return resultList.size() > 0 ? resultList : null;
    }


    /**
     * getResultOfSearch similar words with word in class
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchInClass() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                List<String> fieldValuesList = new ArrayList<>();
                List<String> docNamesList = new ArrayList<>();
                OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(QUERY_CACHE.get(className));
                Iterable<ODocument> oDocuments = (Iterable<ODocument>) (query.getText().contains("?") ? db.query(query, searchWord): db.query(query));

                for (ODocument oDocument : oDocuments) {
                    List<String> result = searchInFieldValues(oDocument);
                    if (result != null) fieldValuesList.addAll(result);
                    String doc = searchDocument(oDocument);
                    if (doc != null) docNamesList.add(doc);
                }
                return  getResultListOfSearch(fieldValuesList, docNamesList, null);
            }
        }.execute();
    }

    /**
     * getResultOfSearch word in line
     * @param word getResultOfSearch word
     * @param line string where word can be
     * @return true if word is in line
     */
    private boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }

    public void setGlobalClassSearch(boolean globalClassSearch) {
        this.globalClassSearch = globalClassSearch;
    }

//    public void setGlobalSearch(boolean globalSearch) {
//        this.globalSearch = globalSearch;
//    }

    public void setGlobalClassNamesSearch(boolean globalClassNamesSearch) {
        this.globalClassNamesSearch = globalClassNamesSearch;
    }

}