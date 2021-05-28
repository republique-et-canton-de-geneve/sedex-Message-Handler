/*
 * $Id$
 *
 * Copyright 2013 by Swiss Federal Administration
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of the Swiss Federal Administration. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with the Swiss Federal Administration.
 */
package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.util.FileUtils;
import java.io.File;
import java.util.Date;
import org.apache.commons.configuration.ConfigurationException;

/**
 * The SigningOutbox for MH configuration. The configuration from the p12 files and passwords is done in the sedex
 * configuration file certificateConfiguration.xml. Sedex is able to change the certificates (p12). So the configuration
 * file (and it's data) will be updated periodically. <p /> The complete functionality is described in the Mantis:
 * 0006281. This is a new feature from MH v3.1.0.
 *
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 27.02.2013
 */
public class SigningOutboxSedexCfg extends SigningOutbox {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SigningOutboxSedexCfg.class.
          getName());

  private final File certificateConfiguration;

  private SedexCertConfig sedexCertConfig;

  private long lastModification = -1;

  /**
   *
   * @param certificateConfiguration the sedex certificateConfiguration.xml file
   * @param signingOutboxDir Directory which may contain 0...n PDFs to sign.
   * @param signingProfile Profile (configuration file) for the BatchSinger.
   * @param processedDir Null allowed. Successful signed PDFs will be moved to processedDir. If processedDir is null,
   * successful signed PDFs will be deleted.
   */
  public SigningOutboxSedexCfg(File certificateConfiguration, File signingOutboxDir, File signingProfile,
          File processedDir) throws ConfigurationException {
    super(signingOutboxDir, signingProfile, processedDir);
    this.certificateConfiguration = certificateConfiguration;
    
    LOG.info("Sedex Certificate Config: " + certificateConfiguration.getAbsolutePath());
    refresh();
  }

  /**
   * Reloads the certificateConfiguration file (see constructor). All changed parameters will be updated.
   */
  @Override
  public final void refresh() throws ConfigurationException {
    
    if(requiresReload()) {
      LOG.info("Sedex Certificate Config changed. Reload file: " + certificateConfiguration.getAbsolutePath());
      sedexCertConfig = new SedexCertConfigFactory(certificateConfiguration).getSedexCertConfig();
      LOG.info("Sedex Certificate Config selected: " + sedexCertConfig.toString());
    }
  }

  /**
   * Checks if the file was modified since last check.
   * 
   * @return
   * @throws ConfigurationException 
   */
  private boolean requiresReload() throws ConfigurationException {
    FileUtils.isFile(certificateConfiguration, ".certificateConfigFile[@filePath]");

    if(certificateConfiguration.lastModified() != lastModification) {
      lastModification = certificateConfiguration.lastModified();
      return true;
    }

    return false;
  }

  public File getCertificateConfiguration() {
    return certificateConfiguration;
  }

  @Override
  public File getP12File() {
    return sedexCertConfig.getP12File();
  }

  @Override
  public String getPassword() {
    return sedexCertConfig.getPassword();
  }
}