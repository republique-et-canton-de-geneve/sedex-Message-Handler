/*
 * $Id: ClientConfigurationFactory.java 340 2015-08-16 14:51:19Z sasha $
 *
 * Copyright (C) 2006-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package ch.admin.suis.msghandler.config;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ch.admin.suis.msghandler.checker.StatusCheckerConfiguration;
import ch.admin.suis.msghandler.common.ClientCommons;
import ch.admin.suis.msghandler.common.LocalRecipient;
import ch.admin.suis.msghandler.common.MessageType;
import ch.admin.suis.msghandler.common.ReceiptsFolder;
import ch.admin.suis.msghandler.log.LogServiceConfiguration;
import ch.admin.suis.msghandler.log.ProtocolWriter;
import ch.admin.suis.msghandler.naming.ScriptedNamingService;
import ch.admin.suis.msghandler.receiver.ReceiverConfiguration;
import ch.admin.suis.msghandler.sedex.SedexAdapterConfiguration;
import ch.admin.suis.msghandler.sender.SenderConfiguration;
import ch.admin.suis.msghandler.servlet.CommandInterfaceConfiguration;
import ch.admin.suis.msghandler.util.FileUtils;
import ch.glue.fileencryptor.CipherFactory;
import ch.glue.fileencryptor.Decryptor;
import ch.glue.fileencryptor.DecryptorConfiguration;
import ch.glue.fileencryptor.KeyStoreFactory;
import ch.glue.fileencryptor.PrivateKeyFactory;

/**
 * The
 * <code>ClientConfigurationFactory</code> stores the configuration parameter into the configuration object. The
 * subclasses extending this class must create their own instances of configuration and pass them to the
 * <code>store</code> method.
 *
 * @author Alexander Nikiforov
 * @author $Author: sasha $
 * @version $Revision: 340 $
 */
public class ClientConfigurationFactory {

	/**
	 * logger
	 */
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ClientConfigurationFactory.class
			.getName());

	private final XMLConfiguration xmlConfig;

	private static final String MSG_TYPES = "[@msgTypes]";
	private static final String DIR_PATH = ".[@dirPath]";

	private final ClientConfiguration clientConfiguration = new ClientConfiguration();

	/**
	 * Used for Configuration Check. No doublication entries allowed
	 */
	private final Set<File> checkSigningOutboxDirSet = new HashSet<>();

	/**
	 * Used for Configuration Check. No doublication entries allowed
	 */
	private final Set<File> checkSigningProcessedDirSet = new HashSet<>();

	private final Map<String, Set<Integer>> checkSedexIdMsgType = new HashMap<>();

	/**
	 * Constructor. Do not forget to call the "init()" method!!!
	 *
	 * @param configPath xml configuration file
	 * @throws ConfigurationException Config Problems
	 */
	public ClientConfigurationFactory(String configPath) throws ConfigurationException {
		FileUtils.isFile(configPath, "config path");

		xmlConfig = new XMLConfiguration();
		// enable the schema validation
		xmlConfig.setSchemaValidation(true);

    /*
	 * Start Mantis 0006347
     * Problem: Wenn im config.xml ein \\ vorhanden war wurde von der xmlConfig nur ein \ zur�ckgeliefert. Mit
     * xmlConfig.setDelimiterParsingDisabled(true); wird dies unterdr�ckt und wir sollten wie gewohnt zwei "\" erhalten.
     *
     * Siehe auch:
     * https://extranet.glue.ch/mantis/view.php?id=6347
     * http://mail-archives.apache.org/mod_mbox/commons-issues/201003.mbox/%3C91316976.200621268302587552.JavaMail.jira@brutus.apache.org%3E
     * https://issues.apache.org/jira/browse/CONFIGURATION-411
     */
		xmlConfig.setDelimiterParsingDisabled(true);
		//Das Problem besteht weiterhin bei XML Attributen. xmlConfig.setAttributeSplittingDisabled(true) bietet hier abhilfe.
		xmlConfig.setAttributeSplittingDisabled(true);
		//End Mantis 0006347

		xmlConfig.setFileName(configPath);
		try {
			xmlConfig.load();
		} catch (ConfigurationException ex) {
			LOG.error("XML File: " + configPath + " could not be loaded. It seems the XML file is not valid");
			throw ex;
		}
	}

	/**
	 * for unit test only!
	 *
	 * @return The XML config.
	 */
	XMLConfiguration getXmlConfig() {
		return xmlConfig;
	}

	/**
	 * Initialize the factory with a XML file located at the given path. This is a relative path to a location somewhere
	 * in the classpath.
	 * Has to be called!
	 */
	@SuppressWarnings("unchecked")
	public void init() throws ConfigurationException {
	        // set the unlimited policy directly. Siehe https://golb.hplar.ch/p/JCE-policy-changes-in-Java-SE-8u151-and-8u152
	        Security.setProperty("crypto.policy", "unlimited");
	        
		// load the BouncyCastle provider
		Security.addProvider(new BouncyCastleProvider());

		checkSigningOutboxDirSet.clear(); // clear set...
		checkSigningProcessedDirSet.clear();

		clientConfiguration.setSedexAdapterConfiguration(createSedexAdapterConfig(xmlConfig));
		LOG.info("Sedex adapter configuration added, " + clientConfiguration.getSedexAdapterConfiguration());

		final String baseDir = createBaseDir(xmlConfig);

		clientConfiguration.setWorkingDir(createWorkingDir(xmlConfig));
		
		// SEDEX-175 - cleans the working dir up.
		
		cleanUpWorkingDir(clientConfiguration.getWorkingDir());

		// Sets the outbox limit
		long secondsControllerBeforeSendingStuff = xmlConfig.getLong("messageHandler.minimumFileAge[@waitFor]", 0);
		if (secondsControllerBeforeSendingStuff == 0) {
			LOG.warn("No delayer has been set with the key messageHandler.minimumFileAge.waitFor, which means every file " +
					"will be sent ASAP instead of waiting for a bit ! This is usually a bad idea for big files as " +
					"they tend to be slow...");
		}

		Outbox.secondsBeforeSending = secondsControllerBeforeSendingStuff;

		//Sets the inbox limit
		long maximumInboxFiles = xmlConfig.getLong("messageHandler.maximumIncomingMessages[@accept]", Long.MAX_VALUE);
		if (maximumInboxFiles == 0) {
			LOG.error("MessageHandler is configured to accept a maximum of 0 documents in each inbox. This is unacceptable.");
			throw new ConfigurationException("Cannot put 0 as a throttle for inboxes.");
		}
		Inbox.incomingMessageLimit = maximumInboxFiles;

		// **************** receiver-specific settings
		ReceiverConfiguration receiverConfiguration = setupReceiver();

		// **************** checker-specific settings
		StatusCheckerConfiguration statusCheckerConfiguration = setupChecker();

		final String defaultSenderCronValue = xmlConfig.getString("messageHandler.defaultOutboxCheck[@cron]");
		if (StringUtils.isBlank(defaultSenderCronValue)) {
			throw new ConfigurationException("Missing attribute: messageHandler.defaultOutboxCheck[@cron]");
		}
		// create default sender configuration - with the default cron
		final SenderConfiguration defaultSenderConfiguration = new SenderConfiguration(defaultSenderCronValue);

		setupNativeApps(defaultSenderConfiguration, baseDir, receiverConfiguration);

		if (!defaultSenderConfiguration.getOutboxes().isEmpty()) {
			// if the default config contains at least one outbox, add it to the client config
			clientConfiguration.addSenderConfiguration(defaultSenderConfiguration);
			// MANTIS 5023
			LOG.info("sender added, " + defaultSenderConfiguration);
		}
		// Setting up transparent apps...
		setupTransparentApps(defaultSenderCronValue, baseDir, receiverConfiguration, statusCheckerConfiguration);
		// Launching jetty...
		setupHttpServer();
	}

	private ReceiverConfiguration setupReceiver() throws ConfigurationException {
		// **************** receiver-specific settings
		ReceiverConfiguration receiverConfiguration = new ReceiverConfiguration();
		final String sedexInboxCron = xmlConfig.getString("messageHandler.sedexInboxDirCheck[@cron]");
		if (StringUtils.isBlank(sedexInboxCron)) {
			throw new ConfigurationException("Missing attribute: messageHandler.sedexInboxDirCheck[@cron]");
		}
		receiverConfiguration.setCron(sedexInboxCron);
		return receiverConfiguration;
	}

	private StatusCheckerConfiguration setupChecker() throws ConfigurationException {
		// **************** checker-specific settings
		StatusCheckerConfiguration statusCheckerConfiguration = new StatusCheckerConfiguration();
		final String sedexReceiptCron = xmlConfig.getString("messageHandler.sedexReceiptDirCheck[@cron]");
		if (StringUtils.isBlank(sedexReceiptCron)) {
			throw new ConfigurationException("Missing attribute: messageHandler.sedexReceiptDirCheck[@cron]");
		}
		statusCheckerConfiguration.setCron(sedexReceiptCron);
		return statusCheckerConfiguration;
	}

	private void setupNativeApps(SenderConfiguration defaultSenderConfiguration, String baseDir,
								 ReceiverConfiguration receiverConfiguration)
			throws ConfigurationException {

		// ************** the native applications
		LOG.info("Configure the native MH applications...");
		final List natives = xmlConfig.configurationsAt("nativeApp");

		// We could use a foreach, but that would involve using objects. I don't like that.
		for (Iterator i = natives.iterator(); i.hasNext(); ) {
			HierarchicalConfiguration sub = (HierarchicalConfiguration) i.next();

			// the sedex ID of this application
			final String sedexId = sub.getString(".[@participantId]");

			// ************** outboxes
			final List outboxes = sub.configurationsAt(".outbox");

			// We could use a foreach, but that would involve using objects. I don't like that.
			for (Iterator j = outboxes.iterator(); j.hasNext(); ) {
				setupNativeOutbox(j, baseDir, sedexId, defaultSenderConfiguration);
			}

			// inboxes
			for (final HierarchicalConfiguration inboxSub : sub.configurationsAt(".inbox")) {
				String inboxDir = inboxSub.getString("[@dirPath]");
				File tmpDir = FileUtils.createPath(baseDir, inboxDir);

				List<HierarchicalConfiguration> decryptorConfigs = inboxSub.configurationsAt(".decrypt");

				final Inbox inbox = decryptorConfigs.isEmpty() ? new NativeAppInbox(tmpDir, sedexId, MessageType.from(inboxSub
						.getString(MSG_TYPES))) :
						new DecryptingInbox(tmpDir, sedexId, MessageType.from(inboxSub.getString(MSG_TYPES)));

				checkSedexIdMsgTypes(inbox);
				receiverConfiguration.addInbox(inbox);

				if (!decryptorConfigs.isEmpty()) {
					clientConfiguration.setDecryptor(new Decryptor(setUpDecryptorConfiguration(decryptorConfigs)));
				}

			}
		}
	}

	private void setupNativeOutbox(Iterator j, String baseDir, String sedexId,
								   SenderConfiguration defaultSenderConfiguration) throws ConfigurationException {

		final HierarchicalConfiguration outboxSub = (HierarchicalConfiguration) j.next();

		final MessageType messageType = new MessageType(outboxSub.getInt(".[@msgType]"));

		String resolverFile = outboxSub.getString(".recipientIdResolver[@filePath]");
		File scriptFile = FileUtils.createPath(baseDir, resolverFile);
		FileUtils.isFile(scriptFile, ".recipientIdResolver[@filePath]");
		final ScriptedNamingService resolver = new ScriptedNamingService(scriptFile, outboxSub.getString(
				".recipientIdResolver[@method]"));

		String outboxDir = outboxSub.getString(DIR_PATH);
		final Outbox outbox = new Outbox(FileUtils.createPath(baseDir, outboxDir), sedexId, messageType, resolver);

		LOG.info("participant ID resolver configured, " + resolver + " for outbox " + outbox.getDirectory());

		final String outboxCron = outboxSub.getString("[@cron]");
		if (null != outboxCron) {
			// the outbox defines its own cron, create a separate sender configuration for this outbox
			// create the sender configuration - with the cron from the outbox
			final SenderConfiguration senderConfiguration = new SenderConfiguration(outboxCron);
			senderConfiguration.addOutbox(outbox);

			// and add this configuration to the client config
			clientConfiguration.addSenderConfiguration(senderConfiguration);
			LOG.info("sender added, " + senderConfiguration);
		} else {
			// add this outbox to the default configuration
			defaultSenderConfiguration.addOutbox(outbox);
		}

		// the signing outboxes
		final List cfgSigningOutboxes = outboxSub.configurationsAt(".signingOutbox");

		setupSiginingOutbox(cfgSigningOutboxes, baseDir, outbox);
	}

	private void setupSiginingOutbox(List cfgSigningOutboxes, String baseDir, Outbox outbox)
			throws ConfigurationException {

		for (Iterator k = cfgSigningOutboxes.iterator(); k.hasNext(); ) {
			HierarchicalConfiguration signingOutboxSub = (HierarchicalConfiguration) k.next();

			// where the original files are
			final String srcDirName = signingOutboxSub.getString(DIR_PATH);
			// where the original files should be moved to
			final String processDir = signingOutboxSub.getString(".[@processedDir]");

			final String batchSignerCfg = signingOutboxSub.getString(".[@signingProfilePath]");

			File fileSigningOutbox = FileUtils.createPath(baseDir, srcDirName);
			FileUtils.isDirectory(fileSigningOutbox, DIR_PATH);

			File fileProcessDir = StringUtils.isEmpty(processDir) ? null : FileUtils.createPath(baseDir, processDir);
			if (fileProcessDir != null) {
				FileUtils.isDirectory(fileProcessDir, ".[@processedDir]");
			}

			File fileBatchSignerCfg = FileUtils.createPath(baseDir, batchSignerCfg);
			FileUtils.isFile(fileBatchSignerCfg, ".[@signingProfilePath]");

			checkSignatureOutbox(fileSigningOutbox, fileProcessDir);

			SigningOutbox signingOutbox = null;
			List certificateConfigs = signingOutboxSub.configurationsAt(".certificate");
			List certificateFileConfigs = signingOutboxSub.configurationsAt(".certificateConfigFile");

			//First check if the signing config is in the MH config.xml file. Such as p12 file and password
			if (certificateConfigs.size() == 1) {
				HierarchicalConfiguration certificateConfigsSub = (HierarchicalConfiguration) certificateConfigs.get(0);
				final String p12FileName = certificateConfigsSub.getString(".[@filePath]");
				File fileP12 = FileUtils.createPath(baseDir, p12FileName);
				FileUtils.isFile(fileP12, "certificate[@filePath]");

				final String p12Password = certificateConfigsSub.getString(".[@password]");
				signingOutbox = new SigningOutboxMHCfg(fileP12, p12Password, fileSigningOutbox,
						fileBatchSignerCfg, fileProcessDir);
				LOG.info("SigningOutbox p12 file: " + fileP12.getAbsolutePath());

			} //Second if not in MH config.xml then try the sedex certificateConfiguration
			else if (certificateFileConfigs.size() == 1) {
				HierarchicalConfiguration certificateConfigsSub = (HierarchicalConfiguration) certificateFileConfigs.get(0);
				final String fileName = certificateConfigsSub.getString(".[@filePath]");
				File sedexCertConfig = FileUtils.createPath(baseDir, fileName);

				signingOutbox = new SigningOutboxSedexCfg(sedexCertConfig, fileSigningOutbox,
						fileBatchSignerCfg, fileProcessDir);
				LOG.info("SigningOutbox sedex certificateConfiguration file: " + sedexCertConfig.getAbsolutePath());
			}

			outbox.addSigningOutbox(signingOutbox); // add it
			LOG.info("signing outbox added to " + outbox.getDirectory() + ": " + signingOutbox);
		}
	}

	private DecryptorConfiguration setUpDecryptorConfiguration(List<HierarchicalConfiguration> decryptorConfigs) {

		// create the private key factory out of the configuration parameters
		final HierarchicalConfiguration decryptorConfig = decryptorConfigs.get(0);
		final String algorithm = decryptorConfig.getString("[@algorithm]");
		final String keystore = decryptorConfig.getString("[@keystore]");
		final String password = decryptorConfig.getString("[@password]");
		final String certificateAlias = decryptorConfig.getString("[@certificateAlias]");

		final PrivateKeyFactory privateKeyFactory = KeyStoreFactory.forPkcs12(new File(keystore), certificateAlias,
				password);

		return new DecryptorConfiguration.Builder()
				.setTargetDirectory(new File(clientConfiguration.getWorkingDir(), ClientCommons.INBOX_TMP_DIR))
				.setPrivateKeyFactory(privateKeyFactory)
				.setCipherFactory(CipherFactory.forAlgorithm(algorithm))
				.build();
	}

	private void setupTransparentApps(String defaultSenderCronValue, String baseDir,
									  ReceiverConfiguration receiverConfiguration,
									  StatusCheckerConfiguration statusCheckerConfiguration)
			throws ConfigurationException {

		// ************** transparent applications
		LOG.info("Configure the transparent applications...");
		final List transparent = xmlConfig.configurationsAt("transparentApp");
		for (Iterator i = transparent.iterator(); i.hasNext(); ) {
			HierarchicalConfiguration sub = (HierarchicalConfiguration) i.next();

			// the sedex ID of this application
			final String sedexId = sub.getString(".[@participantId]");

			final String outboxDir = sub.getString("outbox[@dirPath]");
			// the outbox
			if (null != outboxDir) {
				final String cronValue = sub.getString("outbox[@cron]");

				// the outbox exists
				final SenderConfiguration senderConfiguration = new SenderConfiguration(
						StringUtils.defaultIfEmpty(cronValue, defaultSenderCronValue));

				senderConfiguration.addOutbox(new Outbox(FileUtils.createPath(baseDir, outboxDir)));

				clientConfiguration.addTransparentSenderConfiguration(senderConfiguration);
				// MANTIS 5023
				LOG.info("transparent sender added, " + senderConfiguration);
			}

			// inboxes
			final List inboxes = sub.configurationsAt(".inbox");
			for (Iterator j = inboxes.iterator(); j.hasNext(); ) {
				final HierarchicalConfiguration inboxSub = (HierarchicalConfiguration) j.next();

				String inboxDir = inboxSub.getString("[@dirPath]");
				File tmpDir = FileUtils.createPath(baseDir, inboxDir);
				Inbox inbox = new TransparentInbox(tmpDir, sedexId, MessageType.from(inboxSub.getString(MSG_TYPES)));
				checkSedexIdMsgTypes(inbox);
				receiverConfiguration.addInbox(inbox);
			}

			// receipt folder
			if (sub.getString("receipts[@dirPath]") != null) {
				String receiptsDir = sub.getString("receipts[@dirPath]");
				File tmpDir = FileUtils.createPath(baseDir, receiptsDir);
				statusCheckerConfiguration.addReceiptFolder(new ReceiptsFolder(tmpDir, sedexId,
						MessageType.from(sub.getString("receipts[@msgTypes]"))));
			}
		}

		// wrap-up for MANTIS 5023
		clientConfiguration.setReceiverConfiguration(receiverConfiguration);
		LOG.info("receiver added, " + receiverConfiguration);

		clientConfiguration.setStatusCheckerConfiguration(statusCheckerConfiguration);
		LOG.info("status checker added, " + statusCheckerConfiguration);

		// **************** status database settings
		clientConfiguration.setLogServiceConfiguration(createLogServiceConfig(xmlConfig));

		// the localRecipients (replacement from targetDirectoryResolver)
		final List<HierarchicalConfiguration> localRecipients = xmlConfig.configurationsAt(
				"messageHandler.localRecipients.localRecipient");

		for (HierarchicalConfiguration lr : localRecipients) {
			final String recipientId = lr.getString("[@recipientId]");
			final String msgTypes = lr.getString(MSG_TYPES);
			LocalRecipient localRecipient = new LocalRecipient(recipientId, msgTypes);
			LOG.info("LocalRecipient: " + localRecipient);
			clientConfiguration.addLocalRecipient(localRecipient);
		}

		// if the protocol files should be created
		ProtocolWriter.getInstance()
				.setActive(xmlConfig.getBoolean("messageHandler.protocol[@createPerMessageProtocols]", false));
		LOG.info(ProtocolWriter.getInstance().isActive() ? "protocol writer configured" : "protocol writer not configured");

	}

	private void setupHttpServer() {
		// **************** webservice interface via HTTP
		CommandInterfaceConfiguration commandInterfaceConfiguration = new CommandInterfaceConfiguration();
		if (xmlConfig.getString("messageHandler.webserviceInterface[@host]") != null) {
			commandInterfaceConfiguration.setPort(xmlConfig.getInt("messageHandler.webserviceInterface[@port]"));
			commandInterfaceConfiguration.setHost(xmlConfig.getString("messageHandler.webserviceInterface[@host]"));
			LOG.info("webservice interface configured, " + commandInterfaceConfiguration);
		} else {
			LOG.info("webservice interface will be disabled");
		}
		clientConfiguration.setCommandInterfaceConfiguration(commandInterfaceConfiguration);

	}

	/**
	 * status database settings
	 *
	 * @param xmlConfig The XML config for the log service
	 * @return a {@link LogServiceConfiguration} (used as a factory)
	 * @throws ConfigurationException Config problems
	 */
	private LogServiceConfiguration createLogServiceConfig(XMLConfiguration xmlConfig) throws ConfigurationException {
		LogServiceConfiguration logServiceConfiguration = new LogServiceConfiguration();
		clientConfiguration.setLogServiceConfiguration(logServiceConfiguration);

		// the base directory for the log service
		String dbPath = xmlConfig.getString("messageHandler.statusDatabase[@dirPath]");
		FileUtils.isDirectory(dbPath, "messageHandler.statusDatabase[@dirPath]");
		logServiceConfiguration.setLogBase(dbPath);
		logServiceConfiguration.setMaxAge(xmlConfig.getInt("messageHandler.statusDatabase[@dataHoldTimeInDays]"));
		logServiceConfiguration.setResend(xmlConfig.getBoolean("messageHandler.statusDatabase[@resend]", false));
		LOG.info("message status database configured, " + logServiceConfiguration);

		return logServiceConfiguration;
	}

	/**
	 * Gets the sedex configuration
	 *
	 * @param xmlConfig The XML Config
	 * @return A {@link SedexAdapterConfiguration}
	 * @throws ConfigurationException Config Problems
	 */
	private SedexAdapterConfiguration createSedexAdapterConfig(XMLConfiguration xmlConfig) throws ConfigurationException {
		String participantId = xmlConfig.getString("sedexAdapter.participantId");

		String inboxDir = xmlConfig.getString("sedexAdapter.inboxDir");
		FileUtils.isDirectory(inboxDir, "sedexAdapter.inboxDir");

		String outboxDir = xmlConfig.getString("sedexAdapter.outboxDir");
		FileUtils.isDirectory(outboxDir, "sedexAdapter.outboxDir");

		String receiptDir = xmlConfig.getString("sedexAdapter.receiptDir");
		FileUtils.isDirectory(receiptDir, "sedexAdapter.receiptDir");

		String sentDir = xmlConfig.getString("sedexAdapter.sentDir");
		FileUtils.isDirectory(sentDir, "sedexAdapter.sentDir");

		return new SedexAdapterConfiguration(participantId, inboxDir, outboxDir, receiptDir, sentDir);
	}

	/**
	 * Handles the workingDir configuration
	 *
	 * @param xmlConfig The XML Config
	 * @return A String
	 * @throws ConfigurationException Config Problems
	 */
	private String createWorkingDir(XMLConfiguration xmlConfig) throws ConfigurationException {
		final String workingDir = xmlConfig.getString("messageHandler.workingDir[@dirPath]");

		FileUtils.isDirectory(workingDir, "messageHandler.workingDir[@dirPath]");
		FileUtils.isDirectory(new File(workingDir, ClientCommons.SENT_DIR), ClientCommons.SENT_DIR);
		FileUtils.isDirectory(new File(workingDir, ClientCommons.CORRUPTED_DIR), ClientCommons.CORRUPTED_DIR);
		FileUtils.isDirectory(new File(workingDir, ClientCommons.UNKNOWN_DIR), ClientCommons.UNKNOWN_DIR);
		FileUtils.isDirectory(new File(workingDir, ClientCommons.INBOX_TMP_DIR), ClientCommons.INBOX_TMP_DIR);
		FileUtils.isDirectory(new File(workingDir, ClientCommons.OUTBOX_TMP_DIR), ClientCommons.OUTBOX_TMP_DIR);

		LOG.info("workingDir is: " + workingDir);
		return workingDir;
	}

	/**
	 * SEDEX-175 - Moves everything that remains in inbox and outbox temporary directories into the corrupted directory.
	 * @param workingDir the working directory.
	 */
	
	private static void cleanUpWorkingDir(String workingDir)
	{
	    File corruptedDir = new File(workingDir, ClientCommons.CORRUPTED_DIR);
	    File inboxTmpDir  = new File(workingDir, ClientCommons.INBOX_TMP_DIR);
	    File outboxTmpDir = new File(workingDir, ClientCommons.OUTBOX_TMP_DIR);
	    
	    moveToAnotherDir(inboxTmpDir,  corruptedDir);
	    moveToAnotherDir(outboxTmpDir, corruptedDir);
	}
	
	/**
	 * Moves a file or a directory content to another directory
	 * @param source the source file or directory
	 * @param targetDir the traget directory
	 */
	
	private static void moveToAnotherDir(File source, File targetDir)
	{
	    if (source.isDirectory())
	    {
		File[] files = source.listFiles();
		
		for (File file : files)
		{
		    moveToAnotherDir(file, targetDir);
		}
	    }
	    else
	    {
		try
		{
		    FileUtils.moveToDirectory(source, targetDir);
		    LOG.info("File " + source.getAbsolutePath() + " moved to " + targetDir.getAbsolutePath());
		}
		catch (IOException e)
		{
		    LOG.warn("File " + source.getAbsolutePath() + " could not be moved to " + targetDir.getAbsolutePath());
		}
	    }
	}
	
	/**
	 * Handles the BaseDir configuration.<br /> May return null.
	 *
	 * @param xmlConfig The XML config
	 * @return A String
	 * @throws ConfigurationException Config problems
	 */
	private String createBaseDir(XMLConfiguration xmlConfig) throws ConfigurationException {
		final String baseDir = xmlConfig.getString("messageHandler.baseDir[@dirPath]");
		if (baseDir == null) {
			LOG.info("No baseDir is set. All paths have to be absolute...");
		} else {
			FileUtils.isDirectory(baseDir, "messageHandler.baseDir[@dirPath]");
			LOG.info("baseDir is: " + baseDir);
		}

		return baseDir;
	}

	/**
	 * This method validates the config.xml. A physical signing outbox (and corresponding processed) directory is only
	 * allowed be referenced once.
	 *
	 * @param signatureOutboxDir A File
	 * @param processedDir       the processed dir
	 * @throws ConfigurationException Config problems
	 */
	private void checkSignatureOutbox(File signatureOutboxDir, File processedDir) throws ConfigurationException {
		if (checkSigningOutboxDirSet.contains(signatureOutboxDir)) {
			throw new ConfigurationException("XML doublication error: <signingOutbox ... name=\""
					+ signatureOutboxDir.getAbsolutePath() + " ... \"> already defined. This value has to be unique.");
		}
		checkSigningOutboxDirSet.add(signatureOutboxDir);

		if (processedDir != null) {
			if (checkSigningProcessedDirSet.contains(processedDir)) {
				throw new ConfigurationException("XML doublication error: <signingOutbox ... processedDir=\""
						+ processedDir.getAbsolutePath() + " ... \"> already defined. This value has to be unique.");
			}
			checkSigningProcessedDirSet.add(processedDir);
		}
	}

	/**
	 * �berpr�ft dass es keine doppelten SedexId mit gleichen MsgTypes gibt.
	 *
	 * @param inbox The Inbox
	 * @throws ConfigurationException Config problems
	 */
	private void checkSedexIdMsgTypes(Inbox inbox) throws ConfigurationException {
		String sedexId = inbox.getSedexId();
		List<MessageType> msgTypes = inbox.getMessageTypes();

		for (MessageType mt : msgTypes) {
			LOG.debug("checkSedexIdMsgTypes: " + sedexId + ", " + mt.getType());
			if (checkSedexIdMsgType.containsKey(sedexId)) {
				Set<Integer> msgTypesSet = checkSedexIdMsgType.get(sedexId);
				if (msgTypesSet.contains(mt.getType())) {
					throw new ConfigurationException("There's already a participantId with msgType defined. " + sedexId + ", "
							+ mt.getType());
				} else {
					checkSedexIdMsgType.get(sedexId).add(mt.getType());
				}
			} else {
				Set<Integer> mySet = new HashSet<>();
				mySet.add(mt.getType());
				checkSedexIdMsgType.put(sedexId, mySet);
			}
		}
	}

	/**
	 * Returns the current client configuration object. If this factory is not inialized, this method returns
	 * <code>null</code>.
	 *
	 * @return ClientConfiguration the client config
	 */
	public ClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}
}
