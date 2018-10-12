/*
 * $Id: Signer.java 327 2014-01-27 13:07:13Z blaser $
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
package ch.admin.suis.msghandler.signer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

import ch.admin.suis.batchsigner.BatchException;
import ch.admin.suis.batchsigner.BatchRunner;
import ch.admin.suis.batchsigner.BatchRunnerBuilder;
import ch.admin.suis.msghandler.config.SigningOutbox;
import ch.admin.suis.msghandler.util.FileUtils;

/**
 * This class is responsible for the PDF sign task.
 *
 * @author kb
 * @author $Author: blaser $
 * @version $Revision: 327 $
 * @since 03.07.2012
 */
public class Signer extends ISignerArguments {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Signer.class.getName());

	private static final String SIGNED_SUFFIX = "-sig.pdf";

	/**
	 * List of signing outboxes to be searched for files to sign.
	 */
	private final List<SigningOutbox> signingOutboxes;

	/**
	 * In this directory all successfully signed PDFs will be stored. This is usually the "normal" MessageHandler outbox
	 * directory.
	 */
	private final File signedDir;

	  /** In this directory all PDFs that can't be signed will be moved. This is usually the "normal" MessageHandler
	   * corrupted directory.
	   */
	  private final File corruptedDir;

	  /**
	 * Constructor for one SigningOutbox. Used for Unit Tests
	 *
	 * @param signingOutbox The Signing outbox
	 * @param signedDir     In this directory all successful signed PDFs will be stored. This should be the "normal"
	 *                      MessageHandler outbox directory.
	 */
	Signer(SigningOutbox signingOutbox, File signedDir, File corruptedDir) {
		this.signingOutboxes = Collections.singletonList(signingOutbox);
		this.signedDir = signedDir;
	        this.corruptedDir = corruptedDir;
	}

	/**
	 * Constructor for multiple SigningOutboxes. Required if one "outbox" has multiple "signingOutbox".
	 *
	 * @param signingOutboxes Multiple Signing outboxes
	 * @param signedDir       In this directory all successful signed PDFs will be stored. This should be the "normal"
	 *                        MessageHandler outbox directory.
	 */
	public Signer(List<SigningOutbox> signingOutboxes, File signedDir, File corruptedDir) {
		this.signingOutboxes = signingOutboxes;
		this.signedDir = signedDir;
	        this.corruptedDir = corruptedDir;
	}

	/**
	 * Signs all PDFs in all SigningOutboxes. Signed PDFs will be stored in the "signedDir" (see constructor) directory.
	 *
	 * @return all signed PDFs. Only successfully signed PDFs or empty list if there wasn't a PDF to sign.
	 * @throws SignerException If something goes wrong..
	 */
	public List<File> sign() throws SignerException, ConfigurationException {
		final List<File> results = new ArrayList<>();

		for (SigningOutbox signingOutbox : signingOutboxes) {
			LOG.debug("Sign process for SigningOutbox: " + signingOutbox.getName());
			List<File> signed = sign(signingOutbox);
			results.addAll(signed);
		}

		return results;
	}

	/**
	 * Signs the PDF files in one single signing outbox.
	 *
	 * @param signingOutbox The signing outbox where the PDF files to sign a located.
	 * @return A list of names of those files which had to be signed. Only the name of the files, which could be
	 * successfully signed are returned. The list may be empty.
	 * @throws SignerException        Signer went head in the wall.
	 * @throws ConfigurationException Configuration is doomed !
	 */
	private List<File> sign(SigningOutbox signingOutbox) throws SignerException, ConfigurationException {

		final List<File> pdfsToSign = signingOutbox.getAllPDFsToSign();
		
		    LOG.debug("Number of PDF files to sign for " + signingOutbox.getName() + " = " + pdfsToSign.size());

		    if (pdfsToSign.isEmpty()) {
		      return Collections.EMPTY_LIST;
		    }

                /**
		 * Do a refresh. Required for the SigningOutboxSedexCfg.
                 */
		    
		signingOutbox.refresh();
		
		    final List<File> results = new ArrayList<>(pdfsToSign.size());
		    final BatchRunnerBuilder builder = makeBatchRunnerBuilder(signingOutbox);

		    // we use the BatchSigner to signed a batch of a single file. By this means we prevent, that a file, that cannot
		    // be signed, prevents to following files in the list to be signed (cf. MSGHANDLER-64)
		    for (File pdfToSign : pdfsToSign)
		    {
		      try
		      {
		        BatchRunner batchRunner = builder.buildMinimal();
		        File signedPdf = determineDestinationFile(signedDir, pdfToSign);
		        batchRunner.addFile(pdfToSign, signedPdf);
		        batchRunner.go(); // a batch of one file
		        results.add(pdfToSign);
		        LOG.info(String.format("Signed file %s, the result is %s", pdfToSign.getName(), signedPdf.getAbsolutePath()) );
		      } catch (BatchException ex)
		      {
		        moveToCorruptedAndLog(pdfToSign, ex);
		      }
		    }

		    LOG.info("Number of PDF file successfully signed in " + signingOutbox.getName() + ": " + results.size());

		    return results;
	}

        private void moveToCorruptedAndLog(File pdfToSign, BatchException ex)
	{
	    try {
	      final String destFilename = FileUtils.moveToDirectory(pdfToSign, corruptedDir);
	      LOG.fatal(String.format("PDF file %s cannot be signed. Moved to %s Proceeding with next.", pdfToSign.
	          getAbsolutePath(), destFilename), ex);
	    } catch (IOException ioe) {
	      LOG.fatal(String.format("PDF file %s cannot be signed and could not be moved to %s.", pdfToSign.
	          getAbsolutePath(), corruptedDir.getAbsolutePath()), ioe);
	    }
	    // da wir weiterfahren wollen, wird die Exception geschluckt
	}

        /** Creates a new BatchRunnerBuilder configured for a specific signing outbox.
        *
        * @param signingOutbox signing outbox to configure the BatchRunnerBuilder for.
        *
        * @return a new BatchRunnerBuilder
        * @throws SignerException if configuration fails for some reason.
        */
       private BatchRunnerBuilder makeBatchRunnerBuilder(SigningOutbox signingOutbox) throws SignerException {
          Map<String, String> arguments = makeConfigForBatchSigner(signingOutbox);
          try {
            BatchRunnerBuilder builder = new BatchRunnerBuilder();
            builder.fromMap(arguments);
            return builder;
          }
          catch(BatchException ex){
           String msg = "Cannot build a BatchRunnerBuilder " + ex.getMessage() + " for Signing Outbox: " + signingOutbox.getName();
           LOG.fatal(msg, ex);
           throw new SignerException(msg, ex);
         }
       }

       private Map<String, String> makeConfigForBatchSigner(SigningOutbox signingOutbox)
       {
         Map<String, String> arguments = new HashMap<>();
	 arguments.put(action, defaultAction);
	 arguments.put(p12File, signingOutbox.getP12File().getAbsolutePath());
	 arguments.put(p12Password, signingOutbox.getPassword());
	 arguments.put(signaturePropertyFile, signingOutbox.getSigningProfile().getAbsolutePath());
	 arguments.put(certificationType, defaultCertificationType);
         return arguments;
       }

       /**
        * Given a file to be signed in a certain directory, this method returns the (name of) the file, which will be the
        * result of the signing process.
        *
        * @param directory Directory, where a file is to be created
        * @param pdfToSign the PDF file to be signed
        * @return the (name of) the file, which will be the
        * resulat of the signing process
        */
       private File determineDestinationFile(File directory, File pdfToSign) {
         String destFileName = pdfToSign.getName().substring(0, pdfToSign.getName().lastIndexOf(".")) + SIGNED_SUFFIX;
         String uniqueFileName = FileUtils.getFilename(directory, destFileName);
         File file = new File(uniqueFileName);
         LOG.debug("Create Sign unique file: " + pdfToSign.getName() + " -> " + file.getName());
         if(!destFileName.equals(file.getName())) {
           LOG.error("Name conflict. Illegal file in outbox directory. Solved with renaming: " + destFileName + " -> " + file.
                   getName());
         }
         return file;
       }

       /**
	 * This method must be called after signing the PDF files.
	 * <p>
	 * If SigningOutbox.getProcessedDir() is set, this method will move all signed PDF files from "signingOutboxDir" to
	 * "processedDir". If no processedDir ist set, the signed PDFs will be deleted!
	 *
	 * @param signedFiles List of all files, which have been signed (e.g. the original files, not the signed ones!).
	 */
	public void cleanUp(List<File> signedFiles) {
		for (SigningOutbox signingOutbox : signingOutboxes) {

			File processedDir = signingOutbox.getProcessedDir();
			List<File> originalPDFs = signingOutbox.getAllPDFsToSign();

			LOG.debug("Cleaning SigningOutbox directory: " + signingOutbox.getName() + ", Nbr of PDFs: " + originalPDFs.size());

			if (originalPDFs.isEmpty()) {
				LOG.debug("Nothing to clean in SigningOutbox: " + signingOutbox.getName());
				continue;
			}

			if (processedDir == null) {
				// No processedDir set: delete the files in question
				LOG.info("Clean " + signingOutbox.getName() + " after signing: Deleted "
						+ deletePdf(signedFiles, originalPDFs) + " PDFs");
			} else {
				// processedDir set: move the files in question
				LOG.info("Clean " + signingOutbox.getName() + " after signing: Moved "
						+ movePdf(signedFiles, originalPDFs, processedDir) + " PDFs to " + processedDir.getName());
			}
		}
	}

	/**
	 * Deletes the signed PDF once signed.
	 *
	 * @param signedFiles  The signed files
	 * @param originalPDFs The signing outbox files
	 * @return the number of cleaned files
	 */
	private int deletePdf(List<File> signedFiles, List<File> originalPDFs) {
		int deletedFilesCnt = 0;
		for (File pdfToDelete : originalPDFs) {
			if (signedFiles.contains(pdfToDelete)) {
				// this file has been signed -> delete
				if (pdfToDelete.delete()) {
					deletedFilesCnt++;
				} else {
					LOG.warn("Unable to delete file: " + pdfToDelete.getAbsolutePath());
				}
			}
		}
		return deletedFilesCnt;
	}

	private int movePdf(List<File> signedFiles, List<File> originalPDFs, File processedDir) {
		int movedFilesCnt = 0;
		for (File srcFile : originalPDFs) {
			if (signedFiles.contains(srcFile)) {
				// this file has been signed -> move it
				File destFile = new File(processedDir, srcFile.getName());
				try {
					FileUtils.moveFile(srcFile, destFile);
					movedFilesCnt++;
				} catch (IOException ex) {
					LOG.error("Unable to move file. Src: " + srcFile.getAbsolutePath() + " to dest: " + destFile, ex);
				}
			}
		}
		return movedFilesCnt;
	}
}
