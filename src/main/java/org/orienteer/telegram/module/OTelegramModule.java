package org.orienteer.telegram.module;

import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.orienteer.telegram.bot.OTelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramModule extends AbstractOrienteerModule{

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramModule.class);
	public static final String NAME = "telegram";
	public static final String OCLASS_NAME = "OTelegramBot";
	public static final String OPROPERTY_USERNAME = "username";
	public static final String OPROPERTY_TOKEN = "token";
	public static final String OPROPERTY_USER_SESSION = "user_session";

	protected OTelegramModule() {
		super(NAME, 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OCLASS_NAME, OMODULE_CLASS)
				.oProperty(OPROPERTY_USERNAME, OType.STRING).notNull()
				.oProperty(OPROPERTY_TOKEN, OType.STRING).notNull()
				.oProperty(OPROPERTY_USER_SESSION, OType.LONG).defaultValue("30").notNull();

		return new ODocument(helper.getOClass());
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		BotConfig botConfig = readBotConfig(db);
		LOG.debug(botConfig.toString());
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		BotLogger.setLevel(Level.WARNING);
		BotLogger.registerLogger(new ConsoleHandler());
		try {
			LOG.debug("Register bot " + botConfig.USERNAME);
			telegramBotsApi.registerBot(OTelegramBot.getOrienteerTelegramBot(botConfig));
		} catch (TelegramApiRequestException e) {
			LOG.error("Cannot register bot");
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
	}


	protected BotConfig readBotConfig(ODatabaseDocument db) {
		ORecordIteratorClass<ODocument> oTelegramBots = db.browseClass(OCLASS_NAME);
		String username = null;
		String token = null;
		long userSession = 0;
		if (oTelegramBots.hasNext()) {
			ODocument bot = oTelegramBots.next();
			if (bot.field(OMODULE_ACTIVATE)) {
				username = bot.field(OPROPERTY_USERNAME, OType.STRING);
				token = bot.field(OPROPERTY_TOKEN, OType.STRING);
				userSession = bot.field(OPROPERTY_USER_SESSION, OType.LONG);
			}
		}
		LOG.info("Bot USERNAME: " + username);
		LOG.info("Bot token: " + token);
		LOG.info("User session: " + userSession);
		return new BotConfig(username, token, userSession);
    }

    public class BotConfig {
        public final String USERNAME;
        public final String TOKEN;
		public final long USER_SESSION;

        BotConfig(String username, String token, long userSession) {
            USERNAME = username;
            TOKEN = token;
			USER_SESSION = userSession;
        }

		@Override
		public String toString() {
			return "BotConfig:"
					+ "\nUsername: " + USERNAME
					+ "\nBot token: " + TOKEN
					+ "\nUser session: " + USER_SESSION;
		}
	}
}