/*
 * $Id$
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

import ch.admin.suis.msghandler.checker.StatusCheckerConfiguration;
import ch.admin.suis.msghandler.common.LocalRecipient;
import ch.admin.suis.msghandler.log.LogServiceConfiguration;
import ch.admin.suis.msghandler.receiver.ReceiverConfiguration;
import ch.admin.suis.msghandler.sedex.SedexAdapterConfiguration;
import ch.admin.suis.msghandler.sender.SenderConfiguration;
import ch.admin.suis.msghandler.servlet.CommandInterfaceConfiguration;
import ch.glue.fileencryptor.Decryptor;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The
 * <code>ClientConfiguration</code> describes the parameters common to both receiver and sender. These parameters
 * include the certificate paths and the
 * <code>GovLinkConfiguration</code> object used.
 *
 * @author Alexander Nikiforov
 * @author $Author$
 * @version $Revision$
 */
public class ClientConfiguration {

  /**
   * logger
   */
  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ClientConfiguration.class.
          getName());

  private String workingDir;

  /**
   * several senders
   */
  private List<SenderConfiguration> senderConfigurations = new ArrayList<SenderConfiguration>();

  /**
   * several transparent senders
   */
  private List<SenderConfiguration> transparentSenderConfigurations = new ArrayList<SenderConfiguration>();

  private ReceiverConfiguration receiverConfiguration = new ReceiverConfiguration();

  private StatusCheckerConfiguration statusCheckerConfiguration = new StatusCheckerConfiguration();

  private LogServiceConfiguration logServiceConfiguration = new LogServiceConfiguration();

  private CommandInterfaceConfiguration commandInterfaceConfiguration = new CommandInterfaceConfiguration();

  private SedexAdapterConfiguration sedexAdapterConfiguration;

  private Map<String, LocalRecipient> localRecipients = new HashMap<String, LocalRecipient>();

  private Decryptor decryptor;

  /**
   * The internal working directory of the message handler.
   *
   * @param workingDir
   * @throws ConfigurationException
   */
  public void setWorkingDir(String workingDir) throws ConfigurationException {
    this.workingDir = workingDir;
  }

  /**
   * The internal working directory of the message handler.
   *
   * @return
   */
  public String getWorkingDir() {
    return workingDir;
  }

  /**
   * @return Returns the receiverConfiguration.
   */
  public ReceiverConfiguration getReceiverConfiguration() {
    return receiverConfiguration;
  }

  /**
   * Returns iterator over the stored sender configurations.
   *
   * @return Returns the senderConfiguration.
   */
  public List<SenderConfiguration> getSenderConfigurations() {
    return senderConfigurations;
  }

  /**
   * Returns the stored sender configurations.
   *
   * @return Returns the senderConfiguration.
   */
  public List<SenderConfiguration> getTransparentSenderConfigurations() {
    return transparentSenderConfigurations;
  }

  /**
   * @return Returns the statusCheckerConfiguration.
   */
  public StatusCheckerConfiguration getStatusCheckerConfiguration() {
    return statusCheckerConfiguration;
  }

  /**
   * @return Returns the logServiceConfiguration.
   */
  public LogServiceConfiguration getLogServiceConfiguration() {
    return logServiceConfiguration;
  }

  /**
   * @param logServiceConfiguration The logServiceConfiguration to set.
   */
  public void setLogServiceConfiguration(LogServiceConfiguration logServiceConfiguration) {
    this.logServiceConfiguration = logServiceConfiguration;
  }

  /**
   * @param receiverConfiguration The receiverConfiguration to set.
   */
  public void setReceiverConfiguration(ReceiverConfiguration receiverConfiguration) {
    this.receiverConfiguration = receiverConfiguration;
  }

  /**
   * Adds a new sender configuration. This method does check, if a configuration with the same name has been already
   * added.
   *
   * @param senderConfiguration the senderConfiguration to add; cannot be <code>null</code>
   *
   * @return <code>true</code>, if the configuration was added, and <code>false</code> otherwise
   */
  public void addSenderConfiguration(SenderConfiguration senderConfiguration) throws ConfigurationException {
    Validate.notNull(senderConfiguration, "sender configuration cannot be null");

    // check if we already have a configuration with the same name!
    for(SenderConfiguration existing : senderConfigurations) {
      if(StringUtils.equals(existing.getName(), senderConfiguration.getName())) {
        throw new ConfigurationException("sender configuration with the name " + senderConfiguration.getName()
                + " already exists; this sender will not be started: " + senderConfiguration);
      }
    }
    senderConfigurations.add(senderConfiguration);
  }

  /**
   * Adds a new transparent sender configuration. This method does check, if a configuration with the same name has been
   * already added.
   *
   * @param senderConfiguration the senderConfiguration to add; cannot be <code>null</code>
   *
   * @return <code>true</code>, if the configuration was added, and <code>false</code> otherwise
   */
  public void addTransparentSenderConfiguration(SenderConfiguration senderConfiguration) throws ConfigurationException {
    Validate.notNull(senderConfiguration);

    // check if we already have a configuration with the same name!
    for(SenderConfiguration existing : transparentSenderConfigurations) {
      if(StringUtils.equals(existing.getName(), senderConfiguration.getName())) {
        throw new ConfigurationException("transparent sender configuration with the name " + senderConfiguration.getName()
                + " already exists; this transparent sender will not be started: " + senderConfiguration);
      }
    }

    transparentSenderConfigurations.add(senderConfiguration);
  }

  public Map<String, LocalRecipient> getLocalRecipients() {
    return localRecipients;
  }

  /**
   *
   * @param localRecipient
   * @return <code>true</code>, if the configuration was added, and <code>false</code> otherwise
   */
  public void addLocalRecipient(LocalRecipient localRecipient) throws ConfigurationException {
    Validate.notNull(localRecipient, "LocalRecipient cannot be null");

    if(localRecipients.containsKey(localRecipient.getRecipientId())) {
      throw new ConfigurationException("Already a localRecipient added with recipientId: " + localRecipient.
              getRecipientId());
    }


    localRecipients.put(localRecipient.getRecipientId(), localRecipient);
  }

  /**
   * @param statusCheckerConfiguration The statusCheckerConfiguration to set.
   */
  public void setStatusCheckerConfiguration(StatusCheckerConfiguration statusCheckerConfiguration) {
    this.statusCheckerConfiguration = statusCheckerConfiguration;
  }

  /**
   * @return Returns the commandInterfaceConfiguration.
   */
  public CommandInterfaceConfiguration getCommandInterfaceConfiguration() {
    return commandInterfaceConfiguration;
  }

  /**
   * @param commandInterfaceConfiguration The commandInterfaceConfiguration to set.
   */
  public void setCommandInterfaceConfiguration(CommandInterfaceConfiguration commandInterfaceConfiguration) {
    this.commandInterfaceConfiguration = commandInterfaceConfiguration;
  }

  public void setSedexAdapterConfiguration(SedexAdapterConfiguration sedexAdapterConfiguration) {
    this.sedexAdapterConfiguration = sedexAdapterConfiguration;
  }

  /**
   * @return Returns the sedexAdapterConfiguration.
   */
  public SedexAdapterConfiguration getSedexAdapterConfiguration() {
    return sedexAdapterConfiguration;
  }

  /**
   * Returns the configured decryptor object.
   *
   * @return
   */
  public Decryptor getDecryptor()
  {
    return decryptor;
  }

  public void setDecryptor(final Decryptor decryptor)
  {
    this.decryptor = decryptor;
  }
}
