package org.orienteer.telegram.bot.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.http.util.Args;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.OTelegramCache;
import org.orienteer.telegram.module.OTelegramModule;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * Implements {@link IOTelegramDescription} for represents {@link OClass} description in Telegram
 */
public class OClassTelegramDescription implements IOTelegramDescription<Map<Integer, String>> {
    private final String className;
    private final OTelegramBot bot;

    /**
     * Constructor
     * @param classLink {@link String} which contains Telegram link to {@link OClass}
     * @param bot {@link OTelegramBot} bot which need description
     */
    public OClassTelegramDescription(String classLink, OTelegramBot bot) {
        this.bot = Args.notNull(bot, "bot");
        className = classLink.substring(BotState.GO_TO_CLASS.getCommand().length());
    }

    /**
     * Get description of class as map
     * @return {@link Map<Integer, String>} which contains description
     */
    @Override
    public Map<Integer, String> getDescription() {
        return new DBClosure<Map<Integer, String>>() {
            @Override
            protected Map<Integer, String> execute(ODatabaseDocument db) {
                Map<Integer, String> result = Maps.newHashMap();
                if (OTelegramCache.getClassCache().containsKey(className)) {
                    OClass oClass = OTelegramCache.getClassCache().get(className);
                    List<String> telegramDocs = getTelegramDocuments(oClass, db);
                    String head = createOClassHeadDescription(oClass, !telegramDocs.isEmpty());
                    if (!telegramDocs.isEmpty()) {
                        result.put(0, head + Markdown.BOLD.toString("1. ") + telegramDocs.get(0) + "\n");
                        for (int i = 1; i < telegramDocs.size(); i++) {
                            if (i % 10 == 0) {
                                result.put(i, head + Markdown.BOLD.toString((i + 1) + ". ") + telegramDocs.get(i) + "\n");
                            } else {
                                result.put(i, Markdown.BOLD.toString((i + 1) + ". ") + telegramDocs.get(i) + "\n");
                            }
                        }
                    } else result.put(0, head);
                } else {
                    result = new HashMap<>();
                    result.put(0, MessageKey.SEARCH_FAILED_CLASS_BY_NAME.toLocaleString(bot));
                }
                return result;
            }
        }.execute();
    }

    private String createOClassHeadDescription(OClass oClass, boolean docsPresent) {
        StringBuilder sb = new StringBuilder();
        sb.append(Markdown.BOLD.toString(MessageKey.CLASS_DESCRIPTION_MSG.toLocaleString(bot))).append("\n\n")
                .append(Markdown.BOLD.toString(MessageKey.NAME.toLocaleString(bot))).append(" ")
                .append(oClass.getName()).append("\n")
                .append(Markdown.BOLD.toString(MessageKey.SUPER_CLASSES.toLocaleString(bot))).append(" ");
        appendSuperClasses(sb, oClass, OTelegramCache.getClassCache());
        appendProperties(sb, oClass);
        if (docsPresent) sb.append(Markdown.BOLD.toString(MessageKey.CLASS_DOCUMENTS.toLocaleString(bot))).append("\n");
        else sb.append(Markdown.BOLD.toString(MessageKey.WITHOUT_DOCUMENTS.toLocaleString(bot)));
        return sb.toString();
    }

    private void appendSuperClasses(StringBuilder sb, OClass oClass, Map<String, OClass> classCache) {
        if (!oClass.getSuperClasses().isEmpty()) {
            for (OClass superClass : oClass.getSuperClasses()) {
                if (classCache.containsKey(superClass.getName())) {
                    sb.append("/").append(superClass.getName()).append(" ");
                }
            }
        } else sb.append(MessageKey.WITHOUT_SUPER_CLASSES.toLocaleString(bot));
        sb.append("\n");
    }

    private void appendProperties(StringBuilder sb, OClass oClass) {
        List<String> properties = getTelegramProperties(oClass);
        if (!properties.isEmpty()) {
            for (String property : properties) {
                sb.append(property)
                        .append("\n");
            }
        } else sb.append(MessageKey.WITHOUT_PROPERTIES.toLocaleString(bot));
        sb.append("\n");
    }

    private List<String> getTelegramProperties(OClass oClass) {
        List<String> result = Lists.newArrayList();
        if (OTelegramModule.TELEGRAM_BOT_CLASS_DESCRIPTION.getValue(oClass)) {
            Collection<OProperty> properties = oClass.properties();
            for (OProperty property : properties) {
                result.add(Markdown.BOLD.toString(property.getName() + ":") + " "
                        + property.getDefaultValue() + " (" + MessageKey.DEFAULT_VALUE.toLocaleString(bot) + ")");
            }
            Collections.sort(result);
        }
        return result;
    }

    private List<String> getTelegramDocuments(OClass oClass, ODatabaseDocument db) {
        List<String> result = Lists.newArrayList();
        if (OTelegramModule.TELEGRAM_BOT_DOCUMENTS_LIST.getValue(oClass)) {
            ORecordIteratorClass<ODocument> docs = db.browseClass(oClass.getName());
            for (ODocument oDocument : docs) {
                String docId = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                        + "\\_" + oDocument.getIdentity().getClusterId()
                        + "\\_" + oDocument.getIdentity().getClusterPosition();
                String docName = OTelegramUtil.getDocumentName(oDocument, bot);
                result.add(docName + " " + docId);
            }
            Collections.sort(result);
        }
        return result;
    }
}
